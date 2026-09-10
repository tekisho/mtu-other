package assignment1;

import java.util.LinkedList;

public class GraphAdjList extends AbstractGraph {
    private record Edge(int destination, double weight) {}
    private final LinkedList<Edge>[] neighbours;

    public GraphAdjList(int noOfVertices, boolean directed) {
        super(noOfVertices, directed);

        neighbours = new LinkedList[noOfVertices];
        for (int i = 0; i < noOfVertices; i++) {
            neighbours[i] = new LinkedList<>();
        }
    }

    public void addEdge(int source, int destination, double weight) {
        neighbours[source].add(new Edge(destination, weight));

        if (!directed) {
            neighbours[destination].add(new Edge(source, weight));
        }
    }

    public void removeEdge(int source, int destination) {
        for (Edge edge : neighbours[source]) {
            if (edge.destination == destination) {
                neighbours[source].remove(edge);
                if (!directed) {
                    neighbours[edge.destination].remove(new Edge(source, edge.weight));
                }
                break;
            }
        }
    }

    public double getWeight(int source, int destination) {
        for (Edge edge : neighbours[source]) {
            if (edge.destination == destination) {
                return edge.weight;
            }
        }
        return Double.NaN;
    }

    public int[] getNeighbours(int vertex) {
        int[] adjacentVertList = new int[neighbours[vertex].size()];

        int i = 0;
        for (Edge edge : neighbours[vertex]) {
            adjacentVertList[i++] = edge.destination;
        }

        return adjacentVertList;
    }

    public int getDegree(int vertex) {
        int degree = 0;

        for (Edge edge : neighbours[vertex]) {
            degree++;
        }

        if (directed) {
            for (int i = 0; i < noOfVertices; i++) {
                for (Edge edge : neighbours[i]) {
                    if (edge.destination == vertex) {
                        degree++;
                        break;
                    }
                }
            }
        }

        return degree;
    }

    public boolean isPath(int[] nodes)
    {
        boolean isStillPath = false;

        for (int i = 0; i < nodes.length - 1; i++) {
            for (Edge edge : neighbours[nodes[i]]) {
                if (edge.destination == nodes[i + 1]) {
                    isStillPath = true;
                    break;
                } else {
                    isStillPath = false;
                }
            }

            if (!isStillPath) {
                return false;
            }
        }

        return true;
    }

    public int getNoOfEdges() {
        int totalEdges = 0;

        for (int i = 0; i < noOfVertices; i++) {
            totalEdges += neighbours[i].size();
        }

        return !directed ? totalEdges / 2 : totalEdges;
    }
}
