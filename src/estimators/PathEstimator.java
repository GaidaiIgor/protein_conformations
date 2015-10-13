package estimators;

import core.Edge;
import core.Path;

public interface PathEstimator {
    //    boolean is_correct(core.Path current, nodeinfo.LongestPathInfo next, double edge_weight, List<core.Node> graph);
    double path_connection_score(Path path1, Path path2, int path1_node_index, int path2_node_index, Edge edge);

    double path_score(Path path);
}
