function cost = edgeRmsd(edgePdb)
first = edgePdb.Model(1);
last = edgePdb.Model(end);
cost = modelRmsd(first, last);