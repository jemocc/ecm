package org.cc.common.component;

import com.alibaba.nacos.common.utils.ConcurrentHashSet;
import com.google.gson.reflect.TypeToken;
import org.cc.common.config.ThreadPool;
import org.cc.common.model.EventMessage;
import org.cc.common.pojo.EventMessageType;
import org.cc.common.utils.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint("/open-ws-conn/{uid}")
public class WSService implements ApplicationContextAware {
    private final Logger log = LoggerFactory.getLogger(WSService.class);
    private static ApplicationContext applicationContext;

    private static final ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> uidMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> sessionIdMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashSet<String> watchSessions = new ConcurrentHashSet<>();

    private void addConn(Session session, String uid) {
        log.info("用户[{}]建立WS连接[{}]", uid, session.getId());
        sessionMap.put(session.getId(), session);
        sessionIdMap.put(uid, session.getId());
        uidMap.put(session.getId(), uid);
    }

    private void removeConn(Session session) {
        String sessionId = session.getId();
        String uid = uidMap.get(sessionId);
        sessionMap.remove(sessionId);
        sessionIdMap.remove(uid);
        uidMap.remove(sessionId);
        log.info("用户[{}]连接[{}]移除", uid, sessionId);
        if (watchSessions.remove(sessionId)) {
            log.info("移除线程池监控连接[{}],剩余连接[{}]", sessionId, watchSessions.size());
            if (watchSessions.size() == 0) {
                ThreadPool.closeWatch();
            }
        }

    }

    @OnOpen
    public void onOpen(Session session, @PathParam("uid") String uid) {
        addConn(session, uid);
    }

    @OnClose
    public void onClose(Session session) {
        removeConn(session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        EventMessage<String> eventMessage = JsonUtil.json2Bean(message, new TypeToken<EventMessage<String>>(){}.getType());
        log.info("服务端收到客户端[{}]的消息:{} => {}", session.getId(), message, JsonUtil.bean2Json_FN(eventMessage));
        if (eventMessage.getWsMessageType() == EventMessageType.THREAD_POOL_WATCH) {
            handlerThreadPoolEvent(session);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
        removeConn(session);
    }

    /**
     * 向客户端发送消息
     * @param message   消息内容
     * @param session   ws session
     * @return  true-成功
     */
    private Boolean sendMessage(EventMessage<?> message, Session session) {
        try {
            session.getBasicRemote().sendText(JsonUtil.bean2Json(message));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    public Boolean sendMessage(EventMessage<?> message, String uid) {
        Session session = sessionMap.get(sessionIdMap.get(uid));
        return sendMessage(message, session);
    }
    public Map<String, Boolean> sendMessage(EventMessage<?> message, List<String> uidList) {
        Map<String, Boolean> result = new HashMap<>(uidList.size());
        uidList.forEach(i -> {
            result.put(i, sendMessage(message, i));
        });
        return result;
    }

    public void sendMessageToWatcherAsync(EventMessage<?> message) {
        watchSessions.iterator().forEachRemaining(sessionId -> sendMessage(message, sessionMap.get(sessionId)));
    }

    public static void sendMessageToWatcher(EventMessage<?> message) {
        BeanManager.getBean(WSService.class).sendMessageToWatcherAsync(message);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        WSService.applicationContext = applicationContext;
    }

    private void handlerThreadPoolEvent(Session session) {
        if (applicationContext != null) {
            watchSessions.add(session.getId());
            log.info("添加线程池监控连接[{}],当前连接总数[{}]", session.getId(), watchSessions.size());
            ThreadPool.openWatch();
        } else {
            log.info("applicationContext == null");
        }
    }
}
