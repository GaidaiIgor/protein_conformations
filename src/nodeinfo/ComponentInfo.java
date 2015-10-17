package nodeinfo;

import core.Node;

public class ComponentInfo implements NodeInfo {
    public Node node;
    public boolean visited = false;
    public int component = -1;

    public ComponentInfo(Node node) {
        this.node = node;
    }

    public ComponentInfo(ComponentInfo other) {
        node = other.node;
        visited = other.visited;
        component = other.component;
    }

    public Node getNode() {
        return node;
    }

    @Override
    public String toString() {
        return node.toString() + ", component: " + component;
    }
}
