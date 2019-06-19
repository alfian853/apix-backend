package com.future.apix.service.command;

public interface Command<RESPONSE, REQUEST> {
    RESPONSE executeCommand(REQUEST request);
}
