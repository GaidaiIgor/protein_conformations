% accepts several paths and make pdb for each
function writeGraphPathPdb(paths, pathToEdges)
for i = 1:length(paths)
    nextPathPath = char(paths{i});
    summaryPdb = pdbForGraphPath(nextPathPath, pathToEdges);
    nextOutName = [nextPathPath, '.pdb'];
    pdbwrite(nextOutName, summaryPdb);
end