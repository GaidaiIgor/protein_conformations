function optimizedLoopsTrms = trmOptimizeLoops( loopsTrms, ...
    minConsideredAngleValueChange, trmFunction, ...
    trmFunctionAdditionalParameters, algorithm, MaxIter, tolx)
%TRMOPTIMIZELOOPS optimizes loops transformation models sets loopsTrms.
%   See trmFmincon for parameters details.
%
% Protein Transformation Toolbox for MATLAB
%
% By Sergey Knyazev, 2012.
% sergey.n.knyazev@gmail.com
%
optimizedLoopsTrms = loopsTrms;
if isempty(minConsideredAngleValueChange)
    minConsideredAngleValueChange = 0;
end
for i = 1:length(loopsTrms)
    for j = 1:length(loopsTrms(i).trms)
        I = setdiff(1:size(loopsTrms(i).trms(j).psi,1), ...
            2:3:size(loopsTrms(i).trms(j).psi,1));
        fprintf('Loop %d/%d in model %d/%d is started\n',...
            i,length(loopsTrms),j,length(loopsTrms(i).trms));
        diffs = abs(circ_dist(loopsTrms(i).trms(j).psi(I,end), ...
            loopsTrms(i).trms(j).psi(I,1)));
        optimizedLoopsTrms(i).trms(j) = ...
            trmFmincon(loopsTrms(i).trms(j), ...
            I(diffs>=minConsideredAngleValueChange), trmFunction, ...
            trmFunctionAdditionalParameters, algorithm, MaxIter, tolx);
    end
end
end

