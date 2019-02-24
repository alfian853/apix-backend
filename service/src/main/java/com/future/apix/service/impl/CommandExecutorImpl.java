package com.future.apix.service.impl;

import com.future.apix.service.command.Command;
import com.future.apix.service.CommandExecutorService;
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
    public <T, R> T execute(Class<? extends Command<T, R>> commandClass, R request) {
        return this.context.getBean(commandClass).executeCommand(request);
    }
}
