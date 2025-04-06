package net.itsthesky.terrawars.api.services;

import lombok.Getter;
import net.itsthesky.terrawars.util.Colors;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;

/**
 * This interface defines the contract for a chat service.
 * <br>
 * Those methods will use the MiniMessage system to format
 * messages, and adventure API to send them.
 */
public interface IChatService {

    /**
     * A message builder.
     */
    @Getter
    class MessageBuilder {

        private String message;
        private Audience audience;
        private List<TextColor> scheme;

        private MessageSeverity severity;
        private Object[] args;
        private Player source;

        public MessageBuilder() {
            this.args = new Object[0];
        }

        public MessageBuilder message(@NotNull String message) {
            this.message = message;
            return this;
        }

        public MessageBuilder audience(@NotNull Audience audience) {
            this.audience = audience;
            return this;
        }

        public MessageBuilder scheme(@Nullable List<TextColor> scheme) {
            this.scheme = scheme;
            return this;
        }

        public MessageBuilder severity(@NotNull MessageSeverity severity) {
            this.severity = severity;
            return scheme(severity.getScheme());
        }

        public MessageBuilder args(@Nullable Object... args) {
            this.args = args;
            return this;
        }

        public MessageBuilder source(@Nullable Player source) {
            this.source = source;
            return this;
        }

        public boolean isValid() {
            return message != null && audience != null;
        }
    }

    @Getter
    class TitleBuilder {
        private Audience audience;

        private String title;
        private String subtitle;

        private Duration fadeIn;
        private Duration stay;
        private Duration fadeOut;

        private List<TextColor> scheme;

        public TitleBuilder() {
            this.fadeIn = Duration.ofSeconds(1);
            this.stay = Duration.ofSeconds(3);
            this.fadeOut = Duration.ofSeconds(1);
        }

        public TitleBuilder audience(@NotNull Audience audience) {
            this.audience = audience;
            return this;
        }

        public TitleBuilder title(String title) {
            this.title = title;
            return this;
        }

        public TitleBuilder subtitle(String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        public TitleBuilder fadeIn(Duration fadeIn) {
            this.fadeIn = fadeIn;
            return this;
        }

        public TitleBuilder stay(Duration stay) {
            this.stay = stay;
            return this;
        }

        public TitleBuilder fadeOut(Duration fadeOut) {
            this.fadeOut = fadeOut;
            return this;
        }

        public TitleBuilder scheme(List<TextColor> scheme) {
            this.scheme = scheme;
            return this;
        }

        public boolean isValid() {
            return title != null && subtitle != null && audience != null;
        }
    }

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
        NEUTRAL(Colors.SLATE, 'N'),

        /**
         * Warning message. May be used for warning messages,
         * or important messages.
         */
        WARNING(Colors.AMBER, 'W'),

        /**
         * Error message. May be used for error messages,
         * or important messages.
         */
        ERROR(Colors.RED, 'X'),

        /**
         * Success message. May be used for success messages,
         * or important messages.
         */
        SUCCESS(Colors.GREEN, 'Y'),

        /**
         * Info message. May be used for info messages,
         * or important messages.
         */
        INFO(Colors.BLUE, 'I'),

        /**
         * Debug message. May be used for debug messages,
         * or important messages.
         */
        DEBUG(Colors.INDIGO, 'D');

        private final List<TextColor> scheme;
        private final char icon;

        MessageSeverity(List<TextColor> scheme, char icon) {
            this.scheme = scheme;
            this.icon = icon;
        }
    }

    /**
     * Join the given components together into a
     * single one.
     * @param components the components to join
     * @param joiner the joiner to use
     * @return the joined component
     */
    default @NotNull Component join(@NotNull List<Component> components, @NotNull Component joiner) {
        return Component.join(JoinConfiguration.separator(joiner), components);
    }

    /**
     * Join the given components together into a
     * single one, using a newline as the joiner.
     * @param components the components to join
     * @return the joined component
     */
    default @NotNull Component joinNewLine(@NotNull List<Component> components) {
        return join(components, Component.newline());
    }

    /**
     * Send a message to a specific audience. The given
     * message may contain MiniMessage tags.
     * @param audience the audience to send the message to
     * @param severity the severity of the message
     * @param message the message to send
     */
    default void sendMessage(@NotNull Audience audience, @NotNull MessageSeverity severity, @NotNull String message) {
        sendMessage(new MessageBuilder()
                .audience(audience)
                .severity(severity)
                .message(message));
    }

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
    default void sendMessage(@NotNull Audience audience, @NotNull MessageSeverity severity, @NotNull String message, Object... args) {
        sendMessage(new MessageBuilder()
                .audience(audience)
                .severity(severity)
                .message(message)
                .args(args));
    }

    /**
     * Sends a message using a pre-built message builder.
     * @param builder the message builder to use
     * @throws IllegalArgumentException if the builder is not valid
     * @see MessageBuilder
     * @see #sendMessage(Audience, MessageSeverity, String)
     */
    void sendMessage(@NotNull IChatService.MessageBuilder builder);

    /**
     * Construct a component from a message. The given
     * message may contain MiniMessage tags.
     * @param message the message to format
     * @return the formatted message
     */
    @NotNull Component format(@NotNull String message, @NotNull TagResolver... tagResolvers);

    /**
     * Construct a component from a message. The given
     * message may contain MiniMessage tags.
     * <br>
     * You can specify here a {@link Colors#SLATE color schemes}.
     * @param message the message to format
     * @param scheme the color scheme to use
     * @param tagResolvers the tag resolvers to use
     * @return the formatted message
     */
    @NotNull Component format(@NotNull String message, @NotNull List<TextColor> scheme, @NotNull TagResolver... tagResolvers);

    /**
     * Convert a rich {@link Component} to a simple string.
     * @param message the message to unformat
     * @return the unformatted message
     */
    @NotNull String unformat(@NotNull Component message);

    /**
     * Send a title & subtitle using a pre-built title builder.
     * @param builder the title builder to use
     * @throws IllegalArgumentException if the builder is not valid
     * @see TitleBuilder
     */
    void sendTitle(@NotNull TitleBuilder builder);
}
