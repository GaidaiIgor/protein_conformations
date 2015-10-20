package estimators;

import core.Edge;
import core.Graph;
import core.Node;
import core.Path;

import java.util.Comparator;

public class LongestPathEstimator implements PathEstimator {
    @Override
    public int compare(Path path1, Path path2) {
        return Comparator.<Path>comparingInt(p -> -p.getTotalNodes()).thenComparing(Path::getTotalWeight).compare(path1, path2);
    }

    @Override
    public double pathScore(Path path) {
        return path.getTotalNodes();
    }

    @Override
    public Path mergePaths(Path path1, Path path2) {
        for (int nodes_given = 0; nodes_given <= path1.getEdges().size() + path2.getEdges().size() - 2; ++nodes_given) {
            int path1_given = Integer.min(nodes_given, path1.getEdges().size() - 1);
            int path2_given = nodes_given - path1_given;
            while (path2_given <= Integer.min(nodes_given, path2.getEdges().size() - 1)) {
                int path1_break_index = path1.getEdges().size() - 1 - path1_given;
                //noinspection UnnecessaryLocalVariable
                int path2_break_index = path2_given;
                Node first = path1.getEdges().get(path1_break_index).getSecond();
                Node second = path2.getEdges().get(path2_break_index).getSecond();
                Edge bridge = Graph.getConnectingEdge(first, second);
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
