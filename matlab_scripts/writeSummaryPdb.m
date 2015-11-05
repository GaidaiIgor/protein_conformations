function writeSummaryPdb(pathToPath, proteinName, pathToEdges, pathToOutput)
summaryPdb = pdbForGraphPath(pathToPath, proteinName, pathToEdges);
pdbwrite(pathToOutput, summaryPdb);