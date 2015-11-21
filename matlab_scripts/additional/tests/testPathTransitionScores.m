function result = testPathTransitionScores(pathToPath, proteinName, pathToEdges)
pathFile = fopen(pathToPath, 'rt');
nextLine = fgetl(pathFile);
edge = cellfun(@str2num, strsplit(nextLine));
summaryPdb = getOrientedPdbForEdge(edge, proteinName, pathToEdges);
scores = edgeTransitionsScores(summaryPdb);
while true
    nextLine = fgetl(pathFile);
    if ~ischar(nextLine)
        break
    end
    edge = cellfun(@str2num, strsplit(nextLine));
    edgePdb = getOrientedPdbForEdge(edge, proteinName, pathToEdges);
    scores = [scores edgeTransitionsScores(edgePdb)];
end
fclose(pathFile);
result = scores;