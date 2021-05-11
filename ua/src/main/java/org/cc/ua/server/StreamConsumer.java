package org.cc.ua.server;

import org.springframework.messaging.Message;

public interface StreamConsumer {

    void consumerMsg(Message<String> message);
}
