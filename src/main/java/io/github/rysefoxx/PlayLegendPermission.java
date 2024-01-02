package io.github.rysefoxx;

import io.github.rysefoxx.command.CommandGroup;
import io.github.rysefoxx.database.AsyncDatabaseManager;
import io.github.rysefoxx.database.ConnectionManager;
import io.github.rysefoxx.database.DatabaseTableManager;
import io.github.rysefoxx.listener.ConnectionListener;
import io.github.rysefoxx.listener.SignListener;
import io.github.rysefoxx.manager.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

/**
 * @author Rysefoxx
 * @since 02.01.2024
 */
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
    private GroupMemberManager groupMemberManager;
    private GroupPermissionManager groupPermissionManager;
    private ScoreboardManager scoreboardManager;

    @Override
    public void onEnable() {
        initializeManagers();
        initializeCommands();
        initializeListeners();
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
        connectionManager = new ConnectionManager(this);
        asyncDatabaseManager = new AsyncDatabaseManager();
        databaseTableManager = new DatabaseTableManager(this, connectionManager);
        languageManager = new LanguageManager(this);
        groupPermissionManager = new GroupPermissionManager(this, connectionManager, asyncDatabaseManager);
        groupManager = new GroupManager(this, connectionManager, asyncDatabaseManager, groupPermissionManager);
        groupMemberManager = new GroupMemberManager(this, connectionManager, asyncDatabaseManager, groupManager, groupPermissionManager, languageManager);
        scoreboardManager = new ScoreboardManager(groupMemberManager, languageManager);
        groupMemberManager.setScoreboardManager(scoreboardManager);
    }

    /**
     * Initializes all commands.
     */
    private void initializeCommands() {
        CommandGroup commandGroup = new CommandGroup(groupManager, groupMemberManager, groupPermissionManager, languageManager, scoreboardManager);
        Objects.requireNonNull(getCommand("group")).setExecutor(commandGroup);
        Objects.requireNonNull(getCommand("group")).setTabCompleter(commandGroup);
    }

    /**
     * Initializes all listeners.
     */
    private void initializeListeners() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new ConnectionListener(this, groupMemberManager, groupPermissionManager, languageManager, scoreboardManager), this);
        pluginManager.registerEvents(new SignListener(groupMemberManager), this);
    }
}