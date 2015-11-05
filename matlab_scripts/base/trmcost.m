function cost = trmcost(trmodel)
%TRMCOST Calculates cost of the transformation.
%   trmcost(trmodel) returns cost of the transformation. The cost is 
%   calculated as a weighted sum of the distances between atoms of 
%   adjacent configurations.
%
% Protein Transformation Toolbox for MATLAB

% By Gaik Tamazian, 2012.
% tamaz.g@star.math.spbu.ru

coordscell = trmrestorecoords(trmodel);
nModels = length(coordscell);
cost = 0;

for i = 1:(nModels-1)
   cost = cost + ...
       sum(sum((coordscell{i+1} - coordscell{i}).^2, 2) .* trmodel.m);
end

end

