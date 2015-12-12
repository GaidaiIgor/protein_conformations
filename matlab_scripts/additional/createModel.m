function [r, kParam, psi] = createModel(model)
nAtoms = length(model.Atom);
r = zeros(nAtoms-1, 1);
kParam = zeros(nAtoms-2, 1);
psi = zeros(nAtoms-3, 1);

deltax = diff([[model.Atom.X]' [model.Atom.Y]' [model.Atom.Z]']);
r(:,1) = sqrt(sum(deltax.^2, 2));
kParam(:,1) = acos(dot(deltax(1:end-1,:), deltax(2:end,:), 2) ./ (r(1:end-1,1) .* r(2:end,1)));
N = cross(deltax(1:end-1,:), deltax(2:end,:), 2);
psi(:,1) = atan2(r(2:end-1,1) .* dot(deltax(1:end-2,:), N(2:end,:), 2), dot(N(1:end-1,:), N(2:end,:), 2));
end