package estimators;

import core.AbstractNode;
import core.Path;

import java.util.Comparator;

public interface PathEstimator<T extends AbstractNode<T>> extends Comparator<Path<T>> {
    Path<T> mergePaths(Path<T> path1, Path<T> path2);
}
