package estimators;

import core.Edge;
import core.Path;

public class LongestPathEstimator implements PathEstimator {
    // TODO: probably implement better logic
    @Override
    public double path_connection_score(Path path1, Path path2, int path1_node_index, int path2_node_index, Edge edge) {
        if (!is_correct(path1, path2, path1_node_index, path2_node_index, edge)) {
            return 0;
        }
        return path1_node_index + 1 + path2.edges.size() - path2_node_index;
//        return Math.pow(current.edges.size() + 1, 2) * 100000000 / (current.total_weight + edge_weight);
    }

    @Override
    public double path_score(Path path) {
        return path.edges.size();
    }

    // TODO: implement reasonable logic
    public boolean is_correct(Path path1, Path path2, int path1_node_id, int path2_node_id, Edge edge) {
        return true;
    }
}
