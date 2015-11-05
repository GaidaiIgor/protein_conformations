package core;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractNode<T extends AbstractNode> {
    private final int id;
    private final List<Edge<T>> edges = new ArrayList<>();

    public AbstractNode(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public List<Edge<T>> getEdges() {
        return edges;
    }

    @Override
    public String toString() {
        return String.format("id: %d", id);
    }
}
