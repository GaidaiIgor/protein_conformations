function mindist = pdbmininteratomicdist(PDBStruct)
%PDBMININTERATOMICDIST Returns the least interatomic distances.
%   pdbmininteratomicdist(PDBStruct) returns the vector of the least
%	interatomic distances between the atoms within transformation 
%   configurations. Each element of the vector corresponds to the certain
%   configuration.
%
% Protein Transformation Toolbox for MATLAB

% By Gaik Tamazian, 2012.
% tamaz.g@star.math.spbu.ru

coords = pdbextractcoords(PDBStruct);
mindist = length(coords);

for i = 1:length(coords)
	mindist(i) = min(pdist(coords{i}));
end

end

