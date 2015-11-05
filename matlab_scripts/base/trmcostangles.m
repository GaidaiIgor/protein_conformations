function cost = trmcostangles(trmodel, indices, angles)
%TRMCOSTANGLES Calculates transformation cost of specified angles.
%   trmcostangles(trmodel,indices,angles) calculates cost of the trmodel
%	transformation with the torsion angles values specified by the
%   angles vector. Indices of the torsion angles are also specified.
%
%   Example:
%       I = setdiff(1:size(t.psi, 1), 2:3:size(t.psi, 1));
%       trmcostangles(t, I, t.psi(I,2:end-1))
%
% Protein Transformation Toolbox for MATLAB

% By Gaik Tamazian, 2012.
% tamaz.g@star.math.spbu.ru

trmodel.psi(indices,2:end-1) = ...
    reshape(angles, length(indices), size(trmodel.psi, 2) - 2);
cost = trmcost(trmodel);

end

