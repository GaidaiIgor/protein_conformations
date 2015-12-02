function plotShape()
hold on
grid on
xlim([-5 5]);
ylim([-5 5]);
n = 10;
m = 1.25 * n;
range = .1:.1:m/2;
for r1 = range
    r2 = otherR(r1, n, m);
    [x, y] = circcirc(-n/2, 0, r1, n/2, 0, r2);
    scatter(x(1), y(1), 1, 'b');
    scatter(x(2), y(2), 1, 'b');
end
for r2 = range
    r1 = otherR(r2, n, m);
    [x, y] = circcirc(-n/2, 0, r1, n/2, 0, r2);
    scatter(x(1), y(1), 1, 'b');
    scatter(x(2), y(2), 1, 'b');
end
hold off
end

function b = otherR(a, n, m)
k = 2 * n / m - 1;
b = n - k * a;
end