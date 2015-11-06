package core;

import estimators.DecompositionEstimator;
import estimators.PathEstimator;
import infoproviders.EdgeInfoProvider;
import infoproviders.NodeInfoProvider;
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

public class HierarchicalGraph extends Graph<HierarchicalNode> {
    private final int totalSize;
    private Map<Integer, Map<Integer, Set<Path<HierarchicalNode>>>> cachedPaths = null;

    public HierarchicalGraph() {
        this(new ArrayList<>());
    }

    public HierarchicalGraph(List<HierarchicalNode> nodes) {
        super(nodes);
        totalSize = nodes.stream().mapToInt(HierarchicalNode::getSize).sum();
    }

    private static <T extends NodeInfo<HierarchicalNode>> Stream<T> getNeighboursStream(T node, List<T> graph) {
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

    public static HierarchicalGraph getFromInputStream(InputStream inputStream) {
        Scanner in = new Scanner(inputStream);
        int verticesAmount = in.nextInt();
        int edgesAmount = in.nextInt();
        List<HierarchicalNode> nodes = new ArrayList<>();
        for (int i = 0; i < verticesAmount; ++i) {
            nodes.add(readNextNode(in, i));
        }
        HierarchicalGraph graph = new HierarchicalGraph(nodes);
        for (int i = 0; i < edgesAmount; ++i) {
            int node1Id = in.nextInt();
            int node2Id = in.nextInt();
            double weight = in.nextDouble();
            int id = in.nextInt();
            graph.addBidirectionalEdge(graph.getNodes().get(node1Id), graph.getNodes().get(node2Id), weight, id);
        }
        return graph;
    }

    private static HierarchicalNode readNextNode(Scanner in, int nodeId) {
        int oldId = in.nextInt();
        return new HierarchicalNode(nodeId, null, oldId);
    }

    public void addBidirectionalEdge(HierarchicalNode node1, HierarchicalNode node2, double weight, int edgeId) {
        addEdge(node1, node2, weight, edgeId);
        addEdge(node2, node1, weight, edgeId);
    }

    public Edge<HierarchicalNode> addEdge(HierarchicalNode node1, HierarchicalNode node2, double weight, int edgeId) {
        Edge<HierarchicalNode> newEdge = new Edge<>(node1, node2, weight, edgeId);
        node1.getEdges().add(newEdge);
        getEdges().add(newEdge);
        return newEdge;
    }

    private static <T extends NodeInfo> T getCorrespondingInfo(HierarchicalNode node, List<T> allInfos) {
        return allInfos.get(node.getId());
    }

    private static <T extends NodeInfo> List<Integer> getIds(List<T> infos) {
        return infos.stream().map(i -> i.getNode().getId()).collect(Collectors.toList());
    }

    private static <T extends Collection<Path<HierarchicalNode>>> T trimPaths(T paths) {
        for (Path<HierarchicalNode> path : paths) {
            path.getUsedIds().remove(path.getEdges().get(0).getSecond().getId());
            path.getUsedIds().remove(path.getEdges().get(path.getEdges().size() - 1).getSecond().getId());
            path.getEdges().subList(0, 2).clear();
            HierarchicalNode first = path.getEdges().get(0).getFirst();
            path.getEdges().add(0, new Edge<>(null, first, 0));
            path.getEdges().remove(path.getEdges().size() - 1);
            path.setTotalNodes(path.getTotalNodes() - 2);
        }
        return paths;
    }

    private static Set<Path<HierarchicalNode>> connectComponentPaths(Set<Path<HierarchicalNode>> node1Paths,
                                                                     Set<Path<HierarchicalNode>> node2Paths,
                                                                     PathEstimator<HierarchicalNode> estimator) {
        Set<Path<HierarchicalNode>> result = new HashSet<>();
        for (Path<HierarchicalNode> path1 : node1Paths) {
            for (Path<HierarchicalNode> path2 : node2Paths) {
                Path<HierarchicalNode> connected = estimator.mergePaths(path1, path2);
                result.add(connected);
            }
        }
        return result;
    }

    private static <E extends Path<HierarchicalNode>, T extends Collection<E>>
    T siftPaths(T allPaths, long maxPaths, PathEstimator<HierarchicalNode> estimator, Supplier<T> tSupplier) {
        if (allPaths.size() <= maxPaths) {
            return allPaths;
        }
        return allPaths.stream().sorted(estimator).limit(maxPaths).collect(Collectors.toCollection(tSupplier));
    }

    private static Set<Integer> neighbourComponents(HierarchicalNode node, List<Integer> components) {
        //        neighbours.remove(components.get(node.getId()));
        return node.getEdges().stream().map(e -> components.get(e.getSecond().getId())).collect(Collectors.toSet());
    }

    private static <E extends Path<HierarchicalNode>, T extends Collection<E>>
    void assignPath(E path, List<Integer> components, Map<Integer, Map<Integer, T>> sorter, Supplier<T> tSupplier) {
        List<Edge<HierarchicalNode>> pathEdges = path.getEdges();
        HierarchicalNode first = pathEdges.get(0).getSecond();
        HierarchicalNode last = pathEdges.get(pathEdges.size() - 1).getSecond();
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

    private static <E extends Path<HierarchicalNode>, T extends Collection<E>, R extends Collection<E>>
    Map<Integer, Map<Integer, R>> separatePaths(T paths, Supplier<R> collectionSupplier, List<Integer> nodesAddresses,
                                                long maxPathsPerBucket, PathEstimator<HierarchicalNode> estimator) {
        Map<Integer, Map<Integer, R>> result = new HashMap<>();
        for (E path : paths) {
            assignPath(path, nodesAddresses, result, collectionSupplier);
        }
        for (Integer leftId : result.keySet()) {
            for (Integer rightId : result.get(leftId).keySet()) {
                R currentBucket = result.get(leftId).get(rightId);
                result.get(leftId).put(rightId, siftPaths(currentBucket, maxPathsPerBucket, estimator, collectionSupplier));
            }
        }
        return result;
    }

    private static Set<Path<HierarchicalNode>> getPathsEndingAt(Integer componentId, Map<Integer, Map<Integer,
            Set<Path<HierarchicalNode>>>> allPaths) {
        Set<Path<HierarchicalNode>> result = new HashSet<>();
        for (Map<Integer, Set<Path<HierarchicalNode>>> innerMap : allPaths.values()) {
            Set<Path<HierarchicalNode>> current = innerMap.get(componentId);
            if (current != null) {
                result.addAll(current);
            }
        }
        return result.size() == 0 ? null : result;
    }

    private static Set<Path<HierarchicalNode>> getPathsStartingAt(Integer componentId, Map<Integer, Map<Integer,
            Set<Path<HierarchicalNode>>>> allPaths) {
        Set<Path<HierarchicalNode>> result = new HashSet<>();
        Map<Integer, Set<Path<HierarchicalNode>>> correctMap = allPaths.get(componentId);
        if (correctMap == null) {
            return null;
        }
        correctMap.values().forEach(result::addAll);
        return result;
    }

    private static Set<Path<HierarchicalNode>> getAllPaths(Map<Integer, Map<Integer, Set<Path<HierarchicalNode>>>> allPaths) {
        Set<Path<HierarchicalNode>> result = new HashSet<>();
        allPaths.values().forEach(m -> m.values().forEach(result::addAll));
        return result;
    }

    public static List<Double> estimateAllDecompositions(List<List<ComponentInfo>> decompositions, DecompositionEstimator estimator) {
        List<Double> result = new ArrayList<>();
        decompositions.forEach(d -> result.add(estimator.estimateDecomposition(d)));
        return result;
    }

    private static List<ComponentInfo> mapComponentInfos(List<ComponentInfo> infos, HierarchicalGraph toMap) {
        for (ComponentInfo info : infos) {
            info.node = toMap.getNodes().get(info.node.getId());
        }
        return infos;
    }

    private static boolean filterPath(Path<HierarchicalNode> path, int startId, int endId) {
        return (startId == -1 || path.getEdges().get(0).getSecond().getId() == startId) &&
                (endId == -1 || path.getEdges().get(path.getEdges().size() - 1).getSecond().getId() == endId);
    }

    public void addBidirectionalEdge(HierarchicalNode node1, HierarchicalNode node2, double weight) {
        addEdge(node1, node2, weight);
        addEdge(node2, node1, weight);
    }

    public Edge<HierarchicalNode> addEdge(HierarchicalNode node1, HierarchicalNode node2, double weight) {
        Edge<HierarchicalNode> newEdge = new Edge<>(node1, node2, weight);
        node1.getEdges().add(newEdge);
        getEdges().add(newEdge);
        return newEdge;
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
            HierarchicalNode currentComponentMember = groups.get(i).get(0).node;
            HierarchicalNode nextComponentMember = groups.get(i + 1).get(0).node;
            Edge newEdge1 = addEdge(currentComponentMember, nextComponentMember, Double.POSITIVE_INFINITY);
            Edge newEdge2 = addEdge(nextComponentMember, currentComponentMember, Double.POSITIVE_INFINITY);
            result.add(newEdge1);
            result.add(newEdge2);
        }
        return result;
    }

    // O(E * log(V))
    public HierarchicalGraph minimalSpanningTree() {
        List<Edge> fictive = connectComponents();
        List<Boolean> isInTree = new ArrayList<>(Collections.nCopies(getNodes().size(), false));
        PriorityQueue<Edge<HierarchicalNode>> edgeQueue = new PriorityQueue<>(Comparator.comparingDouble(Edge::getWeight));

        List<HierarchicalNode> new_nodes = getNodes().stream().map(n -> new HierarchicalNode(n.getId())).collect(Collectors.toList());
        HierarchicalGraph result = new HierarchicalGraph(new_nodes);

        HierarchicalNode start = getNodes().get(0);
        isInTree.set(start.getId(), true);
        start.getEdges().forEach(edgeQueue::add);

        int nodesLeft = getNodes().size() - 1;
        while (nodesLeft > 0) {
            Edge<HierarchicalNode> nextEdge = edgeQueue.poll();
            if (isInTree.get(nextEdge.getSecond().getId())) {
                continue;
            }
            result.addBidirectionalEdge(result.getNodes().get(nextEdge.getFirst().getId()),
                    result.getNodes().get(nextEdge.getSecond().getId()), nextEdge.getWeight());
            HierarchicalNode newNode = nextEdge.getSecond();
            isInTree.set(newNode.getId(), true);
            newNode.getEdges().stream().filter(e -> !isInTree.get(e.getSecond().getId())).forEach(edgeQueue::add);
            nodesLeft -= 1;
        }
        removeEdges(fictive);
        return result;
    }

    // O(V + E)
    public List<TreeInfo> collectTreeInfo() {
        List<TreeInfo> result = getNodes().stream().map(TreeInfo::new).collect(Collectors.toList());
        setTreeInfo(result.get(0), 0, result);
        return result;
    }

    private void splitByEdge(Edge edge, Map<Integer, Integer> componentSizes, List<TreeInfo> tree, HierarchicalGraph mst) {
        TreeInfo node1 = tree.get(edge.getFirst().getId());
        TreeInfo node2 = tree.get(edge.getSecond().getId());
        TreeInfo parent = node1.level < node2.level ? node1 : node2;
        TreeInfo child = node1.level > node2.level ? node1 : node2;
        int oldSize = updateTreeSizes(child.subtreeSize, parent, tree);
        updateComponentsSizes(componentSizes, oldSize, oldSize - child.subtreeSize, child.subtreeSize);
        mst.removeBidirectionalEdge(edge);
    }

//    private void setParentNode(HierarchicalNode parent) {
//        nodes.forEach(n -> n.setParent(parent));
//    }

    // O(V + E)
    public List<ComponentInfo> getComponentInfo() {
        List<ComponentInfo> result = getNodes().stream().map(ComponentInfo::new).collect(Collectors.toList());
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
        getEdges().remove(edge);
    }

    // O(E)
    public void removeBidirectionalEdge(Edge edge) {
        Edge reverse = getConnectingEdge(edge.getSecond(), edge.getFirst());
        removeEdge(edge);
        removeEdge(reverse);
    }

    // O(V + E)
    // ids should be unique
    // underlying graph is copying by reference
    public HierarchicalGraph subgraph(List<Integer> nodeIds) {
        List<Integer> idCorrespondence = new ArrayList<>(Collections.nCopies(getNodes().size(), -1));
        IntStream.range(0, nodeIds.size()).forEach(i -> idCorrespondence.set(nodeIds.get(i), i));

        List<Boolean> isSelected = new ArrayList<>(Collections.nCopies(getNodes().size(), false));
        nodeIds.forEach(i -> isSelected.set(i, true));

        List<HierarchicalNode> newNodes = IntStream.range(0, nodeIds.size()).
                mapToObj(i -> HierarchicalNode.forkOther(i, getNodes().get(nodeIds.get(i)))).
                collect(Collectors.toList());
        HierarchicalGraph newGraph = new HierarchicalGraph(newNodes);
        getEdges().stream().filter(e -> isSelected.get(e.getFirst().getId()) && isSelected.get(e.getSecond().getId())).
                forEach(e -> newGraph.
                        addEdge(newNodes.get(idCorrespondence.get(e.getFirst().getId())), newNodes.get(idCorrespondence.get(e.getSecond()
                                .getId()
                        )), e.getWeight()));
        return newGraph;
    }

    private Map<List<Integer>, Double> deduceComponentEdges(List<HierarchicalNode> componentNodes, List<ComponentInfo> decompositionInfo) {
        Map<List<Integer>, List<Double>> newEdgesInfo = new HashMap<>();
        for (Edge<HierarchicalNode> e : getEdges()) {
            HierarchicalNode first = componentNodes.get(getCorrespondingInfo(e.getFirst(), decompositionInfo).component);
            HierarchicalNode second = componentNodes.get(getCorrespondingInfo(e.getSecond(), decompositionInfo).component);

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
    public HierarchicalGraph hierarchicalDecomposition(int maxVerticesPerGraph) {
        if (maxVerticesPerGraph < 2) {
            throw new AssertionError("Max vertices should be >= 2");
        }
        if (getNodes().size() <= maxVerticesPerGraph) {
            return this;
        }
        List<ComponentInfo> decompositionInfo = decompositionInfo(maxVerticesPerGraph);
        Map<Integer, List<ComponentInfo>> components = decompositionInfo.stream().collect(Collectors.groupingBy(ci -> ci.component));
        List<HierarchicalNode> newNodes = new ArrayList<>(Collections.nCopies(components.size(), new HierarchicalNode(0)));
        for (Map.Entry<Integer, List<ComponentInfo>> entry : components.entrySet()) {
            HierarchicalGraph subgraph = subgraph(getIds(entry.getValue()));
            HierarchicalNode parent = new HierarchicalNode(entry.getKey(), subgraph, -1);
            newNodes.set(entry.getKey(), parent);
        }
        HierarchicalGraph newGraph = new HierarchicalGraph(newNodes);
        Map<List<Integer>, Double> newEdges = deduceComponentEdges(newNodes, decompositionInfo);
        for (Map.Entry<List<Integer>, Double> e : newEdges.entrySet()) {
            newGraph.addEdge(newNodes.get(e.getKey().get(0)), newNodes.get(e.getKey().get(1)), e.getValue());
        }
        return newGraph.hierarchicalDecomposition(maxVerticesPerGraph);
    }

    // O(V * (V + E))
    public List<List<ComponentInfo>> allDecompositions() {
        HierarchicalGraph mst = minimalSpanningTree();
        List<List<ComponentInfo>> result = new ArrayList<>();
        result.add(mapComponentInfos(mst.getComponentInfo(), this));

        PriorityQueue<Edge> edgeQueue = new PriorityQueue<>(Comparator.comparingDouble(e -> -e.getWeight()));
        edgeQueue.addAll(mst.getEdges().stream().filter(e -> e.getFirst().getId() < e.getSecond().getId()).collect(Collectors.toList()));

        while (!edgeQueue.isEmpty()) {
            Edge maxEdge = edgeQueue.poll();
            mst.removeBidirectionalEdge(maxEdge);
            result.add(mapComponentInfos(mst.getComponentInfo(), this));
        }
        return result;
    }

    public List<ComponentInfo> decompositionInfo(int maxVerticesPerGraph) {
        HierarchicalGraph mst = minimalSpanningTree();
        List<TreeInfo> treeInfo = mst.collectTreeInfo();

        PriorityQueue<Edge> edgeQueue = new PriorityQueue<>(Comparator.comparingDouble(e -> -e.getWeight()));
        edgeQueue.addAll(mst.getEdges().stream().filter(e -> e.getFirst().getId() < e.getSecond().getId()).collect(Collectors.toList()));

        TreeMap<Integer, Integer> componentSizes = new TreeMap<>();
        componentSizes.put(getNodes().size(), 1);
        while (componentSizes.lastKey() > maxVerticesPerGraph) {
            Edge max_edge = edgeQueue.poll();
            splitByEdge(max_edge, componentSizes, treeInfo, mst);
        }
        return mst.getComponentInfo();
    }

    private void addSourceSink() {
        HierarchicalNode source = new HierarchicalNode(getNodes().size());
        HierarchicalNode sink = new HierarchicalNode(getNodes().size() + 1);
        getNodes().forEach(n -> addEdge(source, n, 0));
        getNodes().forEach(n -> addEdge(n, sink, 0));
        getNodes().add(source);
        getNodes().add(sink);
    }

    public void writeAsDotToFile(String filename, List<NodeInfoProvider> nodeInfoProviders, List<EdgeInfoProvider> edgeInfoProviders) {
        try {
            FileOutputStream output = new FileOutputStream(filename);
            writeDotRepresentation(output, nodeInfoProviders, edgeInfoProviders);
            output.close();
        }
        catch (IOException exception) {
            System.err.println(exception.getMessage());
        }
    }

    public void writeDotRepresentation(OutputStream outputStream, List<NodeInfoProvider> nodeInfoProviders,
                                       List<EdgeInfoProvider> edgeInfoProviders) {
        StringBuilder builder = new StringBuilder();
        builder.append("digraph {").append(System.lineSeparator());
        for (HierarchicalNode node : getNodes()) {
            builder.append(node.getId());
            if (!nodeInfoProviders.isEmpty()) {
                builder.append(" [");
                for (NodeInfoProvider provider : nodeInfoProviders) {
                    builder.append(provider.provideInfo(node)).append(" ");
                }
                builder.append("]");
            }
            builder.append(System.lineSeparator());
        }

        for (Edge edge : getEdges()) {
            builder.append(edge.getFirst().getId()).append(" -> ").append(edge.getSecond().getId());
            if (!edgeInfoProviders.isEmpty()) {
                builder.append(" [");
                for (EdgeInfoProvider provider : edgeInfoProviders) {
                    builder.append(provider.provideInfo(edge)).append(" ");
                }
                builder.append("]");
            }
            builder.append(System.lineSeparator());
        }
        builder.append("}");
        PrintWriter writer = new PrintWriter(outputStream);
        writer.print(builder.toString());
        writer.flush();
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
        getNodes().forEach(n -> writer.format("%d [label=%d];%n", n.getId(), n.getId()));
        getEdges().forEach(e -> writer.format("%d -> %d [weight=%.0f label=%.0f];%n", e.getFirst().getId(), e.getSecond().getId(),
                e.getWeight(), e.getWeight()));
        writer.println("}");
        writer.flush();
    }

    private Set<Path<HierarchicalNode>> mapPaths(Set<Path<HierarchicalNode>> decomposedPaths, HierarchicalGraph initial) {
        return decomposedPaths.stream().map(p -> mapPath(p, initial)).collect(Collectors.toCollection(HashSet::new));
    }

    private Path<HierarchicalNode> mapPath(Path<HierarchicalNode> decomposed, HierarchicalGraph initial) {
        List<Edge<HierarchicalNode>> initialEdges = new ArrayList<>();
        HierarchicalNode previousVertex = initial.getNodes().get(decomposed.getEdges().get(0).getSecond().getOldId());
        initialEdges.add(new Edge<>(null, previousVertex, 0));
        for (int i = 1; i < decomposed.getEdges().size(); ++i) {
            HierarchicalNode nextVertex = initial.getNodes().get(decomposed.getEdges().get(i).getSecond().getOldId());
            Edge<HierarchicalNode> connectingEdge = getConnectingEdge(previousVertex, nextVertex);
            initialEdges.add(connectingEdge);
            previousVertex = nextVertex;
        }
        Set<Integer> usedIds = new HashSet<>();
        initialEdges.forEach(e -> usedIds.add(e.getSecond().getId()));
        return new Path<>(usedIds, initialEdges, decomposed);
    }

    private void writeHierarchy(List<String> parentIds) {
        String name = "graph" + String.join("_", parentIds) + ".dot";
        writeAsDotToFile(name);
        if (getNodes().get(0).getUnderlyingGraph() == null) {
            return;
        }
        for (int i = 0; i < getNodes().size(); ++i) {
            List<String> copy = new ArrayList<>(parentIds);
            copy.add(String.valueOf(i));
            getNodes().get(i).getUnderlyingGraph().writeHierarchy(copy);
        }
    }

    private void setComponentAddress(List<List<Integer>> addresses, List<Integer> currentAddress) {
        for (HierarchicalNode node : getNodes()) {
            List<Integer> newAddress = new ArrayList<>(currentAddress);
            newAddress.add(node.getId());
            if (node.getUnderlyingGraph() == null) {
                addresses.set(node.getOldId(), newAddress);
            } else {
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

    public Set<Path<HierarchicalNode>> calculateSuboptimalLongestPath(PathEstimator<HierarchicalNode> estimator, int maxNodesPerGraph,
                                                                      long maxPathsPerBucket) {
        return calculateSuboptimalLongestPath(estimator, maxNodesPerGraph, maxPathsPerBucket, -1, -1);
    }

    // O(log(V) * (V * (V + E) + T(merge)))
    public Set<Path<HierarchicalNode>> calculateSuboptimalLongestPath(PathEstimator<HierarchicalNode> estimator, int maxNodesPerGraph,
                                                                      long maxPathsPerBucket,
                                                                      int startId, int endId) {
        HierarchicalGraph decomposed = hierarchicalDecomposition(maxNodesPerGraph);
        List<List<Integer>> componentAddresses = decomposed.nodeComponentAddress();
//        decomposed.writeHierarchy(new ArrayList<>());
//        writeAsDotToFile("initial.dot");
        Map<Integer, Map<Integer, Set<Path<HierarchicalNode>>>> result =
                decomposed.calculateSuboptimalPathsHelper(estimator, this, maxPathsPerBucket, 1, componentAddresses, startId, endId, 0);
        return result.get(0).get(0);
    }

    private List<Integer> getLevelAddresses(List<List<Integer>> componentAddresses, int graphLevel) {
        return componentAddresses.stream().map(list -> list.get(graphLevel)).collect(Collectors.toList());
    }

    // O(log(V) * T(merge))
    private Map<Integer, Map<Integer, Set<Path<HierarchicalNode>>>>
    calculateSuboptimalPathsHelper(PathEstimator<HierarchicalNode> estimator, HierarchicalGraph initial, long maxPathsPerBucket,
                                   int graphLevel, List<List<Integer>> componentAddress, int startId, int endId, int parentId) {
        if (cachedPaths != null) {
            return cachedPaths;
        }
        List<Integer> previousLevelAddresses = getLevelAddresses(componentAddress, graphLevel - 1);
        List<Integer> currentLevelAddresses = getLevelAddresses(componentAddress, graphLevel);
        addSourceSink();
        LongestPathInfo<HierarchicalNode> pathInfo = calculateLongestPaths(getNodes().size() - 2).get(getNodes().size() - 1);
        Set<Path<HierarchicalNode>> currentLevelGraphPaths = trimPaths(pathInfo.allPaths);

        // if start or end id don't belong to this cluster, set them to -1
        startId = startId == -1 ? -1 : previousLevelAddresses.get(startId) == parentId ? startId : -1;
        endId = endId == -1 ? -1 : previousLevelAddresses.get(endId) == parentId ? endId : -1;
        // leave only paths from start to end
        int thisStartId = startId == -1 ? -1 : currentLevelAddresses.get(startId);
        int thisEndId = endId == -1 ? -1 : currentLevelAddresses.get(endId);
        currentLevelGraphPaths = currentLevelGraphPaths.stream().filter(p -> filterPath(p, thisStartId, thisEndId)).
                collect(Collectors.toCollection(HashSet::new));

        if (pathInfo.allPaths.iterator().next().getEdges().get(0).getSecond().getUnderlyingGraph() == null) {
            Set<Path<HierarchicalNode>> mapped = mapPaths(currentLevelGraphPaths, initial);
            cachedPaths = separatePaths(mapped, HashSet::new, previousLevelAddresses, maxPathsPerBucket, estimator);
            return cachedPaths;
        }

        Set<Path<HierarchicalNode>> allCorrectPaths = new HashSet<>();
        nextPath:
        for (Path<HierarchicalNode> path : currentLevelGraphPaths) {
            HierarchicalNode first = path.getEdges().get(0).getSecond();
            Map<Integer, Map<Integer, Set<Path<HierarchicalNode>>>> currentUnderlyingPaths = first.getUnderlyingGraph().
                    calculateSuboptimalPathsHelper(estimator, initial, maxPathsPerBucket, graphLevel + 1, componentAddress, startId, endId,
                            first.getId());
            if (path.getEdges().size() == 1) {
                allCorrectPaths.addAll(getAllPaths(currentUnderlyingPaths));
                continue;
            }
            Set<Path<HierarchicalNode>> correctCurrentPaths = getPathsEndingAt(path.getEdges().get(1).getSecond().getId(),
                    currentUnderlyingPaths);
            if (correctCurrentPaths == null) {
                continue;
            }

            for (int i = 1; i < path.getEdges().size() - 1; ++i) {
                HierarchicalNode next = path.getEdges().get(i).getSecond();
                Map<Integer, Map<Integer, Set<Path<HierarchicalNode>>>> nextUnderlyingPaths = next.getUnderlyingGraph().
                        calculateSuboptimalPathsHelper(estimator, initial, maxPathsPerBucket, graphLevel + 1, componentAddress, startId,
                                endId, next.getId());

                Integer previousId = path.getEdges().get(i - 1).getSecond().getId();
                assert nextUnderlyingPaths != null;
                Map<Integer, Set<Path<HierarchicalNode>>> correctStartNextPaths = nextUnderlyingPaths.get(previousId);
                if (correctStartNextPaths == null) {
                    continue nextPath;
                }

                Integer nextId = path.getEdges().get(i + 1).getSecond().getId();
                Set<Path<HierarchicalNode>> correctNextPaths = correctStartNextPaths.get(nextId);
                if (correctNextPaths == null) {
                    continue nextPath;
                }

                correctCurrentPaths = connectComponentPaths(correctCurrentPaths, correctNextPaths, estimator);
            }

            HierarchicalNode last = path.getEdges().get(path.getEdges().size() - 1).getSecond();
            Map<Integer, Map<Integer, Set<Path<HierarchicalNode>>>> nextUnderlyingPaths = last.getUnderlyingGraph().
                    calculateSuboptimalPathsHelper(estimator, initial, maxPathsPerBucket, graphLevel + 1, componentAddress, startId, endId,
                            last.getId());
            Set<Path<HierarchicalNode>> correctEndPaths = getPathsStartingAt(path.getEdges().get(path.getEdges().size() - 2).getSecond()
                    .getId(),
                    nextUnderlyingPaths);
            if (correctEndPaths == null) {
                continue;
            }
            correctCurrentPaths = connectComponentPaths(correctCurrentPaths, correctEndPaths, estimator);
            allCorrectPaths.addAll(correctCurrentPaths);
        }
//        List<Integer> getLevelAddresses = getLevelAddresses(componentAddress, graphLevel);
        cachedPaths = separatePaths(allCorrectPaths, HashSet::new, previousLevelAddresses, maxPathsPerBucket, estimator);
        return cachedPaths;
    }

    // O*(V^V)
    public List<LongestPathInfo<HierarchicalNode>> calculateLongestPaths(int startNodeId) {
        List<LongestPathInfo<HierarchicalNode>> infos = getNodes().stream().map(LongestPathInfo::new).collect(Collectors.toList());
//        writeAsDotToFile("test.dot");
        LongestPathInfo<HierarchicalNode> startNode = infos.get(startNodeId);
        Path<HierarchicalNode> trivialPath = Path.getTrivialPath(getNodes().get(startNodeId));
        startNode.allPaths.add(trivialPath);
        startNode.currentPaths.add(trivialPath);
        List<Edge<HierarchicalNode>> allEdges = getEdges().stream().collect(Collectors.toList());

        boolean nextPathsPending = true;
        while (nextPathsPending) {
            nextPathsPending = false;
            for (Edge<HierarchicalNode> edge : allEdges) {
                LongestPathInfo<HierarchicalNode> node1Info = infos.get(edge.getFirst().getId());
                LongestPathInfo<HierarchicalNode> node2Info = infos.get(edge.getSecond().getId());
                nextPathsPending |= tryUpdateNext(node1Info, node2Info, edge);
            }
            updateLongestPathsInfos(infos);
        }
        return infos;
    }

    // O*(V^V)
    private boolean tryUpdateNext(LongestPathInfo<HierarchicalNode> previous, LongestPathInfo<HierarchicalNode> next,
                                  Edge<HierarchicalNode> edge) {
        List<Path<HierarchicalNode>> extendedPaths = previous.currentPaths.stream().
                filter(p -> !p.getUsedIds().contains(next.node.getId())).
                map(p -> Path.merge(p, Path.getTrivialPath(next.node), p.getEdges().size() - 1, 0, edge)).
                filter(p -> !next.allPaths.contains(p)).
                collect(Collectors.toList());
        next.allPaths.addAll(extendedPaths);
        next.nextPaths.addAll(extendedPaths);
        return next.nextPaths.size() > 0;
    }

    private void updateLongestPathsInfos(List<LongestPathInfo<HierarchicalNode>> toUpdate) {
        for (LongestPathInfo info : toUpdate) {
            info.currentPaths = info.nextPaths;
            info.nextPaths = new HashSet<>();
        }
    }
}