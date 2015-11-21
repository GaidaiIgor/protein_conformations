function result = modelTransitionScore(model1, model2, model3)
model1 = modelBackbone(model1);
model2 = modelBackbone(model2);
model3 = modelBackbone(model3);
[~, ~, model1Torsion] = createModel(model1);
[~, ~, model2Torsion] = createModel(model2);
[~, ~, model3Torsion] = createModel(model3);
result = transitionScore(model1Torsion, model2Torsion, model3Torsion);