package com.knoldus.impl;

import com.google.inject.AbstractModule;
import com.knoldus.api.NotificationService;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;

public class NotificationModule extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindServices(serviceBinding(NotificationService.class, NotificationImpl.class));
    }
}
