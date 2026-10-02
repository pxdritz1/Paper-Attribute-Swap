package px.attribute.swap;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import org.bukkit.Bukkit;

final class PaperAttributeSwapCompatibility {
    private static final String PAPER_GLOBAL_CONFIGURATION = "io.papermc.paper.configuration.GlobalConfiguration";

    private PaperAttributeSwapCompatibility() {
    }

    static void restoreVanillaEquipmentTiming() {
        if (!Bukkit.getMinecraftVersion().startsWith("26.2")) {
            throw new IllegalStateException("This plugin requires Paper 26.2.x");
        }

        try {
            Class<?> configurationType = Class.forName(PAPER_GLOBAL_CONFIGURATION);
            Object configuration = configurationType.getMethod("get").invoke(null);
            Field unsupportedSettingsField = configurationType.getField("unsupportedSettings");
            Object unsupportedSettings = unsupportedSettingsField.get(configuration);
            Field updateEquipmentOnPlayerActions = unsupportedSettings.getClass().getField("updateEquipmentOnPlayerActions");
            updateEquipmentOnPlayerActions.setBoolean(unsupportedSettings, false);
            if (updateEquipmentOnPlayerActions.getBoolean(unsupportedSettings)) {
                throw new IllegalStateException("Paper kept updateEquipmentOnPlayerActions enabled");
            }
        } catch (ClassNotFoundException | NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Unable to restore vanilla equipment attribute timing on Paper 26.2.x", exception);
        }
    }
}