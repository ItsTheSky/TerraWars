package net.itsthesky.terrawars.core.config;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.itsthesky.terrawars.api.model.game.generator.GameGeneratorType;
import org.bukkit.Location;

@Getter
@Setter
@NoArgsConstructor
public class GameGeneratorConfig {

    @SerializedName("generator_type")
    private GameGeneratorType generatorType = GameGeneratorType.DIAMOND;

    @SerializedName("generator_location")
    private Location generatorLocation = null;

}
