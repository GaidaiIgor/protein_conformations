function minconfs = pdbmininteratomicdist(PDBStruct)
%PDBMININTERATOMICDIST Returns the least interatomic distances.
%   pdbmininteratomicdist(PDBStruct) returns the vector of the least
%	interatomic distances between the atoms that belong to the same
%	transformation conformation. Each element of the vector corresponds
%	to the certain conformation.
%
% Protein Transformation Toolbox for MATLAB

% By Gaik Tamazian, 2012.
% tamaz.g@star.math.spbu.ru

coords = pdbextractcoords(PDBStruct);
minconfs = length(coords);

for i = 1:length(coords)
	minconfs(i) = min(pdist(coords{i}));
end

end

