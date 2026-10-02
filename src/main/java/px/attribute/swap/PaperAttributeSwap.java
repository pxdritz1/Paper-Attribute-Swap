package px.attribute.swap;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import px.attribute.swap.managers.PluginManager;
import px.attribute.swap.listeners.PlayerListener;

public class PaperAttributeSwap extends JavaPlugin {
    private static final Set<String> SUPPORTED_LANGUAGES = Set.of("en", "es", "pt");
    private YamlConfiguration messages;
    private String language;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getLogger().info("Configuration file: " + getDataFolder().toPath().resolve("config.yml"));
        loadMessages();

        if (getConfig().getBoolean("attribute-swapping.enabled", true)) {
            try {
                PaperAttributeSwapCompatibility.restoreVanillaEquipmentTiming();
                getLogger().info(message("attribute-swapping.enabled"));
            } catch (IllegalStateException exception) {
                getLogger().severe(message("compatibility.failed"));
                throw exception;
            }
        } else {
            getLogger().info(message("attribute-swapping.disabled"));
        }
        
        // Initialize managers
        PluginManager.getInstance().initialize();
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        
        getLogger().info(message("plugin.enabled"));
    }

    @Override
    public void onDisable() {
        getLogger().info(language == null ? "Plugin disabled." : message("plugin.disabled"));
    }

    private void loadMessages() {
        String configuredLanguage = getConfig().getString("language", "en");
        language = configuredLanguage == null ? "en" : configuredLanguage.toLowerCase(Locale.ROOT);
        if (!SUPPORTED_LANGUAGES.contains(language)) {
            getLogger().warning("Unsupported language '" + configuredLanguage + "'; using English.");
            language = "en";
            getConfig().set("language", language);
            saveConfig();
        }

        try (InputStream resource = getResource("messages.yml")) {
            if (resource == null) {
                throw new IllegalStateException("Missing messages.yml resource");
            }
            messages = YamlConfiguration.loadConfiguration(new InputStreamReader(resource, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load messages.yml", exception);
        }
    }

    private String message(String key) {
        return messages.getString(language + "." + key, messages.getString("en." + key, key));
    }
}
