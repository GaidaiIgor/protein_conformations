function filterEdgesPdb(bestEdgesNames, edgesPath, outputPath)
for name = bestEdgesNames
    copyfile(fullfile(edgesPath, char(name)), fullfile(outputPath, [getStrictEdgeNameKey(char(name)) '.pdb']));
end