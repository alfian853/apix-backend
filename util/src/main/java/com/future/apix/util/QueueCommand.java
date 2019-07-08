package com.future.apix.util;

public interface QueueCommand<RESPONSE, REQUEST> {
    //please apply "synchronized" to the implementation method
    RESPONSE execute(REQUEST request);
}
