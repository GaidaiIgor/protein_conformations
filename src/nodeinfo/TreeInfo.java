package nodeinfo;

import core.HierarchicalNode;

public class TreeInfo implements NodeInfo {
    public HierarchicalNode node = null;
    public int level = -1;
    public int subtreeSize = 1;
    public boolean visited = false;

    public TreeInfo(HierarchicalNode node) {
        this.node = node;
    }

    public TreeInfo(HierarchicalNode node, int level, int subtreeSize) {
        this.node = node;
        this.level = level;
        this.subtreeSize = subtreeSize;
    }

    public HierarchicalNode getNode() {
        return node;
    }

    @Override
    public String toString() {
        return node.toString() + String.format(" level: %d, subtreeSize: %d", level, subtreeSize);
    }
}
