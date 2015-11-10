import core.*;
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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class DummyFilter<T extends AbstractNode<T>> implements PathFilter<T> {
    @Override
    public Set<Path<T>> filterPaths(Collection<Path<T>> allPaths, int maxPaths) {
        return getSorted(allPaths).limit(maxPaths).collect(Collectors.toCollection(HashSet::new));
    }

    public Stream<Path<T>> getSorted(Collection<Path<T>> allPaths) {
        return allPaths.stream().
                sorted(Comparator.<Path<T>>comparingInt(p -> -p.getTotalNodes()).thenComparingDouble(Path::getTotalWeight));
    }
}

class Main {
    public static int getClosestIndex(List<Double> numbers, Double value) {
        return IntStream.range(0, numbers.size()).boxed().min(Comparator.comparingDouble(i -> Math.abs(numbers.get(i) - value))).get();
    }

    public static void main(String[] args) {
        try {
            Graph<CommonNode> graph = getGraph();
            DummyFilter<CommonNode> filter = new DummyFilter<>();
            EllipsePathFinder<CommonNode> pathFinder = new EllipsePathFinder<>(graph, 0.1, 100, filter);
            Edge<CommonNode> longest = graph.getEdges().stream().max(Comparator.comparingDouble(Edge::getWeight)).get();
            int startId = longest.getFirst().getId();
            int endId = longest.getSecond().getId();
            Set<Path<CommonNode>> paths = pathFinder.findPaths(startId, endId);
            Path<CommonNode> best = filter.getSorted(paths).findFirst().get();
            MappedPath<CommonNode> mappedPath = new MappedPath<>(best);
            int oldStartId = best.getEdges().get(0).getSecond().getOldId();
            int oldEndId = best.getEdges().get(best.getEdges().size() - 1).getSecond().getOldId();
            mappedPath.export("path_" + oldStartId + "_" + oldEndId + "_ellipse1_calmodulin_rmsd_full");

//            Path<HierarchicalNode> bestPath = longestPathTest(graph);
//            MappedPath.fromPath(bestPath).export("path");
//            PdbWorker.pdbForPath(bestPath, Paths.get("C:\\Users\\gaida_000.DartLenin-PC\\Desktop\\w\\out"), "out.pdb", "1CFC");
//            List<ComponentInfo> bestDecomposition = clusterizationTest(graph);
//            List<Integer> clusterPath = clusterPath(bestPath, bestDecomposition);
//            System.out.println("Clusters:");
//            System.out.println(clusterPath);
        }
        catch (IOException exception) {
            System.err.println(exception.getMessage());
        }
    }

    public static Graph<CommonNode> getGraph() throws IOException {
        FileInputStream pseudo_csv = new FileInputStream("E:\\IdeaProjects\\itmo\\conformations\\matlab_scripts\\calmodulin_rmsd_full");
        PipedOutputStream convert_stream = new PipedOutputStream();
        PipedInputStream graph_description = new PipedInputStream(convert_stream);

        new Thread(() -> io.FileFormatConverter.convertFromPseudoCsv(pseudo_csv, convert_stream)).start();

        Graph<CommonNode> graph = Graph.getFromInputStream(graph_description, CommonNode::readNode);
        graph_description.close();
        pseudo_csv.close();
        convert_stream.close();

//        FileInputStream graph_description = new FileInputStream("input");
//        HierarchicalGraph graph = HierarchicalGraph.getFromInputStream(graph_description);
//        graph_description.close();

        return graph;
    }

    public static Path<HierarchicalNode> longestPathTest(HierarchicalGraph graph) throws IOException {
//        FileOutputStream graph_dot = new FileOutputStream("graph.dot");
//        graph.writeDotRepresentation(graph_dot);
//        graph_dot.close();

        int maxNodesPerGraph = 6; // > 1
        long maxPathsPerBucket = 5;
        PathEstimator<HierarchicalNode> estimator = new LongestPathEstimator<>();
        Set<Path<HierarchicalNode>> longest = graph.calculateSuboptimalLongestPath(estimator, maxNodesPerGraph, maxPathsPerBucket, 1, 2);
        Path<HierarchicalNode> best = longest.stream().max(estimator).get();
        System.out.println(best);
        System.out.println("Old ids:");
        System.out.println((new MappedPath<>(best)).toStringOldId());

        return best;
    }

    public static List<ComponentInfo> clusterizationTest(HierarchicalGraph graph) throws IOException {
        List<List<ComponentInfo>> decompositions = graph.allDecompositions();
        List<Double> scores = HierarchicalGraph.estimateAllDecompositions(decompositions, new MaxWeightDecompositionEstimator());
        List<ComponentInfo> bestDecomposition = decompositions.get(8);
        List<NodeInfoProvider> nodeInfoProviders = Collections.singletonList(new NodeColorInfoProvider(bestDecomposition));
        List<EdgeInfoProvider> edgeInfoProviders = Collections.singletonList(new EdgeWeightInfoProvider());
        graph.writeAsDotToFile("test.dot", nodeInfoProviders, edgeInfoProviders);

        return bestDecomposition;
    }

    public static List<Integer> clusterPath(Path<HierarchicalNode> path, List<ComponentInfo> clusterization) {
        List<Integer> clusters = new ArrayList<>();
        for (Edge<HierarchicalNode> edge : path.getEdges()) {
            HierarchicalNode next = edge.getSecond();
            clusters.add(clusterization.get(next.getId()).component);
        }
        return clusters;
    }
}
