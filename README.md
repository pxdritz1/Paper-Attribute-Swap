# Paper Attribute Swap

Restores vanilla equipment attribute update timing on Paper 26.2.x. Requires Java 25.

## Configuration

The plugin creates `plugins/PaperAttributeSwap/config.yml` on first startup:

```yaml
language: en

attribute-swapping:
	enabled: true
```

Supported languages are `en` (English), `es` (Spanish), and `pt` (Portuguese). English is used if the configured language is unsupported.

`attribute-swapping.enabled` controls the shared vanilla timing restoration:

- `true`: disables Paper's extra equipment update calls during certain player actions. Normal vanilla equipment detection and attribute calculations remain active.
- `false`: leaves Paper's configured equipment update behavior unchanged.

This is intentionally one global switch. Paper exposes one shared setting for these extra update calls; it does not provide separate controls for Breach, Lunge, individual items, or equipment slots. Splitting those into independent toggles would require exploit-specific behavior rather than restoring the underlying vanilla mechanism.

Restart the server after changing `config.yml`. The setting is applied when the plugin enables and is not written back to Paper's global configuration file.

## How It Works

Paper 26.2 enables `unsupported-settings.update-equipment-on-player-actions` by default. Its patches call `Player.detectEquipmentUpdates()` at additional points while processing player actions. That detector compares current equipment with its previous snapshot and updates equipment effects and modifiers; moving the call earlier changes the vanilla timing window.

When enabled, this plugin sets `GlobalConfiguration.get().unsupportedSettings.updateEquipmentOnPlayerActions` to `false` in memory. The normal detector, item-provided modifiers, and vanilla attribute/attack calculations remain server-controlled. The plugin has no per-player state and does not special-case items, slots, enchantments, or attacks.

The setting is internal Paper code, not part of the public Bukkit/Paper API. Access is isolated in `PaperAttributeSwapCompatibility` using reflection. Startup fails if the expected class or members are unavailable, and the plugin refuses Minecraft versions outside 26.2.x.

Sources reviewed:

- [Paper 26.2 LivingEntity patch](https://github.com/PaperMC/Paper/blob/9240f586a4aa2623b3581e331817d527cbae2723/paper-server/patches/sources/net/minecraft/world/entity/LivingEntity.java.patch)
- [Paper 26.2 Player patch](https://github.com/PaperMC/Paper/blob/9240f586a4aa2623b3581e331817d527cbae2723/paper-server/patches/sources/net/minecraft/world/entity/player/Player.java.patch)
- [Paper 26.2 global configuration](https://github.com/PaperMC/Paper/blob/9240f586a4aa2623b3581e331817d527cbae2723/paper-server/src/main/java/io/papermc/paper/configuration/GlobalConfiguration.java)
- [Paper 26.2 packet handling patches](https://github.com/PaperMC/Paper/blob/9240f586a4aa2623b3581e331817d527cbae2723/paper-server/patches/sources/net/minecraft/server/network/ServerGamePacketListenerImpl.java.patch)

## Build and Install

Requires JDK 25 and access to the Paper Maven repository.

```sh
sh gradlew clean build
```

Install `build/libs/Paper Attribute Swap-1.0.0.jar` in the `plugins/` directory of a Paper 26.2.x server, then start or restart the server. With the feature enabled, the console should report that vanilla equipment attribute timing was restored.

## In-Game Verification

Compare a test Paper 26.2.x server with a vanilla 26.2.x reference using the same rules, difficulty, items, target, and input sequence. Record video or damage/velocity logs and repeat each sequence. Equipment swapping must be tested with real clients because the public API does not reproduce packet processing at the same instant.

1. **Normal attributes:** compare damage, attack speed, and displayed attributes for weapons and armor with modifiers, without rapid swapping. Results should match vanilla.
2. **Rapid swapping:** perform an equipment swap and attack sequence that reproduces vanilla Attribute Swapping; compare with the same sequence on vanilla.
3. **Breach:** against an armored target, repeat a Breach Swapping sequence known to work on the vanilla reference; compare damage and the equipment state observed by the attack.
4. **Lunge:** repeat a Lunge Swapping sequence with equipment and enchantments supported by the version; compare movement and results with vanilla.
5. **Equipment slots:** swap main hand, off hand, helmet, chestplate, leggings, and boots. For slots without a relevant combat interaction, check that attribute values and effects follow the equipped item as they do in vanilla.
6. **Multiple modifiers:** equip an item with several modifiers and verify values and operations after equipping, removing, and swapping it.
7. **No modifiers:** equip and remove items without modifiers; no unexpected attribute changes should occur.
8. **Consecutive swaps:** rapidly perform A → B → C → A → B → C, then wait a tick. Final values should reflect the current item without stale modifiers.
9. **Multiplayer:** have two players perform different sequences simultaneously. One player's attributes, damage, and effects must not affect the other.

This workspace does not include an executable Paper/vanilla server or Minecraft clients, so in-game behavior tests must be run in a game environment. Local verification consists of compiling and packaging the plugin.
