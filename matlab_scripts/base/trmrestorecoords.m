function coordscell = trmrestorecoords(trmodel)
%TRMRESTORECOORDS Restores Cartesian coordinates of transformation atoms.
%   trmrestorecoords(trmodel) returns a cell array of matrices that contain
%   Cartesian coordinates of the atoms that constitute the transformation
%   configurations.
%
% Protein Transformation Toolbox for MATLAB

% By Gaik Tamazian, 2012.
% tamaz.g@star.math.spbu.ru

m = size(trmodel.psi, 2);
n = size(trmodel.StartCoords, 1);
coordscell = cell(1, m);
coordscell{1} = trmodel.StartCoords;

for i = 2:m
    coordscell{i} = restorecoords(trmodel.r(:,i), ...
        trmodel.alpha(:,i), trmodel.psi(:,i));
    
    if trmodel.type == 1
      [~,~,transform] = procrustes(coordscell{i-1}(2:3:end,:), ...
             coordscell{i}(2:3:end,:), ...
            'scaling', false, 'reflection', false);      
      coordscell{i} = coordscell{i} * transform.T + ...
          repmat(transform.c(1,:), n, 1);
    else
        coordscell{i} = coordscell{i}*trmodel.U{i} + ...
            trmodel.t{i};        
    end
end

end