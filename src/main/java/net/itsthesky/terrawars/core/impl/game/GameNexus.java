package net.itsthesky.terrawars.core.impl.game;

import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.api.model.game.IGameNexus;
import net.itsthesky.terrawars.api.model.game.IGameTeam;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.TextDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameNexus implements IGameNexus {

    private final Map<Integer, NexusStats> stats;
    private final Location location;
    private final GameTeam team;
    private final int maxLevel;

    private NexusCrystal crystal;

    private int level;

    public GameNexus(@NotNull GameTeam gameTeam) {
        this.location = gameTeam.getConfig().getNexusLocation();
        this.team = gameTeam;

        this.stats = new HashMap<>();
        this.maxLevel = 3;

        this.stats.put(1, new NexusStats(500, 5, 30));
        this.stats.put(2, new NexusStats(1000, 10, 20));
        this.stats.put(3, new NexusStats(1500, 15, 10));

        this.level = 1;

        this.crystal = new NexusCrystal();
    }

    @Override
    public @NotNull IGameTeam getTeam() {
        return team;
    }

    @Override
    public @NotNull Location getLocation() {
        return location;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public @NotNull NexusStats getStats(int level) {
        if (level > maxLevel) {
            throw new IllegalArgumentException("Level cannot be greater than max level");
        }

        return stats.getOrDefault(level, new NexusStats(500, 5, 30));
    }

    @Override
    public Game getGame() {
        return team.getGame();
    }

    public class NexusCrystal {

        private EnderCrystal crystal;
        private TextDisplay display;

        public NexusCrystal() {
            location.getWorld().spawn(location, EnderCrystal.class, crystal -> {
                this.crystal = crystal;

                this.crystal.setInvulnerable(true);
                this.crystal.setCustomNameVisible(false);
            });
            location.getWorld().spawn(location.add(0, 1, 0), TextDisplay.class, display -> {
                this.display = display;

                this.display.setBillboard(Display.Billboard.CENTER);
                this.display.setAlignment(TextDisplay.TextAlignment.CENTER);
                this.display.setShadowed(true);
                updateTextDisplay();
            });
        }

        private void updateTextDisplay() {
            // make a cool progress for the hearts using ❤
            final var filledColor = "<shade-red:500>";
            final var emptyColor = "<shade-slate:500>";
            final var maxHearts = 10;
            final var filledHearts = (int) Math.ceil((double) getStats().maxHealth() / 100);
            final var emptyHearts = maxHearts - filledHearts;
            final var filled = filledColor + "❤".repeat(filledHearts);
            final var empty = emptyColor + "❤".repeat(emptyHearts);
            final var hearts = filled + empty;

            final List<String> lines = List.of(
                    "<accent>✦ <text>" + getTeam().getBiome().getName() + " Nexus <accent>- <base>Level " + getLevel() + " <accent>✦",
                    hearts
            );

            final var chatService = getGame().getChatService();

            this.display.text(chatService.joinNewLine(lines.stream()
                    .map(line -> chatService.format(line, getTeam().getColorScheme()))
                    .toList()));
        }
    }
}
