function model = modelBackbone(model, atomNames)
if nargin < 2
    atomNames = {'N', 'C', 'CA'};
end
model.Atom = model.Atom(ismember({model.Atom.AtomName}, atomNames));
model.HeterogenAtom = [];