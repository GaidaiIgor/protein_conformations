package core;

public final class Edge {
    public final Node node1;
    public final Node node2;
    public final double weight;

    public Edge(Node node1, Node node2, double weight) {
        this.node1 = node1;
        this.node2 = node2;
        this.weight = weight;
    }

    public Node get_opposite_end(Node node) {
        return node1.id == node.id ? node2 : node1;
    }

    @Override
    public String toString() {
        return node1.toString() + " - " + node2.toString();
    }
}