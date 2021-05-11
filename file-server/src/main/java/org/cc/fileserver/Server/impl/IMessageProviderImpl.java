package org.cc.fileserver.Server.impl;

import org.cc.fileserver.Server.IMessageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

/**
 * @ClassName: IMessageProviderImpl
 * @Description: TODO
 * @Author: CC
 * @Date 2021/5/11 17:12
 * @ModifyRecords: v1.0 new
 */
@EnableBinding(Source.class)
public class IMessageProviderImpl implements IMessageProvider {
    private final Logger log = LoggerFactory.getLogger(IMessageProviderImpl.class);
    private final MessageChannel output;

    public IMessageProviderImpl(MessageChannel output) {
        this.output = output;
    }

    @Override
    public void sendTestMsg(String msg) {
        boolean sr = output.send(MessageBuilder.withPayload("from file-server: " + msg).build());
        log.info("provider send message [{}], sr [{}]", msg, sr);
    }
}
