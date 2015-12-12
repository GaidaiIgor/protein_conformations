function rmsd = trmCalcRmsd(trm, i, j)
%TRMCALCRMSD returns RMSD between i and j conformations of trm
%   trm - protein transformation.
%
% By Sergey Knyazev, 2013.
% sergey.n.knyazev@gmail.com
n = size(trm.StartCoords, 1);
coords1 = restorecoords(trm.r(:,i), trm.kParam(:,i), trm.psi(:,i));
coords2 = restorecoords(trm.r(:,j), trm.kParam(:,j), trm.psi(:,j));
[~,~,transform] = procrustes(coords1,coords2, ...
    'scaling', false, 'reflection', false);
    coords2 = coords2 * transform.T + ...
          repmat(transform.c(1,:), n, 1);
rmsd = sqrt(sum(sum((coords2 - coords1).^2,2))/n);
end

