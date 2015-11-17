% accepts several paths and make pdb for each
function writeGraphPathPdb(pathToPaths, pathsNames, proteinName, pathToEdges, pathToOutput)
for i = 1:length(pathsNames)
    nextPathPath = char(fullfile(pathToPaths, pathsNames(i)));
    summaryPdb = pdbForGraphPath(nextPathPath, proteinName, pathToEdges);
    nextOutName = fullfile(pathToOutput, [char(pathsNames(i)), '.pdb']);
    pdbwrite(nextOutName, summaryPdb);
end
