package net.itsthesky.terrawars.core.services;

import net.itsthesky.terrawars.api.services.base.Service;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.util.Checks;
import net.itsthesky.terrawars.util.Colors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService implements IChatService {

    private final MiniMessage MINI_MESSAGE;

    private ChatService() {
        this.MINI_MESSAGE = MiniMessage.miniMessage();
    }

    @Override
    public @NotNull Component format(@NotNull String message, @NotNull TagResolver... tagResolvers) {
        Checks.notNull(message, "Message cannot be null");
        return MINI_MESSAGE.deserialize(message, tagResolvers);
    }

    @Override
    public void sendMessage(@NotNull IChatService.MessageBuilder builder) {
        Checks.notNull(builder, "Builder cannot be null");
        Checks.isTrue(builder.isValid(), "Builder is not valid");
        Checks.isTrue(builder.getSeverity() == null || builder.getSource() == null, "Builder cannot have both source and severity set");

        final var placeholders = new ArrayList<TagResolver>();

        if (builder.getSource() != null) {
            placeholders.add(Placeholder.component("source", builder.getSource().displayName().style(Style.style()
                    .color(Colors.SLATE_300)
                    .hoverEvent(builder.getSource().asHoverEvent())
                    .build())));
        }

        placeholders.add(Placeholder.styling("text", builder.getScheme().get(Colors.SHADE_200)));
        placeholders.add(Placeholder.styling("base", builder.getScheme().get(Colors.SHADE_500)));
        placeholders.add(Placeholder.styling("accent", builder.getScheme().get(Colors.SHADE_700)));

        String message = String.format(builder.getMessage(), builder.getArgs());
        if (builder.getSource() != null) {
            message = "<accent><source> <b>&7»</b> <text>" + message;
        } else {
            final var severity = Optional.of(builder.getSeverity()).orElse(MessageSeverity.NEUTRAL);
            message = "<accent><b>[</b><base>" + severity.getIcon() + "<accent><b>]</b> <text>" + message;
        }

        final var component = format(message, TagResolver.resolver(placeholders));
        builder.getAudience().sendMessage(component);
    }

    @Override
    public void sendTitle(@NotNull TitleBuilder builder) {
        Checks.notNull(builder, "Builder cannot be null");
        Checks.isTrue(builder.isValid(), "Builder is not valid");

        final var placeholders = List.of(
                Placeholder.styling("text", builder.getScheme().get(Colors.SHADE_200)),
                Placeholder.styling("base", builder.getScheme().get(Colors.SHADE_500)),
                Placeholder.styling("accent", builder.getScheme().get(Colors.SHADE_700))
        );

        final var title = format("<text>" + builder.getTitle(), TagResolver.resolver(placeholders));
        final var subtitle = format("<text>" + builder.getSubtitle(), TagResolver.resolver(placeholders));

        builder.getAudience().showTitle(Title.title(title, subtitle, Title.Times.times(
                builder.getFadeIn(),
                builder.getStay(),
                builder.getFadeOut()
        )));
    }
}
