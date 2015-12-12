function [loopsInitTrms trmLoopsAnglesIntervals minAngleDiff loopsAngles] ...
    = trmGetLoops(trm, minAngleDiff, maxAnglesCount, loopVicinity, ...
    loopBorderAtomsMass)
%TRMGETLOOPS extracts loops from transformation model.
%   Returns initial loops transformation models and n x 2 matrix with 
%   angles indices in original transformation model trm.
%   Every row represents loop.
%   1st column represent loop begin, 2nd column represent hnge end.
%   trmgetloops(trm, minAngleDiff, maxAnglesCount, loopVicinity)
%   trm - transformation model
%   minAnglesDiff - minimal angle change in trm to consider angle as part
%   of loop.
%   maxAnglesCount - maximal angles count in model considered as loop
%   angles.
%   loopVicinity - amino acid residues count in vicinity of every loop.
%   Vicinity don't contain angles can be considered as loop angles.
%   Vicinity should be integer greater than 0.
%
%   Example:
%   [loopsTrms trmLoopsAnglesIntervals] = trmgetloops(trm2W8,2,[],2,10^6);
%
% Protein Transformation Toolbox for MATLAB
%
% By Sergey Knyazev, 2012.
% sergey.n.knyazev@gmail.com

diffs = abs(circ_dist(trm.psi(:,end),trm.psi(:,1)));
if isempty(minAngleDiff)
    minAngleDiff = min(diffs);
end
if ~isempty(maxAnglesCount)
    diffsSorted = sort(diffs,'descend');
    minAngleDiff ...
        = max(minAngleDiff, diffsSorted(maxAnglesCount+1));
end
loopsAngles = find(diffs>minAngleDiff);
if isempty(loopsAngles)
    loopsInitTrms = [];
    trmLoopsAnglesIntervals = [];
    minAngleDiff = [];
    loopsAngles = [];
    return
end
loopsParts = [(floor((loopsAngles(:)-1)/3) - loopVicinity)*3 + 1 ...
    (floor((loopsAngles(:)-1)/3) + 1 + loopVicinity)*3];
borders = [0 find(loopsParts(2:length(loopsParts(:,1)),1) - ...
    loopsParts(1:length(loopsParts(:,1))-1,2) > 1)' ...
    length(loopsParts(:,1))];
trmLoopsAnglesIntervals = zeros(length(borders)-1,2);
trmLoopsAnglesIntervals(:) = ...
    [loopsParts(borders(1:length(borders)-1)+1,1) + loopVicinity*3 ...
    loopsParts(borders(2:length(borders)),2) - (loopVicinity-1)*3];
% if protein border is loop, then last amino has no psi.
if trmLoopsAnglesIntervals(end,2) > length(trm.psi(:,1))
    trmLoopsAnglesIntervals(end,2) = trmLoopsAnglesIntervals(end,2) - 3;
end
loopsInitTrms(length(trmLoopsAnglesIntervals(:,1)),1) = trm;
for i = 1:length(loopsInitTrms)
loopsInitTrms(i) = trmGetLoop(trm,trmLoopsAnglesIntervals(i,1), ...
    trmLoopsAnglesIntervals(i,2), loopBorderAtomsMass);
end

end

function loopTrm = trmGetLoop(trm, beginAngle, endAngle, ...
    loopBorderAtomsMass)
%TRMGETHINGE Creates tramsformation model is part of original model from
%   torsion angles #beginAngle to torseon angle #endAngle. If 
%   loopBorderAtomsMass is present function also
%   change masses to loopBorderAtomsMass of 3 border atoms from each side
%   Example:
%   loopTrm = trmGetLoop(trm2w8, 829, 873, 10^6)
%
% Protein Transformation Toolbox for MATLAB
%
% By Sergey Knyazev, 2012.
% sergey.n.knyazev@gmail.com

loopTrm = trm;
loopTrm.m = trm.m(beginAngle:endAngle+3);
loopTrm.StartCoords = trm.StartCoords(beginAngle:endAngle+3,:);
loopTrm.FinishCoords = trm.FinishCoords(beginAngle:endAngle+3,:);
loopTrm.r = trm.r(beginAngle:endAngle+2,:);
loopTrm.kParam = trm.kParam(beginAngle:endAngle+1,:);
loopTrm.psi = trm.psi(beginAngle:endAngle,:);
if ~isempty(loopBorderAtomsMass)
    if beginAngle ~= 1
        loopTrm.m(1:3) = loopBorderAtomsMass;
    end
    if endAngle ~= length(trm.psi(:,1))
        loopTrm.m(length(loopTrm.m)-2:length(loopTrm.m)) = loopBorderAtomsMass;
    end
end
end

