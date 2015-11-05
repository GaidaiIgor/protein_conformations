function trmModels = trmGenerateInitialModels(trm, bidirectionAngles,...
    minBidirectionAngleValue)
%TRMGENERATEINITIALMODELS genrates transformation models with different
%   initial transformations.
%   Torseon angles in transformation model denoted in 
%   bidirectionAngles are angles which can be moved as clockwise as
%   anticlockwise. Let D ={d_1, d_2, ..., d_n} be binary vector where
%   d_i denotes movement direction of i-th torseon angle denoted in 
%   bidirectionAngles. So there are 2^bidirectionAngles initial models.
%   Function generates all possible models with all possible
%   combinations of D.
%
%   Example:
%   trm2w8BidirectionAngles = trmGetMostChangeableAngles( trm, [], 2);
%   trmModels = trmGenerateInitialModels(trm2w8, trm2w8BidirectionAngles);
%
% Protein Transformation Toolbox for MATLAB
%
% By Sergey Knyazev, 2012.
% sergey.n.knyazev@gmail.com

if isempty(bidirectionAngles)
    bidirectionAngles = ...
        trmGetMostChangeableAngles(trm,[],minBidirectionAngleValue);
end

trm.psiLongArc = false(length(trm.psi(:,1)),1);
trm.psiDirections = zeros(length(trm.psi(:,1)),1);
trmModels = repmat(trm, 1, pow2(length(bidirectionAngles)));

for i = 1 : pow2(length(bidirectionAngles))
    arcs = bitget(i-1,1:length(bidirectionAngles));
    longArcs = bidirectionAngles(arcs == 1);
    trmModels(i).psi(longArcs,2:end-1) = ...
        interpcirc(trmModels(i).psi(longArcs,1), ...
        trmModels(i).psi(longArcs,end), size(trm.psi,2)-2, true);
    dist = trmGetDist(trm.psi(:,end), trm.psi(:,1));
    
    trmModels(i).psiLongArc(longArcs) = true;
    trmModels(i).psiDirections(dist < 0) = -1;
    trmModels(i).psiDirections(dist > 0) = 1;
    trmModels(i).psiDirections(longArcs)...
        = -trmModels(i).psiDirections(longArcs);
    [x, idx] = ...
        min(abs(trmGetDist(trm.psi(longArcs,end), trm.psi(longArcs,1))));
    trmModels(i).biggestAngle = longArcs(idx);
    if isempty(trmModels(i).biggestAngle)
    [x, trmModels(i).biggestAngle] = ...
        max(abs(trmGetDist(trm.psi(:,end), trm.psi(:,1))));
    end
end
end

function  dist = trmGetDist(endAngles, beginAngles)
    dist = endAngles - beginAngles;
    dist(dist < -pi) = dist(dist < -pi) + 2*pi;
    dist(dist > pi) =  dist(dist > pi) - 2*pi;
end