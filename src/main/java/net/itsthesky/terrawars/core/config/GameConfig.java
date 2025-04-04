package net.itsthesky.terrawars.core.config;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.itsthesky.terrawars.api.model.game.IGame;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class GameConfig {

    private World world;
    private Location lobby;
    private List<GameTeamConfig> teams = new ArrayList<>();
    @SerializedName("game_size")
    private IGame.GameSize gameSize = IGame.GameSize.SOLO;

}
