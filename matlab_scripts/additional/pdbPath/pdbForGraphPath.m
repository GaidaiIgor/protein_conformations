function result = pdbForGraphPath(pathToPath, pathToEdges)
pathFile = fopen(pathToPath, 'rt');
nextLine = fgetl(pathFile);
edge = cellfun(@str2num, strsplit(nextLine));
summaryPdb = getOrientedPdbForEdge(edge, pathToEdges);
while true
    nextLine = fgetl(pathFile);
    if ~ischar(nextLine)
        break
    end
    edge = cellfun(@str2num, strsplit(nextLine));
    edgePdb = getOrientedPdbForEdge(edge, pathToEdges);
    lastModel = summaryPdb.Model(end);
    for i = 2:length(edgePdb.Model)
        nextModel = edgePdb.Model(i);
        alignedModel = alignModels(lastModel, nextModel);
        alignedModel.MDLSerNo = length(summaryPdb.Model) + 1;
        summaryPdb.Model(end + 1) = alignedModel;
        lastModel = alignedModel;
    end
end
fclose(pathFile);
result = summaryPdb;