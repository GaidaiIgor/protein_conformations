import core.Edge;
import core.Graph;
import core.Node;
import core.Path;
import estimators.LongestPathEstimator;
import estimators.MaxWeightDecompositionEstimator;
import estimators.PathEstimator;
import infoproviders.EdgeInfoProvider;
import infoproviders.EdgeWeightInfoProvider;
import infoproviders.NodeColorInfoProvider;
import infoproviders.NodeInfoProvider;
import io.PdbWorker;
import nodeinfo.ComponentInfo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.IntStream;

class Main {
    public static int getClosestIndex(List<Double> numbers, Double value) {
        return IntStream.range(0, numbers.size()).boxed().min(Comparator.comparingDouble(i -> Math.abs(numbers.get(i) - value))).get();
    }

    public static void main(String[] args) {
//        String test = "aqbawbaeb";
//        Pattern pattern = Pattern.compile("(?:a(\\w)b)+");
//        Matcher m = pattern.matcher(test);
//        if (m.matches()) {
//            int g = m.groupCount();
//            String t = m.group(0);
//        }
        try {
            Graph graph = getGraph();
            Path bestPath = longestPathTest(graph);
            PdbWorker.pdbForPath(bestPath, Paths.get("C:\\Users\\gaida_000.DartLenin-PC\\Desktop\\w\\out"), "out.pdb", "1CFC");
//            List<ComponentInfo> bestDecomposition = clusterizationTest(graph);
//            List<Integer> clusterPath = clusterPath(bestPath, bestDecomposition);
//            System.out.println("Clusters:");
//            System.out.println(clusterPath);
        }
        catch (IOException exception) {
            System.err.println(exception.getMessage());
        }
    }

    public static Graph getGraph() throws IOException {
        FileInputStream pseudo_csv = new FileInputStream("calmodulin_best_paths.csv");
        PipedOutputStream convert_stream = new PipedOutputStream();
        PipedInputStream graph_description = new PipedInputStream(convert_stream);

        new Thread(() -> io.FileFormatConverter.convertFromPseudoCsv(pseudo_csv, convert_stream)).start();

        Graph graph = Graph.getFromInputStream(graph_description);
        graph_description.close();
        pseudo_csv.close();
        convert_stream.close();

//        FileInputStream graph_description = new FileInputStream("input");
//        Graph graph = Graph.getFromInputStream(graph_description);
//        graph_description.close();

        return graph;
    }

    public static Path longestPathTest(Graph graph) throws IOException {
//        FileOutputStream graph_dot = new FileOutputStream("graph.dot");
//        graph.writeDotRepresentation(graph_dot);
//        graph_dot.close();

        int maxNodesPerGraph = 6; // > 1
        long maxPathsPerBucket = 5;
        PathEstimator estimator = new LongestPathEstimator();
        Set<Path> longest = graph.calculateSuboptimalLongestPath(estimator, maxNodesPerGraph, maxPathsPerBucket, 1, 2);
        Path best = longest.stream().max(estimator).get();
        System.out.println(best);
        System.out.println("Old ids:");
        System.out.println(best.toStringOldId());

        return best;
    }

    public static List<ComponentInfo> clusterizationTest(Graph graph) throws IOException {
        List<List<ComponentInfo>> decompositions = graph.allDecompositions();
        List<Double> scores = Graph.estimateAllDecompositions(decompositions, new MaxWeightDecompositionEstimator());
        List<ComponentInfo> bestDecomposition = decompositions.get(8);
        List<NodeInfoProvider> nodeInfoProviders = Collections.singletonList(new NodeColorInfoProvider(bestDecomposition));
        List<EdgeInfoProvider> edgeInfoProviders = Collections.singletonList(new EdgeWeightInfoProvider());
        graph.writeAsDotToFile("test.dot", nodeInfoProviders, edgeInfoProviders);

        return bestDecomposition;
    }

    public static List<Integer> clusterPath(Path path, List<ComponentInfo> clusterization) {
        List<Integer> clusters = new ArrayList<>();
        for (Edge edge : path.getEdges()) {
            Node next = edge.getSecond();
            clusters.add(clusterization.get(next.getId()).component);
        }
        return clusters;
    }
}
