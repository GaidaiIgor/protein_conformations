function result = testTransitionScores(pathToPath, proteinName, pathToEdges)
pathFile = fopen(pathToPath, 'rt');
nextLine = fgetl(pathFile);
edge = cellfun(@str2num, strsplit(nextLine));
summaryPdb = getPdbForEdge(edge, proteinName, pathToEdges);
scores = edgeTransitionsScores(summaryPdb);
while true
    nextLine = fgetl(pathFile);
    if ~ischar(nextLine)
        break
    end
    edge = cellfun(@str2num, strsplit(nextLine));
    edgePdb = getPdbForEdge(edge, proteinName, pathToEdges);
    scores = [scores edgeTransitionsScores(edgePdb)];
end
fclose(pathFile);
result = scores;