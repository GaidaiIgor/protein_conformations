function writeBestEdges(edgesPath, outputPath, edgeEstimator)
groups = values(groupEdges(edgesPath));
names = cell(1, length(groups));
scores = zeros(1, length(groups));
for i = 1:length(groups)
    fprintf('iteration %d out of %d\n', i, length(groups));
    group = groups{i};
    [bestEdgeName, bestEdgeScore] = estimateEdgeGroup(group, edgesPath, edgeEstimator);
    names(i) = bestEdgeName;
    scores(i) = bestEdgeScore;
end
writeEdges(names, scores, outputPath);