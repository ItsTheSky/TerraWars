package net.itsthesky.terrawars.core.gui;

import lombok.Setter;
import net.itsthesky.terrawars.TerraWars;
import net.itsthesky.terrawars.api.services.IChatService;
import net.itsthesky.terrawars.api.services.base.IServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages entry handlers for different types.
 * This class is responsible for registering and retrieving handlers
 * for specific types.
 */
public class EntryHandlerManager {

    private final List<EntryHandler<?>> handlers = new ArrayList<>();
    /**
     * -- SETTER --
     *  Sets the service provider for this manager.
     *
     * @param serviceProvider The service provider
     */
    @Setter
    private IServiceProvider serviceProvider;
    
    /**
     * Registers a new handler.
     * @param handler The handler to register
     */
    public void registerHandler(@NotNull EntryHandler<?> handler) {
        handlers.add(handler);
    }
    
    /**
     * Gets a handler for the given type.
     * @param type The type to get a handler for
     * @return The handler, or null if no handler is found
     */
    @Nullable
    public EntryHandler<?> getHandlerForType(@NotNull Class<?> type) {
        return handlers.stream()
                .filter(handler -> handler.supports(type))
                .findFirst()
                .orElse(null);
    }

    /**
     * Gets the service provider for this manager.
     * @return The service provider
     */
    @Nullable
    public IServiceProvider getServiceProvider() {
        return serviceProvider;
    }
    
    /**
     * Gets the chat service from the service provider.
     * @return The chat service, or null if not available
     */
    @Nullable
    public IChatService getChatService() {
        if (serviceProvider == null) {
            return null;
        }
        
        try {
            return serviceProvider.getService(IChatService.class);
        } catch (Exception e) {
            return null;
        }
    }
}