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
import java.nio.file.Paths;
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
            int startId = 7;//7 9 6
            int endId = 9;//9 18 9
            List<String> graphsNamesSuffixes = Arrays.asList("_sqrt", "_sqrt_2");
            String graphName = "2D21";
            List<String> graphsNames = graphsNamesSuffixes.stream().map(suffix -> graphName + suffix).collect(Collectors.toList());
            List<Graph<CommonNode>> graphs = getGraphs(Paths.get("E:\\IdeaProjects\\itmo\\conformations\\graphs"), graphsNames);

//            for (Graph<CommonNode> graph: graphs) {
//                List<Edge<CommonNode>> longest = graph.getEdges().stream().sorted(Comparator.comparingDouble(e -> -e.getWeight())).
//                collect(Collectors.toList());
//                startId = longest.get(0).getFirst().getId();
//                endId = longest.get(0).getSecond().getId();
//            }

            generatePaths(startId, endId, Paths.get("E:\\IdeaProjects\\itmo\\conformations\\paths"), graphs);

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

    public static List<Graph<CommonNode>> getGraphs(java.nio.file.Path graphsPath, List<String> graphsNames) throws IOException {
        List<Graph<CommonNode>> result = new ArrayList<>();
        for (String graphName : graphsNames) {
            String nextGraphPath = graphsPath.resolve(graphName).toString();
            Graph<CommonNode> graph = getGraph(nextGraphPath);
            graph.setName(graphName);
            result.add(graph);
        }
        return result;
    }

    public static void generatePaths(int startOldId, int endOldId, java.nio.file.Path pathsPath, List<Graph<CommonNode>> graphs)
            throws IOException {
        for (Graph<CommonNode> graph : graphs) {
            int startId = mapFromOldId(startOldId, graph);
            int endId = mapFromOldId(endOldId, graph);

            DummyFilter<CommonNode> filter = new DummyFilter<>();
            EllipsePathFinder<CommonNode> pathFinder = new EllipsePathFinder<>(graph, 0.2, 100, filter);
            Set<Path<CommonNode>> paths = pathFinder.findPaths(startId, endId);
            Path<CommonNode> best = filter.getSorted(paths).findFirst().get();
            MappedPath<CommonNode> mappedPath = new MappedPath<>(best);
            int oldStartId = best.getEdges().get(0).getSecond().getOldId();
            int oldEndId = best.getEdges().get(best.getEdges().size() - 1).getSecond().getOldId();
            String ellipseParam = String.valueOf(pathFinder.getPerifocalDistShare());
            String nextPathPath = pathsPath.resolve("path_" + oldStartId + "_" + oldEndId + "_ellipse" + ellipseParam + "_" +
                    graph.getName()).toString();
            mappedPath.export(nextPathPath);
        }
    }

    public static Graph<CommonNode> getGraph(String path) throws IOException {
        FileInputStream pseudo_csv = new FileInputStream(path);
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

    public static <T extends AbstractMappedNode<T>> int mapFromOldId(int oldId, Graph<T> graph) {
        return graph.getNodes().stream().filter(n -> n.getOldId() == oldId).findFirst().get().getId();
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
