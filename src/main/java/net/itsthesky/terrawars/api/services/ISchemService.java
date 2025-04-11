package net.itsthesky.terrawars.api.services;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/**
 * Service related to pasting schematics. The main
 * implementation will be used WE/FAWE system.
 */
public interface ISchemService {

    /**
     * Pastes a schematic at the given location.
     * The schematics' name must match a file located at <code>plugins/TerraWars/schematics/</code>.
     * <br>
     * You <b>MUST</b> include its extension, like <code>my_schematic.schem</code>.
     * @param name the name of the schematic to paste
     * @param location the location where the schematic will be pasted
     */
    void pasteSchematic(@NotNull String name, @NotNull Location location,
                        boolean ignoreAir);

}
