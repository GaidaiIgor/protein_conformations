function result = conformationsTransitionScore(edgePdb12, edgePdb23)
model9 = edgePdb12.Model(9);
model10 = edgePdb12.Model(10);
model2 = edgePdb23.Model(2);
result = modelTransitionScore(model9, model10, model2);