function newtrmmodel = trmfixrotations(trmodel)
%TRMFIXROTATIONS Sets or updates transformations for a model.
%   trmfixrotations(trmodel)
%   There are two types of a transformation model: in which conformations
%   are superimposed on each other using Kabsch transformation (that is, a
%   translation vector and a rotation matrix are calculated for each
%   conformation afresh, this is a first-type model) and in which
%   translations and rotations are calculated when the model is created and
%   are fixed further (this is a second-type model). This function converts
%   a first-type model to a second-type one (i.e., rotations and
%   translations become fixed) or updates rotations and translations for
%   a second-type model (Kabsch transformation is used) depending on the
%   type of the given transformation model.
%
% Protein Transformation Toolbox for MATLAB

% By Gaik Tamazian, 2013.
% tamaz.g@star.math.spbu.ru

M = size(trmodel.psi, 2); % the number of intermediate models

if trmodel.type == 1
    % create rotation matrices and transition vectors
    trmodel.U = cell(1, M);  % rotations matrices
    trmodel.t = cell(1, M);  % translation vectors

end

prevcoords = trmodel.StartCoords;
trmodel.U{1} = eye(3);
trmodel.t{1} = zeros(size(prevcoords));

% calculate rotation matrices and transition vector for every intermediate
% model
for i = 2:M
    currcoords = restorecoords(trmodel.r(:,i), ...
        trmodel.kParam(:,i), trmodel.psi(:,i));
    [~, ~, temp] = procrustes(prevcoords(2:3:end,:), ...
        currcoords(2:3:end,:), 'scaling', false, 'reflection', false);
    trmodel.U{i} = temp.T;
    trmodel.t{i} = repmat(temp.c(1,:), size(currcoords, 1), 1);
    prevcoords = currcoords * trmodel.U{i} + trmodel.t{i};
end

trmodel.type = 2;
newtrmmodel = trmodel;

end
