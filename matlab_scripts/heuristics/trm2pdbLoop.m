function PDBStruct = trm2pdbLoop(trmodel, initialPDBStruct)
%TRM2PDBLOOP Converts a loop transformation model to a PDB struct. 
%   trm2pdbLoop(trmodel, initialPDBStruct) returns the PDB structure that
%   correposonds to the transformation presented in the transformation
%   model trmodel. The initial PDB structure the transformartion has been 
%   created from is also required.
%
% Protein Transformation Toolbox for MATLAB

% By Gaik Tamazian, 2012.
% tamaz.g@star.math.spbu.ru

% Sergey Knyazev, 2013.
% sergey.n.knyazev@gmail.com

PDBStruct = initialPDBStruct;
PDBStruct.Model = PDBStruct.Model(1);
nModels = size(trmodel.psi, 2);
coordscell = trmrestorecoordsLoop(trmodel);

for i = 1:nModels
    PDBStruct.Model(i) = initialPDBStruct.Model(1);
    for j = 1:size(coordscell{i}(:,1), 1)
        PDBStruct.Model(i).Atom(j).X = coordscell{i}(j,1);
        PDBStruct.Model(i).Atom(j).Y = coordscell{i}(j,2);
        PDBStruct.Model(i).Atom(j).Z = coordscell{i}(j,3);
    end
end

end

