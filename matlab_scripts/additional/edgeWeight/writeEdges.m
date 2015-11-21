function writeEdges(nameList, weightList, outPath)
assert(length(nameList) == length(weightList), 'Array lengths dont match')
outFile = fopen(outPath, 'wt');
fprintf(outFile, 'modelFileName,cost\n');
for i = 1:length(nameList)
    fprintf(outFile, '%s,%f\n', char(nameList(i)), weightList(i));
end
fclose(outFile);