package core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class Node {
    private final int id;
    private final List<Edge> edges = new ArrayList<>();
    private final Graph underlyingGraph;
    private final int oldId;
    private final Set<Integer> adjacentMetaNodes = new HashSet<>();

    public Node(int id) {
        this(id, null, -1);
    }

    public Node(int id, Graph underlyingGraph, int oldId) {
        this.id = id;
        this.underlyingGraph = underlyingGraph;
        this.oldId = oldId;
    }

//    public void setUnderlyingGraph(Graph value) {
//        underlyingGraph = value;
//    }

    public static Node forkOther(int id, Node other) {
        return new Node(id, other.underlyingGraph, other.id);
    }

    public Graph getUnderlyingGraph() {
        return underlyingGraph;
    }

    public int getId() {
        return id;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public int getOldId() {
        return oldId;
    }

    public Set<Integer> getAdjacentMetaNodes() {
        return adjacentMetaNodes;
    }

    @Override
    public String toString() {
        return String.format("id: %d, old id: %d, size: %d", id, oldId, getSize());
    }

    public int getSize() {
        if (underlyingGraph == null) {
            return 1;
        }
        return underlyingGraph.getTotalSize();
    }
}