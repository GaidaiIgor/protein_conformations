package estimators;

import core.Node;

import java.util.List;

public interface GoodnessEstimator {
    double estimate_goodness(List<List<Node>> clusters);
}
