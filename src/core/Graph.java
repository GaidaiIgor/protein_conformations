package core;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.BiFunction;

public class Graph<T extends AbstractNode<T>> {
    private final List<T> nodes;
    private final List<Edge<T>> edges;

    public Graph(List<T> nodes) {
        this.nodes = nodes;
        edges = new ArrayList<>();
    }

    // format:
    // 1st line: total amount of vertices = n
    // 2nd line: total amount of edges = e
    // next n lines: description of n vertices, one per line
    // next e lines: description of e edges, one per line
    public static <T extends AbstractNode<T>> Graph getFromInputStream(InputStream inputStream,
                                                                       BiFunction<Scanner, Integer, T> nodeReader) {
        Scanner in = new Scanner(inputStream);
        int verticesAmount = in.nextInt();
        int edgesAmount = in.nextInt();
        List<T> nodes = new ArrayList<>();
        for (int i = 0; i < verticesAmount; ++i) {
            nodes.add(nodeReader.apply(in, i));
        }
        Graph<T> graph = new Graph<>(nodes);
        for (int i = 0; i < edgesAmount; ++i) {
            int node1Id = in.nextInt();
            int node2Id = in.nextInt();
            double weight = in.nextDouble();
            int id = in.nextInt();
            graph.addBidirectionalEdge(graph.getNodes().get(node1Id), graph.getNodes().get(node2Id), weight, id);
        }
        return graph;
    }

    public void addBidirectionalEdge(T node1, T node2, double weight, int edgeId) {
        addEdge(node1, node2, weight, edgeId);
        addEdge(node2, node1, weight, edgeId);
    }

    public List<T> getNodes() {
        return nodes;
    }

    public Edge<T> addEdge(T node1, T node2, double weight, int edgeId) {
        Edge<T> newEdge = new Edge<>(node1, node2, weight, edgeId);
        node1.getEdges().add(newEdge);
        edges.add(newEdge);
        return newEdge;
    }

    public static <T extends AbstractNode<T>> Edge<T> getConnectingEdge(T first, T second) {
        return first.getEdges().stream().filter(e -> e.getSecond() == second).findFirst().orElse(null);
    }

    public List<Edge<T>> getEdges() {
        return edges;
    }
}
