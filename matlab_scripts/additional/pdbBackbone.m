function pdb = pdbBackbone(pdb)
pdb.Model = arrayfun(@(model) modelBackbone(model), pdb.Model);
end