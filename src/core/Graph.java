package core;

import estimators.PathEstimator;
import nodeinfo.ComponentInfo;
import nodeinfo.LongestPathInfo;
import nodeinfo.NodeInfo;
import nodeinfo.TreeInfo;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Graph {
    private final List<Node> nodes;
    private final List<Edge> edges;
    private final int totalSize;
    private Set<Path> cachedPaths = null;

    public Graph() {
        this(new ArrayList<>());
    }

    public Graph(List<Node> nodes) {
        this.nodes = nodes;
        edges = new ArrayList<>();
        totalSize = nodes.stream().mapToInt(Node::getSize).sum();
    }

    private static <T extends NodeInfo> Stream<T> getNeighboursStream(T node, List<T> graph) {
        return node.getNode().getEdges().stream().map(e -> graph.get(e.getSecond().getId()));
    }

    // O(V + E)
    private static void setTreeInfo(TreeInfo current, int level, List<TreeInfo> tree) {
        current.visited = true;
        current.level = level;
        List<TreeInfo> children = getNeighboursStream(current, tree).filter(i -> !i.visited).collect(Collectors.toList());
        if (!children.isEmpty()) {
            children.forEach(c -> setTreeInfo(c, level + 1, tree));
            current.subtreeSize = children.stream().mapToInt(c -> c.subtreeSize).sum() + 1;
        }
    }

    private static TreeInfo getParent(TreeInfo current, List<TreeInfo> tree) {
        return getNeighboursStream(current, tree).filter(n -> n.level < current.level).findFirst().orElse(null);
    }

    // O(V + E)
    // returns previous size
    private static int updateTreeSizes(int tweakSize, TreeInfo current, List<TreeInfo> tree) {
        TreeInfo parent = getParent(current, tree);
        int currentSize = current.subtreeSize;
        current.subtreeSize -= tweakSize;
        return parent == null ? currentSize : updateTreeSizes(tweakSize, parent, tree);
    }

    private static void addMultisetValue(Map<Integer, Integer> map, Integer value) {
        map.computeIfAbsent(value, v -> 0);
        map.put(value, map.get(value) + 1);
    }

    private static void removeMultisetValue(Map<Integer, Integer> map, Integer value) {
        int count = map.get(value);
        if (count == 0) {
            System.err.println("Error: value is absent");
        }
        if (count == 1) {
            map.remove(value);
            return;
        }
        map.put(value, count - 1);
    }

    private static void updateComponentsSizes(Map<Integer, Integer> componentsSizes, int oldComponentSize, int newComponent1Size, int newComponent2Size) {
        removeMultisetValue(componentsSizes, oldComponentSize);
        addMultisetValue(componentsSizes, newComponent1Size);
        addMultisetValue(componentsSizes, newComponent2Size);
    }

    private static void setComponentInfo(ComponentInfo current, int component, List<ComponentInfo> all_info) {
        current.visited = true;
        current.component = component;
        getNeighboursStream(current, all_info).filter(n -> !n.visited).forEach(n -> setComponentInfo(n, component, all_info));
    }

    public static Graph getFromInputStream(InputStream inputStream) {
        Scanner in = new Scanner(inputStream);
        int verticesAmount = in.nextInt();
        int edgesAmount = in.nextInt();
        List<Node> nodes = IntStream.range(0, verticesAmount).mapToObj(Node::new).collect(Collectors.toList());
        Graph graph = new Graph(nodes);
        for (int i = 0; i < edgesAmount; ++i) {
            int node1Id = in.nextInt();
            int node2Id = in.nextInt();
            double weight = in.nextDouble();
            graph.addBidirectionalEdge(graph.nodes.get(node1Id), graph.nodes.get(node2Id), weight);
        }
        return graph;
    }

    public void addBidirectionalEdge(Node node1, Node node2, double weight) {
        addEdge(node1, node2, weight);
        addEdge(node2, node1, weight);
    }

    public Edge addEdge(Node node1, Node node2, double weight) {
        Edge newEdge = new Edge(node1, node2, weight);
        node1.getEdges().add(newEdge);
        edges.add(newEdge);
        return newEdge;
    }

    private static <T extends NodeInfo> T getCorrespondingInfo(Node node, List<T> allInfos) {
        return allInfos.get(node.getId());
    }

    private static <T extends NodeInfo> List<Integer> getIds(List<T> infos) {
        return infos.stream().map(i -> i.getNode().getId()).collect(Collectors.toList());
    }

    public static Edge getConnectingEdge(Node first, Node second) {
        return first.getEdges().stream().filter(e -> e.getSecond() == second).findFirst().orElse(null);
    }

    private static <T extends Collection<Path>> T trimPaths(T paths, PathEstimator estimator) {
        for (Path path : paths) {
            path.getUsedIds().remove(path.getEdges().get(0).getSecond().getId());
            path.getUsedIds().remove(path.getEdges().get(path.getEdges().size() - 1).getSecond().getId());
            path.getEdges().subList(0, 2).clear();
            Node first = path.getEdges().get(0).getFirst();
            path.getEdges().add(0, new Edge(null, first, 0));
            path.getEdges().remove(path.getEdges().size() - 1);
            path.setTotalNodes(path.getTotalNodes() - 2);
            path.setScore(estimator.pathScore(path));
        }
        return paths;
    }

    private static Set<Path> connectComponentPaths(Set<Path> node1Paths, Set<Path> node2Paths, PathEstimator estimator) {
        Set<Path> result = new HashSet<>();
        result.addAll(node1Paths);
        result.addAll(node2Paths);
        for (Path path1 : node1Paths) {
            for (Path path2 : node2Paths) {
                Path connected = estimator.mergePaths(path1, path2);
                if (connected != null) {
                    result.add(connected);
                    if (connected.getTotalNodes() > 11) {
                        System.out.println(connected);
                    }
                }
            }
        }
        return result;
    }

    private static Set<Path> siftPaths(Set<Path> allPaths, long maxPaths, PathEstimator estimator) {
        if (allPaths.size() <= maxPaths) {
            return allPaths;
        }
        return allPaths.stream().sorted(estimator).limit(maxPaths).collect(Collectors.toCollection(HashSet::new));
    }

    public int getTotalSize() {
        return totalSize;
    }

    // connects components of this graph with edges of infinite weight
    // returns list of added edges
    private List<Edge> connectComponents() {
        List<Edge> result = new ArrayList<>();
        List<ComponentInfo> componentInfos = getComponentInfo();
        Map<Integer, List<ComponentInfo>> componentGroups = componentInfos.stream().collect(Collectors.groupingBy(ci -> ci.component));
        List<List<ComponentInfo>> groups = new ArrayList<>(componentGroups.values());
        for (int i = 0; i < componentGroups.size() - 1; ++i) {
            Node currentComponentMember = groups.get(i).get(0).node;
            Node nextComponentMember = groups.get(i + 1).get(0).node;
            Edge newEdge1 = addEdge(currentComponentMember, nextComponentMember, Double.POSITIVE_INFINITY);
            Edge newEdge2 = addEdge(nextComponentMember, currentComponentMember, Double.POSITIVE_INFINITY);
            result.add(newEdge1);
            result.add(newEdge2);
        }
        return result;
    }

    // O(E * log(V))
    public Graph minimalSpanningTree() {
        List<Edge> fictive = connectComponents();
        List<Boolean> isInTree = new ArrayList<>(Collections.nCopies(nodes.size(), false));
        PriorityQueue<Edge> edgeQueue = new PriorityQueue<>(Comparator.comparingDouble(Edge::getWeight));

        List<Node> new_nodes = nodes.stream().map(n -> new Node(n.getId())).collect(Collectors.toList());
        Graph result = new Graph(new_nodes);

        Node start = nodes.get(0);
        isInTree.set(start.getId(), true);
        start.getEdges().forEach(edgeQueue::add);

        int nodesLeft = nodes.size() - 1;
        while (nodesLeft > 0) {
            Edge nextEdge = edgeQueue.poll();
            if (isInTree.get(nextEdge.getSecond().getId())) {
                continue;
            }
            result.addBidirectionalEdge(result.nodes.get(nextEdge.getFirst().getId()), result.nodes.get(nextEdge.getSecond().getId()), nextEdge.getWeight());
            Node newNode = nextEdge.getSecond();
            isInTree.set(newNode.getId(), true);
            newNode.getEdges().stream().filter(e -> !isInTree.get(e.getSecond().getId())).forEach(edgeQueue::add);
            nodesLeft -= 1;
        }
        removeEdges(fictive);
        return result;
    }

    // O(V + E)
    public List<TreeInfo> collectTreeInfo() {
        List<TreeInfo> result = nodes.stream().map(TreeInfo::new).collect(Collectors.toList());
        setTreeInfo(result.get(0), 0, result);
        return result;
    }

    private void splitByEdge(Edge edge, Map<Integer, Integer> componentSizes, List<TreeInfo> tree, Graph mst) {
        TreeInfo node1 = tree.get(edge.getFirst().getId());
        TreeInfo node2 = tree.get(edge.getSecond().getId());
        TreeInfo parent = node1.level < node2.level ? node1 : node2;
        TreeInfo child = node1.level > node2.level ? node1 : node2;
        int oldSize = updateTreeSizes(child.subtreeSize, parent, tree);
        updateComponentsSizes(componentSizes, oldSize, oldSize - child.subtreeSize, child.subtreeSize);
        mst.removeBidirectionalEdge(edge);
    }

    public List<ComponentInfo> getComponentInfo() {
        List<ComponentInfo> result = nodes.stream().map(ComponentInfo::new).collect(Collectors.toList());
        int component = 0;
        for (int i = 0; i < result.size(); ++i) {
            if (!result.get(i).visited) {
                setComponentInfo(result.get(i), component, result);
                component += 1;
            }
        }
        return result;
    }

    public void removeEdges(Collection<Edge> edges) {
        edges.forEach(this::removeEdge);
    }

    public void removeEdge(Edge edge) {
        edge.getFirst().getEdges().remove(edge);
        edges.remove(edge);
    }

    public void removeBidirectionalEdge(Edge edge) {
        Edge reverse = getConnectingEdge(edge.getSecond(), edge.getFirst());
        removeEdge(edge);
        removeEdge(reverse);
    }

    // O(V + E)
    // ids should be unique
    // underlying graph is copying by reference
    public Graph subgraph(List<Integer> nodeIds) {
        List<Integer> idCorrespondence = new ArrayList<>(Collections.nCopies(nodes.size(), -1));
        IntStream.range(0, nodeIds.size()).forEach(i -> idCorrespondence.set(nodeIds.get(i), i));

        List<Boolean> isSelected = new ArrayList<>(Collections.nCopies(nodes.size(), false));
        nodeIds.forEach(i -> isSelected.set(i, true));

        List<Node> newNodes = IntStream.range(0, nodeIds.size()).mapToObj(i -> Node.forkOther(i, nodes.get(nodeIds.get(i)))).
                collect(Collectors.toList());
        Graph newGraph = new Graph(newNodes);
        edges.stream().filter(e -> isSelected.get(e.getFirst().getId()) && isSelected.get(e.getSecond().getId())).forEach(e -> newGraph.
                addEdge(newNodes.get(idCorrespondence.get(e.getFirst().getId())), newNodes.get(idCorrespondence.get(e.getSecond().getId())), e.getWeight()));
        return newGraph;
    }

    private Map<List<Integer>, Double> deduceComponentEdges(List<Node> componentNodes, List<ComponentInfo> decompositionInfo) {
        Map<List<Integer>, List<Double>> newEdgesInfo = new HashMap<>();
        for (Edge e : edges) {
            Node first = componentNodes.get(getCorrespondingInfo(e.getFirst(), decompositionInfo).component);
            Node second = componentNodes.get(getCorrespondingInfo(e.getSecond(), decompositionInfo).component);

            if (first != second) {
                List<Integer> edgeDescription = Arrays.asList(first.getId(), second.getId());
                newEdgesInfo.putIfAbsent(edgeDescription, new ArrayList<>());
                newEdgesInfo.get(edgeDescription).add(e.getWeight());
            }
        }
        Map<List<Integer>, Double> result = new HashMap<>();
        newEdgesInfo.entrySet().forEach(e -> result.put(e.getKey(), e.getValue().stream().collect(Collectors.averagingDouble(d -> d))));
        return result;
    }

    // O(V * (V + E) * log(V))
    // Supports only connected graphs. If graph is disconnected then it will be made connected.
    public Graph hierarchicalDecomposition(int maxVerticesPerGraph) {
        if (maxVerticesPerGraph <= 1) {
            throw new RuntimeException("Max vertices should be >= 2");
        }
        if (nodes.size() <= maxVerticesPerGraph) {
            return this;
        }
        List<ComponentInfo> decompositionInfo = decompositionInfo(maxVerticesPerGraph);
        Map<Integer, List<ComponentInfo>> components = decompositionInfo.stream().collect(Collectors.groupingBy(ci -> ci.component));
        List<Node> newNodes = new ArrayList<>(Collections.nCopies(components.size(), new Node(0)));
        for (Map.Entry<Integer, List<ComponentInfo>> entry : components.entrySet()) {
            Graph subgraph = subgraph(getIds(entry.getValue()));
            newNodes.set(entry.getKey(), new Node(entry.getKey(), subgraph, -1));
        }
        Graph newGraph = new Graph(newNodes);
        Map<List<Integer>, Double> newEdges = deduceComponentEdges(newNodes, decompositionInfo);
        for (Map.Entry<List<Integer>, Double> e : newEdges.entrySet()) {
            newGraph.addEdge(newNodes.get(e.getKey().get(0)), newNodes.get(e.getKey().get(1)), e.getValue());
        }
        return newGraph.hierarchicalDecomposition(maxVerticesPerGraph);
    }

    // O(V * (V + E))
    public List<ComponentInfo> decompositionInfo(int maxVerticesPerGraph) {
        Graph mst = minimalSpanningTree();
        List<TreeInfo> treeInfo = mst.collectTreeInfo();

        PriorityQueue<Edge> edgeQueue = new PriorityQueue<>(Comparator.comparingDouble(e -> -e.getWeight()));
        edgeQueue.addAll(mst.edges.stream().filter(e -> e.getFirst().getId() < e.getSecond().getId()).collect(Collectors.toList()));

        TreeMap<Integer, Integer> componentSizes = new TreeMap<>();
        componentSizes.put(nodes.size(), 1);
        while (componentSizes.lastKey() > maxVerticesPerGraph) {
            Edge max_edge = edgeQueue.poll();
            splitByEdge(max_edge, componentSizes, treeInfo, mst);
        }
        return mst.getComponentInfo();
    }

    private void addSourceSink() {
        Node source = new Node(nodes.size());
        Node sink = new Node(nodes.size() + 1);
        nodes.forEach(n -> addEdge(source, n, 0));
        nodes.forEach(n -> addEdge(n, sink, 0));
        nodes.add(source);
        nodes.add(sink);
    }

    public void writeAsDotToFile(String filename) {
        try {
            FileOutputStream output = new FileOutputStream(filename);
            writeDotRepresentation(output);
            output.close();
        }
        catch (IOException exception) {
            System.err.println(exception.getMessage());
        }
    }

    public void writeDotRepresentation(OutputStream outputStream) {
        PrintWriter writer = new PrintWriter(outputStream);
        writer.println("digraph {");
        nodes.forEach(n -> writer.format("%d [label=%d];%n", n.getId(), n.getId()));
        edges.forEach(e -> writer.format("%d -> %d [weight=%.0f label=%.0f];%n", e.getFirst().getId(), e.getSecond().getId(), e.getWeight(), e.getWeight()));
        writer.println("}");
        writer.flush();
    }

    private Path mapPath(Path decomposed, Graph initial) {
        List<Edge> initialEdges = new ArrayList<>();
        Node previousVertex = initial.nodes.get(decomposed.getEdges().get(0).getSecond().getOldId());
        initialEdges.add(new Edge(null, previousVertex, 0));
        for (int i = 1; i < decomposed.getEdges().size(); ++i) {
            Node nextVertex = initial.nodes.get(decomposed.getEdges().get(i).getSecond().getOldId());
            Edge connectingEdge = getConnectingEdge(previousVertex, nextVertex);
            initialEdges.add(connectingEdge);
            previousVertex = nextVertex;
        }
        Set<Integer> usedIds = new HashSet<>();
        initialEdges.forEach(e -> usedIds.add(e.getSecond().getId()));
        return new Path(usedIds, initialEdges, decomposed);
    }

    private Set<Path> mapPaths(Set<Path> decomposedPaths, Graph initial) {
        return decomposedPaths.stream().map(p -> mapPath(p, initial)).collect(Collectors.toCollection(HashSet::new));
    }

    private void writeHierarchy(List<String> parentIds) {
        String name = "graph" + String.join("_", parentIds) + ".dot";
        writeAsDotToFile(name);
        if (nodes.get(0).getUnderlyingGraph() == null) {
            return;
        }
        for (int i = 0; i < nodes.size(); ++i) {
            List<String> copy = new ArrayList<>(parentIds);
            copy.add(String.valueOf(i));
            nodes.get(i).getUnderlyingGraph().writeHierarchy(copy);
        }
    }

    // O(log(V) * (V * (V + E) + T(merge)))
    public Set<Path> calculateSuboptimalLongestPath(PathEstimator estimator, int maxNodesPerGraph, long maxPathsPerNode) {
        Graph decomposed = hierarchicalDecomposition(maxNodesPerGraph);
//        decomposed.writeHierarchy(new ArrayList<>());
//        writeAsDotToFile("initial.dot");
        return decomposed.calculateSuboptimalPathsHelper(estimator, this, maxPathsPerNode);
    }

    // O(log(V) * T(merge))
    // initial should be decomposed graph
    private Set<Path> calculateSuboptimalPathsHelper(PathEstimator estimator, Graph initial, long maxPathsPerGraph) {
        if (cachedPaths != null) {
            return cachedPaths;
        }
        addSourceSink();
        LongestPathInfo pathInfo = calculateLongestPaths(nodes.size() - 2, estimator).get(nodes.size() - 1);
        Set<Path> currentLevelGraphPaths = siftPaths(trimPaths(pathInfo.allPaths, estimator), maxPathsPerGraph, estimator);
        if (pathInfo.allPaths.iterator().next().getEdges().get(0).getSecond().getUnderlyingGraph() == null) {
            cachedPaths = mapPaths(currentLevelGraphPaths, initial);
            return cachedPaths;
        }

        Set<Path> allUnderlyingPath = new HashSet<>();
        for (Path path : currentLevelGraphPaths) {
            Set<Path> currentUnderlyingPaths = path.getEdges().get(0).getSecond().getUnderlyingGraph().
                    calculateSuboptimalPathsHelper(estimator, initial, maxPathsPerGraph);
            for (int i = 1; i < path.getEdges().size(); ++i) {
                Set<Path> nextNodeUnderlyingPaths = path.getEdges().get(i).getSecond().getUnderlyingGraph().
                        calculateSuboptimalPathsHelper(estimator, initial, maxPathsPerGraph);
                currentUnderlyingPaths = connectComponentPaths(currentUnderlyingPaths, nextNodeUnderlyingPaths, estimator);
            }
            allUnderlyingPath.addAll(currentUnderlyingPaths);
        }
        cachedPaths = siftPaths(allUnderlyingPath, maxPathsPerGraph, estimator);
        return cachedPaths;
    }

    private void updateLongestPathsInfos(List<LongestPathInfo> toUpdate) {
        for (LongestPathInfo info : toUpdate) {
            info.currentPaths = info.nextPaths;
            info.nextPaths = new HashSet<>();
        }
    }

    // O*(V^V)
    public List<LongestPathInfo> calculateLongestPaths(int startNodeId, PathEstimator estimator) {
        List<LongestPathInfo> infos = nodes.stream().map(LongestPathInfo::new).collect(Collectors.toList());
//        writeAsDotToFile("test.dot");
        LongestPathInfo startNode = infos.get(startNodeId);
        Path trivialPath = Path.getTrivialPath(nodes.get(startNodeId), estimator);
        startNode.allPaths.add(trivialPath);
        startNode.currentPaths.add(trivialPath);
        List<Edge> allEdges = edges.stream().collect(Collectors.toList());

        boolean nextPathsPending = true;
        while (nextPathsPending) {
            nextPathsPending = false;
            for (Edge edge : allEdges) {
                LongestPathInfo node1Info = infos.get(edge.getFirst().getId());
                LongestPathInfo node2Info = infos.get(edge.getSecond().getId());
                nextPathsPending |= tryUpdateNext(node1Info, node2Info, edge, estimator);
            }
            updateLongestPathsInfos(infos);
        }
        return infos;
    }

    // O*(V^V)
    private boolean tryUpdateNext(LongestPathInfo previous, LongestPathInfo next, Edge edge, PathEstimator estimator) {
        List<Path> extendedPaths = previous.currentPaths.stream().filter(p -> !p.getUsedIds().contains(next.node.getId())).
                map(p -> Path.merge(p, Path.getTrivialPath(next.node, estimator), p.getEdges().size() - 1, 0, edge, estimator)).
                filter(p -> !next.allPaths.contains(p)).
                collect(Collectors.toList());
        next.allPaths.addAll(extendedPaths);
        next.nextPaths.addAll(extendedPaths);
        return next.nextPaths.size() > 0;
    }
}