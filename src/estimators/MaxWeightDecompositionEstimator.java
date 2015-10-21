package estimators;

import core.Edge;
import nodeinfo.ComponentInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MaxWeightDecompositionEstimator implements DecompositionEstimator {
    // O(E)
    @Override
    public double estimateDecomposition(List<ComponentInfo> componentInfos) {
        Map<Integer, List<ComponentInfo>> components = componentInfos.stream().collect(Collectors.groupingBy(ci -> ci.component));
        List<Double> componentEstimations = new ArrayList<>();
        for (Map.Entry<Integer, List<ComponentInfo>> entry : components.entrySet()) {
            Double maxEdgeWeight = entry.getValue().stream().
                    map(ci -> ci.node.getEdges().stream().
                            filter(e -> componentInfos.get(e.getFirst().getId()).component ==
                                    componentInfos.get(e.getSecond().getId()).component)).
                    flatMap(s -> s).map(Edge::getWeight).max(Double::compare).orElse(null);
            componentEstimations.add(maxEdgeWeight == null ? 0 : maxEdgeWeight);
        }
        return componentEstimations.stream().collect(Collectors.maxBy(Double::compare)).get();
    }
}
