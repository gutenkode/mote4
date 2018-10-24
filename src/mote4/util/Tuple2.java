package mote4.util;

public class Tuple2<L,R> {

    public final L left;
    public final R right;

    public Tuple2(L left, R right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public int hashCode() { return left.hashCode() ^ right.hashCode(); }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Tuple2))
            return false;
        Tuple2 t = (Tuple2)o;
        return left.equals(t.left) && right.equals(t.right);
    }
}
