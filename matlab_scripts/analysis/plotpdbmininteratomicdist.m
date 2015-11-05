function h = plotpdbmininteratomicdist(PDBStruct, showInterpolated)
%PLOTPDBMININTERATOMICDIST Plot the minimal interatomic distances.
%   plotpdbmininteratomicdist(PDBStruct, showInterpolated) produces a plot
%   of minimal interatomic distances with each configuration of the
%   transformation specified in PDBStruct. showInterpolated indicates if 
%   distances for the transformation derived by circular interpolation of 
%   torsion angles between the fisrt and the last configurations are
%   plotted. Default value of showInterpolated is false.
%
%   If showInterpolated value is true, then minimal interatomic distances
%   for the interpolated transformation are plotted afterwards the
%   distances for the given transformation.
%
% Protein Transformation Toolbox for MATLAB

% By Gaik Tamazian, 2013.
% gaik.tamazian@gmail.com

if nargin < 2
    showInterpolated = false;
end

if showInterpolated
    % produce the same plot but for the transformation derived from the
    % given one by torsion angle interpolation
    x1 = PDBStruct; x1.Model = PDBStruct.Model(1);
    x2 = PDBStruct; x2.Model = PDBStruct.Model(end);
    t = trmcreate(x1, x2, length(PDBStruct.Model) - 2, 1);
    h = plot([pdbmininteratomicdist(PDBStruct)', ...
            pdbmininteratomicdist(trm2pdb(t, x1))', ], '-s');
else
    h = plot(pdbmininteratomicdist(PDBStruct), '-s');
end

xlabel('Configuration Number');
ylabel('Minimal Interatomic Distance (in Angstroms)');

end

