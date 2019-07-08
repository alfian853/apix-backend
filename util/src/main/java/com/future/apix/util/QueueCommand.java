package com.future.apix.util;

public interface QueueCommand<T> {
    //please apply "synchronized" to the implementation method
    T execute();
}
