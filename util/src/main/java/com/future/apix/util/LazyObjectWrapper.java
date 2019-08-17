package com.future.apix.util;

public class LazyObjectWrapper<T> {

    private T instance;

    private ObjectInitiator<? extends T> initiator;

    public LazyObjectWrapper(ObjectInitiator<? extends T> initiator){
        this.initiator = initiator;
    }

    private synchronized T initObject(){
        return this.initiator.initObject();
    }

    public void reInitObject(){
        this.instance = this.initObject();
    }

    public T get(){
        if(this.instance == null){
            this.instance = this.initObject();
            if(this.instance == null)this.initiator.onInitFailed();
        }

        return this.instance;
    }

}

