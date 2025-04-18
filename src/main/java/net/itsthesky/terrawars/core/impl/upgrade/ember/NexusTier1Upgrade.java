package net.itsthesky.terrawars.core.impl.upgrade.ember;

import net.itsthesky.terrawars.api.model.upgrade.UpgradeCategory;
import net.itsthesky.terrawars.core.impl.upgrade.common.BaseNexusTierUpgrade;
import org.bukkit.Material;

public class NexusTier1Upgrade extends BaseNexusTierUpgrade {

    public NexusTier1Upgrade() {
        super(1, UpgradeCategory.EMBER, 750, -1, 7);

        addCost(1, Material.DIAMOND, 6);
        addCost(1, Material.GOLD_INGOT, 16);
    }

}
