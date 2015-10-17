package estimators;

import core.Path;

import java.util.Comparator;

public interface PathEstimator extends Comparator<Path> {
    double pathScore(Path path);

    Path mergePaths(Path path1, Path path2);
}
