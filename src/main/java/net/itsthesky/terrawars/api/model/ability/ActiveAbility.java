package net.itsthesky.terrawars.api.model.ability;

import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class ActiveAbility extends AbstractAbility {
    protected ActiveAbility(String id, Material icon, String displayName, List<String> description, int cooldownSeconds) {
        super(id, icon, displayName, description, cooldownSeconds, AbilityType.ACTIVE);
    }

    public boolean use(IGamePlayer player, @NotNull IGame game) {
        if (isOnCooldown(player))
            return false;
        
        if (execute(player, game)) {
            startCooldown(player);
            return true;
        }

        return false;
    }
    
    protected abstract boolean execute(@NotNull IGamePlayer player, @NotNull IGame game);
}