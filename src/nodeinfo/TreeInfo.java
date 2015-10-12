package nodeinfo;

import core.Node;

public class TreeInfo implements NodeInfo {
    public Node node = null;
    public int level = -1;
    public int subtree_size = 1;
    public boolean visited = false;

    public TreeInfo(Node node) {
        this.node = node;
    }

    public TreeInfo(Node node, int level, int subtree_size) {
        this.node = node;
        this.level = level;
        this.subtree_size = subtree_size;
    }

    public Node get_node() {
        return node;
    }

    @Override
    public String toString() {
        return node.toString() + String.format(" level: %d, subtree_size: %d", level, subtree_size);
    }
}
