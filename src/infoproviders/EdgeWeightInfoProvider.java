package infoproviders;

import core.Edge;

public class EdgeWeightInfoProvider implements EdgeInfoProvider {
    @Override
    public String provideInfo(Edge edge) {
        return "weight=" + String.valueOf(edge.getWeight());
    }
}
