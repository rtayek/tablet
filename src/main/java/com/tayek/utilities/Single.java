package com.tayek.utilities;
public class Single<First> {
    public Single() {
        this(null);
    }
    public Single(First first) {
        this.first=first;
    }
    @Override public String toString() {
        return "["+first+"]";
    }
    @Override public int hashCode() {
        final int prime=31;
        int result=1;
        result=prime*result+((first==null)?0:first.hashCode());
        return result;
    }
    @Override public boolean equals(Object obj) {
        if(this==obj) return true;
        if(obj==null) return false;
        if(getClass()!=obj.getClass()) return false;
        Single other=(Single)obj;
        if(first==null) {
            if(other.first!=null) return false;
        } else if(!first.equals(other.first)) return false;
        return true;
    }
    public First first;
}
