function result = edgeTransitionsScores(edgePdb)
torsion = torsionAngles(edgePdb);
result = arrayfun(@(i) transitionScore(torsion(:, i), torsion(:, i + 1), torsion(:, i + 2)), 1:size(torsion, 2) - 2);