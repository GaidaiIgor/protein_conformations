function model = modelBackbone(model)
model.Atom = model.Atom(ismember({model.Atom.AtomName}, {'N' 'C' 'CA'}));
model.HeterogenAtom = [];