% recalculates edge weights given in specified file and wirtes new values
% to specified file
function rewriteEdgesWeights(oldWeightsFilePath, newWeightsOutputPath, edgesPath, weightEstimator)
edgesNames = getEdgesNames(oldWeightsFilePath);
edgesCost = calculateEdgesCost(edgesNames, edgesPath, weightEstimator);
writeEdges(edgesNames, edgesCost, newWeightsOutputPath);