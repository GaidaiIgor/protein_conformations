function loopsInitTrms = trmGenerateLoopsInitialModels( loopsTrms, ...
    minBidirectionAngleValue)
%TRMGENERATELOOPSINITIALMODELS Generates sets of loops initial models ...
%   for futere optimization. See trmGenerateInitialModels for details.
%
% Protein Transformation Toolbox for MATLAB
%
% By Sergey Knyazev, 2012.
% sergey.n.knyazev@gmail.com
%
loopsInitTrms = repmat(struct('trms',[]),length(loopsTrms),1);
for i = 1:length(loopsTrms)
    loopsInitTrms(i).trms = trmGenerateInitialModels(loopsTrms(i),...
        [], minBidirectionAngleValue);
end
end

