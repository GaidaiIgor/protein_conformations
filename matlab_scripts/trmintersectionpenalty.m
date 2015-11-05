function penalty = trmintersectionpenalty(trmodel, L, A, B)
%TRMINTERSECTIONPENALTY The transformation penalty for atom intersections.
%   trmintersectionpenalty(trmmodel, L, A, B) calculates the penalty for 
%   transformation atoms that belong to the same conformation and are
%   situated too close to each other. L is the least acceptable distance
%   between the atoms. A is the positive number that specifies
%   the penalty value. B is the parameter of smaxmin function.
%
% Protein Transformation Toolbox for MATLAB

% By Gaik Tamazian, 2012.
% tamaz.g@star.math.spbu.ru

coords = trmrestorecoords(trmodel);
mins = zeros(1, length(coords));

for i = 1:length(coords)
    mins(i) = smaxmin(pdist(coords{i}), B);
end

penalty = exp(A*(L - smaxmin(mins, B))) - 1;

end
