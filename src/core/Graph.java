package core;

import estimators.PathEstimator;
import nodeinfo.ComponentInfo;
import nodeinfo.LongestPathInfo;
import nodeinfo.NodeInfo;
import nodeinfo.TreeInfo;
import utility.ApproximateComparator;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Graph {
    private static final Comparator<List<Integer>> pair_comparator = Comparator.<List<Integer>>comparingInt(l -> l.get(0)).
            thenComparing(l -> l.get(1));
    //    private final double double_comparison_tolerance = 0.0001;
    private final double score_comparison_tolerance = 0.1;
    //    private final Comparator<Double> double_comparator = new utility.ApproximateComparator(double_comparison_tolerance);
    private final Comparator<Double> score_comparator = new ApproximateComparator(score_comparison_tolerance);
    private List<Node> nodes = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();

    public Graph() {
    }

    public Graph(List<Node> nodes) {
        this.nodes = nodes;
    }

    private static <T extends NodeInfo> Stream<T> get_neighbours_stream(T node, List<T> graph) {
        return node.get_node().edges.stream().map(e -> graph.get(e.get_second().id));
    }

    // O(V + E)
    private static void set_tree_info(TreeInfo current, int level, List<TreeInfo> tree) {
        current.visited = true;
        current.level = level;
        List<TreeInfo> children = get_neighbours_stream(current, tree).filter(i -> !i.visited).collect(Collectors.toList());
        if (!children.isEmpty()) {
            children.forEach(c -> set_tree_info(c, level + 1, tree));
            current.subtree_size = children.stream().mapToInt(c -> c.subtree_size).sum() + 1;
        }
    }

    private static TreeInfo get_parent(TreeInfo current, List<TreeInfo> tree) {
        return get_neighbours_stream(current, tree).filter(n -> n.level < current.level).findFirst().orElse(null);
    }

    // O(V + E)
    // returns previous size
    private static int update_tree_sizes(int tweak_size, TreeInfo current, List<TreeInfo> tree) {
        TreeInfo parent = get_parent(current, tree);
        int current_size = current.subtree_size;
        current.subtree_size -= tweak_size;
        return parent == null ? current_size : update_tree_sizes(tweak_size, parent, tree);
    }

    private static void add_multiset_value(Map<Integer, Integer> map, Integer value) {
        map.computeIfAbsent(value, v -> 0);
        map.put(value, map.get(value) + 1);
    }

    private static void remove_multiset_value(Map<Integer, Integer> map, Integer value) {
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

    private static void update_components_sizes(Map<Integer, Integer> components_sizes, int old_component_size, int new_component1_size,
                                                int new_component2_size) {
        remove_multiset_value(components_sizes, old_component_size);
        add_multiset_value(components_sizes, new_component1_size);
        add_multiset_value(components_sizes, new_component2_size);
    }

    private static void set_component_info(ComponentInfo current, int component, List<ComponentInfo> all_info) {
        current.visited = true;
        current.component = component;
        get_neighbours_stream(current, all_info).filter(n -> !n.visited).forEach(n -> set_component_info(n, component, all_info));
    }

    public static Graph get_from_input_stream(InputStream input_stream) {
        Scanner in = new Scanner(input_stream);
        int vertices_amount = in.nextInt();
        int edges_amount = in.nextInt();
        Graph graph = new Graph();
        graph.nodes = IntStream.range(0, vertices_amount).mapToObj(Node::new).collect(Collectors.toList());
        for (int i = 0; i < edges_amount; ++i) {
            int node1_id = in.nextInt();
            int node2_id = in.nextInt();
            double weight = in.nextDouble();
            graph.add_bidirectional_edge(graph.nodes.get(node1_id), graph.nodes.get(node2_id), weight);
        }
        return graph;
    }

    public void add_bidirectional_edge(Node node1, Node node2, double weight) {
        add_edge(node1, node2, weight);
        add_edge(node2, node1, weight);
    }

    public void add_edge(Node node1, Node node2, double weight) {
        Edge new_edge = new Edge(node1, node2, weight);
        node1.edges.add(new_edge);
        edges.add(new_edge);
    }

    private static <T extends NodeInfo> T get_corresponding_info(Node node, List<T> all_infos) {
        return all_infos.get(node.id);
    }

    private static <T extends NodeInfo> List<Integer> get_ids(List<T> infos) {
        return infos.stream().map(i -> i.get_node().id).collect(Collectors.toList());
    }

    private static Edge get_connecting_edge(Node first, Node second) {
        return first.edges.stream().filter(e -> e.get_second() == second).findFirst().orElse(null);
    }

    // merges specified paths with every possible way according to edges in initial graph
    private static Set<Path> possible_connections(Path path1, Path path2, Graph initial, PathEstimator estimator) {
        Set<Path> result = new HashSet<>();
        for (int i = 0; i < path1.edges.size(); ++i) {
            for (int j = 0; j < path2.edges.size(); ++j) {
                Node path1_break_node = path1.edges.get(i).second;
                Node path2_break_node = path2.edges.get(i).second;
                Node path1_break_image = initial.nodes.get(path1_break_node.old_id);
                Node path2_break_image = initial.nodes.get(path2_break_node.old_id);
                Edge bridge = get_connecting_edge(path1_break_image, path2_break_image);
                if (bridge != null) {
                    Path connected = Path.merge(path1, path2, i, j, bridge, estimator);
                    result.add(connected);
                }
            }
        }
        return result;
    }

    private static <T extends Collection<Path>> T trim_paths(T paths) {
        for (Path path : paths) {
            path.used_ids.remove(path.edges.get(0).first.id);
            path.used_ids.remove(path.edges.get(path.edges.size() - 1).second.id);
            path.edges.remove(0);
            path.edges.remove(path.edges.size() - 1);
        }
        return paths;
    }

    private static Set<Path> connect_node_paths(Set<Path> node1_paths, Set<Path> node2_paths, Graph initial, PathEstimator estimator) {
        Set<Path> result = new HashSet<>();
        for (Path path1 : node1_paths) {
            for (Path path2 : node2_paths) {
                Set<Path> possible_connections = possible_connections(path1, path2, initial, estimator);
                result.addAll(possible_connections);
            }
        }
        return result;
    }

    private static Set<Path> sift_paths(Set<Path> all_paths, long max_paths) {
        if (max_paths == Long.MAX_VALUE) {
            return all_paths;
        }
        return all_paths.stream().sorted(Comparator.comparingDouble(p -> -p.score)).limit(max_paths).
                collect(Collectors.toCollection(HashSet::new));
    }

    // O(E * log(V))
    public Graph minimal_spanning_tree() {
        List<Boolean> is_in_tree = new ArrayList<>(Collections.nCopies(nodes.size(), false));
        PriorityQueue<Edge> edge_queue = new PriorityQueue<>(Comparator.comparingDouble(e -> e.weight));

        Graph result = new Graph();
        result.nodes = nodes.stream().map(n -> new Node(n.id)).collect(Collectors.toList());

        Node start = nodes.get(0);
        is_in_tree.set(start.id, true);
        start.edges.forEach(edge_queue::add);

        int nodes_left = nodes.size() - 1;
        while (nodes_left > 0) {
            Edge next_edge = edge_queue.poll();
            if (is_in_tree.get(next_edge.second.id)) {
                continue;
            }
            result.add_bidirectional_edge(result.nodes.get(next_edge.first.id), result.nodes.get(next_edge.second.id), next_edge.weight);
            Node new_node = next_edge.second;
            is_in_tree.set(new_node.id, true);
            new_node.edges.stream().filter(e -> !is_in_tree.get(e.second.id)).forEach(edge_queue::add);
            nodes_left -= 1;
        }
        return result;
    }

    // O(V + E)
    public List<TreeInfo> collect_tree_info() {
        List<TreeInfo> result = nodes.stream().map(TreeInfo::new).collect(Collectors.toList());
        set_tree_info(result.get(0), 0, result);
        return result;
    }

    private void split_by_edge(Edge edge, Map<Integer, Integer> component_sizes, List<TreeInfo> tree, Graph mst) {
        TreeInfo node1 = tree.get(edge.first.id);
        TreeInfo node2 = tree.get(edge.second.id);
        TreeInfo parent = node1.level < node2.level ? node1 : node2;
        TreeInfo child = node1.level > node2.level ? node1 : node2;
        int old_size = update_tree_sizes(child.subtree_size, parent, tree);
        update_components_sizes(component_sizes, old_size, old_size - child.subtree_size, child.subtree_size);
        mst.remove_bidirectional_edge(edge);
    }

    public List<ComponentInfo> get_component_info() {
        List<ComponentInfo> result = nodes.stream().map(ComponentInfo::new).collect(Collectors.toList());
        int component = 0;
        for (int i = 0; i < result.size(); ++i) {
            if (!result.get(i).visited) {
                set_component_info(result.get(i), component, result);
                component += 1;
            }
        }
        return result;
    }

    public void remove_edge(Edge edge) {
        edge.first.edges.remove(edge);
        edges.remove(edge);
    }

    public void remove_bidirectional_edge(Edge edge) {
        Edge reverse = get_connecting_edge(edge.second, edge.first);
        remove_edge(edge);
        remove_edge(reverse);
    }

    // O(V + E)
    // ids should be unique
    // underlying graph is copying by reference
    public Graph subgraph(List<Integer> node_ids) {
        List<Integer> id_correspondence = new ArrayList<>(Collections.nCopies(nodes.size(), -1));
        IntStream.range(0, node_ids.size()).forEach(i -> id_correspondence.set(node_ids.get(i), i));

        List<Boolean> is_selected = new ArrayList<>(Collections.nCopies(nodes.size(), false));
        node_ids.forEach(i -> is_selected.set(i, true));

        List<Node> new_nodes = IntStream.range(0, node_ids.size()).
                mapToObj(i -> new Node(i, nodes.get(node_ids.get(i)).underlying_graph, node_ids.get(i))).
                collect(Collectors.toList());
        Graph new_graph = new Graph(new_nodes);
        edges.stream().filter(e -> is_selected.get(e.first.id) && is_selected.get(e.second.id)).forEach(e -> new_graph.
                add_edge(new_nodes.get(id_correspondence.get(e.first.id)), new_nodes.get(id_correspondence.get(e.second.id)), e.weight));
        return new_graph;
    }

    private Map<List<Integer>, Double> deduce_component_edges(List<Node> component_nodes, List<ComponentInfo> decomposition_info) {
        Map<List<Integer>, List<Double>> new_edges_info = new HashMap<>();
        for (Edge e : edges) {
            Node first = component_nodes.get(get_corresponding_info(e.first, decomposition_info).component);
            Node second = component_nodes.get(get_corresponding_info(e.second, decomposition_info).component);

            if (first != second) {
                List<Integer> edge_description = Arrays.asList(first.id, second.id);
                new_edges_info.putIfAbsent(edge_description, new ArrayList<>());
                new_edges_info.get(edge_description).add(e.weight);
            }
        }
        Map<List<Integer>, Double> result = new HashMap<>();
        new_edges_info.entrySet().forEach(e -> result.put(e.getKey(), e.getValue().stream().collect(Collectors.averagingDouble(d -> d))));
        return result;
    }

    // O(V * (V + E) * log(V))
    public Graph hierarchical_decomposition(int max_vertices_per_graph) {
        if (max_vertices_per_graph <= 1) {
            throw new RuntimeException("Max vertices should be >= 2");
        }
        if (nodes.size() <= max_vertices_per_graph) {
            return this;
        }
        List<ComponentInfo> decomposition_info = decomposition_info(max_vertices_per_graph);
        Map<Integer, List<ComponentInfo>> components = decomposition_info.stream().collect(Collectors.groupingBy(ci -> ci.component));
        List<Node> new_nodes = IntStream.range(0, components.size()).mapToObj(Node::new).collect(Collectors.toList());
        for (Map.Entry<Integer, List<ComponentInfo>> entry : components.entrySet()) {
            Graph subgraph = subgraph(get_ids(entry.getValue()));
            new_nodes.get(entry.getKey()).set_underlying_graph(subgraph);
        }
        Graph new_graph = new Graph(new_nodes);
        Map<List<Integer>, Double> new_edges = deduce_component_edges(new_nodes, decomposition_info);
        for (Map.Entry<List<Integer>, Double> e : new_edges.entrySet()) {
            new_graph.add_bidirectional_edge(new_nodes.get(e.getKey().get(0)), new_nodes.get(e.getKey().get(1)), e.getValue());
        }
        return new_graph.hierarchical_decomposition(max_vertices_per_graph);
    }

    // O(V * (V + E))
    public List<ComponentInfo> decomposition_info(int max_vertices_per_graph) {
        Graph mst = minimal_spanning_tree();
        List<TreeInfo> tree_info = mst.collect_tree_info();

        PriorityQueue<Edge> edge_queue = new PriorityQueue<>(Comparator.comparingDouble(e -> -e.weight));
        edge_queue.addAll(mst.edges);

        TreeMap<Integer, Integer> component_sizes = new TreeMap<>();
        component_sizes.put(nodes.size(), 1);
        while (component_sizes.lastKey() > max_vertices_per_graph) {
            Edge max_edge = edge_queue.poll();
            split_by_edge(max_edge, component_sizes, tree_info, mst);
        }
        return mst.get_component_info();
    }

    private void add_source_sink() {
        Node source = new Node(nodes.size());
        Node sink = new Node(nodes.size() + 1);
        nodes.forEach(n -> add_bidirectional_edge(n, source, 0));
        nodes.forEach(n -> add_bidirectional_edge(n, sink, 0));
        nodes.add(source);
        nodes.add(sink);
    }

    public void write_dot_representation(OutputStream output_stream) {
        PrintWriter writer = new PrintWriter(output_stream);
        writer.println("graph {");
        edges.forEach(e -> writer.format("%d -- %d%n", e.first.id, e.second.id));
        writer.println("}");
        writer.flush();
    }

    private Path map_path(Path decomposed, Graph initial) {
        List<Edge> initial_edges = new ArrayList<>();
        Node previous_vertex = initial.nodes.get(decomposed.edges.get(0).second.old_id);
        initial_edges.add(new Edge(null, previous_vertex, 0));
        for (int i = 1; i < decomposed.edges.size(); ++i) {
            Node next_vertex = initial.nodes.get(decomposed.edges.get(i).second.old_id);
            Edge connecting_edge = get_connecting_edge(previous_vertex, next_vertex);
            initial_edges.add(connecting_edge);
        }
        Set<Integer> used_ids = new HashSet<>();
        initial_edges.forEach(e -> used_ids.add(e.second.id));
        return new Path(used_ids, initial_edges, decomposed.total_weight, decomposed.score);
    }

    private Set<Path> map_paths(Set<Path> decomposed_paths, Graph initial) {
        return decomposed_paths.stream().map(p -> map_path(p, initial)).collect(Collectors.toCollection(HashSet::new));
    }

    public Set<Path> calculate_suboptimal_longest_path(PathEstimator estimator, int max_nodes_per_graph, long max_paths_per_node) {
        Graph decomposed = hierarchical_decomposition(max_nodes_per_graph);
        return decomposed.calculate_suboptimal_paths_helper(estimator, this, max_paths_per_node);
    }

    private Set<Path> calculate_suboptimal_paths_helper(PathEstimator estimator, Graph initial, long max_paths_per_node) {
        add_source_sink();
        LongestPathInfo path_info = calculate_longest_paths(nodes.size() - 2, estimator).get(nodes.size() - 1);
        Set<Path> current_level_graph_paths = trim_paths(path_info.paths);
        if (path_info.paths.iterator().next().edges.get(0).second.underlying_graph == null) {
            return map_paths(current_level_graph_paths, initial);
        }

        Set<Path> all_underlying_path = new HashSet<>();
        for (Path path : current_level_graph_paths) {
            Set<Path> current_underlying_paths = path.edges.get(0).second.underlying_graph.
                    calculate_suboptimal_paths_helper(estimator, initial, max_paths_per_node);
            for (int i = 1; i < path.edges.size(); ++i) {
                Set<Path> next_node_underlying_paths = path.edges.get(i).second.underlying_graph.
                        calculate_suboptimal_paths_helper(estimator, initial, max_paths_per_node);
                current_underlying_paths = connect_node_paths(current_underlying_paths, next_node_underlying_paths, initial, estimator);
            }
            all_underlying_path.addAll(current_underlying_paths);
        }
        return sift_paths(all_underlying_path, max_paths_per_node);
    }

    // O*(V!)
    public List<LongestPathInfo> calculate_longest_paths(int start_node_id, PathEstimator estimator) {
        List<LongestPathInfo> infos = nodes.stream().map(LongestPathInfo::new).collect(Collectors.toList());
        LongestPathInfo start_node = infos.get(start_node_id);
        Path trivial_path = Path.get_trivial_path(nodes.get(start_node_id), estimator);
        start_node.paths.add(trivial_path);
        start_node.best_path = trivial_path;
        List<Edge> all_edges = edges.stream().collect(Collectors.toList());

        boolean estimation_changed = true;
        while (estimation_changed) {
            estimation_changed = false;
            // deep copy
            List<LongestPathInfo> next_infos = infos.stream().map(LongestPathInfo::new).collect(Collectors.toList());
            for (Edge edge : all_edges) {
                LongestPathInfo node1_previous_info = infos.get(edge.first.id);
                LongestPathInfo node1_next_info = next_infos.get(edge.first.id);
                LongestPathInfo node2_previous_info = infos.get(edge.second.id);
                LongestPathInfo node2_next_info = next_infos.get(edge.second.id);
                estimation_changed |= try_update_next(node1_previous_info, node2_next_info, edge, estimator);
                estimation_changed |= try_update_next(node2_previous_info, node1_next_info, edge, estimator);
            }
            infos = next_infos;
        }
        return infos;
    }

    // O*(V!)
    // Tries to extend every edges in previous using next as next vertex. Replace best edges in next if any of the obtained paths will be
    // better.
    private boolean try_update_next(LongestPathInfo previous, LongestPathInfo next, Edge edge, PathEstimator estimator) {
        List<Path> compatible_paths = previous.paths.stream().filter(p -> !p.used_ids.contains(next.node.id)).
                map(p -> Path.merge(p, Path.get_trivial_path(next.node, estimator), p.edges.size() - 1, 0, edge, estimator)).
                collect(Collectors.toList());
        if (compatible_paths.isEmpty()) {
            return false;
        }
        Path best_path = compatible_paths.stream().max(Comparator.comparingDouble(p -> p.score)).get();
        if (score_comparator.compare(best_path.score, next.best_path.score) == 1) {
            next.best_path = best_path;
            next.paths.clear();
        }

        List<Path> almost_equal = compatible_paths.stream().filter(p -> score_comparator.compare(p.score, next.best_path.score) == 0).
                collect(Collectors.toList());
        int size_before_update = next.paths.size();
        next.paths.addAll(almost_equal);
        return next.paths.size() > size_before_update;
    }

    public List<Node> get_nodes() {
        return nodes;
    }

    public List<List<Node>> get_components() {
        List<ComponentInfo> dfs_infos = nodes.stream().map(ComponentInfo::new).collect(Collectors.toList());
        List<List<Node>> result = new ArrayList<>();
        dfs_infos.stream().filter(info -> !info.visited).forEach(info -> {
            List<Node> next_component = new ArrayList<>();
            collect_component(info, next_component, dfs_infos);
            result.add(next_component);
        });
        return result;
    }

    private void collect_component(ComponentInfo start, List<Node> component, List<ComponentInfo> all_info) {
        start.visited = true;
        component.add(start.node);
        for (Edge edge : start.node.edges) {
            ComponentInfo next = all_info.get(edge.get_second(start.node).id);
            if (!next.visited) {
                collect_component(next, component, all_info);
            }
        }
    }
}

// Deprecated methods:

//    public void pop_edge() {
//        core.Edge removed = edges.poll();
//        removed.first.edges.remove(removed);
//        removed.second.edges.remove(removed);
//    }

// asymptotic: O(V + E * logE)
//    public List<List<core.Node>> k_edge_pop_clusterization(int pop_amount) {
//        for (int i = 0; i < pop_amount; ++i) {
//            pop_edge();
//        }
//        return get_components();
//    }

// asymptotic: O(V + E * (logE + T(estimator)))
//    public List<List<core.Node>> estimation_function_clusterization(estimators.GoodnessEstimator estimator, int steps_ahead) {
//        List<List<core.Node>> best_clusterization = Collections.singletonList(nodes);
//        double best_estimation = estimator.estimate_goodness(best_clusterization);
//        int iterations_since_best = 0;
//        while (iterations_since_best <= steps_ahead) {
//            pop_edge();
//            List<List<core.Node>> new_clusters = get_components();
//            double goodness_estimation = estimator.estimate_goodness(new_clusters);
//            if (goodness_estimation > best_estimation) {
//                best_clusterization = new_clusters;
//                best_estimation = goodness_estimation;
//                iterations_since_best = 0;
//            } else {
//                iterations_since_best += 1;
//            }
//        }
//        return best_clusterization;
//    }

//    public List<nodeinfo.DijkstraInfo> calculate_paths(int start_index, estimators.PathEstimator estimator) {
//        List<core.Node> graph = nodes;
//        List<nodeinfo.DijkstraInfo> nodes_info = graph.stream().map(n -> new nodeinfo.DijkstraInfo(n, null, Double
// .POSITIVE_INFINITY)).
//                collect(Collectors.toList());
//        nodeinfo.DijkstraInfo start = nodes_info.get(start_index);
//        start.distance = 0;
//        PriorityQueue<nodeinfo.DijkstraInfo> info_queue = new PriorityQueue<>((a, b) -> Double.compare(a.distance, b.distance));
//        info_queue.add(new nodeinfo.DijkstraInfo(start));
//        while (!info_queue.isEmpty()) {
//            nodeinfo.DijkstraInfo current_info = nodes_info.get(info_queue.poll().node.id);
//            if (current_info.visited) {
//                continue;
//            }
//            for (core.Edge edge : current_info.node.edges) {
//                nodeinfo.DijkstraInfo adjacent = nodes_info.get(edge.get_second(current_info.node).id);
//                if (current_info.distance + edge.weight < adjacent.distance && estimator.is_correct(adjacent, current_info)) {
//                    adjacent.distance = current_info.distance + edge.weight;
//                    adjacent.previous = current_info;
//                    info_queue.add(new nodeinfo.DijkstraInfo(adjacent));
//                }
//            }
//            current_info.visited = true;
//        }
//        return nodes_info;
//    }