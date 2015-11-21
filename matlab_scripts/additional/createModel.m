function [r, alpha, psi] = createModel(model)
nAtoms = length(model.Atom);
r = zeros(nAtoms-1, 1);
alpha = zeros(nAtoms-2, 1);
psi = zeros(nAtoms-3, 1);

deltax = diff([[model.Atom.X]' [model.Atom.Y]' [model.Atom.Z]']);
r(:,1) = sqrt(sum(deltax.^2, 2));
alpha(:,1) = acos(dot(deltax(1:end-1,:), deltax(2:end,:), 2) ./ (r(1:end-1,1) .* r(2:end,1)));
N = cross(deltax(1:end-1,:), deltax(2:end,:), 2);
psi(:,1) = atan2(r(2:end-1,1) .* dot(deltax(1:end-2,:), N(2:end,:), 2), dot(N(1:end-1,:), N(2:end,:), 2));
end