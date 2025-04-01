package net.itsthesky.terrawars.core.config;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;

@Getter
@Setter
@NoArgsConstructor
public class GameTeamConfig {

    @EntryDetails(
            name = "Spawn Location",
            description = "The location where the team will spawn. Try to count +1 for the Y axis.",
            icon = Material.GRASS_BLOCK,
            isRequired = true
    )
    @SerializedName("spawn_location")
    private Location spawnLocation;

    @EntryDetails(
            name = "Nexus Location",
            description = "The location where the nexus will be placed. Try to count +1 for the Y axis.",
            icon = Material.END_CRYSTAL,
            isRequired = true
    )
    @SerializedName("nexus_location")
    private Location nexusLocation;

    @EntryDetails(
            name = "Generator Location",
            description = "The location where the generator will be placed. Try to count +1 for the Y axis.",
            icon = Material.IRON_BLOCK,
            isRequired = true
    )
    @SerializedName("generator_location")
    private Location generatorLocation;

}
