function rmsds = pdbGetTrmRmsd( trmPdb )
%PDBGETTRMRMSD returns RMSD between neighbouring conformations of trmPdb
%   trmPdb - PDB with transformation
%
% By Sergey Knyazev, 2013.
% sergey.n.knyazev@gmail.com

n = length(trmPdb.Model)-1;
rmsds = zeros(1,n);
for i = 1:n
    pdb1 = trmPdb;
    pdb1.Model = trmPdb.Model(i);
    pdb2 = trmPdb;
    pdb2.Model = trmPdb.Model(i+1);
    rmsds(i) = calcRmsd(pdb1, pdb2);
end

end

function rmsd = calcRmsd(pdb1, pdb2)
rmsd = sqrt(sum(sum((atomiccoords(pdb1) - atomiccoords(pdb2)).^2,2))/...
    length(pdb1.Model.Atom));
end

