function loopsPdbs = trmsLoops2pdbs(pdb, loopsTrms, ...
    loopsAnglesIntervals)
%TRMSLOOPS2PDBS creates pdb transformation model 
%   for every loop transformation model in loopsTrms set.
%	pdb - original protein pdb
%	loopsTrms - set of loops transformations.
%	loopsAnglesIntervals - loops angles intervals in original
%		protein transformation model.
%
% Protein Transformation Toolbox for MATLAB
%
% By Sergey Knyazev, 2013.
% sergey.n.knyazev@gmail.com
loopsPdbs = repmat(struct('pdbs',[]),length(loopsTrms),1);
for i = 1:length(loopsTrms)
    loopFirstAminoResidueNumber = loopsAnglesIntervals(i,1);
    loopLastAminoResidueNumber = loopsAnglesIntervals(i,2)+3;
    initLoopPdb = [];
    initLoopPdb.Model.Atom = pdb.Model.Atom(...
        loopFirstAminoResidueNumber:loopLastAminoResidueNumber);
    p = repmat(struct('Model',[]),1,length(loopsTrms(i).trms));
    for j = 1:length(loopsTrms(i).trms)
        p(j) = trm2pdbLoop(loopsTrms(i).trms(j),initLoopPdb);
    end
    loopsPdbs(i).pdbs = p;
end
end


