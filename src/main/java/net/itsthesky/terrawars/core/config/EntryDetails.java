package net.itsthesky.terrawars.core.config;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface EntryDetails {

    @NotNull String name();

    @NotNull String description() default "";

    @NotNull Material icon() default Material.STONE;

    boolean isRequired() default false;

    //region List Entries
    int min() default -1;
    int max() default -1;
    //endregion

}
