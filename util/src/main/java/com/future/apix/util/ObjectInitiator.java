package com.future.apix.util;

public interface ObjectInitiator<T> {
    T initObject();
    void onInitFailed();
}
