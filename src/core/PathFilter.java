package core;

import java.util.Collection;
import java.util.Set;

public interface PathFilter<T extends AbstractNode<T>> {
    Set<Path<T>> filterPaths(Collection<Path<T>> allPaths, int maxPaths);
}
