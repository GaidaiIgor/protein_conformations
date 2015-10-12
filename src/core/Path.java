package core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public class Path implements Comparable<Path> {
    public Set<Integer> used_ids = new HashSet<>();
    public List<Integer> path = new ArrayList<>();
    public double total_weight = 0;
    public double score = 0;

    public Path deep_copy() {
        Path copy = new Path();
        copy.used_ids.addAll(used_ids);
        copy.path.addAll(path);
        copy.total_weight = total_weight;
        copy.score = score;
        return copy;
    }

    public Path extend(int next_id, double edge_weight, double new_score) {
        path.add(next_id);
        used_ids.add(next_id);
        total_weight += edge_weight;
        score = new_score;
        return this;
    }

    public int compareTo(Path other) {
        return IntStream.range(0, path.size()).map(i -> Integer.compare(path.get(i), other.path.get(i))).filter(i -> i != 0).findFirst().
                orElse(0);
    }
}
