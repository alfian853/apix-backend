package com.future.apix.command;

public interface Command<RESPONSE, REQUEST> {
    RESPONSE execute(REQUEST request);
}
