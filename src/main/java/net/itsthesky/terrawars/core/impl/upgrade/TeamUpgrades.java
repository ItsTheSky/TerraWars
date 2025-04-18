package net.itsthesky.terrawars.core.impl.upgrade;

import net.itsthesky.terrawars.api.model.upgrade.ITeamUpgrade;
import net.itsthesky.terrawars.core.impl.upgrade.crystal.EmeraldGeneratorUpgrade;
import net.itsthesky.terrawars.core.impl.upgrade.crystal.NexusTier2Upgrade;
import net.itsthesky.terrawars.core.impl.upgrade.ember.GeneratorSpeedUpgrade;
import net.itsthesky.terrawars.core.impl.upgrade.ember.NexusTier1Upgrade;

import java.util.HashSet;
import java.util.Set;

public final class TeamUpgrades {

    //region Ember Tier

    public static final ITeamUpgrade GENERATOR_SPEED = new GeneratorSpeedUpgrade();
    public static final ITeamUpgrade NEXUS_TIER_1 = new NexusTier1Upgrade();

    //endregion

    //region Crystal Tier

    public static final ITeamUpgrade EMERALD_GENERATOR = new EmeraldGeneratorUpgrade();
    public static final ITeamUpgrade NEXUS_TIER_2 = new NexusTier2Upgrade();

    //endregion


    private static final Set<ITeamUpgrade> UPGRADES = new HashSet<>();

    public static Set<ITeamUpgrade> getUpgrades() {
        if (UPGRADES.isEmpty()) {
            try {
                final var fields = TeamUpgrades.class.getDeclaredFields();
                for (final var field : fields) {
                    if (ITeamUpgrade.class.isAssignableFrom(field.getType())) {
                        final var upgrade = (ITeamUpgrade) field.get(null);
                        UPGRADES.add(upgrade);
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return UPGRADES;
    }

}
