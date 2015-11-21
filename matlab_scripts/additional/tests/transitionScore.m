function res = transitionScore(torsion1, torsion2, torsion3)
torsion12 = arrayfun(@(f, s) angleDifference(f, s), torsion1, torsion2);
torsion23 = arrayfun(@(f, s) angleDifference(f, s), torsion2, torsion3);
% res = dot(ab,bc) / norm(ab) / norm(bc);
res = sum(abs(torsion12 - torsion23));