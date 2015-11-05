function h = plotpdbanglediff(PDBStruct, isSorted, useDegrees)
%PLOTPDBANGLEDIFF Produce a plot of differences between torsion angles.
%   plotpdbanglediff(PDBStruct, isSorted, useDegrees) plots circular 
%   differences between torsion angles of configurations that define the 
%   transformation speficied in PDBStruct (that is, the first and the last 
%   configurations) is produced. If isSorted is true, then angles on the 
%   plot are sorted decreasingly according to corresponding circular 
%   difference values. If useDegrees is true, then circular differences are
%   plotted in degrees, otherwise in radians. By default, isSorted is set 
%   to true and useDegrees is set to false. 
%
% Protein Transformation Toolbox for MATLAB

% By Gaik Tamazian, 2013.
% gaik.tamazian@gmail.com

if nargin < 3
    useDegrees = false;
end

if nargin < 2
    isSorted = true;
end

[~, ~, psi] = createmodel(PDBStruct);
valuesToPlot = abs(circ_dist(psi(:, 1), psi(:, end)));
if useDegrees
    valuesToPlot = valuesToPlot/pi*180;
end
if isSorted
    valuesToPlot = sort(valuesToPlot, 'descend');
end

h = plot(valuesToPlot);
if isSorted
    xlabel('Torsion Angle Difference Rank')
else
    xlabel('Torsion Angle Number')
end

if useDegrees
    ylabel('Absolute Value of Circular Difference (in Degrees)');
else
    ylabel('Absolute Value of Circular Difference');
end

end

