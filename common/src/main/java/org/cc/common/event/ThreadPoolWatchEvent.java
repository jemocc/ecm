package org.cc.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ThreadPoolWatchEvent extends ApplicationEvent {
    private final Integer opt; //0-关闭，1-开启

    public ThreadPoolWatchEvent(Object source, Integer opt) {
        super(source);
        this.opt = opt;
    }
}
