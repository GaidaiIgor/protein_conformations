import core.Graph;
import core.Path;
import estimators.LongestPathEstimator;
import estimators.MaxWeightDecompositionEstimator;
import estimators.PathEstimator;
import infoproviders.EdgeInfoProvider;
import infoproviders.EdgeWeightInfoProvider;
import infoproviders.NodeColorInfoProvider;
import infoproviders.NodeInfoProvider;
import nodeinfo.ComponentInfo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

class Main {

    public static int getClosestIndex(List<Double> numbers, Double value) {
        return IntStream.range(0, numbers.size()).boxed().min(Comparator.comparingDouble(i -> Math.abs(numbers.get(i) - value))).get();
    }

    public static void longestPathTest() throws IOException {
        FileInputStream pseudo_csv = new FileInputStream("calmodulin_best_paths.csv");
        PipedOutputStream convert_stream = new PipedOutputStream();
        PipedInputStream graph_description = new PipedInputStream(convert_stream);

        new Thread(() -> io.FileFormatConverter.convert_from_pseudo_csv(pseudo_csv, convert_stream)).start();

        Graph graph = Graph.getFromInputStream(graph_description);
        graph_description.close();
        pseudo_csv.close();
        convert_stream.close();

//        FileOutputStream graph_dot = new FileOutputStream("graph.dot");
//        graph.writeDotRepresentation(graph_dot);
//        graph_dot.close();

        int maxNodesPerGraph = 6; // > 1
        long maxPathsPerBucket = 5;
        PathEstimator estimator = new LongestPathEstimator();
        Set<Path> longest = graph.calculateSuboptimalLongestPath(estimator, maxNodesPerGraph, maxPathsPerBucket);
        Path best = longest.stream().max(estimator).get();
        System.out.println(best.getEdges().size());
        System.out.println(best);
    }

    public static void main(String[] args) {
        try {
            clusterizationTest();
        }
        catch (IOException exception) {
            System.err.println(exception.getMessage());
        }
    }

    public static void clusterizationTest() throws IOException {
        FileInputStream graph_description = new FileInputStream("input");
        Graph graph = Graph.getFromInputStream(graph_description);
        graph_description.close();

        List<List<ComponentInfo>> decompositions = graph.allDecompositions();
        List<Double> scores = Graph.estimateAllDecompositions(decompositions, new MaxWeightDecompositionEstimator());
        List<ComponentInfo> bestDecomposition = decompositions.get(2);
        List<NodeInfoProvider> nodeInfoProviders = Collections.singletonList(new NodeColorInfoProvider(bestDecomposition));
        List<EdgeInfoProvider> edgeInfoProviders = Collections.singletonList(new EdgeWeightInfoProvider());
        graph.writeAsDotToFile("test.dot", nodeInfoProviders, edgeInfoProviders);
    }
}
