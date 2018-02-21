package stockstream.tv.twitch;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WriteCallback;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import stockstream.tv.data.Channel;
import stockstream.util.JSONUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@WebSocket
public class WebTVSocket {

    @Getter
    private Channel nowPlaying;

    private final static Map<Session, String> usernameMap = new ConcurrentHashMap<>();
    private int nextUserNumber = 1;

    public void changeChannel(final Channel channel) {
        nowPlaying = channel;

        usernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
            try {
                session.getRemote().sendString(JSONUtil.serializeObject(channel).get(), new WriteCallback() {
                    @Override
                    public void writeFailed(final Throwable x) {
                        log.warn("Can't send message to tradeCommand list {}", x.toString());
                    }

                    @Override
                    public void writeSuccess() {
                    }
                });
            } catch (final Exception e) {
                log.warn(e.getMessage(), e);
            }
        });
    }

    @OnWebSocketConnect
    public void onConnect(final Session user) throws Exception {
        String username = "User" + nextUserNumber++;
        usernameMap.put(user, username);
    }

    @OnWebSocketClose
    public void onClose(final Session user, final int statusCode, final String reason) {
        usernameMap.remove(user);
    }

    @OnWebSocketMessage
    public void onMessage(final Session user, final String message) {
    }

}
