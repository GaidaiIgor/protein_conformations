import core.Graph;
import core.Path;
import estimators.LongestPathEstimator;
import estimators.PathEstimator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class Main {
    public static void main(String[] args) {
        try {
            FileInputStream pseudo_csv = new FileInputStream("calmodulin_best_paths.csv");
            PipedOutputStream convert_stream = new PipedOutputStream();
            PipedInputStream graph_description = new PipedInputStream(convert_stream);

            new Thread(() -> io.FileFormatConverter.convert_from_pseudo_csv_to_adequate_graph_description(pseudo_csv, convert_stream)).start();
            Graph graph = Graph.getFromInputStream(graph_description);
            pseudo_csv.close();
            convert_stream.close();
            graph_description.close();

            FileOutputStream graph_dot = new FileOutputStream("graph.dot");
            graph.writeDotRepresentation(graph_dot);
            graph_dot.close();

            int max_nodes_per_graph = 10; // > 1
            long max_paths_per_node = 100;
            PathEstimator estimator = new LongestPathEstimator();
//            FileInputStream input = new FileInputStream("input");
//            Graph graph = Graph.getFromInputStream(input);
            Set<Path> longest = graph.calculateSuboptimalLongestPath(estimator, max_nodes_per_graph, max_paths_per_node);
            System.out.println(longest.size());
            Path best = longest.stream().max(estimator).get();
            System.out.println(best.getEdges().size());
            System.out.println(best);

//            List<LongestPathInfo> infos = graph.calculateLongestPaths(10, new LongestPathEstimator());
//            int pop_amount = Integer.parseInt(args[1]);
//            List<List<core.Node>> clusters = k_edge_pop_clusterization(graph, pop_amount);
//            List<List<core.Node>> clusters = graph.estimation_function_clusterization(new estimators.MaxWeightGoodnessEstimator(), 0);
//            graph.calculate_paths(0, new estimators.LongestPathEstimator());
        }
        catch (IOException exception) {
            System.err.println(exception.getMessage());
        }
    }

    public static class Test<T> {
        public List<T> list = new ArrayList<>();

        public <E extends T> void add(E elem) {
            list.add(elem);
        }
    }
}
