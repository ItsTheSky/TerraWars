package net.itsthesky.terrawars.core.impl.upgrade.crystal;

import net.itsthesky.terrawars.api.model.upgrade.UpgradeCategory;
import net.itsthesky.terrawars.core.impl.upgrade.TeamUpgrades;
import net.itsthesky.terrawars.core.impl.upgrade.common.BaseNexusTierUpgrade;
import org.bukkit.Material;

public class NexusTier2Upgrade extends BaseNexusTierUpgrade {

    public NexusTier2Upgrade() {
        super(2, UpgradeCategory.CRYSTAL, 1500, 45, 10);

        addCost(1, Material.DIAMOND, 8);
        addCost(1, Material.GOLD_INGOT, 24);
        addCost(1, Material.EMERALD, 4);
        addCost(1, Material.AMETHYST_SHARD, 1);

        addRequiredUpgrade(TeamUpgrades.NEXUS_TIER_1, 1);
    }

}
