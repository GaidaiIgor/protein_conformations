function pdb = pdbBackbone(pdb, atomNames)
if nargin < 2
    atomNames = {'N', 'C', 'CA'};
end
pdb.Model = arrayfun(@(model) modelBackbone(model, atomNames), pdb.Model);
end