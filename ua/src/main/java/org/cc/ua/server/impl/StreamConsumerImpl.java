package org.cc.ua.server.impl;

import org.cc.ua.server.StreamConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.Message;

/**
 * @ClassName: ConsumerImpl
 * @Description: TODO
 * @Author: CC
 * @Date 2021/5/11 17:18
 * @ModifyRecords: v1.0 new
 */
@EnableBinding(Sink.class)
public class StreamConsumerImpl implements StreamConsumer {
    private final Logger log = LoggerFactory.getLogger(StreamConsumerImpl.class);

    @Override
    @StreamListener(Sink.INPUT)
    public void consumerMsg(Message<String> message) {
        log.info("Stream consumer recv: {}", message);
    }
}
