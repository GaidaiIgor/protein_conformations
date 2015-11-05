%    References:
%      Douglas L. Theobald (2005)
%      "Rapid calculation of RMSD using a quaternion-based characteristic
%      polynomial."
%      Acta Crystallographica A 61(4):478-480.
%
%      Pu Liu, Dmitris K. Agrafiotis, and Douglas L. Theobald (2009)
%      "Fast determination of the optimal rotational matrix for macromolecular 
%      superpositions."
%      in press, Journal of Computational Chemistry

% Protein Transformation Toolbox for MATLAB

% By Sergey Knyazev, 2012.
% sergey.n.knyazev@gmail.com

%  Function:       Rapid calculation of the least-squares rotation using a 
%                  quaternion-based characteristic polynomial and 
%                  a cofactor matrix

% Superposition coords2 onto coords1 -- in other words, coords2 is rotated, coords1 is held fixed
function [error, rmsd, rot] = CalcRMSDRotationalMatrix(coords1, coords2, weight)
% center the structures -- if precentered you can omit this step
if nargin == 3
    coords1 = CenterCoords(coords1, weight);
    coords2 = CenterCoords(coords2, weight);
else
    coords1 = CenterCoords(coords1);
    coords2 = CenterCoords(coords2);
end
% calculate the (weighted) inner product of two structures
if nargin == 3
    [E0, A] = weightedInnerProduct(coords1, coords2, weight);
else
    [E0, A] = weightedInnerProduct(coords1, coords2);
end

% calculate the RMSD & rotational matrix
    [error, rmsd, rot] = FastCalcRMSDAndRotation(A, E0, size(coords1,1), -1);
end

function coords = CenterCoords(coords, weight)
if nargin == 2
    wsum = sum(weight);
    xsum = sum(coords(:,1).*weight)/wsum;
    ysum = sum(coords(:,2).*weight)/wsum;
    zsum = sum(coords(:,3).*weight)/wsum;
else
    xsum = sum(coords(:,1))/size(coords,1);
    ysum = sum(coords(:,2))/size(coords,1);
    zsum = sum(coords(:,3))/size(coords,1);
end
coords(:,1) = coords(:,1) - xsum;
coords(:,2) = coords(:,2) - ysum;
coords(:,3) = coords(:,3) - zsum;
end

function [E0, A] = weightedInnerProduct(coords1, coords2, weight)
if nargin == 3
    E0 = (trace((coords1.*repmat(weight,1,3)).'*coords1) + ...
        trace((coords2.*repmat(weight,1,3)).'*coords2)) * 0.5;
    A = coords1.*repmat(weight,1,3);
    A = A.'*coords2;
else
    E0 = (trace(coords1.'*coords1) + trace(coords2.'*coords2)) * 0.5;
    A = coords1.'*coords2;
end
end

function [error, rmsd, rot] = FastCalcRMSDAndRotation(A, E0, len, minScore)
evecprec = 1e-6;
evalprec = 1e-11;
C = zeros(3);
rot = zeros(3,3);

Sxx = A(1,1);
Sxy = A(1,2);
Sxz = A(1,3);
Syx = A(2,1);
Syy = A(2,2);
Syz = A(2,3);
Szx = A(3,1);
Szy = A(3,2);
Szz = A(3,3);

Sxx2 = Sxx * Sxx;
Syy2 = Syy * Syy;
Szz2 = Szz * Szz;

Sxy2 = Sxy * Sxy;
Syz2 = Syz * Syz;
Sxz2 = Sxz * Sxz;

Syx2 = Syx * Syx;
Szy2 = Szy * Szy;
Szx2 = Szx * Szx;

SyzSzymSyySzz2 = 2.0*(Syz*Szy - Syy*Szz);
Sxx2Syy2Szz2Syz2Szy2 = Syy2 + Szz2 - Sxx2 + Syz2 + Szy2;

C(3) = -2.0 * (Sxx2 + Syy2 + Szz2 + Sxy2 + Syx2 + Sxz2 + Szx2 + Syz2 + Szy2);
C(2) = 8.0 * (Sxx*Syz*Szy + Syy*Szx*Sxz + Szz*Sxy*Syx - Sxx*Syy*Szz - Syz*Szx*Sxy - Szy*Syx*Sxz);

SxzpSzx = Sxz + Szx;
SyzpSzy = Syz + Szy;
SxypSyx = Sxy + Syx;
SyzmSzy = Syz - Szy;
SxzmSzx = Sxz - Szx;
SxymSyx = Sxy - Syx;
SxxpSyy = Sxx + Syy;
SxxmSyy = Sxx - Syy;
Sxy2Sxz2Syx2Szx2 = Sxy2 + Sxz2 - Syx2 - Szx2;

C(1) = Sxy2Sxz2Syx2Szx2 * Sxy2Sxz2Syx2Szx2...
     + (Sxx2Syy2Szz2Syz2Szy2 + SyzSzymSyySzz2) * (Sxx2Syy2Szz2Syz2Szy2 - SyzSzymSyySzz2)...
     + (-(SxzpSzx)*(SyzmSzy)+(SxymSyx)*(SxxmSyy-Szz)) * (-(SxzmSzx)*(SyzpSzy)+(SxymSyx)*(SxxmSyy+Szz))...
     + (-(SxzpSzx)*(SyzpSzy)-(SxypSyx)*(SxxpSyy-Szz)) * (-(SxzmSzx)*(SyzmSzy)-(SxypSyx)*(SxxpSyy+Szz))...
     + (+(SxypSyx)*(SyzpSzy)+(SxzpSzx)*(SxxmSyy+Szz)) * (-(SxymSyx)*(SyzmSzy)+(SxzpSzx)*(SxxpSyy+Szz))...
     + (+(SxypSyx)*(SyzmSzy)+(SxzmSzx)*(SxxmSyy-Szz)) * (-(SxymSyx)*(SyzpSzy)+(SxzmSzx)*(SxxpSyy-Szz));

mxEigenV = E0;
for i = 0:50,
    oldg = mxEigenV;
    x2 = mxEigenV*mxEigenV;
    b = (x2 + C(3))*mxEigenV;
    a = b + C(2);
    delta = ((a*mxEigenV + C(1))/(2.0*x2*mxEigenV + b + a));
    mxEigenV = mxEigenV - delta;
%        printf('\n diff[%3d]: %16g %16g %16g', i, mxEigenV - oldg, evalprec*mxEigenV, mxEigenV);
    if (abs(mxEigenV - oldg) < abs(evalprec*mxEigenV))
        break;
    end
end

if (i == 50) 
   fprintf(stderr,'\nMore than %d iterations needed!\n', i);
end

% the abs() is to guard against extremely small, but *negative* numbers due to floating point error */
rms = sqrt(abs(2.0 * (E0 - mxEigenV)/len));
rmsd = rms;
%    printf('\n\n %16g %16g %16g \n', rms, E0, 2.0 * (E0 - mxEigenV)/len);

if (minScore > 0) 
    if (rms < minScore)
        error = -1;
        return % Don't bother with rotation.
    end
end

a11 = SxxpSyy + Szz-mxEigenV; a12 = SyzmSzy; a13 = - SxzmSzx; a14 = SxymSyx;
a21 = SyzmSzy; a22 = SxxmSyy - Szz-mxEigenV; a23 = SxypSyx; a24= SxzpSzx;
a31 = a13; a32 = a23; a33 = Syy-Sxx-Szz - mxEigenV; a34 = SyzpSzy;
a41 = a14; a42 = a24; a43 = a34; a44 = Szz - SxxpSyy - mxEigenV;
a3344_4334 = a33 * a44 - a43 * a34; a3244_4234 = a32 * a44-a42*a34;
a3243_4233 = a32 * a43 - a42 * a33; a3143_4133 = a31 * a43-a41*a33;
a3144_4134 = a31 * a44 - a41 * a34; a3142_4132 = a31 * a42-a41*a32;
q1 =  a22*a3344_4334-a23*a3244_4234+a24*a3243_4233;
q2 = -a21*a3344_4334+a23*a3144_4134-a24*a3143_4133;
q3 =  a21*a3244_4234-a22*a3144_4134+a24*a3142_4132;
q4 = -a21*a3243_4233+a22*a3143_4133-a23*a3142_4132;

qsqr = q1 * q1 + q2 * q2 + q3 * q3 + q4 * q4;

% The following code tries to calculate another column in the adjoint matrix when the norm of the 
% current column is too small.
% Usually this commented block will never be activated.  To be absolutely safe this should be
% uncommented, but it is most likely unnecessary.  
if (qsqr < evecprec)
    q1 =  a12*a3344_4334 - a13*a3244_4234 + a14*a3243_4233;
    q2 = -a11*a3344_4334 + a13*a3144_4134 - a14*a3143_4133;
    q3 =  a11*a3244_4234 - a12*a3144_4134 + a14*a3142_4132;
    q4 = -a11*a3243_4233 + a12*a3143_4133 - a13*a3142_4132;
    qsqr = q1*q1 + q2 *q2 + q3*q3+q4*q4;

    if (qsqr < evecprec)
        a1324_1423 = a13 * a24 - a14 * a23;
        a1224_1422 = a12 * a24 - a14 * a22;
        a1223_1322 = a12 * a23 - a13 * a22;
        a1124_1421 = a11 * a24 - a14 * a21;
        a1123_1321 = a11 * a23 - a13 * a21;
        a1122_1221 = a11 * a22 - a12 * a21;

        q1 =  a42 * a1324_1423 - a43 * a1224_1422 + a44 * a1223_1322;
        q2 = -a41 * a1324_1423 + a43 * a1124_1421 - a44 * a1123_1321;
        q3 =  a41 * a1224_1422 - a42 * a1124_1421 + a44 * a1122_1221;
        q4 = -a41 * a1223_1322 + a42 * a1123_1321 - a43 * a1122_1221;
        qsqr = q1*q1 + q2 *q2 + q3*q3+q4*q4;

        if (qsqr < evecprec)
            q1 =  a32 * a1324_1423 - a33 * a1224_1422 + a34 * a1223_1322;
            q2 = -a31 * a1324_1423 + a33 * a1124_1421 - a34 * a1123_1321;
            q3 =  a31 * a1224_1422 - a32 * a1124_1421 + a34 * a1122_1221;
            q4 = -a31 * a1223_1322 + a32 * a1123_1321 - a33 * a1122_1221;
            qsqr = q1*q1 + q2 *q2 + q3*q3 + q4*q4;
                
            if (qsqr < evecprec)
                % if qsqr is still too small, return the identity matrix.
                rot = eye(3);
                error = 0;
                return
            end
        end
    end
end

normq = sqrt(qsqr);
q1 = q1/normq;
q2 = q2/normq;
q3 = q3/normq;
q4 = q4/normq;

a2 = q1 * q1;
x2 = q2 * q2;
y2 = q3 * q3;
z2 = q4 * q4;

xy = q2 * q3;
az = q1 * q4;
zx = q4 * q2;
ay = q1 * q3;
yz = q3 * q4;
ax = q1 * q2;

rot(1,1) = a2 + x2 - y2 - z2;
rot(1,2) = 2 * (xy + az);
rot(1,3) = 2 * (zx - ay);
rot(2,1) = 2 * (xy - az);
rot(2,2) = a2 - x2 + y2 - z2;
rot(2,3) = 2 * (yz + ax);
rot(3,1) = 2 * (zx + ay);
rot(3,2) = 2 * (yz - ax);
rot(3,3) = a2 - x2 - y2 + z2;

error = 1;
end
