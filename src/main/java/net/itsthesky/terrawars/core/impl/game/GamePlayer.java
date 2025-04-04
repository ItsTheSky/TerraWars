package net.itsthesky.terrawars.core.impl.game;

import lombok.Getter;
import lombok.Setter;
import net.itsthesky.terrawars.api.model.ability.IAbility;
import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.model.game.IGameTeam;
import net.itsthesky.terrawars.util.Checks;
import net.itsthesky.terrawars.util.Colors;
import net.itsthesky.terrawars.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Getter @Setter
public class GamePlayer implements IGamePlayer {

    private final OfflinePlayer offlinePlayer;
    private final IGame game;

    private GamePlayerState state;
    private @Nullable IGameTeam team;

    private @Nullable IAbility selectedAbility;

    public GamePlayer(OfflinePlayer player, IGame game) {
        this.offlinePlayer = player;
        this.game = game;

        this.state = GamePlayerState.WAITING;

        this.team = null;
        this.selectedAbility = null;
    }


    @Override
    public @Nullable IGameTeam getTeam() throws IllegalStateException {
        if (team == null)
            throw new IllegalStateException("Game has not started yet, player is not in a team.");

        return team;
    }

    @Override
    public void setTeam(@Nullable IGameTeam team) throws IllegalStateException {
        Checks.notNull(team, "Team cannot be null");

        this.team = team;
    }

    @Override
    public void setState(@NotNull GamePlayerState state) {
        Checks.notNull(state, "State cannot be null");

        this.state = state;
    }

    private static final int ABILITY_SLOT = 8;

    public void setSelectedAbility(@Nullable IAbility ability) {
        this.selectedAbility = ability;

        setupHotbar(false);
    }

    @Override
    public void setupHotbar(boolean clear) {
        if (!isOnline())
            return;

        final var player = this.offlinePlayer.getPlayer();
        if (clear)
            player.getInventory().clear();

        if (this.selectedAbility == null) {
            player.getInventory().setItem(ABILITY_SLOT, new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                    .noMovement()
                    .name("<accent><b>No ability selected", Colors.RED)
                    .lore(Colors.RED, "<text>Select your ability at a <base>shopkeeper<text>!")
                    .getItem());
        } else {
            player.getInventory().setItem(ABILITY_SLOT,
                    selectedAbility.buildHotBarItem(this));
        }
    }
}
