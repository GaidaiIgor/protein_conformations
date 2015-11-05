function cost = trmcostphipsi(trmodel, angles)
%TRMCOSTPHIPSI Calculates cost as a function of psi and phi angles.
%   trmcostphipsi(trmodel, angles) calculates cost of the trmodel 
%   transformation with the specified torsion angles phi and psi of
%   intermediate configurations. Angles must be a vector of a matrix that 
%   length is equal to (2*(n-3)/3)*(m-2), where n is the number of atoms in
%   a configuration, m is the number of configurations in the 
%   transformation. Elements of the vector or the matrix are reshaped to
%   fill torsion angles matrix of the transformation.
%
%   Example:
%       I = setdiff(1:size(t.psi, 1), 2:3:size(t.psi, 1));
%       trmcostphipsi(t.psi(I,2:end-1));
%
% Protein Transformation Toolbox for MATLAB

% By Gaik Tamazian, 2012.
% tamaz.g@star.math.spbu.ru

trmodel.psi(setdiff(1:end, 2:3:end),2:end-1) = ...
    reshape(angles, size(trmodel.psi, 1)*2/3, size(trmodel.psi, 2) - 2);
coordscell = trmrestorecoords(trmodel);
n = length(coordscell);
cost = 0;

for i = 1:n-1
   cost = cost + ...
       sum(sum((coordscell{i+1} - coordscell{i}).^2, 2) .* trmodel.m);
end

end