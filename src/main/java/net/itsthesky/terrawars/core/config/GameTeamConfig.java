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

    @JsonAdapter(LocationAdapter.class)
    @SerializedName("shopkeeper_location")
    private Location shopkeeperLocation;

    @JsonAdapter(LocationAdapter.class)
    @SerializedName("upgrades_location")
    private Location upgradesLocation;

    @JsonAdapter(LocationAdapter.class)
    @SerializedName("chest_location")
    private Location chestLocation;

    @SerializedName("biome_id")
    private String biomeId;

    public boolean isValid() {
        return spawnLocation != null && nexusLocation != null && generatorLocation != null
                && shopkeeperLocation != null && upgradesLocation != null && chestLocation != null
                && biomeId != null;
    }

}
