package net.itsthesky.terrawars.api.services.base;

/**
 * This interface is a marker for all services in the application.
 * Although it is optional for a service to implement this interface,
 * it cna be useful so the {@link IServiceProvider service provider} may
 * call additional methods on the service such as {@link #init} or {@link #destroy}.
 */
public interface IService {

    /**
     * This method is called when the service is initialized.
     * It is a good place to register commands, listeners, etc.
     */
    default void init() {
        // Default implementation does nothing
    }

    /**
     * This method is called when the service is destroyed.
     * It is a good place to unregister commands, listeners, etc.
     */
    default void destroy() {
        // Default implementation does nothing
    }

}
