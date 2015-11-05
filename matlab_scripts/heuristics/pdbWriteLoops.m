function pdbWriteLoops(pdbs, loopsAnglesIntervals, path)
%PDBWRITELOOPS Writes pdbs into path folder.
%
% pdbs - vector of pdb structures.
% loopsAnglesIntervals - are used to determine loops recidues intervals.
%	Recidues intervals are used for determining subfolder name.
% path - folder name.
%
% Protein Transformation Toolbox for MATLAB
%
% By Sergey Knyazev, 2012.
% sergey.n.knyazev@gmail.com

for i = 1:length(pdbs)
    loopFirstAminoResidueNumber = ceil(loopsAnglesIntervals(i,1)/3);
    loopLastAminoResidueNumber = ceil(loopsAnglesIntervals(i,2)/3);
    if ~isempty(loopsAnglesIntervals)
        workDir = [path sprintf('/%d-%d', ...
            loopFirstAminoResidueNumber, loopLastAminoResidueNumber)];
    else
        workDir = [path sprintf('/%d',i)];
    end
    mkdir(workDir);
    for j = 1:length(pdbs(i).pdbs)
        fileName = [workDir sprintf('/%d.pdb',j)];
        pdbwrite(fileName, pdbs(i).pdbs(j));
    end
end
end