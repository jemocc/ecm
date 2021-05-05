package org.cc.common.model;

import lombok.Getter;
import lombok.Setter;
import org.cc.common.pojo.EventMessageType;
import org.cc.common.utils.JsonUtil;

import java.io.Serializable;

@Getter
@Setter
public class EventMessage<T> implements Serializable {

    private EventMessageType wsMessageType;

    private T data;

    public EventMessage() {
    }

    public EventMessage(EventMessageType wsMessageType, T data) {
        this.wsMessageType = wsMessageType;
        this.data = data;
    }

    @Override
    public String toString() {
        return JsonUtil.bean2Json(this);
    }
}
