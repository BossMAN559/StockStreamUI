package stockstream.tv.cache;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import stockstream.logic.PubSub;
import stockstream.logic.Scheduler;
import stockstream.tv.data.InfoMessage;
import stockstream.tv.util.TextUtil;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class InfoMessageCache {

    @Autowired
    private PubSub pubSub;

    @Autowired
    private Scheduler scheduler;

    private Map<String, InfoMessage> idToInfo = new ConcurrentHashMap<>();
    private Set<String> storedURLs = Collections.synchronizedSet(new HashSet<>());
    private List<InfoMessage> messages = Collections.synchronizedList(new ArrayList<>());

    private int iterator = 0;
    private int idCount = 0;

    @PostConstruct
    public void init() {
        pubSub.subscribeFunctionToClassType(this::addMessage, InfoMessage.class);
        scheduler.scheduleJob(this::resort, 20L, 300L, TimeUnit.SECONDS);
    }

    public synchronized Optional<String> getURLForId(final String id) {
        return Optional.ofNullable(idToInfo.get(id).getUrl());
    }

    public synchronized Map<String, InfoMessage> getNext() {
        if (messages.size() == 0) {
            return Collections.emptyMap();
        }

        if (iterator > messages.size()) {
            iterator = 0;
        }

        final InfoMessage nextMessage = messages.get(iterator++);
        final String messageId = String.format("!%s", idCount);
        idToInfo.put(messageId, nextMessage);
        idCount++;
        if (idCount > 99) {
            idCount = 0;
        }

        return ImmutableMap.of(messageId, nextMessage);
    }

    private synchronized void resort() {
        log.info("Shuffling and filtering {} InfoMessages", messages.size());
        final List<InfoMessage> messageList = Collections.synchronizedList(new ArrayList<>());
        final Queue<InfoMessage> queue = new ConcurrentLinkedQueue<>(messages);

        final long now = new Date().getTime();

        while (!queue.isEmpty()) {
            InfoMessage message = queue.poll();
            if (message == null) {
                continue;
            }

            final long msAgo = now - message.getTimestamp();

            if (msAgo > 2 * 3600000) {
                //continue;
            }

            messageList.add(message);
        }

        Collections.shuffle(messageList);
        messages = messageList;

        log.info("Have {} left", messageList.size());

        iterator = 0;
    }

    private String sanitizeText(final String inputText) {
        return TextUtil.replaceNonPrintableText(TextUtil.stripURLS(inputText));
    }

    private synchronized Void addMessage(final InfoMessage infoMessage) {
        if (infoMessage.getText().length() <= 45 || infoMessage.getText().length() > 375) {
            return null;
        }
        if (storedURLs.contains(infoMessage.getUrl())) {
            return null;
        }

        infoMessage.setText(sanitizeText(infoMessage.getText()));
        infoMessage.setSender(sanitizeText(infoMessage.getSender()));
        messages.add(infoMessage);
        storedURLs.add(infoMessage.getUrl());
        return null;
    }

}
