#include "gradient.h"

#include <stdlib.h>
#include <stdbool.h>
#include <stdint.h>
#include <float.h>

#include <time.h>
#include <math.h>


#define MAX_ITERATONS    1000

#define VIEW_CENTER      (VIEW_SIZE / 2)

#define INITIAL_MULT     3

#define SHORT_STEP       1
#define LONG_STEP        2
#define JUMP_STEP        (LONG_STEP * 2)
#define JUMP_MISSED_CAP  3
#define RANDOM_JUMP_STEP (VIEW_SIZE / 2 + 1)


// height growth vector from the current view point (i.e., offset from the VIEW_CENTER to view local maxima)
typedef struct direction {
    int dx;
    int dy;
} direction_offset;

typedef struct hill_point {
    path_point path;
    float height;
} hill_point;

path_point generate_initial_position();

path_point find_highest_point();

path_point       find_local_maxima(float [VIEW_SIZE][VIEW_SIZE]);
direction_offset find_local_direction(path_point);
path_point       to_global_point(path_point, direction_offset);

bool is_out_of_bounds(float);
bool is_plateau(path_point, float [VIEW_SIZE][VIEW_SIZE]);
bool is_reverse_direction(direction_offset, direction_offset);
bool is_adjacent_to_edge(path_point);
bool is_global_maxima(path_point, float [VIEW_SIZE][VIEW_SIZE]);

path_point step_forward(path_point, direction_offset, uint32_t);
path_point step_back(path_point, direction_offset);
path_point jump_over(path_point, direction_offset, uint32_t, uint32_t);
path_point random_jump_step(path_point, uint32_t);

int generate_random_number(int, int);

/**
 * Finds the highest point in the given surface (aka peak); steepest-ascent hill-climbing alghorithm (with plateau detection & jumps);
 * would not work properly if global maxima could be flat, could be faster if actual "empirical gradient" was used.
 */
path_point find_highest_point() {
    float view[VIEW_SIZE][VIEW_SIZE];

    path_point view_point = generate_initial_position();

    path_point local_maxima   = { .x = 0, .y = 0 };                 // view local maxima (not local maxima of the surface)
    hill_point current_maxima = {                                   // global coordinates & value of the view local maxima
        .path.x = 0, 
        .path.y = 0, 
        .height = -FLT_MAX 
    };
    hill_point last_maxima = current_maxima;                        // used for "backtracking"

    direction_offset current_direction    = { .dx = 0, .dy = 0 };
    direction_offset last_valid_direction = current_direction;

    // counters, that used to modify jump step
    uint32_t same_plateau_jumps  = 0;
    uint32_t out_of_bounds_jumps = 0;
    uint32_t missed_jumps        = 0;
    uint32_t random_jumps        = 0;


    for (int i = 0; i < MAX_ITERATONS; i++) {
        generate_view(view, view_point.y, view_point.x); 
        
        local_maxima      = find_local_maxima(view);
        current_direction = find_local_direction(local_maxima);

        last_maxima = current_maxima;

        current_maxima.path   = to_global_point(view_point, current_direction);
        current_maxima.height = view[local_maxima.y][local_maxima.x];

        // current view point is out of bounds only after unsuccesful jump over plateau or random jump
        if (is_out_of_bounds(current_maxima.height)) {
            if (i == 0) {
                view_point = generate_initial_position();
                i = 0;
                continue;
            }

            view_point = last_maxima.path;
            out_of_bounds_jumps++;
            continue;
        }

        if (is_plateau(local_maxima, view)) {

            // on the same plateau after jump
            if (fabs(last_maxima.height - current_maxima.height) < 1e-6) {
                same_plateau_jumps++;
            }
            // first time on the plateau or on the different plateau after jump
            else {
                same_plateau_jumps = 0;

                // allowing only fixed number of jumps to avoid stuck in plateaus due to invalid direction
                if (missed_jumps < JUMP_MISSED_CAP) {
                    missed_jumps++;
                }
                else {
                    view_point = random_jump_step(view_point, RANDOM_JUMP_STEP + random_jumps);
                    random_jumps++;
                    continue;
                }
            }

            view_point = jump_over(view_point, last_valid_direction, out_of_bounds_jumps, same_plateau_jumps);
            continue;
        }
        out_of_bounds_jumps = 0;


        // if after jump over plateau current direction points to it (i.e., points backwards), then do large random jump to escape this area
        if (is_reverse_direction(current_direction, last_valid_direction)) {
            view_point = random_jump_step(view_point, RANDOM_JUMP_STEP + random_jumps);
            random_jumps++;
            continue;
        }
        missed_jumps = 0; // it's important to reset this counter here, since if earlier in case previous random jump again on the plateau this could lead to infinite cycle


        // point is considered as global maxima only if: 
        // 0) it's not out of bounds (i.e., == -1.0f); 1) it's not a plateau; 2) it's not on the edge of the current view; 3) it is view local maxima
        if (!is_adjacent_to_edge(local_maxima)) {
            if (declare_peak(current_maxima.path.x, current_maxima.path.y))
                return current_maxima.path;

            // otherwise do random jump to escape surface local maxima
            view_point = random_jump_step(view_point, RANDOM_JUMP_STEP + random_jumps);
            random_jumps++;
            continue;
        }
        random_jumps = 0;
        
        // required to cope with plateaus
        last_valid_direction = current_direction;

        // otherwise keep going; long_step (= 2) is optimal, but short step (= 1) required since global maxima could not be on the edge of the view
        view_point = step_forward(
            view_point, 
            current_direction, 
            current_maxima.path.y == last_maxima.path.y && current_maxima.path.x == last_maxima.path.x ? SHORT_STEP : LONG_STEP
        );
    }

    return current_maxima.path;
}

/**
 * @brief Generates initial position with some offset from left upper corner of the matrix.
 */
path_point generate_initial_position() {
    // starting from VIEW_CENTER allows optimal start from the left upper corner without out of bounds values
    return (path_point) {
        .x = VIEW_CENTER * generate_random_number(SHORT_STEP, VIEW_SIZE * INITIAL_MULT),
        .y = VIEW_CENTER * generate_random_number(SHORT_STEP, VIEW_SIZE * INITIAL_MULT)
    };
}

/**
 * @brief Finds view point with the highest value (i.e., local maxima).
 * 
 * @param view current view
 */
path_point find_local_maxima(float view[VIEW_SIZE][VIEW_SIZE]) {
    path_point current = { .x = 0, .y = 0 };
    float current_value = -FLT_MAX;

    for (size_t i = 0; i < VIEW_SIZE; i++) {
        for (size_t j = 0; j < VIEW_SIZE; j++) {
            if (current_value <= view[i][j]) { // <= / <
                current_value = view[i][j];
                current.x = j;
                current.y = i;
            }
        }
    }
    
    return current;
}

/**
 * @brief Finds global coordinates of the new point using provided offset & last view point.
 * 
 * @param view_point current view point
 * @param offset     direction to view local maxima
 */
path_point to_global_point(path_point view_point, direction_offset offset) {
    return (path_point) {
        .x = view_point.x + offset.dx,
        .y = view_point.y + offset.dy 
    };
}


/**
 * @brief Detects if current generated view is out of bound (using view local maxima).
 * 
 * @param local_maxima current view local maxima value
 */
bool is_out_of_bounds(float local_maxima) {
    return local_maxima == -1.0f;
}

/**
 * @brief Detects if selected target point inside of the current view located on the plateau / shoulder.
 * 
 * IMPORTANT: in the context of algorithm, plateau / shoulder is an area in which no neighbours with a value greater than the target point,
 *            and at least one neighbour with equal value exists.
 * 
 * @param target_point the point to be looked at  
 * @param view         current view
 */
bool is_plateau(path_point target_point, float view[VIEW_SIZE][VIEW_SIZE]) {
    bool same_neighbor_exists = false;

    for (int i = target_point.y - 1; i < target_point.y + 2; i++) {
        for (int j = target_point.x - 1; j < target_point.x + 2; j++) {
            // out of bounds points skip
            if ((i < 0 || i >= VIEW_SIZE) || (j < 0 || j >= VIEW_SIZE))
                continue;

            // no sense to check target point itself
            if (i == target_point.y && j == target_point.x)
                continue;

            if (view[target_point.y][target_point.x] < view[i][j])
                return false;

            if (fabs(view[i][j] - view[target_point.y][target_point.x]) < 1e-6)
                same_neighbor_exists = true;
        }
    }

    return same_neighbor_exists;
}

/**
 * @brief Detects if current direction is equal to reversed last direction.
 * 
 * @param current_direction current direction to view local maxima
 * @param last_direction    previous valid direction
 */
bool is_reverse_direction(direction_offset current_direction, direction_offset last_direction) {
    return (current_direction.dy == -last_direction.dy && current_direction.dx == -last_direction.dx);
}

/**
 * @brief Detects if provided local point adjacant to the edge(s) of the view.
 * 
 * @param target_point the point to be looked at
 */
bool is_adjacent_to_edge(path_point target_point) {
    return (target_point.x == 0 || target_point.x == VIEW_SIZE - 1 || target_point.y == 0 || target_point.y == VIEW_SIZE - 1);
}


/**
 * @brief Finds direction vector from the point provided.
 * 
 * @param target_point the point to be looked at
 */
direction_offset find_local_direction(path_point target_point) {
    return (direction_offset) { 
        .dx = target_point.x - VIEW_CENTER, 
        .dy = target_point.y - VIEW_CENTER 
    };
}

/**
 * @brief Finds new view point values after REGULAR STEP.
 * 
 * @param view_point current view point
 * @param direction  current direction to view local maxima
 * @param multiplier step size (SHORT or LONG)
 */
path_point step_forward(path_point view_point, direction_offset direction, uint32_t multiplier) {
    return (path_point) { 
        .x = view_point.x + direction.dx * multiplier, 
        .y = view_point.y + direction.dy * multiplier 
    };
}

/**
 * @brief Finds new view point values after JUMP.
 * 
 * @param view_point current view point
 * @param direction  direction to local maxima from view point
 * @param decrease_factor step increment counter
 * @param increase_factor step decrement counter
 */
path_point jump_over(path_point view_point, direction_offset direction, uint32_t decrease_factor, uint32_t increase_factor) {
    // e.g. in case if start on plateau, generates random direction [-1 / 1][-1 / 1]
    if (direction.dx == 0 && direction.dy == 0) // if this statement will be comented out then jumps become almost purely random
        do {
            direction.dx = generate_random_number(-1, 1);
            direction.dy = generate_random_number(-1, 1);
        } while (direction.dx == 0 && direction.dy == 0);


    // the most important part; minor deviations -1..1 in new coordinates allows jump to escape from cycles 
    return (path_point) {
        .x = view_point.x + direction.dx * (JUMP_STEP - decrease_factor + increase_factor),
        .y = view_point.y + direction.dy * (JUMP_STEP - decrease_factor + increase_factor)
    };
}

/**
 * @brief Finds next view point to evase local maxima by randomly generating direction.
 * 
 * @param view_point current view point
 * @param step       step size multiplier
 */
path_point random_jump_step(path_point view_point, uint32_t step) {
    direction_offset direction;
    do {
        direction.dx = generate_random_number(-1, 1);
        direction.dy = generate_random_number(-1, 1);
    } while (direction.dx == 0 && direction.dy == 0);

    return (path_point) {
        .x = view_point.x + direction.dx * step,
        .y = view_point.y + direction.dy * step
    };
}

/**
 * @brief Generates random integer in diapason from min to max (inclusive).
 * 
 * @param min minimum possible value
 * @param max maximum possible value
 */
int generate_random_number(int min, int max) {
    return min + (rand() % (max + 1 - min));
}