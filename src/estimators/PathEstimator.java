package estimators;

import core.Node;
import core.Path;
import nodeinfo.LongestPathInfo;

import java.util.List;

public interface PathEstimator {
    //    boolean is_correct(core.Path current, nodeinfo.LongestPathInfo next, double edge_weight, List<core.Node> graph);
    double score(Path current, LongestPathInfo next, double edge_weight, List<Node> graph);
}
