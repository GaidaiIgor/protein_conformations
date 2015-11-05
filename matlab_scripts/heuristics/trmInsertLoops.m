function trm = trmInsertLoops(trm, loopsTrms, loopsTrmsAngles, ...
    loopsIndices)
%TRMINSERTLOOPS insert loops transformation into protein
% transformation.
%   trm - protein transformation.
%	loopsTrms - vector of loops transformations.
%	loopsTrmsAngles - loops torsion angles in protein
%		transformation.
%	loopsIndices - indices of inserted loops in loopsTrms vector.
%
% By Sergey Knyazev, 2013.
% sergey.n.knyazev@gmail.com
for i = 1:length(loopsTrms)
    trm.psi(loopsTrmsAngles(i,1):loopsTrmsAngles(i,2),:) = ...
        loopsTrms(i).trms(loopsIndices(i)).psi;
end
end

