function [edgeName, score] = estimateEdgeGroup(edgeNamesGroup, edgesPath, estimator)
edgePdbs = arrayfun(@(name) pdbread(fullfile(edgesPath, char(name))), edgeNamesGroup);
edgeScores = arrayfun(estimator, edgePdbs);
[score, index] = min(edgeScores);
edgeName = edgeNamesGroup(index);