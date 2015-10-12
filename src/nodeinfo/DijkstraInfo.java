package nodeinfo;

import core.Node;

public class DijkstraInfo {
    public Node node;
    public DijkstraInfo previous;
    public boolean is_handled = false;
    public double distance = Double.POSITIVE_INFINITY;

    public DijkstraInfo(Node node, DijkstraInfo previous, double distance) {
        this.node = node;
        this.previous = previous;
        this.distance = distance;
    }

    public DijkstraInfo(DijkstraInfo other) {
        node = other.node;
        previous = other.previous;
        is_handled = other.is_handled;
        distance = other.distance;
    }
}