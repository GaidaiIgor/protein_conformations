package estimators;

import core.AbstractNode;
import core.Edge;
import core.HierarchicalGraph;
import core.Path;

import java.util.Comparator;

public class LongestPathEstimator<T extends AbstractNode<T>> implements PathEstimator<T> {
    @Override
    public int compare(Path<T> path1, Path<T> path2) {
        return Comparator.<Path>comparingInt(p -> -p.getTotalNodes()).thenComparing(Path::getTotalWeight).compare(path1, path2);
    }

    @Override
    public Path<T> mergePaths(Path<T> path1, Path<T> path2) {
        for (int nodes_given = 0; nodes_given <= path1.getEdges().size() + path2.getEdges().size() - 2; ++nodes_given) {
            int path1_given = Integer.min(nodes_given, path1.getEdges().size() - 1);
            int path2_given = nodes_given - path1_given;
            while (path2_given <= Integer.min(nodes_given, path2.getEdges().size() - 1)) {
                int path1_break_index = path1.getEdges().size() - 1 - path1_given;
                //noinspection UnnecessaryLocalVariable
                int path2_break_index = path2_given;
                T first = path1.getEdges().get(path1_break_index).getSecond();
                T second = path2.getEdges().get(path2_break_index).getSecond();
                Edge<T> bridge = HierarchicalGraph.getConnectingEdge(first, second);
                if (bridge != null) {
                    return Path.merge(path1, path2, path1_break_index, path2_break_index, bridge);
                }
                --path1_given;
                ++path2_given;
            }
        }
        return null;
    }
}
