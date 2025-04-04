package net.itsthesky.terrawars.core.services;

import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.api.services.base.Service;
import net.itsthesky.terrawars.util.Checks;
import net.itsthesky.terrawars.util.Colors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class ChatService implements IChatService {

    private final MiniMessage MINI_MESSAGE;
    private static final Pattern OVERRIDE_SCHEME_PATTERN = Pattern.compile("\\[(\\w+)]");

    private ChatService() {
        this.MINI_MESSAGE = MiniMessage.builder()
                .editTags(b -> {
                    for (final var colorName : Colors.getAvailableSchemesByName()) {
                        b.tag("shade-" + colorName, (args, context) -> {
                            final String shade = args.popOr("The <"+ colorName +"> tag requires exactly one argument, the level of shade you desire.").value();
                            final int shadeIndex = switch (shade) {
                                case "50" -> Colors.SHADE_50;
                                case "100" -> Colors.SHADE_100;
                                case "200" -> Colors.SHADE_200;
                                case "300" -> Colors.SHADE_300;
                                case "400" -> Colors.SHADE_400;
                                case "500" -> Colors.SHADE_500;
                                case "600" -> Colors.SHADE_600;
                                case "700" -> Colors.SHADE_700;
                                case "800" -> Colors.SHADE_800;
                                case "900" -> Colors.SHADE_900;
                                case "950" -> Colors.SHADE_950;
                                default -> throw new IllegalArgumentException("Invalid shade level: " + shade);
                            };

                            final var scheme = Colors.getSchemeByName(colorName);
                            return Tag.styling(b2 -> b2.color(scheme.get(shadeIndex)));
                        });
                    }
                })
                .build();
    }

    @Override
    public @NotNull Component format(@NotNull String message, @NotNull TagResolver... tagResolvers) {
        return format(message, Colors.SLATE, tagResolvers);
    }

    @Override
    public @NotNull Component format(@NotNull String message, @NotNull List<TextColor> scheme, @NotNull TagResolver... tagResolvers) {
        Checks.notNull(message, "Message cannot be null");
        Checks.notNull(scheme, "Scheme cannot be null");

        // Check if the message contains any color overrides
        final var matcher = OVERRIDE_SCHEME_PATTERN.matcher(message);
        if (matcher.find()) {
            // Extract the color name from the message
            final var colorName = matcher.group(1);
            final var color = Colors.getSchemeByName(colorName);
            if (color == null)
                throw new IllegalArgumentException("Invalid color name: " + colorName);

            message = message.replace(matcher.group(0), "");
            scheme = color;
        }

        final var placeholders = List.of(
                Placeholder.styling("text", scheme.get(Colors.SHADE_200)),
                Placeholder.styling("base", scheme.get(Colors.SHADE_500)),
                Placeholder.styling("accent", scheme.get(Colors.SHADE_700))
        );
        final var placeholdersWithTagResolvers = new ArrayList<TagResolver>(placeholders);
        placeholdersWithTagResolvers.addAll(Arrays.asList(tagResolvers));

        return MINI_MESSAGE.deserialize("<!i>" + message, TagResolver.resolver(placeholdersWithTagResolvers));
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

    @Override
    public @NotNull String unformat(@NotNull Component message) {
        Checks.notNull(message, "Message cannot be null");
        return MINI_MESSAGE.serialize(message);
    }
}
