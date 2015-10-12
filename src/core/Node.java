package core;

import java.util.ArrayList;
import java.util.List;

public class Node {
    public int id;
    public List<Edge> edges = new ArrayList<>();
    public Graph underlying_graph = null;
    int old_id = -1;

    public Node(int id) {
        this.id = id;
    }

    public Node(int id, Graph underlying_graph, int old_id) {
        this.id = id;
        this.underlying_graph = underlying_graph;
        this.old_id = old_id;
    }

    public void set_underlying_graph(Graph value) {
        underlying_graph = value;
    }

    @Override
    public String toString() {
        return String.format("id: %d", id);
    }
}