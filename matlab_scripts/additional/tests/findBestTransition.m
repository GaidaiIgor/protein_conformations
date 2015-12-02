function [bestConformation, bestScore] = findBestTransition(startId, endId, maxId, proteinName, edgePath)
bestScore = Inf;
bestConformation = nan;
for i = 1:maxId
    if i == startId || i == endId
        continue
    end
    edge12 = getOrientedPdbForEdge([startId i], proteinName, edgePath);
    edge23 = getOrientedPdbForEdge([i endId], proteinName, edgePath);
    score = conformationsTransitionScore(edge12, edge23);
    if score < bestScore
        bestScore = score;
        bestConformation = i;
    end
end
