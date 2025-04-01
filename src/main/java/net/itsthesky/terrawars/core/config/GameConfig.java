package net.itsthesky.terrawars.core.config;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.itsthesky.terrawars.api.model.game.IGame;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class GameConfig {

    @EntryDetails(
            name = "World",
            description = "The world where the game will be played.",
            icon = Material.OAK_SAPLING,
            isRequired = true
    )
    private World world;

    @EntryDetails(
            name = "Lobby Location",
            description = "The location used when players are waiting for other players.",
            icon = Material.GRASS_BLOCK,
            isRequired = true
    )
    private Location lobby;

    @EntryDetails(
            name = "Teams",
            description = "Configure the teams of this game.",
            icon = Material.DIAMOND_SWORD,
            isRequired = true,
            min = 4, max = 4
    )
    private List<GameTeamConfig> teams = new ArrayList<>();

    @EntryDetails(
            name = "Game Size",
            description = "The size of the game. This will affect the number of players and the size of the map.",
            icon = Material.MAP,
            isRequired = true
    )
    @SerializedName("game_size")
    private IGame.GameSize gameSize = IGame.GameSize.SOLO;

}
