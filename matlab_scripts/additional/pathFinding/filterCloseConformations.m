function result = filterCloseConformations(activeIndexes, allConformations, referencePath, maxDeviation)
result = activeIndexes;
for index = cell2mat(activeIndexes.keys)
    conformation = allConformations(index);
    if conformationVicinity(conformation, referencePath) > maxDeviation
        remove(result, index);
    end
end