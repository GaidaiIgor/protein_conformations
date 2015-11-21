function cost = edgeRmsdFull(edgePdb)
cost = 0;
currentModel = edgePdb.Model(1);
for i = 2:length(edgePdb.Model)
    nextModel = edgePdb.Model(i);
    cost = cost + modelRmsd(currentModel, nextModel);
    currentModel = nextModel;
end