function writeGraphForPdb(pdbPath, outputPath, edgeEstimator)
pdb = pdbbackbone(pdbread(pdbPath));
totalEdges = length(pdb.Model) * (length(pdb.Model) - 1) / 2;
names = cell(1, totalEdges);
weights = zeros(1, totalEdges);
currentIndex = 1;
for i = 1:length(pdb.Model)
    for j = i+1:length(pdb.Model)
        pdbCopy = pdb;
        pdbCopy.Model = [pdb.Model(i), pdb.Model(j)];
        names(currentIndex) = cellstr(strjoin({pdb.Header.idCode, num2str(i), num2str(j), '1.pdb'}, '_'));
        weights(currentIndex) = edgeEstimator(pdbCopy);
        currentIndex = currentIndex + 1;
    end
end
writeEdges(names, weights, outputPath);