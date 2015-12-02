function res = transitionScore(torsion1, torsion2, torsion3, precision)
if nargin == 3
    precision = 1000;
end
arrayFunc = @(f, s) round(angleDifference(f, s) * precision) / precision;
torsion12 = arrayfun(arrayFunc, torsion1, torsion2);
torsion23 = arrayfun(arrayFunc, torsion2, torsion3);
% res = dot(ab,bc) / norm(ab) / norm(bc);
% res = sum(abs(torsion12 - torsion23));
res = sum(arrayfun(@(f, s) sign(f) ~= 0 && sign(s) ~= 0 && sign(f) ~= sign(s), torsion12, torsion23)) ./ length(torsion1);