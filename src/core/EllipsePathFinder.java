package core;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EllipsePathFinder<T extends AbstractNode<T>> {
    private Graph<T> graph;
    private double perifocalDistShare;
    private int maxPaths;
    private PathFilter<T> pathFilter;

    public EllipsePathFinder(Graph<T> graph, double perifocalDistShare, int maxPaths, PathFilter<T> pathFilter) {
        if (perifocalDistShare < 0 || perifocalDistShare > 0.5) {
            throw new IllegalArgumentException("Illegal share value");
        }

        this.graph = graph;
        this.perifocalDistShare = perifocalDistShare;
        this.maxPaths = maxPaths;
        this.pathFilter = pathFilter;
    }

    public double getPerifocalDistShare() {
        return perifocalDistShare;
    }

    private boolean triangleInequalityHolds(T node1, T node2, T node3) {
        Edge<T> edge12 = Graph.getConnectingEdge(node1, node2);
        Edge<T> edge13 = Graph.getConnectingEdge(node1, node3);
        Edge<T> edge23 = Graph.getConnectingEdge(node2, node3);
        return edge12 != null && edge13 != null && edge23 != null &&
                edge12.getWeight() + edge23.getWeight() >= edge13.getWeight() &&
                edge12.getWeight() + edge13.getWeight() >= edge23.getWeight() &&
                edge13.getWeight() + edge23.getWeight() >= edge12.getWeight();
    }

    private Set<Path<T>> mergePaths(Set<Path<T>> pathsTo, Set<Path<T>> pathsFrom) {
        Set<Path<T>> result = new HashSet<>(pathsTo.size() * pathsFrom.size());
        for (Path<T> path1 : pathsTo) {
            for (Path<T> path2 : pathsFrom) {
                result.add(Path.merge(path1, path2, path1.getEdges().size() - 1, 1, path2.getEdges().get(1)));
            }
        }
        return result;
    }

    private Set<Path<T>> filterPaths(Collection<Path<T>> allPaths) {
        Set<Path<T>> result = pathFilter.filterPaths(allPaths, maxPaths);
        if (result.size() > maxPaths) {
            throw new IllegalArgumentException("Provided filter failed to restrict amount of paths");
        }
        return result;
    }

    private Set<Path<T>> findPathsHelper(T start, T end, List<T> ellipseNodes) {
        Set<Path<T>> allPaths = new HashSet<>();
        Path<T> trivial = Path.getTrivialPath(start);
        trivial.extendWith(Graph.getConnectingEdge(start, end));
        allPaths.add(trivial);

        for (T node : ellipseNodes) {
            Set<Path<T>> pathsTo = findPathsHelper(start, node, getEllipseNodes(ellipseNodes, start, node));
            Set<Path<T>> pathsFrom = findPathsHelper(node, end, getEllipseNodes(ellipseNodes, node, end));
            Set<Path<T>> mergedPaths = mergePaths(pathsTo, pathsFrom);
            allPaths.addAll(mergedPaths);
        }
        return filterPaths(allPaths);
    }

    private List<T> filterTriangularInequality(List<T> allNodes, T node1, T node2) {
        return allNodes.stream().filter(n -> triangleInequalityHolds(node1, node2, n)).collect(Collectors.toList());
    }

    private List<T> getEllipseNodes(List<T> allNodes, T start, T end) {
        List<T> triangularNodes = filterTriangularInequality(allNodes, start, end);
        double perifocalDist = Graph.getConnectingEdge(start, end).getWeight() * perifocalDistShare;
        Ellipse ellipse = new Ellipse(start, end, perifocalDist);
        return triangularNodes.stream().filter(ellipse::isInEllipse).collect(Collectors.toList());
    }

    public Set<Path<T>> findPaths(int startId, int endId) {
        T start = graph.getNodes().get(startId);
        T end = graph.getNodes().get(endId);
        if (Graph.getConnectingEdge(start, end) == null) {
            throw new IllegalArgumentException("Specified nodes are not connected");
        }

        List<T> ellipseNodes = getEllipseNodes(graph.getNodes(), start, end);
        return findPathsHelper(start, end, ellipseNodes);
    }

    private class Ellipse {
        private T a;
        private T b;
        private double perifocalDist;

        public Ellipse(T a, T b, double perifocalDist) {
            this.a = a;
            this.b = b;
            this.perifocalDist = perifocalDist;
        }

        public boolean isInEllipse(T other) {
            return minPerifocalDist(other) <= perifocalDist;
        }

        public double minPerifocalDist(T other) {
            double w = Graph.getConnectingEdge(a, b).getWeight();
            double b1 = Graph.getConnectingEdge(other, a).getWeight();
            double b2 = Graph.getConnectingEdge(other, b).getWeight();
            if (b1 > w || b2 > w) {
                return Double.NaN;
            }
            double s = (b1 + b2 + w) / 2;
            double A = Math.sqrt(s * (s - b1) * (s - b2) * (s - w));
            double h = 2 * A / w;
            double z1 = Math.sqrt(b1 * b1 - h * h);
            double z2 = Math.sqrt(b2 * b2 - h * h);

            double test = -(4 * h * h - w * w + z1 * z1 - 2 * z1 * z2 + z2 * z2) / (w * w - z1 * z1 + 2 * z1 * z2 - z2 * z2);
            double res = z1 / 2 + z2 / 2 - (w * Math.sqrt(test)) / 2;
            double coord = (b1 + w - b2) / 2;
            double height = Math.sqrt(w * perifocalDist - perifocalDist * perifocalDist);

            return z1 / 2 + z2 / 2 - (w * Math.sqrt(-(4 * h * h - w * w + z1 * z1 - 2 * z1 * z2 + z2 * z2) / (w * w - z1 * z1 + 2 * z1 * z2 - z2 * z2))) / 2;
        }
    }
}
