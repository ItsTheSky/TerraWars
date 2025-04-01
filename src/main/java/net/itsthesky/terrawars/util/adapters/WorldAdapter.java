package net.itsthesky.terrawars.util.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.IOException;

public class WorldAdapter extends TypeAdapter<World> {

    @Override
    public void write(JsonWriter jsonWriter, World world) throws IOException {
        if (world == null) {
            jsonWriter.nullValue();
            return;
        }

        jsonWriter.value(world.getName());
    }

    @Override
    public World read(JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        }

        String worldName = jsonReader.nextString();
        World world = Bukkit.getWorld(worldName);
        if (world == null)
            throw new IOException("World not found: " + worldName);
        return world;
    }
}
