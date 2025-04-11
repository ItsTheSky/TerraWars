package net.itsthesky.terrawars.core.config;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.itsthesky.terrawars.api.model.game.IGame;
import net.itsthesky.terrawars.util.adapters.LocationAdapter;
import net.itsthesky.terrawars.util.adapters.WorldAdapter;
import org.bukkit.Location;
import org.bukkit.World;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class GameConfig {


    @JsonAdapter(WorldAdapter.class)
    private World world;
    @JsonAdapter(LocationAdapter.class)
    private Location lobby;

    private ArrayList<GameTeamConfig> teams = new ArrayList<>();

    @SerializedName("game_size")
    private IGame.GameSize gameSize = IGame.GameSize.SOLO;

    @SerializedName("generators")
    private List<GameGeneratorConfig> generators = new ArrayList<>();

    @SerializedName("biome_nodes")
    private List<Location> biomeNodes = new ArrayList<>();

    @SerializedName("death_cooldown")
    private int deathCooldown = 5;

    private transient Runnable saveRunnable;
    public void save() {
        if (saveRunnable != null)
            saveRunnable.run();
    }

    public boolean isValid() {
        if (world == null || lobby == null || teams.isEmpty())
            return false;
        for (GameTeamConfig team : teams)
            if (!team.isValid()) return false;
        return true;
    }
}
