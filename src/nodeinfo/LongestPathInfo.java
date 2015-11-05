package nodeinfo;

import core.AbstractNode;
import core.Path;

import java.util.HashSet;
import java.util.Set;

public class LongestPathInfo<T extends AbstractNode<T>> implements NodeInfo<T> {
    public T node;
    public Set<Path<T>> allPaths = new HashSet<>();
    public Set<Path<T>> currentPaths = new HashSet<>();
    public Set<Path<T>> nextPaths = new HashSet<>();

    public LongestPathInfo(T node) {
        this.node = node;
    }

    // deep copy (almost)
//    public LongestPathInfo(LongestPathInfo other) {
//        node = other.node;
//        allPaths = other.allPaths.stream().map(Path::copy).collect(Collectors.toCollection(HashSet::new));
//    }

    public T getNode() {
        return node;
    }
}
