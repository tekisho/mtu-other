package assignment2;

public class DivideAndConquer {

    public static int fibonacci(int n) {
        // TASK 1.A.a
        throw new RuntimeException("Not yet implemented!");
    }

    public static int search(int[] A, int v)
    {
        // TASK 1.A.b
        throw new RuntimeException("Not yet implemented!");
    }

    public static void hanoi(int n, char A, char B, char C)
    {
        // TASK 1.A.c
        throw new RuntimeException("Not yet implemented!");
    }

    public static void main(String[] args) {
        for (int i=0; i<10; i++) {
            System.out.println(fibonacci(i));
        }
        System.out.println();
        for (int i=0; i<10; i++) {
            System.out.println(search(new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, i));
        }
        System.out.println();
        hanoi(4, 'A', 'B', 'C');
    }
}
