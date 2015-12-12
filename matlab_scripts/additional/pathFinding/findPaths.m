% allConformations: models array
% referencePath: models array
function paths = findPaths(startId, endId, edgesPath, allConformations, activeIndexes, ...
    referencePath, maxDeviation, maxRoughness)
allConformations = pdbBackbone(allConformations, {'CA'});
remove(activeIndexes, [startId, endId]);
activeIndexes = filterCloseConformations(activeIndexes, allConformations, referencePath, maxDeviation);
activeIndexes = filterSmoothConformations(startId, endId, activeIndexes, edgesPath, maxRoughness);
for conformation = allConformations
    
end