package core;

import java.util.Set;

public interface PathFinder<T extends AbstractNode<T>> {
    Set<Path<T>> findPaths(int startId, int endId);
}
