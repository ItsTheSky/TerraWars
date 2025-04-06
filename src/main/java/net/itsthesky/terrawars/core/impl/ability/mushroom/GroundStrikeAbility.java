package net.itsthesky.terrawars.core.impl.ability.mushroom;

import net.itsthesky.terrawars.api.model.ability.ActiveAbility;
import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GroundStrikeAbility extends ActiveAbility {

    public GroundStrikeAbility() {
        super("mushroom_ground_strike", Material.DIRT, "Ground Strike",
                List.of(
                        "Push any enemies around you away",
                        "by <shade-brown:500>5 blocks</shade-brown>."
                ), 20);
    }

    @Override
    protected boolean execute(@NotNull IGamePlayer player, @NotNull IGame game) {

    }
}
