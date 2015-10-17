package nodeinfo;

import core.Node;
import core.Path;

import java.util.HashSet;
import java.util.Set;

public class LongestPathInfo implements NodeInfo {
    public Node node;
    public Set<Path> allPaths = new HashSet<>();
    public Set<Path> currentPaths = new HashSet<>();
    public Set<Path> nextPaths = new HashSet<>();

    public LongestPathInfo(Node node) {
        this.node = node;
    }

    // deep copy (almost)
//    public LongestPathInfo(LongestPathInfo other) {
//        node = other.node;
//        allPaths = other.allPaths.stream().map(Path::copy).collect(Collectors.toCollection(HashSet::new));
//    }

    public Node getNode() {
        return node;
    }
}
