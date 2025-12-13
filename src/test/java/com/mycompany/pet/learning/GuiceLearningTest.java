package com.mycompany.pet.learning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * Learning tests for Google Guice Dependency Injection framework.
 * 
 * These tests help us understand how Guice works before applying it to the main project.
 * Based on concepts from "Test-Driven Development, Build Automation, Continuous Integration" book.
 */
public class GuiceLearningTest {

    // Simple interface for testing
    interface MessageService {
        String getMessage();
    }

    // Implementation
    static class EmailService implements MessageService {
        @Override
        public String getMessage() {
            return "Email message";
        }
    }

    // Another implementation
    static class SmsService implements MessageService {
        @Override
        public String getMessage() {
            return "SMS message";
        }
    }

    // Class that needs injection
    static class MessageClient {
        private final MessageService messageService;

        @Inject
        public MessageClient(MessageService messageService) {
            this.messageService = messageService;
        }

        public String sendMessage() {
            return messageService.getMessage();
        }
    }

    // Test basic binding
    @Test
    public void testBasicBinding() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MessageService.class).to(EmailService.class);
            }
        });

        MessageService service = injector.getInstance(MessageService.class);
        assertNotNull(service);
        assertEquals("Email message", service.getMessage());
    }

    // Test named bindings
    @Test
    public void testNamedBinding() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MessageService.class).annotatedWith(Names.named("email"))
                    .to(EmailService.class);
                bind(MessageService.class).annotatedWith(Names.named("sms"))
                    .to(SmsService.class);
            }
        });

        MessageService emailService = injector.getInstance(
            com.google.inject.Key.get(MessageService.class, Names.named("email")));
        MessageService smsService = injector.getInstance(
            com.google.inject.Key.get(MessageService.class, Names.named("sms")));

        assertEquals("Email message", emailService.getMessage());
        assertEquals("SMS message", smsService.getMessage());
    }

    // Test constructor injection
    @Test
    public void testConstructorInjection() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MessageService.class).to(EmailService.class);
            }
        });

        MessageClient client = injector.getInstance(MessageClient.class);
        assertNotNull(client);
        assertEquals("Email message", client.sendMessage());
    }

    // Test singleton
    @Test
    public void testSingleton() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MessageService.class).to(EmailService.class).asEagerSingleton();
            }
        });

        MessageService service1 = injector.getInstance(MessageService.class);
        MessageService service2 = injector.getInstance(MessageService.class);
        assertSame(service1, service2);
    }

    // Test @Provides method
    static class ProvidesModule extends AbstractModule {
        @Override
        protected void configure() {
            // Can configure other bindings here
        }

        @Provides
        MessageService provideMessageService() {
            return new EmailService();
        }
    }

    @Test
    public void testProvidesMethod() {
        Injector injector = Guice.createInjector(new ProvidesModule());

        MessageService service = injector.getInstance(MessageService.class);
        assertNotNull(service);
        assertEquals("Email message", service.getMessage());
    }

    // Test instance binding
    @Test
    public void testInstanceBinding() {
        MessageService instance = new EmailService();
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MessageService.class).toInstance(instance);
            }
        });

        MessageService service = injector.getInstance(MessageService.class);
        assertSame(instance, service);
    }

    // Test field injection
    static class FieldInjectionClient {
        @Inject
        private MessageService messageService;

        public String sendMessage() {
            return messageService.getMessage();
        }
    }

    @Test
    public void testFieldInjection() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MessageService.class).to(EmailService.class);
            }
        });

        FieldInjectionClient client = injector.getInstance(FieldInjectionClient.class);
        assertNotNull(client);
        assertEquals("Email message", client.sendMessage());
    }

    // Test method injection
    static class MethodInjectionClient {
        private MessageService messageService;

        @Inject
        public void setMessageService(MessageService messageService) {
            this.messageService = messageService;
        }

        public String sendMessage() {
            return messageService.getMessage();
        }
    }

    @Test
    public void testMethodInjection() {
        Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(MessageService.class).to(EmailService.class);
            }
        });

        MethodInjectionClient client = injector.getInstance(MethodInjectionClient.class);
        assertNotNull(client);
        assertEquals("Email message", client.sendMessage());
    }
}

