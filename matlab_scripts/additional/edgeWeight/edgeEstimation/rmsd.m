function result = rmsd(atoms1, atoms2)
if length(atoms1) ~= length(atoms2)
    result = -1;
    return;
end
sum = 0;
for i = 1:length(atoms1)
    sum = sum + (atoms1(i).X - atoms2(i).X) ^ 2;
    sum = sum + (atoms1(i).Y - atoms2(i).Y) ^ 2;
    sum = sum + (atoms1(i).Z - atoms2(i).Z) ^ 2;
end
sum = sum / length(atoms1);
result = sqrt(sum);