function res = transitionScore(a, b, c)
ab = arrayfun(@(f, s) angleDifference(f, s), a, b);
bc = arrayfun(@(f, s) angleDifference(f, s), b, c);
test = arrayfun(@(f, s) f * s, ab, bc) / norm(ab) / norm(bc);
res = dot(ab,bc) / norm(ab) / norm(bc);