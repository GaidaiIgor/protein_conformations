function h = plotpdbrmsd(PDBStruct, showInterpolated, ...
    showFirstLastConfigRMSD)
%PLOTPDBRMSD Produce a plot of RMSD between adjacent configurations.
%   plotpdbrmsd(PDBStruct, showInterpolated, showFirstLastConfigRMSD) plots
%   root-mean-square deviations between adjacent configurations of the
%   transformation specified in PDBStruct. showInterpolated indicates if
%   the same plot for the transformation derived from torsion angle
%   interpolation is produced. showFirstLastConfigRMSD indicates if the
%   line corresponding to RMSD between the first and the last configuations
%   is shown. Default values of showInterpolated and
%   showFirstLastConfigRMSD are true.
%
%   If showInterpolated value is true, then RMSDs for the interpolated
%   transformation is plotted after RMSDs for the given transformation.
%
% Protein Transformation Toolbox for MATLAB

% By Gaik Tamazian, 2013.
% gaik.tamazian@gmail.com

if nargin < 3
    showFirstLastConfigRMSD = true;
end

if nargin < 2
    showInterpolated = true;
end

coords = pdbextractcoords(PDBStruct);

% get coordinates of the interpolated transformation
x1 = PDBStruct; x1.Model = PDBStruct.Model(1);
x2 = PDBStruct; x2.Model = PDBStruct.Model(end);
t = trmcreate(x1, x2, 28, 1);
interp_coords = pdbextractcoords(trm2pdb(t, x1));

% calculate RMSDs
rmsd = zeros(1, length(coords));
interp_rmsd = zeros(1, length(coords));
for i = 2:length(coords)
    rmsd(i) = mean(sqrt(sum((coords{i} - coords{i-1}).^2, 2)));
    interp_rmsd(i) = mean(sqrt(sum((interp_coords{i} - ...
        interp_coords{i-1}).^2, 2)));
end

if showInterpolated
    plot([rmsd', interp_rmsd'], '-s');
else
    plot(rmsd, '-s');
end

if showFirstLastConfigRMSD
    hold on;
    plot(ones(1, length(coords)) * mean(sqrt(sum((coords{1} - ...
        coords{end}).^2, 2))), '--', 'LineWidth', 2, 'Color', 'black');
    hold off;
end

xlabel('Configuration Number');
ylabel('Root-Mean-Square Deviation');

end