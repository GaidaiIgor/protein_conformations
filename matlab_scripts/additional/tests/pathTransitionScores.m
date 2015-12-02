function result = pathTransitionScores(pathToPath, pathToEdges)
pathFile = fopen(pathToPath, 'rt');
nextLine = fgetl(pathFile);
edge = cellfun(@str2num, strsplit(nextLine));
previousPdb = getOrientedPdbForEdge(edge, pathToEdges);
result = edgeTransitionsScores(previousPdb);
while true
    nextLine = fgetl(pathFile);
    if ~ischar(nextLine)
        break
    end
    edge = cellfun(@str2num, strsplit(nextLine));
    nextPdb = getOrientedPdbForEdge(edge, pathToEdges);
    result = [result, conformationsTransitionScore(previousPdb, nextPdb), edgeTransitionsScores(nextPdb)];
    previousPdb = nextPdb;
end
fclose(pathFile);