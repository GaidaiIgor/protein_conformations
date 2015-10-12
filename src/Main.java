import core.Graph;
import estimators.LongestPathEstimator;
import nodeinfo.LongestPathInfo;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

class Main {
    public static void main(String[] args) {
        try {
//            FileInputStream pseudo_csv = new FileInputStream("calmodulin_best_paths.csv");
//            PipedOutputStream convert_stream = new PipedOutputStream();
//            PipedInputStream graph_description = new PipedInputStream(convert_stream);
//
//            new Thread(() -> io.FileFormatConverter.convert_from_pseudo_csv_to_adequate_graph_description(pseudo_csv, convert_stream))
// .start();
//            core.Graph graph = core.Graph.get_from_input_stream(graph_description);
//            pseudo_csv.close();
//            convert_stream.close();
//            graph_description.close();

            int max_nodes_per_graph = 3; // > 1
            FileInputStream input = new FileInputStream("input");
            Graph graph = Graph.get_from_input_stream(input);
            Graph test = graph.hierarchical_decomposition(max_nodes_per_graph);

            FileOutputStream graph_dot = new FileOutputStream("graph.dot");
            graph.write_dot_representation(graph_dot);
            graph_dot.close();

//            add_source_sink(graph);

            List<LongestPathInfo> infos = graph.calculate_longest_paths(10, new LongestPathEstimator());
//            int pop_amount = Integer.parseInt(args[1]);
//            List<List<core.Node>> clusters = k_edge_pop_clusterization(graph, pop_amount);
//            List<List<core.Node>> clusters = graph.estimation_function_clusterization(new estimators.MaxWeightGoodnessEstimator(), 0);
//            graph.calculate_paths(0, new estimators.LongestPathEstimator());
        }
        catch (IOException exception) {
            System.err.println(exception.getMessage());
        }
    }
}
