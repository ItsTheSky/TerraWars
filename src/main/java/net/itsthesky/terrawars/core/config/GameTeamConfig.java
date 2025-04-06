package net.itsthesky.terrawars.core.config;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.itsthesky.terrawars.util.adapters.LocationAdapter;
import org.bukkit.Location;
import org.bukkit.Material;

@Getter
@Setter
@NoArgsConstructor
public class GameTeamConfig {

    @JsonAdapter(LocationAdapter.class)
    @SerializedName("spawn_location")
    private Location spawnLocation;

    @JsonAdapter(LocationAdapter.class)
    @SerializedName("nexus_location")
    private Location nexusLocation;

    @JsonAdapter(LocationAdapter.class)
    @SerializedName("generator_location")
    private Location generatorLocation;

    @SerializedName("biome_id")
    private String biomeId;

}
