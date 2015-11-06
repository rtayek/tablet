package com.tayek.utilities;
public class Range<T extends Comparable> {
    public Range(T from,T to) {
        this.from=from;
        this.to=to;
    }
    public boolean contains(Comparable<T> value) {
        return from.compareTo(value)<=0&&to.compareTo(value)>=0;
    }
    public static <T extends Comparable> Range<T> create(T from,T to) {
        return new Range<T>(from,to);
    }
    public static void main(String[] args) {
        // TODO Auto-generated method stub
    }
    private T from=null;
    private T to=null;
}
