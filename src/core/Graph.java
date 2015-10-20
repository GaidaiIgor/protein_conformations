package core;

import estimators.PathEstimator;
import nodeinfo.ComponentInfo;
import nodeinfo.LongestPathInfo;
import nodeinfo.NodeInfo;
import nodeinfo.TreeInfo;

import java.io.*;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Graph {
    private final List<Node> nodes;
    private final List<Edge> edges;
    private final int totalSize;
    private Map<Integer, Map<Integer, Set<Path>>> cachedPaths = null;

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

    private static void updateComponentsSizes(Map<Integer, Integer> componentsSizes, int oldComponentSize, int newComponent1Size, int
            newComponent2Size) {
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
        for (Path path1 : node1Paths) {
            for (Path path2 : node2Paths) {
                Path connected = estimator.mergePaths(path1, path2);
                result.add(connected);
            }
        }
        return result;
    }

    private static <E extends Path, T extends Collection<E>> T siftPaths(T allPaths, long maxPaths, PathEstimator estimator,
                                                                         Supplier<T> tSupplier) {
        if (allPaths.size() <= maxPaths) {
            return allPaths;
        }
        return allPaths.stream().sorted(estimator).limit(maxPaths).collect(Collectors.toCollection(tSupplier));
    }

    private static Set<Integer> neighbourComponents(Node node, List<Integer> components) {
        //        neighbours.remove(components.get(node.getId()));
        return node.getEdges().stream().map(e -> components.get(e.getSecond().getId())).collect(Collectors.toSet());
    }

    private static <E extends Path, T extends Collection<E>>
    void assignPath(E path, List<Integer> components, Map<Integer, Map<Integer, T>> sorter, Supplier<T> tSupplier) {
        List<Edge> pathEdges = path.getEdges();
        Node first = pathEdges.get(0).getSecond();
        Node last = pathEdges.get(pathEdges.size() - 1).getSecond();
        Set<Integer> leftNeighbours = neighbourComponents(first, components);
        Set<Integer> rightNeighbours = neighbourComponents(last, components);
        for (Integer leftId : leftNeighbours) {
            for (Integer rightId : rightNeighbours) {
                sorter.putIfAbsent(leftId, new HashMap<>());
                sorter.get(leftId).putIfAbsent(rightId, tSupplier.get());
                sorter.get(leftId).get(rightId).add(path);
            }
        }
    }

    private static <E extends Path, T extends Collection<E>, R extends Collection<E>>
    Map<Integer, Map<Integer, R>> separatePaths(T paths, Supplier<R> newInstanceSupplier, List<Integer> nodesAddresses,
                                                long maxPathsPerBucket, PathEstimator estimator) {
        Map<Integer, Map<Integer, R>> result = new HashMap<>();
        for (E path : paths) {
            assignPath(path, nodesAddresses, result, newInstanceSupplier);
        }
        for (Integer leftId : result.keySet()) {
            for (Integer rightId : result.get(leftId).keySet()) {
                R currentBucket = result.get(leftId).get(rightId);
                result.get(leftId).put(rightId, siftPaths(currentBucket, maxPathsPerBucket, estimator, newInstanceSupplier));
            }
        }
        return result;
    }

    public static Edge getConnectingEdge(Node first, Node second) {
        return first.getEdges().stream().filter(e -> e.getSecond() == second).findFirst().orElse(null);
    }

    private static Set<Path> getPathsEndingAt(Integer componentId, Map<Integer, Map<Integer, Set<Path>>> allPaths) {
        Set<Path> result = new HashSet<>();
        for (Map<Integer, Set<Path>> innerMap : allPaths.values()) {
            Set<Path> current = innerMap.get(componentId);
            if (current != null) {
                result.addAll(current);
            }
        }
        return result.size() == 0 ? null : result;
    }

    private static Set<Path> getPathsStartingAt(Integer componentId, Map<Integer, Map<Integer, Set<Path>>> allPaths) {
        Set<Path> result = new HashSet<>();
        Map<Integer, Set<Path>> correctMap = allPaths.get(componentId);
        if (correctMap == null) {
            return null;
        }
        correctMap.values().forEach(result::addAll);
        return result;
    }

    private static Set<Path> getAllPaths(Map<Integer, Map<Integer, Set<Path>>> allPaths) {
        Set<Path> result = new HashSet<>();
        allPaths.values().forEach(m -> m.values().forEach(result::addAll));
        return result;
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
            result.addBidirectionalEdge(result.nodes.get(nextEdge.getFirst().getId()), result.nodes.get(nextEdge.getSecond().getId()),
                    nextEdge.getWeight());
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

//    private void setParentNode(Node parent) {
//        nodes.forEach(n -> n.setParent(parent));
//    }

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
                addEdge(newNodes.get(idCorrespondence.get(e.getFirst().getId())), newNodes.get(idCorrespondence.get(e.getSecond().getId()
                )), e.getWeight()));
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
        if (maxVerticesPerGraph < 2) {
            throw new AssertionError("Max vertices should be >= 2");
        }
        if (nodes.size() <= maxVerticesPerGraph) {
            return this;
        }
        List<ComponentInfo> decompositionInfo = decompositionInfo(maxVerticesPerGraph);
        Map<Integer, List<ComponentInfo>> components = decompositionInfo.stream().collect(Collectors.groupingBy(ci -> ci.component));
        List<Node> newNodes = new ArrayList<>(Collections.nCopies(components.size(), new Node(0)));
        for (Map.Entry<Integer, List<ComponentInfo>> entry : components.entrySet()) {
            Graph subgraph = subgraph(getIds(entry.getValue()));
            Node parent = new Node(entry.getKey(), subgraph, -1);
//            subgraph.setParentNode(parent);
            newNodes.set(entry.getKey(), parent);
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
        edges.forEach(e -> writer.format("%d -> %d [weight=%.0f label=%.0f];%n", e.getFirst().getId(), e.getSecond().getId(), e.getWeight
                (), e.getWeight()));
        writer.println("}");
        writer.flush();
    }

    private Set<Path> mapPaths(Set<Path> decomposedPaths, Graph initial) {
        return decomposedPaths.stream().map(p -> mapPath(p, initial)).collect(Collectors.toCollection(HashSet::new));
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

    private void setComponentAddress(List<List<Integer>> addresses, List<Integer> currentAddress) {
        for (Node node : nodes) {
            if (node.getUnderlyingGraph() == null) {
                addresses.set(node.getOldId(), currentAddress);
            } else {
                List<Integer> newAddress = new ArrayList<>(currentAddress);
                newAddress.add(node.getId());
                node.getUnderlyingGraph().setComponentAddress(addresses, newAddress);
            }
        }
    }

    private List<List<Integer>> nodeComponentAddress() {
        List<List<Integer>> result = new ArrayList<>(Collections.nCopies(getTotalSize(), null));
        List<Integer> currentAddress = new ArrayList<>(Collections.singletonList(0));
        setComponentAddress(result, currentAddress);
        return result;
    }

    // O(log(V) * (V * (V + E) + T(merge)))
    public Set<Path> calculateSuboptimalLongestPath(PathEstimator estimator, int maxNodesPerGraph, long maxPathsPerBucket) {
        Graph decomposed = hierarchicalDecomposition(maxNodesPerGraph);
        List<List<Integer>> componentAddresses = decomposed.nodeComponentAddress();
//        decomposed.writeHierarchy(new ArrayList<>());
//        writeAsDotToFile("initial.dot");
        Map<Integer, Map<Integer, Set<Path>>> result =
                decomposed.calculateSuboptimalPathsHelper(estimator, this, maxPathsPerBucket, 0, componentAddresses);
        return result.get(0).get(0);
    }

    private List<Integer> currentLevelAddresses(List<List<Integer>> componentAddresses, int graphLevel) {
        return componentAddresses.stream().map(list -> list.get(graphLevel)).collect(Collectors.toList());
    }

    // O(log(V) * T(merge))
    // initial should be decomposed graph
    private Map<Integer, Map<Integer, Set<Path>>>
    calculateSuboptimalPathsHelper(PathEstimator estimator, Graph initial, long maxPathsPerBucket, int graphLevel,
                                   List<List<Integer>> componentAddress) {
        if (cachedPaths != null) {
            return cachedPaths;
        }
        addSourceSink();
        LongestPathInfo pathInfo = calculateLongestPaths(nodes.size() - 2, estimator).get(nodes.size() - 1);
        Set<Path> currentLevelGraphPaths = trimPaths(pathInfo.allPaths, estimator);
        if (pathInfo.allPaths.iterator().next().getEdges().get(0).getSecond().getUnderlyingGraph() == null) {
            Set<Path> mapped = mapPaths(currentLevelGraphPaths, initial);
            List<Integer> currentLevelAddresses = currentLevelAddresses(componentAddress, graphLevel);
            cachedPaths = separatePaths(mapped, HashSet::new, currentLevelAddresses, maxPathsPerBucket, estimator);
            return cachedPaths;
        }

        Set<Path> allCorrectPaths = new HashSet<>();
        nextPath:
        for (Path path : currentLevelGraphPaths) {
            Map<Integer, Map<Integer, Set<Path>>> currentUnderlyingPaths = path.getEdges().get(0).getSecond().getUnderlyingGraph().
                    calculateSuboptimalPathsHelper(estimator, initial, maxPathsPerBucket, graphLevel + 1, componentAddress);
            if (path.getEdges().size() == 1) {
                allCorrectPaths.addAll(getAllPaths(currentUnderlyingPaths));
                continue;
            }
            Set<Path> correctCurrentPaths = getPathsEndingAt(path.getEdges().get(1).getSecond().getId(), currentUnderlyingPaths);
            if (correctCurrentPaths == null) {
                continue;
            }

            for (int i = 1; i < path.getEdges().size() - 1; ++i) {
                Map<Integer, Map<Integer, Set<Path>>> nextUnderlyingPaths = path.getEdges().get(i).getSecond().getUnderlyingGraph().
                        calculateSuboptimalPathsHelper(estimator, initial, maxPathsPerBucket, graphLevel + 1, componentAddress);

                Integer previousId = path.getEdges().get(i - 1).getSecond().getId();
                assert nextUnderlyingPaths != null;
                Map<Integer, Set<Path>> correctStartNextPaths = nextUnderlyingPaths.get(previousId);
                if (correctStartNextPaths == null) {
                    continue nextPath;
                }

                Integer nextId = path.getEdges().get(i + 1).getSecond().getId();
                Set<Path> correctNextPaths = correctStartNextPaths.get(nextId);
                if (correctNextPaths == null) {
                    continue nextPath;
                }

                correctCurrentPaths = connectComponentPaths(correctCurrentPaths, correctNextPaths, estimator);
            }

            Map<Integer, Map<Integer, Set<Path>>> nextUnderlyingPaths = path.getEdges().get(path.getEdges().size() - 1).getSecond().
                    getUnderlyingGraph().
                    calculateSuboptimalPathsHelper(estimator, initial, maxPathsPerBucket, graphLevel + 1, componentAddress);
            Set<Path> correctEndPaths = getPathsStartingAt(path.getEdges().get(path.getEdges().size() - 2).getSecond().getId(),
                    nextUnderlyingPaths);
            if (correctEndPaths == null) {
                continue;
            }
            correctCurrentPaths = connectComponentPaths(correctCurrentPaths, correctEndPaths, estimator);
            allCorrectPaths.addAll(correctCurrentPaths);
        }
        List<Integer> currentLevelAddresses = currentLevelAddresses(componentAddress, graphLevel);
        cachedPaths = separatePaths(allCorrectPaths, HashSet::new, currentLevelAddresses, maxPathsPerBucket, estimator);
        return cachedPaths;
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

    private void updateLongestPathsInfos(List<LongestPathInfo> toUpdate) {
        for (LongestPathInfo info : toUpdate) {
            info.currentPaths = info.nextPaths;
            info.nextPaths = new HashSet<>();
        }
    }
}