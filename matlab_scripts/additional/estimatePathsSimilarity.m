function [result, pdb1Index, pdb2Index] = estimatePathsSimilarity(pdbStruct1, pdbStruct2)
alignScore = zeros(length(pdbStruct1.Model), length(pdbStruct2.Model));
for i = 1:length(pdbStruct1.Model)
    for j = 1:length(pdbStruct2.Model)
        alignScore(i, j) = modelRmsd(pdbStruct1.Model(i), pdbStruct2.Model(j));
    end
end
[rowMin, rowMinIndex] = min(alignScore, [], 2);
[result, pdb1Index] = max(rowMin);
pdb2Index = rowMinIndex(pdb1Index);