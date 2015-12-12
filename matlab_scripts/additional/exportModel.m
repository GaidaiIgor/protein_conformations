function exportModel(pdb, modelIndex, outputPath)
pdb.Model = pdb.Model(modelIndex);
pdbwrite(outputPath, pdb);