package net.itsthesky.terrawars.core.impl.upgrade.common;

import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.model.game.IGameTeam;
import net.itsthesky.terrawars.api.model.upgrade.AbstractUpgrade;
import net.itsthesky.terrawars.api.model.upgrade.UpgradeCategory;
import net.itsthesky.terrawars.core.impl.game.GameNexus;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BaseNexusTierUpgrade extends AbstractUpgrade {

    private final int level;

    private final int changedMaxHealth;
    private final int changedHealthRegenDelay;
    private final int changedHealthRegenAmount;

    public BaseNexusTierUpgrade(int level, UpgradeCategory category, int changedMaxHealth, int changedHealthRegenDelay, int changedHealthRegenAmount) {
        super("nexus_tier_" + level, category, "Nexus Tier " + level, List.of(
                "Increase the level of your nexus."
                ), 1, Material.NETHER_STAR);

        this.level = level;
        this.changedMaxHealth = changedMaxHealth;
        this.changedHealthRegenDelay = changedHealthRegenDelay;
        this.changedHealthRegenAmount = changedHealthRegenAmount;
    }

    @Override
    public @NotNull List<String> buildDescription(@NotNull IGameTeam team, int level) {
        final var lore = new ArrayList<String>();

        if (changedMaxHealth != -1)
            lore.add("<shade-slate:800>- <shade-slate:600>Max Health: <shade-amber:500>"
                    + team.getNexus().getStats().getMaxHealth()
                    + " <shade-slate:600>→ <shade-lime:500>"
                    + changedMaxHealth + "♥");
        if (changedHealthRegenDelay != -1)
            lore.add("<shade-slate:800>- <shade-slate:600>Health Regen Delay: <shade-amber:500>"
                    + team.getNexus().getStats().getRegenDelay() + "s"
                    + " <shade-slate:600>→ <shade-lime:500>"
                    + changedHealthRegenDelay + "s");
        if (changedHealthRegenAmount != -1)
            lore.add("<shade-slate:800>- <shade-slate:600>Health Regen Amount: <shade-amber:500>"
                    + team.getNexus().getStats().getRegenPerSec() + "♥/s"
                    + " <shade-slate:600>→ <shade-lime:500>"
                    + changedHealthRegenAmount + "♥/s");

        return lore;
    }

    @Override
    public void applyUpgrade(@NotNull IGamePlayer source, @NotNull IGameTeam team, int level) {
        super.applyUpgrade(source, team, level);

        final var nexus = (GameNexus) team.getNexus();
        if (changedMaxHealth != -1)
            nexus.getStats().setMaxHealth(changedMaxHealth);
        if (changedHealthRegenDelay != -1)
            nexus.getStats().setRegenDelay(changedHealthRegenDelay);
        if (changedHealthRegenAmount != -1)
            nexus.getStats().setRegenPerSec(changedHealthRegenAmount);
        nexus.setLevel(this.level + 1);

        nexus.updateDisplay();
    }
}
