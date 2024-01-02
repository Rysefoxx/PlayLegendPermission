package io.github.rysefoxx.manager;

import io.github.rysefoxx.PlayLegendPermission;
import io.github.rysefoxx.language.Language;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Properties;

public class LanguageManager {

    private final Plugin plugin;
    private final HashMap<String, HashMap<String, String>> translations = new HashMap<>();

    public LanguageManager(@NotNull Plugin plugin) {
        this.plugin = plugin;
        onLoad();
    }

    /**
     * Loads all messages from the plugin to the plugin folder.
     */
    public void onLoad() {
        for (Language language : Language.values()) {
            try {
                this.plugin.saveResource("messages_" + language.getCode() + ".properties", false);
            } catch (IllegalArgumentException exception) {
                this.plugin.getLogger().severe("Failed to save messages for language " + language + "!");
                continue;
            }
            cacheTranslations(plugin, language);
        }
    }

    /**
     * Caches all translations from the file to the {@link HashMap}.
     *
     * @param plugin   The {@link Plugin} to cache the translations from.
     * @param language The {@link Language} to cache the translations from.
     */
    private void cacheTranslations(@NotNull Plugin plugin, @NotNull Language language) {
        HashMap<String, String> translationsMap = new HashMap<>();
        File file = new File(plugin.getDataFolder(), "messages_" + language.getCode() + ".properties");

        if (!file.exists()) {
            plugin.getLogger().severe("Die Übersetzungsdatei für die Sprache " + language + " wurde nicht gefunden!");
            return;
        }

        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(file)) {
            properties.load(fis);
        } catch (IOException exception) {
            plugin.getLogger().severe("Die Übersetzungsdatei für die Sprache " + language + " ist invalide! " + exception.getMessage());
            return;
        }

        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            translationsMap.put(key, value);
        }

        this.translations.put(language.getCode(), translationsMap);
    }

    /**
     * Sends a translated message to the player.
     *
     * @param player     The {@link Player} to send the message to.
     * @param messageKey The message key to send.
     */
    public void sendTranslatedMessage(@NotNull Player player, @NotNull String messageKey) {
        Locale locale = PlayLegendPermission.isUnitTest() ? Locale.ENGLISH : player.locale();
        String translation = this.translations.get(Language.isLanguageSupported(locale.getLanguage()) ? locale.getLanguage() : Language.ENGLISH.getCode()).getOrDefault(messageKey, messageKey);
        player.sendMessage(translation);
    }

    /**
     * Returns a translated message.
     *
     * @param player     The {@link Player} to get the locale from.
     * @param messageKey The message key to get.
     * @return The translated message.
     */
    public @NotNull String getTranslatedMessage(@NotNull Player player, @NotNull String messageKey) {
        Locale locale = player.locale();
        return this.translations.get(Language.isLanguageSupported(locale.getLanguage()) ? locale.getLanguage() : Language.ENGLISH.getCode()).getOrDefault(messageKey, messageKey);
    }

    /**
     * Returns a translated message with replacements.
     *
     * @param player       The {@link Player} to get the locale from.
     * @param messageKey   The message key to get.
     * @param replacements The replacements to replace.
     * @return The translated message.
     */
    public @NotNull String getTranslatedMessage(@NotNull Player player, @NotNull String messageKey, String @NotNull ... replacements) {
        String message = getTranslatedMessage(player, messageKey);
        if (message.equals(messageKey)) return message;

        for (int i = 0; i < replacements.length; i++) {
            message = message.replace("{" + i + "}", replacements[i]);
        }

        return message;
    }
}