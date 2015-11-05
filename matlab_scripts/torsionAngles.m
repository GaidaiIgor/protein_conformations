function result = torsionAngles(pdb)
backbone = pdbbackbone(pdb);
[~, ~, result] = createmodel(backbone);