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

    @SerializedName("spawn_location")
    private Location spawnLocation;

    @SerializedName("nexus_location")
    private Location nexusLocation;

    @SerializedName("generator_location")
    private Location generatorLocation;

}
