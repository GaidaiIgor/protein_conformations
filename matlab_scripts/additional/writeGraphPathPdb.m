function writeGraphPathPdb(pathToPath, proteinName, pathToEdges, pathToOutput)
summaryPdb = pdbForGraphPath(pathToPath, proteinName, pathToEdges);
pdbwrite(pathToOutput, summaryPdb);