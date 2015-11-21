function cost = pdbTransformationCost(PDBStruct, iterationFunction, finishFunction)
%PDBTRANSFORMATIONCOST Calculates transformation cost.
%   pdbtransformationcost(PDBStruct) evaluates cost of the transformation
%   represented by the structure PDBStruct. The cost is calculated as a
%   weighted sum of distances between atoms of adjacent configurations.
%
% Protein Transformation Toolbox for MATLAB

% By Gaik Tamazian, 2012.
% tamaz.g@star.math.spbu.ru

cost = 0;
m = atomicmass({PDBStruct.Model(1).Atom.element});  % masses of the atoms

% Add masses of side chains and masses of one hydrogen atoms to alpha
% carbons.
alphacarbonatoms = PDBStruct.Model(1).Atom( ...
    ismember({PDBStruct.Model(1).Atom.AtomName}, {'CA'}));
m(2:3:end) = m(2:3:end) + sidechainmass({alphacarbonatoms.resName}) + ...
    atomicmass({'H'});

% Add masses of hydrogen atoms to nitrogen atoms of the backbone.
m(1:3:end) = m(1:3:end) + atomicmass({'H'});

% Add masses of oxygen atoms to carbon atoms of the backbone.
m(3:3:end) = m(3:3:end) + atomicmass({'O'});

% Add a mass of two hydrogen atoms to N-end of the protein.
m(1) = m(1) + 2*atomicmass({'H'});

% Add a mass of a hydroxil group to C-end of the protein.
m(end) = m(end) + sum(atomicmass({'O', 'H'}));

nModels = length(PDBStruct.Model);

coordsprev = [[PDBStruct.Model(1).Atom.X]' ...
        [PDBStruct.Model(1).Atom.Y]' [PDBStruct.Model(1).Atom.Z]'];

for i = 2:nModels
    coordscur = [[PDBStruct.Model(i).Atom.X]' ...
        [PDBStruct.Model(i).Atom.Y]' [PDBStruct.Model(i).Atom.Z]'];
    cost = cost + iterationFunction(coordscur, coordsprev, m);
    coordsprev = coordscur;
end
cost = finishFunction(cost);
end