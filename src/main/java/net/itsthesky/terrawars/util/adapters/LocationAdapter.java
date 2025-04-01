package net.itsthesky.terrawars.util.adapters;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.IOException;

public class LocationAdapter extends TypeAdapter<Location> {

    @Override
    public void write(JsonWriter out, Location location) throws IOException {
        if (location == null) {
            out.nullValue();
            return;
        }

        out.beginObject();
        out.name("world").value(location.getWorld().getName());
        out.name("x").value(location.getX());
        out.name("y").value(location.getY());
        out.name("z").value(location.getZ());
        out.name("yaw").value(location.getYaw());
        out.name("pitch").value(location.getPitch());
        out.endObject();
    }

    @Override
    public Location read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        String world = null;
        double x = 0, y = 0, z = 0;
        double yaw = 0, pitch = 0;

        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            switch (name) {
                case "world":
                    world = in.nextString();
                    break;
                case "x":
                    x = in.nextDouble();
                    break;
                case "y":
                    y = in.nextDouble();
                    break;
                case "z":
                    z = in.nextDouble();
                    break;
                case "yaw":
                    yaw = in.nextDouble();
                    break;
                case "pitch":
                    pitch = in.nextDouble();
                    break;
                default:
                    in.skipValue();
                    break;
            }
        }
        in.endObject();

        return new Location(Bukkit.getWorld(world), x, y, z, (float) yaw, (float) pitch);
    }
}