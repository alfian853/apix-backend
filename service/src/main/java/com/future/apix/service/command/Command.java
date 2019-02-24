package com.future.apix.service.command;

import com.future.apix.response.RequestResponse;

public interface Command<RESPONSE extends RequestResponse, REQUEST> {
    RESPONSE executeCommand(REQUEST request);
}
