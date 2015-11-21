% accepts several paths and make pdb for each
function writeGraphPathPdb(paths, proteinName, pathToEdges)
for i = 1:length(paths)
    nextPathPath = char(paths{i});
    summaryPdb = pdbForGraphPath(nextPathPath, proteinName, pathToEdges);
    nextOutName = [nextPathPath, '.pdb'];
    pdbwrite(nextOutName, summaryPdb);
end
