function result = testTriangleInequality(pdb)
result = true;
n = length(pdb.Model);
for i = 1:n
    for j = i+1:n
        for k = j+1:n
            m1 = pdb.Model(i);
            m2 = pdb.Model(j);
            m3 = pdb.Model(k);
            r12 = modelRmsd(m1, m2);
            r13 = modelRmsd(m1, m3);
            r23 = modelRmsd(m2, m3);
            if r12+r13<r23 || r12+r23<r13 || r13+r23<r12
                result = [i, j, k];
                return;
            end
        end
    end
end