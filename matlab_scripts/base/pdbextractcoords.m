function coords = pdbextractcoords(PDBStruct)
%PDBEXTRACTCOORDS Extracts atom coordinates from a PDB structure.
%   pdbextractcoords(PDBStruct) extracts coordinates of the atoms that
%   constitute a protein from a PDB structure. A cell array of matrices is
%   returned and each matrix corresponds to a separate model.
%
% Protein Transformation Toolbox for MATLAB

% By Gaik Tamazian, 2012.
% tamaz.g@star.math.spbu.ru

nModels = length(PDBStruct.Model);
coords = cell(1, nModels);

for i = 1:nModels
    coords{i} = [[PDBStruct.Model(i).Atom.X]' ...
        [PDBStruct.Model(i).Atom.Y]' [PDBStruct.Model(i).Atom.Z]'];
end

end

