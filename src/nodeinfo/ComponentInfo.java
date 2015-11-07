package nodeinfo;

import core.HierarchicalNode;

public class ComponentInfo implements NodeInfo<HierarchicalNode> {
    public HierarchicalNode node;
    public boolean visited = false;
    public int component = -1;

    public ComponentInfo(HierarchicalNode node) {
        this.node = node;
    }

    public ComponentInfo(ComponentInfo other) {
        node = other.node;
        visited = other.visited;
        component = other.component;
    }

    public HierarchicalNode getNode() {
        return node;
    }

    @Override
    public String toString() {
        return node.toString() + ", component: " + component;
    }
}
