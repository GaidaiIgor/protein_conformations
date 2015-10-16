package estimators;

import core.Edge;
import core.Graph;
import core.Node;
import core.Path;

public class LongestPathEstimator implements PathEstimator {
    // TODO: probably implement better logic
//    @Override
//    public double path_connection_score(Path path1, Path path2, int path1_node_index, int path2_node_index, Edge edge) {
//        if (!is_correct(path1, path2, path1_node_index, path2_node_index, edge)) {
//            return 0;
//        }
//        return path1_node_index + 1 + path2.edges.size() - path2_node_index;
////        return Math.pow(current.edges.size() + 1, 2) * 100000000 / (current.total_weight + edge_weight);
//    }

    @Override
    public double path_score(Path path) {
        return path.total_nodes;
    }

    @Override
    public Path merge_paths(Path path1, Path path2) {
        for (int nodes_given = 0; nodes_given < path1.edges.size() + path2.edges.size() - 2; ++nodes_given) {
            int path1_given = Integer.min(nodes_given, path1.edges.size() - 1);
            int path2_given = nodes_given - path1_given;
            while (path2_given <= Integer.min(nodes_given, path2.edges.size() - 1)) {
                int path1_break_index = path1.edges.size() - 1 - path1_given;
                //noinspection UnnecessaryLocalVariable
                int path2_break_index = path2_given;
                Node first = path1.edges.get(path1_break_index).second;
                Node second = path2.edges.get(path2_break_index).second;
                Edge bridge = Graph.get_connecting_edge(first, second);
                if (bridge != null) {
                    return Path.merge(path1, path2, path1_break_index, path2_break_index, bridge, this);
                }
                --path1_given;
                ++path2_given;
            }
        }
        return null;
    }

    // TODO: implement reasonable logic
    public boolean is_correct(Path path1, Path path2, int path1_node_id, int path2_node_id, Edge edge) {
        return true;
    }
}
