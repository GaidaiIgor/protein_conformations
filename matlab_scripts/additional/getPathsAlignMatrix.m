function alignMatrix = getPathsAlignMatrix(pdbStruct1, pdbStruct2)
pdbStruct1 = pdbBackbone(pdbStruct1, {'CA'});
pdbStruct2 = pdbBackbone(pdbStruct2, {'CA'});
alignMatrix = zeros(length(pdbStruct1.Model), length(pdbStruct2.Model));
for i = 1:length(pdbStruct1.Model)
    for j = 1:length(pdbStruct2.Model)
        alignMatrix(i, j) = modelRmsd(pdbStruct1.Model(i), pdbStruct2.Model(j));
    end
end