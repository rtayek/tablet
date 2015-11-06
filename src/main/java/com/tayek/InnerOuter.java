package com.tayek;
class Outer {
    class Inner {
        void inner() {
            System.out.println("inner");
            outer();
            System.out.println(outerX);
        }
    int innerX=1;
    }
    void outer() {
        System.out.println("outer");
    }
    int outerX=2;
}
public class InnerOuter {
    public static void main(String[] args) {
        Outer outer=new Outer();
        Outer.Inner inner=outer.new Inner();
    }
}
