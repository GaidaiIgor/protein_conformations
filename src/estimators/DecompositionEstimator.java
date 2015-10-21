package estimators;

import nodeinfo.ComponentInfo;

import java.util.List;

public interface DecompositionEstimator {
    double estimateDecomposition(List<ComponentInfo> componentInfos);
}
