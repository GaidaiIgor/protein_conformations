package core;

import java.util.List;

public interface PathFilter<T extends AbstractNode<T>> {
    List<Path<T>> filterPaths(List<Path<T>> allPaths, int maxPaths);
}
