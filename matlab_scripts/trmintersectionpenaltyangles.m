function penalty = ...
    trmintersectionpenaltyangles(trmodel, indices, angles, L, A, B)
%TRMINTERSECTIONPENALTYANGLES The transformation penalty of angles.
%   trmintersectionpenaltyangles(trmodel, indices, angles, L, A, B)
%   calculates the penalty for transformation atoms in the same way as
%   trmintersectionpelanty function but it also requires values of the
%	torsion angles specified by their indices.
%
%   Example:
%       A = 100; B = -10;
%       L = min([pdist(coords{1}), pdist(coords{end})]);
%       I = setdiff(1:size(t.psi, 1), 2:3:size(t.psi, 1));
%       trmintersectionpenaltyangles(t, I, t.psi(I,2:end-1), L, A, B)
%
% Protein Transformation Toolbox for MATLAB

% By Gaik Tamazian, 2012.
% tamaz.g@star.math.spbu.ru

trmodel.psi(indices,2:end-1) = ...
    reshape(angles, length(indices), size(trmodel.psi, 2) - 2);
penalty = trmintersectionpenalty(trmodel, L, A, B);

end