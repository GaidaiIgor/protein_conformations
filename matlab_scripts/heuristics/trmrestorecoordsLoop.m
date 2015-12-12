function coordscell = trmrestorecoordsLoop(trmodel)
%TRMRESTORECOORDSLOOP Restores Cartesian coordinates of loop transformation atoms.
%   trmrestorecoordsLoop(trmodel) returns a cell array of matrices that contain
%   Cartesian coordinates of the atoms that constitute the transformation
%   configurations.
%
% Protein Transformation Toolbox for MATLAB
%
% By Gaik Tamazian, 2012.
% tamaz.g@star.math.spbu.ru
%
% Sergey Knyazev, 2013.
% sergey.n.knyazev@gmail.com

m = size(trmodel.psi, 2);
n = size(trmodel.StartCoords, 1);
coordscell = cell(1, m);
coordscell{1} = trmodel.StartCoords;

for i = 2:m
    coordscell{i} = restorecoords(trmodel.r(:,i), ...
        trmodel.kParam(:,i), trmodel.psi(:,i));
    
    if trmodel.type == 1
      [~,~,transform] = procrustes(coordscell{i-1}([1:3 end-2:end],:), ...
             coordscell{i}([1:3 end-2:end],:), ...
            'scaling', false, 'reflection', false);      
      coordscell{i} = coordscell{i} * transform.T + ...
          repmat(transform.c(1,:), n, 1);
    else
        coordscell{i} = coordscell{i}*trmodel.U{i} + ...
            trmodel.t{i};        
    end
end

end