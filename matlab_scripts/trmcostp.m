function cost = trmcostp(trmodel, trmAnglesIndices, trmAnglesValues, p)
%TRMCOSTLP Calculates transformation model cost as a function of psi
%   and phi angles if psi and phi are specified.
%   Or just calculates transformation model cost if psi and phi
%   are not specified.
%   The cost is calculated as a weighted sum of the distances between
%   atoms of adjacent configurations.
%   trmcostlp(trmodel, anglesIndices, anglesValues, p)
%   calculates cost of the trmodel transformation with or without
%   the specified torsion angles phi and psi of
%   intermediate configurations. If psi and phi are specified then this
%   angles must be a vector of a matrix that 
%   length is equal to (2*(n-3)/3)*(m-2), where n is the number of atoms in
%   a configuration, m is the number of configurations in the 
%   transformation. Elements of the vector or the matrix are reshaped to
%   fill torsion angles matrix of the transformation.
%
%   Example:
%       I = setdiff(1:size(t.psi, 1), 2:3:size(t.psi, 1));
%       trmcostlp(t.psi(I,2:end-1),[],[],2);
%
% Protein Transformation Toolbox for MATLAB
%
% By Gaik Tamazian, 2012.
% tamaz.g@star.math.spbu.ru
%
% By Sergey Knyazev, 2012.
% sergey.n.knyazev@gmail.com

if ~isempty(trmAnglesIndices)
    trmodel.psi(trmAnglesIndices,2:end-1) = ...
    reshape(trmAnglesValues, length(trmAnglesIndices), size(trmodel.psi, 2) - 2);
end

cost = trmcostp_private(trmodel, p);

end

function cost = trmcostp_private(trmodel, p)
%TRMCOSTP_PRIVATE Calculates cost of the transformation.
%   trmcostlp_private(trmodel, p) returns cost of the transformation.
%   The cost is calculated as a weighted sum of the distances between
%   atoms of adjacent configurations.
%   trmmodel - transformation model
%   p - norm power
%
% Protein Transformation Toolbox for MATLAB

% By Gaik Tamazian, 2012.
% tamaz.g@star.math.spbu.ru

% By Sergey Knyazev, 2012.
% sergey.n.knyazev@gmail.com

coordscell = trmrestorecoords(trmodel);
nModels = length(coordscell);
cost = 0;

for i = 1:(nModels-1)
   cost = cost + ...
       sum(sum((coordscell{i+1} - coordscell{i}).^p, 2) .* trmodel.m);
end

end