function result = pdbForGraphPath(pathToPath, proteinName, pathToEdges)
pathFile = fopen(pathToPath, 'rt');
nextLine = fgetl(pathFile);
edge = cellfun(@str2num, strsplit(nextLine));
summaryPdb = getPdbForEdge(edge, proteinName, pathToEdges);
while true
    nextLine = fgetl(pathFile);
    if ~ischar(nextLine)
        break
    end
    edge = cellfun(@str2num, strsplit(nextLine));
    edgePdb = getPdbForEdge(edge, proteinName, pathToEdges);
    isReverse = edge(1) > edge(2);
    
    % append edge
    if isReverse
        range = length(edgePdb.Model):-1:2;
    else
        range = 2:length(edgePdb.Model);
    end
    lastModel = summaryPdb.Model(end);
    for i = range
        nextModel = edgePdb.Model(i);
        alignedModel = alignModels(lastModel, nextModel);
        alignedModel.MDLSerNo = length(summaryPdb.Model) + 1;
        summaryPdb.Model(end + 1) = alignedModel;
        lastModel = alignedModel;
    end
    % end append edge
end
fclose(pathFile);
result = summaryPdb;