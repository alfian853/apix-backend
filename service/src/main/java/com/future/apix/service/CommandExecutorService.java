package com.future.apix.service;

import com.future.apix.response.RequestResponse;
import com.future.apix.service.command.Command;

public interface CommandExecutorService {
    <T extends RequestResponse,R> T execute(Class<? extends Command<T,R>> commandClass, R request);
}
