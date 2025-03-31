package net.itsthesky.terrawars.api.services;

import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * This interface defines the contract for a chat service.
 * <br>
 * Those methods will use the MiniMessage system to format
 * messages, and adventure API to send them.
 */
public interface IChatService {

    /**
     * This enum defines the severity of a message sent
     * to an audience.
     */
    @Getter
    enum MessageSeverity {

        /**
         * Simple/Neutral message. May only be used for debugs,
         * notification or simple messages.
         */
        NEUTRAL("<#64748b><bold>[</bold><#cbd5e1>✎<bold><#64748b>]<reset><#e2e8f0> "),

        /**
         * Warning message. May be used for warning messages,
         * or important messages.
         */
        WARNING("<#fbbf24><bold>[</bold><#fef08a>⚠<bold><#fbbf24>]<reset><#fef3c7> "),

        /**
         * Error message. May be used for error messages,
         * or important messages.
         */
        ERROR("<#dc2626><bold>[</bold><#fee2e2>✘<bold><#dc2626>]<reset><#fecaca> "),

        /**
         * Success message. May be used for success messages,
         * or important messages.
         */
        SUCCESS("<#22c55e><bold>[</bold><#bbf7d0>✔<bold><#22c55e>]<reset><#bbf7d0> "),

        /**
         * Info message. May be used for info messages,
         * or important messages.
         */
        INFO("<#3b82f6><bold>[</bold><#bfdbfe>ℹ<bold><#3b82f6>]<reset><#bfdbfe> "),

        /**
         * Debug message. May be used for debug messages,
         * or important messages.
         */
        DEBUG("<#a855f7><bold>[</bold><#e0cfe9>🐞<bold><#a855f7>]<reset><#e0cfe9> ");

        private final String rawPrefix;

        MessageSeverity(String rawPrefix) {
            this.rawPrefix = rawPrefix;
        }
    }

    /**
     * Send a message to a specific audience. The given
     * message may contain MiniMessage tags.
     * @param audience the audience to send the message to
     * @param severity the severity of the message
     * @param message the message to send
     */
    void sendMessage(@NotNull Audience audience, @NotNull MessageSeverity severity, @NotNull String message);

    /**
     * Send a message to a specific audience. The given
     * message may contain MiniMessage tags.
     * <br>
     * The given message will be formatted with the given arguments.
     * It uses the `{X}` syntax to format the message (where X is the index of the argument).
     * @param audience the audience to send the message to
     * @param severity the severity of the message
     * @param message the message to send
     * @param args the arguments to format the message with
     */
    void sendMessage(@NotNull Audience audience, @NotNull MessageSeverity severity, @NotNull String message, Object... args);

    /**
     * Construct a component from a message. The given
     * message may contain MiniMessage tags.
     * @param message the message to format
     * @return the formatted message
     */
    @NotNull Component format(@NotNull String message);
}
