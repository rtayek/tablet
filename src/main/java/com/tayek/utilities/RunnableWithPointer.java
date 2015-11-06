package com.tayek.utilities;
abstract public class RunnableWithPointer<T>implements Runnable {
    public RunnableWithPointer(Single<T> pointer) {
        super();
        this.pointer=pointer;
    }
    final Single<T> pointer;
}
// another choice would be to have this guy constructed with a callback
// the above is not quite right as we have no way to access the pointer
abstract class RunnableWithCallBack implements Runnable {
    interface CallBack {
        void call(Object object);
    }
    public RunnableWithCallBack(CallBack callBack) {
        super();
        this.callBack=callBack;
    }
    final CallBack callBack;
}
