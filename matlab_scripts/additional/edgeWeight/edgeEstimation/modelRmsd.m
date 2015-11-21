function result = modelRmsd(model1, model2)
aligned = alignModels(model1, model2);
result = rmsd(model1.Atom, aligned.Atom);