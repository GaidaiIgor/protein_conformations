function rewriteEdgesWeights(oldWeightsFilePath, newWeightsOutputPath, edgesPath, weightEstimator)
edgesNames = getEdgesNames(oldWeightsFilePath);
edgesCost = calculateEdgesCost(edgesNames, edgesPath, weightEstimator);
writeEdges(edgesNames, edgesCost, newWeightsOutputPath);