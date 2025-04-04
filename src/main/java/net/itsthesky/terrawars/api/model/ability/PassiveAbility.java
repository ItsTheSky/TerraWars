package net.itsthesky.terrawars.api.model.ability;

import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.List;

public abstract class PassiveAbility extends AbstractAbility {
    protected PassiveAbility(String id, Material icon, String displayName, List<String> description, int cooldownSeconds) {
        super(id, icon, displayName, description, cooldownSeconds, AbilityType.PASSIVE);
    }
    
    public <T extends Event> boolean tryTrigger(IGamePlayer player, T event) {
        if (isOnCooldown(player)) return false;
        
        if (shouldTrigger(player, event)) {
            if (trigger(player, event)) {
                startCooldown(player);
                return true;
            }
        }
        return false;
    }
    
    protected abstract <T extends Event> boolean shouldTrigger(IGamePlayer player, T event);
    protected abstract <T extends Event> boolean trigger(IGamePlayer player, T event);
}