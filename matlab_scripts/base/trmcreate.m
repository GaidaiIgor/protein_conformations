function torsmodel = trmcreate(PDBStruct1, PDBStruct2, M, type)
%TRMCREATE Creates a transformation model.
%   trmcreate(PDBStruct1, PDBStruct2, M, type) creates a transformation
%   model from two unimodal PDB structures. The parameters are the number 
%   of intermediate configurations M and the model type.
%
%   There are two model types. For the first type model rotations and
%   translations to fit configurations are calculated at every step. For
%   the second type model the rotations and translations are calculated
%   once during the model creation. By default it is considered that the
%   model belongs to the first type.
%
% Protein Transformation Toolbox for MATLAB

% By Gaik Tamazian, 2012.
% tamaz.g@star.math.spbu.ru

if nargin < 4
    type = 1;
end

% We have already got information about masses of side chains atoms, so we
% can leave only backbone atoms.
PDBStruct1 = pdbbackbone(PDBStruct1);
PDBStruct2 = pdbbackbone(PDBStruct2);

% Get atomic masses of backbone atoms.
torsmodel = struct('m', atomicmass({PDBStruct1.Model.Atom.element}), ...
    'StartCoords', atomiccoords(PDBStruct1), ...
    'FinishCoords', atomiccoords(PDBStruct2));

% Process side chains atoms - get a vector of their masses and add them to
% atomic masses of kParam carbons. Also add a mass of one hydrogen atom.
alphacarbonatoms = PDBStruct1.Model.Atom( ...
    ismember({PDBStruct1.Model.Atom.AtomName}, {'CA'}));
torsmodel.m(2:3:end) = torsmodel.m(2:3:end) + ...
    sidechainmass({alphacarbonatoms.resName}) + atomicmass({'H'});

% Add masses of hydrogen atoms to nitrogen atoms of the backbone.
torsmodel.m(1:3:end) = torsmodel.m(1:3:end) + atomicmass({'H'});

% Add masses of oxygen atoms to carbon atoms of the backbone.
torsmodel.m(3:3:end) = torsmodel.m(3:3:end) + atomicmass({'O'});

% Add a mass of two hydrogen atoms to N-end of the protein.
torsmodel.m(1) = torsmodel.m(1) + 2*atomicmass({'H'});

% Add a mass of a hydroxil group to C-end of the protein.
torsmodel.m(end) = torsmodel.m(end) + sum(atomicmass({'O', 'H'}));

torsmodel.r     = zeros(length(torsmodel.m) - 1, M+2);
torsmodel.kParam = zeros(length(torsmodel.m) - 2, M+2);
torsmodel.psi   = zeros(length(torsmodel.m) - 3, M+2);
torsmodel.type  = type;

% Calculate bond lengths and planar angles of the initial configuration.
deltax = diff(torsmodel.StartCoords);
torsmodel.r(:,1) = sqrt(sum(deltax.^2, 2));
torsmodel.kParam(:,1) = ...
    acos(dot(deltax(1:end-1,:), deltax(2:end,:), 2) ./ ...
    (torsmodel.r(1:end-1,1) .* torsmodel.r(2:end,1)));
        
% Calculate torsion angles of the initial configuration.
N = cross(deltax(1:end-1,:), deltax(2:end,:), 2);
torsmodel.psi(:,1) = ...
    atan2(torsmodel.r(2:end-1,1) .* ...
    dot(deltax(1:end-2,:), N(2:end,:), 2), ...
    dot(N(1:end-1,:), N(2:end,:), 2));

% Calculate bond lengths and planar angles of the final configuration.
deltax = diff(torsmodel.FinishCoords);
torsmodel.r(:,end) = sqrt(sum(deltax.^2, 2));
torsmodel.kParam(:,end) = ...
    acos(dot(deltax(1:end-1,:), deltax(2:end,:), 2) ./ ...
    (torsmodel.r(1:end-1,end) .* torsmodel.r(2:end,end)));

% Calculate torsion angles of the final configuration.
N = cross(deltax(1:end-1,:), deltax(2:end,:), 2);
torsmodel.psi(:,end) = ...
    atan2(torsmodel.r(2:end-1,end) .* ...
    dot(deltax(1:end-2,:), N(2:end,:), 2), ...
    dot(N(1:end-1,:), N(2:end,:), 2));

% Calculate intermediate bond lengths and planar angles.
torsmodel.r(:,2:end-1) = ...
    interpolate(torsmodel.r(:,1), torsmodel.r(:,end), M);
torsmodel.kParam(:,2:end-1) = ...
    interpolate(torsmodel.kParam(:,1), torsmodel.kParam(:,end), M);

% Calculate intermediate torsion angles.
torsmodel.psi(:,2:end-1) = ...
    interpcirc(torsmodel.psi(:,1), torsmodel.psi(:,end), M);

% if we have a model of the second type, then optimal rotations and
% translations must be calculated for each intermediate configuration
% and for the final one
if type == 2
    torsmodel.U = cell(1, M+2);  % rotations matrices
    torsmodel.t = cell(1, M+2);  % translation vectors
    prevcoords = torsmodel.StartCoords;
    torsmodel.U{1} = eye(3);
    torsmodel.t{1} = zeros(size(prevcoords));
    for i = 2:(M+2)
        currcoords = restorecoords(torsmodel.r(:,i), ...
            torsmodel.kParam(:,i), torsmodel.psi(:,i));
        [~, ~, temp] = procrustes(prevcoords(2:3:end,:), ...
            currcoords(2:3:end,:), 'scaling', false, 'reflection', false);
        torsmodel.U{i} = temp.T;
        torsmodel.t{i} = repmat(temp.c(1,:), size(currcoords, 1), 1);
        prevcoords = currcoords * torsmodel.U{i} + torsmodel.t{i};
    end
end

end

function result = interpolate(start, finish, M)
    result = ...
        repmat((M:-1:1)/(M+1), length(start), 1).*repmat(start, 1, M) + ...
        repmat((1:M)/(M+1), length(finish), 1).*repmat(finish, 1, M);
end

function coords = atomiccoords(PDBStruct)
    coords = [[PDBStruct.Model.Atom.X]' [PDBStruct.Model.Atom.Y]' ...
        [PDBStruct.Model.Atom.Z]'];
end

function mass = atomicmass(atom)

persistent elemnotations;
persistent elemweights;
persistent atomicweights;

if any([isempty(elemnotations), isempty(elemweights), ...
        isempty(atomicweights)])
    elemnotations = {'H', 'He', 'Li', 'Be', 'B', 'C', 'N', 'O', 'F', ...
        'Ne', 'Na', 'Mg', 'Al', 'Si', 'P', 'S', 'Cl', 'Ar', 'K', 'Ca', ...
        'Sc', 'Ti', 'V', 'Cr', 'Mn', 'Fe', 'Co', 'Ni', 'Cu', 'Zn', ...
        'Ga', 'Ge', 'As', 'Se', 'Br', 'Kr', 'Rb', 'Sr', 'Y', 'Zr', ...
        'Nb', 'Mo', 'Tc', 'Ru', 'Rh', 'Pd', 'Ag', 'Cd', 'In', 'Sn', ...
        'Sb', 'Te', 'I', 'Xe', 'Cs', 'Ba', 'La'};
    elemweights = [1 4 6.94 9.01 10.81 12.01 14.01 16 19 20.18 23 24.31 ...
        26.98 28.09 30.97 32.07 35.45 39.95 39.1 40.08 44.96 47.88 ...
        50.94 52 54.94 55.85 58.93 58.69 63.55 65.39 69.72 72.59 74.92 ...
        78.96 79.9 83.8 85.47 87.62 88.91 91.22 92.91 95.94 97.91 ...
        101.07 102.91 106.42 107.87 112.41 114.82 118.71 121.75 127.6 ...
        126.91 131.29 132.91 137.33 138.96];
    atomicweights = containers.Map(elemnotations, elemweights, ...
        'UniformValues', true);
end

mass = cell2mat(values(atomicweights, atom))';

end