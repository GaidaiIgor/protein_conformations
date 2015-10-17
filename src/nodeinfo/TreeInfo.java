package nodeinfo;

import core.Node;

public class TreeInfo implements NodeInfo {
    public Node node = null;
    public int level = -1;
    public int subtreeSize = 1;
    public boolean visited = false;

    public TreeInfo(Node node) {
        this.node = node;
    }

    public TreeInfo(Node node, int level, int subtreeSize) {
        this.node = node;
        this.level = level;
        this.subtreeSize = subtreeSize;
    }

    public Node getNode() {
        return node;
    }

    @Override
    public String toString() {
        return node.toString() + String.format(" level: %d, subtreeSize: %d", level, subtreeSize);
    }
}
