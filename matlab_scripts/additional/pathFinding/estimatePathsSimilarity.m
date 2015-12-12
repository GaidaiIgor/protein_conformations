function [result, pdb1Index, pdb2Index] = estimatePathsSimilarity(alignMatrix)
[rowMin, rowMinIndex] = min(alignMatrix, [], 2);
[result, pdb1Index] = max(rowMin);
pdb2Index = rowMinIndex(pdb1Index);