function trm = trmOptimize(trm, anglesCount, trmFunction, ...
    trmFunctionAdditionalParameters, algorithm, MaxIter,tolx)
%TRMOPTIMIZE  Minimizes the cost of toransformation model function.
%   trm = trmOptimize(trm, anglesCount, trmFunction, ...
%   trmFunctionAdditionalParameters, algorithm, MaxIter,tolx)
%   First N most changeble angles takes part during optimization.
%   N angles count for optimization are specified in anglesCount.
%   See trmFmincon for other parameters.
% Protein Transformation Toolbox for MATLAB
%
% By Sergey Knyazev, 2012.
% sergey.n.knyazev@gmail.com

anglesIndices = trmGetMostChangebleAngles(trm, anglesCount);
trm = trmFmincon(trm, anglesIndices , trmFunction, ...
    trmFunctionAdditionalParameters, algorithm, MaxIter,tolx);

end

function anglesIndices = trmGetMostChangebleAngles(trm, anglesCount)
    I = setdiff(1:size(trm.psi, 1), 2:3:size(trm.psi, 1));
    if isempty(anglesCount)
         anglesIndices = I;
    else
        [~, idx] = sort(circ_dist(trm.psi(I,1),trm.psi(I,end)),'descend');
        anglesIndices = sort(I(idx(1:anglesCount)));
    end
end