package net.itsthesky.terrawars.core.services;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import net.itsthesky.terrawars.TerraWars;
import net.itsthesky.terrawars.api.services.IConfigService;
import net.itsthesky.terrawars.api.services.base.IService;
import net.itsthesky.terrawars.api.services.base.Service;
import net.itsthesky.terrawars.util.adapters.LocationAdapter;
import net.itsthesky.terrawars.util.adapters.WorldAdapter;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.logging.Logger;

@Service
public class ConfigService implements IConfigService, IService {

    private final File dataFolder;
    private final Gson gson;

    public ConfigService(@NotNull TerraWars plugin) {
        this.dataFolder = plugin.getDataFolder();
        this.gson = new Gson().newBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(Location.class, new LocationAdapter())
                .registerTypeAdapter(World.class, new WorldAdapter())
                .create();

        if (!dataFolder.exists())
            dataFolder.mkdirs();
    }

    @Override
    public <T> @NotNull T load(@NotNull Class<T> clazz, @NotNull InputStream stream) {
        try (Reader reader = new InputStreamReader(stream)) {
            return gson.fromJson(reader, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config from stream", e);
        }
    }

    @Override
    public <T> @NotNull T load(@NotNull Class<T> clazz, @NotNull String path) {
        File file = new File(dataFolder, path);
        if (!file.exists()) {
            throw new IllegalArgumentException("File " + file.getAbsolutePath() + " does not exist");
        }
        try (InputStream stream = new FileInputStream(file)) {
            return load(clazz, stream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config from " + file.getAbsolutePath(), e);
        }
    }

    @Override
    public void save(@NotNull Object object, @NotNull OutputStream stream) {
        try (Writer writer = new OutputStreamWriter(stream)) {
            gson.toJson(object, object.getClass(), writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save config to stream", e);
        }
    }

    @Override
    public @NotNull File getFile(@NotNull String... path) {
        return new File(dataFolder, String.join(File.separator, path));
    }

    @Override
    public void save(@NotNull Object object, @NotNull String path) {
        File file = new File(dataFolder, path);
        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();
        try (OutputStream stream = new FileOutputStream(file)) {
            save(object, stream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save config to " + file.getAbsolutePath(), e);
        }
    }
}
