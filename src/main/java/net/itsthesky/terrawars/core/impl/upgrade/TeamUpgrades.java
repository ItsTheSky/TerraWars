package net.itsthesky.terrawars.core.impl.upgrade;

import net.itsthesky.terrawars.api.model.upgrade.ITeamUpgrade;

import java.util.HashSet;
import java.util.Set;

public final class TeamUpgrades {

    //region Ember Tier

    public static final ITeamUpgrade GENERATOR_SPEED = new GeneratorSpeedUpgrade();

    //endregion

    //region Crystal Tier

    public static final ITeamUpgrade EMERALD_GENERATOR = new EmeraldGeneratorUpgrade();

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
