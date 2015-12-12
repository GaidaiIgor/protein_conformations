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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class DummyFilter<T extends AbstractNode<T>> implements PathFilter<T> {
    @Override
    public Set<Path<T>> filterPaths(Collection<Path<T>> allPaths, int maxPaths) {
        return getSorted(allPaths).limit(maxPaths).collect(Collectors.toCollection(HashSet::new));
    }

    public static <T extends AbstractNode<T>> Stream<Path<T>> getSorted(Collection<Path<T>> allPaths) {
        return allPaths.stream().
                sorted(Comparator.<Path<T>>comparingInt(p -> -p.getTotalNodes()).thenComparingDouble(Path::getTotalWeight));
    }
}

class Main {
    public static int getClosestIndex(List<Double> numbers, Double value) {
        return IntStream.range(0, numbers.size()).boxed().min(Comparator.comparingDouble(i -> Math.abs(numbers.get(i) - value))).get();
    }

    public static void main(String[] args) throws IOException {
        List<String> graphsNamesSuffixes = Arrays.asList("_original_sqrt");
        String graphName = "1EZO";
        List<String> graphsNames = graphsNamesSuffixes.stream().map(suffix -> graphName + suffix).collect(Collectors.toList());
        List<Graph<CommonNode>> graphs = getGraphs(Paths.get("E:\\IdeaProjects\\itmo\\conformations\\graphs"), graphsNames);
        java.nio.file.Path output = Paths.get("E:\\IdeaProjects\\itmo\\conformations\\paths");

//        for (Graph<CommonNode> graph: graphs) {
//            List<Edge<CommonNode>> longest = graph.getEdges().stream().sorted(Comparator.comparingDouble(e -> e.getWeight())).
//                    collect(Collectors.toList());
//            int x = 1;
//        }

        DummyFilter<CommonNode> filter = new DummyFilter<>();
//        EllipsePathFinder<CommonNode> pathFinder = ;
//        new SumPathFinder<>(graph, 1, 100, filter)

        // 1CFC_original_sqrt max: Arrays.asList(11, 17, 16, 17, 9), Arrays.asList(17, 18, 17, 22, 11)
        // 1CFC_original_sqrt min: Arrays.asList(20, 3, 4, 6, 2), Arrays.asList(25, 15, 5, 11, 16)
        // 1EZO_original_sqrt: Arrays.asList(9, 4, 7, 2, 2), Arrays.asList(10, 9, 9, 4, 3)
        for (double param = 0.1; param <= 1; param += 0.1) {
            double copy = Math.round(param * 10) / 10.;
            findPathsForIds(Arrays.asList(7), Arrays.asList(9), graphs, output,
                    graph -> new SumPathFinder<>(graph, copy, 100, filter));
        }


//
//
//
//        Path<HierarchicalNode> bestPath = longestPathTest(graph);
//        MappedPath.fromPath(bestPath).export("path");
//        PdbWorker.pdbForPath(bestPath, Paths.get("C:\\Users\\gaida_000.DartLenin-PC\\Desktop\\w\\out"), "out.pdb", "1CFC");
//        List<ComponentInfo> bestDecomposition = clusterizationTest(graph);
//        List<Integer> clusterPath = clusterPath(bestPath, bestDecomposition);
//        System.out.println("Clusters:");
//        System.out.println(clusterPath);
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

    public static void findPathsForIds(List<Integer> startIds, List<Integer> endIds, List<Graph<CommonNode>> graphs,
                                       java.nio.file.Path outputPath, Function<Graph<CommonNode>, PathFinder<CommonNode>> pathFinderBuilder)
            throws IOException {
        for (int i = 0; i < startIds.size(); ++i) {
            int startId = startIds.get(i);
            int endId = endIds.get(i);

            for (Graph<CommonNode> graph : graphs) {
                PathFinder<CommonNode> pathFinder = pathFinderBuilder.apply(graph);
                generatePath(startId, endId, outputPath, graph, pathFinder);
            }
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

    public static void generatePath(int startOldId, int endOldId, java.nio.file.Path outputPath, Graph<CommonNode> graph,
                                    PathFinder<CommonNode> pathFinder)
            throws IOException {
        int startId = mapFromOldId(startOldId, graph);
        int endId = mapFromOldId(endOldId, graph);

        Set<Path<CommonNode>> paths = pathFinder.findPaths(startId, endId);
        Path<CommonNode> best = DummyFilter.getSorted(paths).findFirst().get();
        MappedPath<CommonNode> mappedPath = new MappedPath<>(best);
        int oldStartId = best.getEdges().get(0).getSecond().getOldId();
        int oldEndId = best.getEdges().get(best.getEdges().size() - 1).getSecond().getOldId();
        String param = "";
        String method = "";
        if (pathFinder instanceof EllipsePathFinder) {
            param = String.valueOf(((EllipsePathFinder) pathFinder).getPerifocalDistShare());
            method = "ellipse";
        }
        if (pathFinder instanceof SumPathFinder) {
            param = String.valueOf(((SumPathFinder) pathFinder).getKParam());
            method = "sum";
        }
        String filename = String.join("_", String.valueOf(oldStartId), String.valueOf(oldEndId), method, param, graph.getName()) + ".path";
        String nextPathPath = outputPath.resolve(filename).toString();
        //String.format("%d %d %d%n", e.getFirst().getOldId(), e.getSecond().getOldId(), e.getId())
        mappedPath.export(nextPathPath, e -> String.format("%d %d%n", e.getFirst().getOldId(), e.getSecond().getOldId()));
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
