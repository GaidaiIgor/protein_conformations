function appendEdge(edgePdb, summaryPdb, isReverse)
if isReverse
    range = length(edgePdb.Model):-1:1;
else
    range = 1:length(edgePdb.Model);
end
lastModel = summaryPdb.Model(end);
lastModelAtoms = getModelAtomMatrix(lastModel);
for i = range
    nextModel = edgePdb.Model(i);
    nextModelAtoms = getModelAtomMatrix(nextModel);
    [~, alignedNextModelAtoms] = procrustes(lastModelAtoms, nextModelAtoms, 'scaling', false, 'reflection', false);
    alignedX = num2cell(alignedNextModelAtoms(1, :));
    alignedY = num2cell(alignedNextModelAtoms(2, :));
    alignedZ = num2cell(alignedNextModelAtoms(3, :));
    [nextModel.Atom.X] = alignedX{:};
    [nextModel.Atom.Y] = alignedY{:};
    [nextModel.Atom.Z] = alignedZ{:};
    summaryPdb.Model(end + 1) = nextModel;
    lastModelAtoms = alignedNextModelAtoms;
end