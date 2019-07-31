package com.future.apix.service.impl;

import com.future.apix.service.CommandExecutorService;
import com.future.apix.command.Command;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class CommandExecutorImpl implements CommandExecutorService, ApplicationContextAware {

    private ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Override
    public <RESPONSE, REQUEST> RESPONSE executeCommand(Class<? extends Command<RESPONSE, REQUEST>> commandClass,
        REQUEST request) {
        return this.context.getBean(commandClass).execute(request);
    }
}
