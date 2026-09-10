package assignment1;


public class GraphAdjMatrix extends AbstractGraph {
    private final double[][] adjMatrix;

    public GraphAdjMatrix(int noOfVertices, boolean directed) {
        super(noOfVertices, directed);

        adjMatrix = new double[noOfVertices][noOfVertices];
        for (int i = 0; i < noOfVertices; i++) {
            for (int j = 0; j < noOfVertices; j++) {
                adjMatrix[i][j] = Double.NaN;
            }
        }
    }

    public void addEdge(int source, int destination, double weight) {
        adjMatrix[source][destination] = (!directed && source == destination) ? Double.NaN : weight;

        // to make adj. matrix symmetric
        if (!directed && source != destination)
            adjMatrix[destination][source] = weight;
    }

    public void removeEdge(int source, int destination) {
        adjMatrix[source][destination] = Double.NaN;

        if (!directed && source != destination) {
            adjMatrix[destination][source] = Double.NaN;
        }
    }

    public double getWeight(int source, int destination) {
        return adjMatrix[source][destination];
    }

    /**
     * Checks if edge exists (considering self-loops)
     * @param source vertex from which edge is leaving
     * @param destination vertex to which edge is entering
     * @return true if edge between source and destination vertices exists, otherwise - false
     */
    private boolean isEdgeExists(int source, int destination) {
        return isEdgeExists(adjMatrix, source, destination);
    }
    private boolean isEdgeExists(double[][] adjMatrix, int source, int destination) {
        return !Double.isNaN(adjMatrix[source][destination]);
    }

    public int[] getNeighbours(int vertex) {
        int adjacentVertTotal = 0;

        for (int i = 0; i < noOfVertices; i++) {
            if (isEdgeExists(vertex, i)) {
                adjacentVertTotal++;
            }
        }

        int[] adjacentVertList = new int[adjacentVertTotal];
        for (int i = 0, j = 0; i < noOfVertices; i++) {
            if (isEdgeExists(vertex, i)) {
                adjacentVertList[j++] = i;
            }
        }

        return adjacentVertList;
    }

    public int getDegree(int vertex) {
        int degree = 0;

        for (int i = 0; i < noOfVertices; i++) {
            if (isEdgeExists(vertex, i)) {
                degree++;
            }
            if (directed && isEdgeExists(i, vertex)) {
                degree++;
            }
        }

        return degree;
    }

    public boolean isPath(int[] nodes) {
        for (int i = 0; i < nodes.length - 1; i++) {
            if (!isEdgeExists(nodes[i], nodes[i + 1]))
                return false;
        }
        return  true;
    }

    public int getNoOfEdges() {
        int totalEdges = 0;

        // j = i + 1 because not consider self-loops (on the main diagonal of the matrix) for undir graphs
        for (int i = 0; i < noOfVertices; i++) {
            for (int j = !directed ? i + 1 : 0; j < noOfVertices; j++) {
                if (isEdgeExists(i, j))
                    totalEdges++;
            }
        }

        return totalEdges;
    }
}
