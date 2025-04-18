package net.itsthesky.terrawars.core.impl.game;

import lombok.Getter;
import net.itsthesky.terrawars.api.model.biome.IBiome;
import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.api.model.game.IGameNexus;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.model.game.IGameTeam;
import net.itsthesky.terrawars.api.model.upgrade.ITeamUpgrade;
import net.itsthesky.terrawars.core.config.GameTeamConfig;
import net.itsthesky.terrawars.util.Colors;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@Getter
public class GameTeam implements IGameTeam {

    private final UUID id;
    private final GameTeamConfig config;
    private final Map<UUID, GamePlayer> players;
    private final List<GameShopEntity> shopEntities;
    private final Game game;
    private final IBiome biome;
    private final GameNexus nexus;
    private final Location spawnLocation;

    private final Set<GameBiomeNode> capturedNodes;

    private final Map<ITeamUpgrade, Integer> upgrades;

    public GameTeam(GameTeamConfig config, Game game, IBiome biome) {
        this.id = UUID.randomUUID();
        this.players = new HashMap<>(game.getSize().getPlayerPerTeam());

        this.game = game;
        this.config = config;
        this.biome = biome;

        this.nexus = new GameNexus(this);
        this.spawnLocation = config.getSpawnLocation();

        this.upgrades = new HashMap<>();
        this.capturedNodes = new HashSet<>();

        this.shopEntities = new ArrayList<>();
        this.shopEntities.add(new GameShopEntity(this, GameShopEntity.ShopEntityType.SHOPKEEPER));
        this.shopEntities.add(new GameShopEntity(this, GameShopEntity.ShopEntityType.UPGRADES));
    }

    @Override
    public @NotNull Set<IGamePlayer> getPlayers() {
        return new HashSet<>(players.values());
    }

    @Override
    public @NotNull IBiome getBiome() {
        return biome;
    }

    @Override
    public @NotNull IGameNexus getNexus() {
        return nexus;
    }

    @Override
    public @NotNull List<TextColor> getColorScheme() {
        return getBiome().getScheme();
    }

    @Override
    public @Nullable IGamePlayer getPlayer(@NotNull Player player) {
        return players.get(player.getUniqueId());
    }

    @Override
    public boolean shouldApplyUpgrade(@NotNull ITeamUpgrade upgrade) {
        return capturedNodes.size() >= upgrade.getCategory().getRequiredCapturedNodes();
    }

    @Override
    public int getUpgradeLevel(@NotNull ITeamUpgrade upgrade) {
        return upgrades.getOrDefault(upgrade, 0);
    }

    @Override
    public void increaseUpgradeLevel(ITeamUpgrade upgrade) {
        if (upgrades.containsKey(upgrade)) {
            upgrades.put(upgrade, upgrades.get(upgrade) + 1);
        } else {
            upgrades.put(upgrade, 1);
        }
    }

    @Override
    public boolean tryAddPlayer(@NotNull IGamePlayer player) {
        if (players.size() >= game.getSize().getPlayerPerTeam())
            return false;

        this.players.put(player.getPlayer().getUniqueId(), (GamePlayer) player);
        player.setTeam(this);
        return true;
    }

    public void cleanup() {
        this.nexus.cleanup();
        for (GamePlayer player : players.values()) player.cleanup();
        for (GameShopEntity shopEntity : shopEntities) shopEntity.cleanup();

        final var teamChestBlock = config.getChestLocation().getBlock();
        if (teamChestBlock.getState() instanceof final Chest chest)
            chest.getInventory().clear();
    }

    @Override
    public @NotNull Location getTeamChestLocation() {
        return config.getChestLocation();
    }
}
