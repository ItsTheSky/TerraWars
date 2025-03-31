package net.itsthesky.terrawars.core.services;

import net.itsthesky.terrawars.api.services.Service;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.util.Checks;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

@Service
public class ChatService implements IChatService {

    private final MiniMessage MINI_MESSAGE;

    private ChatService() {
        this.MINI_MESSAGE = MiniMessage.miniMessage();
    }

    @Override
    public @NotNull Component format(@NotNull String message) {
        Checks.notNull(message, "Message cannot be null");
        return MINI_MESSAGE.deserialize(message);
    }

    @Override
    public void sendMessage(@NotNull Audience audience, @NotNull MessageSeverity severity, @NotNull String message) {
        sendMessage(audience, severity, message, new Object[0]);
    }

    @Override
    public void sendMessage(@NotNull Audience audience, @NotNull MessageSeverity severity, @NotNull String message, Object... args) {
        Checks.notNull(audience, "Audience cannot be null");
        Checks.notNull(severity, "Severity cannot be null");
        Checks.notNull(message, "Message cannot be null");

        if (args.length > 0)
            message = String.format(message, args);
        message = severity.getRawPrefix() + message;

        final var formatted = format(message);
        audience.sendMessage(formatted);
    }
}
