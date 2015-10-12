package estimators;

import core.Node;
import core.Path;
import nodeinfo.LongestPathInfo;

import java.util.List;

public class LongestPathEstimator implements PathEstimator {
    // TODO: probably implement better logic
    public double score(Path current, LongestPathInfo next, double edge_weight, List<Node> graph) {
        if (!is_correct(current, next, edge_weight, graph)) {
            return 0;
        }
        return current.path.size() + 1;
//        return Math.pow(current.path.size() + 1, 2) * 100000000 / (current.total_weight + edge_weight);
    }

    // TODO: implement reasonable logic
    public boolean is_correct(Path current, LongestPathInfo next, double edge_weight, List<Node> graph) {
        return true;
    }
}
