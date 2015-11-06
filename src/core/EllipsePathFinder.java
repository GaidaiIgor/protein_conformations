package core;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EllipsePathFinder<T extends AbstractNode<T>> {
    private Graph<T> graph;
    private double focusShare;
    private int maxPaths;
    private PathFilter<T> pathFilter;

    public EllipsePathFinder(Graph<T> graph, double focusShare, int maxPaths, PathFilter<T> pathFilter) {
        if (focusShare < 0 || focusShare > 0.5) {
            throw new IllegalArgumentException("Illegal share value");
        }

        this.graph = graph;
        this.focusShare = focusShare;
        this.maxPaths = maxPaths;
        this.pathFilter = pathFilter;
    }

    private boolean triangleInequalityHolds(T node1, T node2, T node3) {
        Edge<T> edge12 = Graph.getConnectingEdge(node1, node2);
        Edge<T> edge13 = Graph.getConnectingEdge(node1, node3);
        Edge<T> edge23 = Graph.getConnectingEdge(node2, node3);
        return edge12.getWeight() + edge23.getWeight() >= edge13.getWeight() &&
                edge12.getWeight() + edge13.getWeight() >= edge23.getWeight() &&
                edge13.getWeight() + edge23.getWeight() >= edge12.getWeight();
    }

    private List<Path<T>> mergePaths(List<Path<T>> pathsTo, List<Path<T>> pathsFrom) {
        List<Path<T>> result = new ArrayList<>(pathsTo.size() * pathsFrom.size());
        for (Path<T> path1 : pathsTo) {
            for (Path<T> path2 : pathsFrom) {
                result.add(Path.merge(path1, path2, path1.getEdges().size() - 1, 1, path2.getEdges().get(1)));
            }
        }
        return result;
    }

    private List<Path<T>> filterPaths(List<Path<T>> allPaths) {
        List<Path<T>> result = pathFilter.filterPaths(allPaths, maxPaths);
        if (result.size() > maxPaths) {
            throw new IllegalArgumentException("Provided filter failed to restrict amount of paths");
        }
        return result;
    }

    private List<Path<T>> findPathsHelper(T start, T end, List<T> ellipseNodes) {
        List<Path<T>> allPaths = new ArrayList<>();
        for (T node : ellipseNodes) {
            List<Path<T>> pathsTo = findPathsHelper(start, node, getEllipseNodes(ellipseNodes, start, node));
            List<Path<T>> pathsFrom = findPathsHelper(node, end, getEllipseNodes(ellipseNodes, node, end));
            List<Path<T>> mergedPaths = mergePaths(pathsTo, pathsFrom);
            allPaths.addAll(mergedPaths);
        }
        return filterPaths(allPaths);
    }

    private List<T> filterTriangularInequality(List<T> allNodes, T node1, T node2) {
        return allNodes.stream().filter(n -> triangleInequalityHolds(node1, node2, n)).collect(Collectors.toList());
    }

    private List<T> getEllipseNodes(List<T> allNodes, T start, T end) {
        List<T> triangularNodes = filterTriangularInequality(allNodes, start, end);
        double focus = Graph.getConnectingEdge(start, end).getWeight() * focusShare;
        Ellipse ellipse = new Ellipse(start, end, focus);
        return triangularNodes.stream().filter(ellipse::isInEllipse).collect(Collectors.toList());
    }

    public List<Path<T>> findPaths(int startId, int endId) {
        T start = graph.getNodes().get(startId);
        T end = graph.getNodes().get(endId);
        List<T> ellipseNodes = getEllipseNodes(graph.getNodes(), start, end);
        return findPathsHelper(start, end, ellipseNodes);
    }

    private class Ellipse {
        private T a;
        private T b;
        private double focus;

        public Ellipse(T a, T b, double focus) {
            if (focus < 0 || focus > 0.5) {
                throw new IllegalArgumentException("Illegal share value");
            }

            this.a = a;
            this.b = b;
            this.focus = focus;
        }

        public boolean isInEllipse(T other) {
            return minFocusFor(other) <= focus;
        }

        public double minFocusFor(T other) {
            double w = Graph.getConnectingEdge(a, b).getWeight();
            double b1 = Graph.getConnectingEdge(other, a).getWeight();
            double b2 = Graph.getConnectingEdge(other, b).getWeight();
            double s = (b1 + b2 + w) / 2;
            double A = Math.sqrt(s * (s - b1) * (s - b2) * (s - w));
            double h = 2 * A / w;
            double z = b1 * b1 - h * h;
            double k = h * h + z * z + w * w / 2 - z * w - w / 2;
            return (w - Math.sqrt(w * w - 4 * k)) / 2;
        }
    }
}
