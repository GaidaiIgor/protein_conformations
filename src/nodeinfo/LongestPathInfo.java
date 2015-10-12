package nodeinfo;

import core.Node;
import core.Path;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class LongestPathInfo implements NodeInfo {
    public Node node;
    public Set<Path> paths = new TreeSet<>();
    public Path best_path = null;

    public LongestPathInfo(Node node) {
        this.node = node;
        best_path = new Path();
        best_path.score = Double.NEGATIVE_INFINITY;
    }

    // deep copy (almost)
    public LongestPathInfo(LongestPathInfo other) {
        node = other.node;
        paths = other.paths.stream().map(Path::deep_copy).collect(Collectors.toCollection(TreeSet::new));
        best_path = other.best_path.deep_copy();
    }

    public Node get_node() {
        return node;
    }
}
