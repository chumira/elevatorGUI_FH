package logic;

import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TimestampedMessageAppender extends AbstractAppender {

    public static class LogEntry {
        public final long timestamp;
        public final String message;

        public LogEntry(long timestamp, String message) {
            this.timestamp = timestamp;
            this.message = message;
        }

        @Override
        public String toString() {
            return "[" + timestamp + "] " + message;
        }
    }

    private final List<LogEntry> entries = Collections.synchronizedList(new ArrayList<>());

    public TimestampedMessageAppender(String name) {
        super(name, null, PatternLayout.createDefaultLayout(), false);
    }

    @Override
    public void append(LogEvent event) {
        long ts = System.currentTimeMillis();
        String msg = event.getMessage().getFormattedMessage();
        entries.add(new LogEntry(ts, msg));
    }

    public List<LogEntry> getEntries() {
        return new ArrayList<>(entries);
    }
}