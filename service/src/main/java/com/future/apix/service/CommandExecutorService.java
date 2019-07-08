package com.future.apix.service;

import com.future.apix.command.Command;

public interface CommandExecutorService {
    <RESPONSE,REQUEST> RESPONSE executeCommand(Class<? extends Command<RESPONSE,REQUEST>> commandClass, REQUEST request);
}
