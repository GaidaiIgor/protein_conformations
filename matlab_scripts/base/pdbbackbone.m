function backbonePDBStruct = pdbbackbone(PDBStruct)
%PDBBACKBONE Extracts a PDB structure with backbone atoms.
%   pdbbackbone(PDBStruct) returns a PDB structure that contains only
%   backbone atoms (that is, atoms with names N, C, and CA).
%
% Protein Transformation Toolbox for MATLAB

% By Gaik Tamazian, 2012.
% tamaz.g@star.math.spbu.ru

backbonePDBStruct = PDBStruct;
for i = 1:length(backbonePDBStruct.Model)
    backbonePDBStruct.Model(i).Atom = PDBStruct.Model(i).Atom( ...
        ismember({PDBStruct.Model(i).Atom.AtomName}, {'N' 'C' 'CA'}));
    backbonePDBStruct.Model(i).HeterogenAtom = []; 
end

end

