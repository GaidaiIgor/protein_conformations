package core;

public final class Edge {
    public final Node first;
    public final Node second;
    public final double weight;

    public Edge(Node first, Node second, double weight) {
        this.first = first;
        this.second = second;
        this.weight = weight;
    }

    public Node get_second() {
        return second;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (first == null ? 0 : first.hashCode());
        result = 31 * result + second.hashCode();
        result = 31 * result + double_hash_code(weight);
        return result;
    }

    private static int double_hash_code(double value) {
        long weight_hash = Double.doubleToLongBits(value);
        return (int) (weight_hash ^ (weight_hash >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Edge)) {
            return false;
        }
        Edge other = (Edge) obj;
        return first == other.first && second == other.second && weight == other.weight;
    }

    @Override
    public String toString() {
        if (first == null) {
            return "void -> " + second.id;
        }
        return first.id + " -> " + second.id;
    }
}