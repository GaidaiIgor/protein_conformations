//package estimators;
//
//import core.Node;
//
//import java.util.Collection;
//import java.util.List;
//
//public class MaxWeightGoodnessEstimator implements GoodnessEstimator {
//    public double max_edge_weight(List<Node> cluster) {
//        return cluster.stream().map(n -> n.edges).flatMap(Collection::stream).mapToDouble(e -> e.weight).max().getAsDouble();
//    }
//
//    // O(E)
//    public double estimate_goodness(List<List<Node>> clusters) {
//        double average_goodness = clusters.stream().mapToDouble(this::max_edge_weight).average().getAsDouble();
//        return average_goodness / clusters_amount_fine(clusters);
//    }
//
//    public double clusters_amount_fine(List<List<Node>> clusters) {
//        return clusters.size();
//    }
//}
