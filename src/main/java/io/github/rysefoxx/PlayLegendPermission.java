package io.github.rysefoxx;

import io.github.rysefoxx.command.CommandGroup;
import io.github.rysefoxx.database.AsyncDatabaseManager;
import io.github.rysefoxx.database.ConnectionManager;
import io.github.rysefoxx.database.DatabaseTableManager;
import io.github.rysefoxx.manager.GroupManager;
import io.github.rysefoxx.manager.LanguageManager;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

@NoArgsConstructor
@Getter
public class PlayLegendPermission extends JavaPlugin {

    @Getter
    private static boolean unitTest = false;
    private ConnectionManager connectionManager;
    private AsyncDatabaseManager asyncDatabaseManager;
    private DatabaseTableManager databaseTableManager;

    private GroupManager groupManager;
    private LanguageManager languageManager;

    @Override
    public void onEnable() {
        initializeManagers();
        initializeCommands();
    }

    @Override
    public void onDisable() {
        this.connectionManager.closeConnection();
        this.asyncDatabaseManager.shutdownExecutorService();
    }

    /**
     * Constructor for unit tests.
     *
     * @param loader      The plugin loader.
     * @param description The plugin description.
     * @param dataFolder  The data folder.
     * @param file        The plugin file.
     */
    @SuppressWarnings("all")
    protected PlayLegendPermission(@NotNull JavaPluginLoader loader, @NotNull PluginDescriptionFile description, @NotNull File dataFolder, @NotNull File file) {
        super(loader, description, dataFolder, file);
        unitTest = true;
    }

    /**
     * Initializes all managers.
     */
    private void initializeManagers() {
        this.languageManager = new LanguageManager(this);
        this.connectionManager = new ConnectionManager(this);
        this.asyncDatabaseManager = new AsyncDatabaseManager();
        this.databaseTableManager = new DatabaseTableManager(this, this.connectionManager);
        this.groupManager = new GroupManager(this, this.connectionManager, this.asyncDatabaseManager);
    }

    /**
     * Initializes all commands.
     */
    private void initializeCommands() {
        Objects.requireNonNull(getCommand("group")).setExecutor(new CommandGroup(this.groupManager, this.languageManager));
    }
}