function mass = sidechainmass(X)
%SIDECHAINMASS Return masses the specified side chains.
%   sidechainmass(X), for a cell array of side chain names X, is a vector
%   of total atomic masses of the specified side chains.
%
%   Protein Transformation Toolbox for MATLAB.

% By Gaik Tamazian, 2013.
% tamaz.g@star.math.spbu.ru

persistent scnotations;
persistent scweights;
persistent sidechainweights;

if any([isempty(scnotations), isempty(scweights), ...
        isempty(sidechainweights)])
    scnotations = {'ALA', 'SER', 'ASN', 'ARG', 'VAL', 'THR', 'GLU', ...
        'LEU', 'CYS', 'HIS', 'LYS', 'ILE', 'TYR', 'ASP', 'MET', 'PRO', ...
        'GLY', 'TRP', 'PHE', 'GLN'};
    scweights = [15 31 59 100 43 45 73 57 46 81 72 57 107 58 75 70 1 ...
        130 91 72];
    sidechainweights = containers.Map(scnotations, scweights, ...
        'UniformValues', true);
end

mass = cell2mat(values(sidechainweights, X)');

end

