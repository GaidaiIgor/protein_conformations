function result = graphTransitionsScores(graphPath, edgesPath)
edgesNames = getEdgesNames(graphPath);
complete = 0;
for i = 1:length(edgesNames)
    if i / length(edgesNames) > complete + 0.1
        complete = complete + 0.1;
        fprintf('%d %% completed\n', complete * 100);
    end
    nextPdb = pdbread(fullfile(edgesPath, [getStrictEdgeNameKey(char(edgesNames{i})), '.pdb']));
    result(i, :) = edgeTransitionsScores(nextPdb);
end