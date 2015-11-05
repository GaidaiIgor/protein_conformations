package infoproviders;

import core.HierarchicalNode;
import nodeinfo.ComponentInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class NodeColorInfoProvider implements NodeInfoProvider {
    private final List<String> componentColors;
    private final List<ComponentInfo> componentInfos;

    public NodeColorInfoProvider(List<ComponentInfo> componentInfos) {
        this.componentInfos = componentInfos;
        int totalComponents = componentInfos.stream().map(ci -> ci.component).collect(Collectors.toSet()).size();
        int colorLevels = (int) Math.ceil(Math.pow(totalComponents, 1. / 3));
        componentColors = new ArrayList<>();
        for (int rLevel = 1; rLevel <= colorLevels; ++rLevel) {
            for (int gLevel = 1; gLevel <= colorLevels; ++gLevel) {
                for (int bLevel = 1; bLevel <= colorLevels; ++bLevel) {
                    int r = 255 * rLevel / colorLevels;
                    int g = 255 * gLevel / colorLevels;
                    int b = 255 * bLevel / colorLevels;
                    componentColors.add(String.format("#%x%x%x", r, g, b));
                }
            }
        }
    }

    @Override
    public String provideInfo(HierarchicalNode node) {
        return "color=\"" + componentColors.get(componentInfos.get(node.getId()).component) + "\"";
    }
}
