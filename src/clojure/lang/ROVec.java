package clojure.lang;

public class ROVec implements IPersistentList, IReduce, Counted, Indexed, ISeq {
    public static final byte[] empty_arr = new byte[0];
    public static final ROVec EMPTY = new ROVec(empty_arr);
    public final byte[] arr;
    public final int start;
    public final int end;
    public final int length;

    public ROVec(byte[] arr) {
        this(arr, 0);
    }

    public ROVec(byte[] arr, int start) {
        this(arr, start, arr.length);
    }

    public ROVec(byte[] arr, int start, int end) {
        this.arr = arr;
        this.start = start;
        this.end = end;
        this.length = end - start;
        if (start < 0 || start > arr.length || end > arr.length || start > end) {
            throw new IndexOutOfBoundsException(this.toString());
        }
    }

    public ROVec(ROVec v, int start) {
        this(v, start, v.length);
    }

    public ROVec(ROVec v, int start, int end) {
        this.arr = v.arr;
        this.start = v.start + start;
        this.end = v.start + end;
        this.length = end - start;
        if (start < 0 || start > v.length || end > v.length || start > end) {
            throw new IndexOutOfBoundsException(this.toString());
        }
    }

    @Override
    public Object nth(int i) {
        if (! (i >= 0 && i < this.length)) {
            throw new IndexOutOfBoundsException(this.toString());
        }
        return this.arr[this.start + i];
    }

    @Override
    public Object nth(int i, Object o) {
        if (! (i >= 0 && i < this.length)) {
            return o;
        }
        return this.arr[this.start + i];
    }

    @Override
    public int count() {
        return this.length;
    }

    @Override
    public Object first() {
        return this.nth(0, null);
    }

    @Override
    public ISeq next() {
        if (this.length <= 1) {
            return null;
        }
        return new ROVec(this, 1);
    }

    @Override
    public ISeq more() {
        return this.next();
    }

    @Override
    public ISeq cons(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object peek() {
        return this.nth(0);
    }

    @Override
    public IPersistentStack pop() {
        return new ROVec(this, 1);
    }

    @Override
    public ISeq empty() {
        return EMPTY;
    }

    @Override
    public boolean equiv(Object o) {
        return false;
    }

    public ISeq seq() {
        if (this.length == 0) {
            return null;
        } else {
            return this;
        }
    }

    public String toString() {
        return String.format("clojure.lang.ROVec([B(%d), %d, %d)", this.arr.length, this.start, this.end);
    }

    @Override
    public Object reduce(IFn iFn) {
        var s = this.nth(0);
        for (int i=1; i<this.length; i++) {
            s = iFn.invoke(s, this.nth(i));
        }
        return s;
    }

    @Override
    public Object reduce(IFn iFn, Object o) {
        var s = o;
        for (int i=0; i<this.length; i++) {
            s = iFn.invoke(s, this.nth(i));
        }
        return s;
    }

    public static void main(String[] args) {
        byte[] bs = {};
        var a = new ROVec(bs);
//        System.out.println((a));
    }
}
