package net.itsthesky.terrawars.core.impl.game;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.itsthesky.terrawars.TerraWars;
import net.itsthesky.terrawars.api.model.biome.IBiome;
import net.itsthesky.terrawars.api.model.game.IGamePlayer;
import net.itsthesky.terrawars.api.services.IBiomeService;

import java.util.*;

@NoArgsConstructor
@Getter
@Setter
public class GameWaitingData {

    private List<List<IGamePlayer>> teamPlayers = new ArrayList<>();
    private Map<UUID, IBiome> playerVotes = new HashMap<>();
    private Map<IBiome, Integer> voteCount = new HashMap<>();

    /**
     * Gets the top voted biomes (up to count)
     * @param count The number of biomes to get
     * @return The top voted biomes
     */
    public List<IBiome> getTopVotedBiomes(int count) {
        // Sort biomes by vote count
        List<Map.Entry<IBiome, Integer>> sortedBiomes = new ArrayList<>(voteCount.entrySet());
        sortedBiomes.sort(Map.Entry.<IBiome, Integer>comparingByValue().reversed());

        // Get top biomes
        List<IBiome> result = new ArrayList<>();
        for (int i = 0; i < Math.min(count, sortedBiomes.size()); i++) {
            result.add(sortedBiomes.get(i).getKey());
        }

        // If we don't have enough biomes, add random ones from the available biomes
        if (result.size() < count) {
            List<IBiome> availableBiomes = new ArrayList<>(TerraWars.instance().serviceProvider().getService(IBiomeService.class).getAvailableBiomes());
            availableBiomes.removeAll(result);
            Collections.shuffle(availableBiomes);

            for (int i = 0; i < count - result.size() && i < availableBiomes.size(); i++) {
                result.add(availableBiomes.get(i));
            }
        }

        return result;
    }

}
