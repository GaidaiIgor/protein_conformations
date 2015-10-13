package core;

import estimators.PathEstimator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Path {
    public Set<Integer> used_ids = new HashSet<>();
    public List<Edge> edges = new ArrayList<>();
    public double total_weight = 0;
    public double score = 0;

    public Path() {
    }

    public Path(Set<Integer> used_ids, List<Edge> edges, double total_weight, double score) {
        this.used_ids = used_ids;
        this.edges = edges;
        this.total_weight = total_weight;
        this.score = score;
    }

    public static Path get_trivial_path(Node single_node, PathEstimator estimator) {
        Path new_path = new Path();
        new_path.used_ids.add(single_node.id);
        new_path.edges.add(new Edge(null, single_node, 0));
        new_path.score = estimator.path_score(new_path);
        return new_path;
    }

    // copy edges by reference
    public static Path merge(Path path1, Path path2, int path1_break_index, int path2_break_index, Edge bridge, PathEstimator estimator) {
        Path new_path = new Path();

        new_path.edges.addAll(path1.edges.subList(0, path1_break_index + 1));
        new_path.edges.add(bridge);
        new_path.edges.addAll(path2.edges.subList(path2_break_index + 1, path2.edges.size()));

        new_path.edges.forEach(e -> new_path.used_ids.add(e.second.id));

        new_path.total_weight = new_path.edges.stream().collect(Collectors.summingDouble(e -> e.weight));
        new_path.score = estimator.path_score(new_path);
        return new_path;
    }

    // edges are copied by reference
    public Path copy() {
        Path copy = new Path();
        copy.used_ids.addAll(used_ids);
        copy.edges.addAll(edges);
        copy.total_weight = total_weight;
        copy.score = score;
        return copy;
    }

    @Override
    public int hashCode() {
        int result = 17;
        for (Edge edge : edges) {
            result = 31 * result + edge.hashCode();
        }
        return result;
    }

    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Path)) {
            return false;
        }
        Path other = (Path) object;
        return edges.size() == other.edges.size() && IntStream.range(0, edges.size()).mapToObj(i -> edges.get(i).equals(other.edges.get(i))).allMatch(b -> b);
    }
}
