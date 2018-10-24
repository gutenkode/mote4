package mote4.util;

public class Tuple3<L,M,R> {

    public final L left;
    public final M middle;
    public final R right;

    public Tuple3(L left, M middle, R right) {
        this.left = left;
        this.middle = middle;
        this.right = right;
    }

    @Override
    public int hashCode() { return left.hashCode() ^ middle.hashCode() ^ right.hashCode(); }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Tuple3))
            return false;
        Tuple3 t = (Tuple3)o;
        return left.equals(t.left) && middle.equals(t.middle) && right.equals(t.right);
    }
}
