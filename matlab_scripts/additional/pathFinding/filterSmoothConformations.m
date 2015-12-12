function result = filterSmoothConformations(startId, endId, activeIndexes, edgesPath, maxRoughness)
result = activeIndexes;
for index = cell2mat(activeIndexes.keys)
    edge12 = getOrientedPdbForEdge([startId index], edgesPath);
    edge23 = getOrientedPdbForEdge([index endId], edgesPath);
    if conformationsTransitionScore(edge12, edge23) > maxRoughness
        remove(result, index);
    end
end