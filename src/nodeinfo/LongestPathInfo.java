package nodeinfo;

import core.Node;
import core.Path;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class LongestPathInfo implements NodeInfo {
    public Node node;
    public Set<Path> paths = new HashSet<>();
//    public Path best_path = null;

    public LongestPathInfo(Node node) {
        this.node = node;
//        best_path = new Path();
//        best_path.score = Double.NEGATIVE_INFINITY;
    }

    // deep fork_other (almost)
    public LongestPathInfo(LongestPathInfo other) {
        node = other.node;
        paths = other.paths.stream().map(Path::copy).collect(Collectors.toCollection(HashSet::new));
//        best_path = other.best_path.copy();
    }

    public Node get_node() {
        return node;
    }
}
