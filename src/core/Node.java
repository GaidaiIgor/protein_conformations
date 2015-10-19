package core;

import java.util.ArrayList;
import java.util.List;

public final class Node {
    private final int id;
    private final List<Edge> edges = new ArrayList<>();
    private final Graph underlyingGraph;
    private final int oldId;
//    private Node parent;
//    private final Set<Integer> adjacentMetaNodes = new HashSet<>();

    public Node(int id) {
        this(id, null, id);
    }

    public Node(int id, Graph underlyingGraph, int oldId) {
        this.id = id;
        this.underlyingGraph = underlyingGraph;
        this.oldId = oldId;
//        this.parent = parent;
    }

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

//    public Node getParent() {
//        return parent;
//    }

//    public void setParent(Node parent) {
//        this.parent = parent;
//    }
    //    public Set<Integer> getAdjacentMetaNodes() {
//        return adjacentMetaNodes;
//    }

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