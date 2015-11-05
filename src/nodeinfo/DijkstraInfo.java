package nodeinfo;

import core.HierarchicalNode;

public class DijkstraInfo {
    public HierarchicalNode node;
    public DijkstraInfo previous;
    public boolean is_handled = false;
    public double distance = Double.POSITIVE_INFINITY;

    public DijkstraInfo(HierarchicalNode node, DijkstraInfo previous, double distance) {
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