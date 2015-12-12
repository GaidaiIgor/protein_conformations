function plotDataPlotyy(data)
[hAxis, hLine1, hLine2] = plotyy(data(:, 1), data(:, 2), data(:, 1), data(:, 3));
xlabel('k');
ylabel(hAxis(1), 'RMSD');
ylabel(hAxis(2), 'Intermediate conformations');
title('1CFC 9-11');
set([hLine1, hLine2], 'Marker', 'o', 'MarkerSize', 8);