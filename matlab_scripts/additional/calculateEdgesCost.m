function edgesCost = calculateEdgesCost(edgesNamesList, edgesPath, estimator)
edgesCost = zeros(length(edgesNamesList), 1);
for i = 1:length(edgesNamesList)
    nextPdbFileName = char(fullfile(edgesPath, edgesNamesList(i)));
    nextPdb = pdbread(nextPdbFileName);
    edgesCost(i) = estimator(nextPdb);
end