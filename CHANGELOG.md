# Changelog

## [1.4.3] — 2026-10-19

### Changed
- The plugin's author now shows as TTS-Studio (plugin list and PlaceholderAPI). Nothing else changes.

## [1.4.2] — 2026-10-14

### Added
- **Kills the killer did not land himself are credited to him**: an end crystal or TNT he set off, a fall or
  lava after his hit. New lists `messages.pvp-<cause>` (`pvp-explosion`, `pvp-fall`, `pvp-lava`, `pvp-void`…)
  and `messages.pvp-indirect`; without them the line comes from `pvp`.
- **Mobs with their own name are called by it** (a MythicMobs boss, a name tag, an NPC), with its colours.
  New list `messages.mob-named`; without it the mob's type list is used.
- **`{killer_papi:<placeholder>}` and `{player_papi:<placeholder>}`**: a PlaceholderAPI value of the killer or
  the victim inside a death line (for example the hearts of a lifesteal killer). A line whose value cannot be
  read is skipped.

### Changed
- **Weapons, mobs and biomes without a name of their own show in each player's game language**
  ("Espada de diamante", "Zombi") instead of the English name of the material or type. Older chat routes
  keep the English name as fallback. Items named with `item_name` (1.20.5+) keep that name.
- Spanish lines: mob names in Spanish, a typo, and streak lines that say "bajas" with one exclamation at most.

### Fixed
- **Citizens NPCs are not players**: a dying NPC (a combat-logger stand-in) no longer gets a death line, a
  death or a kill in the stats, and an NPC that kills a player reads as a named mob instead of PvP.
- **A plugin that kills a player on purpose can keep it quiet**: with the `bdm-silent` metadata on the player
  (set right before the kill), there is no death line and nothing goes to the stats. Meant for combat-logger
  penalties on rejoin, which would otherwise read "died in mysterious ways".

## [1.4.1] — 2026-10-12

### Fixed
- **Crashing into a wall with an elytra has its own death lines** instead of «died in mysterious ways».
  New `messages.kinetic` category, in English and Spanish.
- **New message categories reach servers that already edited their language file.** A missing key now
  falls back to the plugin's own lines instead of the generic one.

## [1.4.0] — 2026-10-12

### Changed
- **`{weapon}` keeps the weapon's own colours.** A named weapon shows in the message as it does on
  the item, gradient and symbols included. The name goes in as finished text, never read as
  MiniMessage, so a name typed on an anvil still cannot add tags to a message everyone sees.

### Added
- **`messages.pvp-unarmed`** (optional): the lines for a killer with an empty hand. With it, `pvp` can
  be all `{weapon}` lines and every armed kill names the weapon.

## [1.3.0] — 2026-10-08

### Added
- **Runs on Folia.** Every task now goes through the TTS-Studio scheduler: on Paper and Spigot nothing changes, and on Folia (`folia-supported: true`) each piece of work runs on the region that owns what it touches. Tested on Folia 1.21.11.

## [1.2.3] — 2026-10-05

### Security
- **A weapon renamed in an anvil can no longer put clickable tags in the death message.** The name went into the message sent to the whole server with its MiniMessage tags interpreted, so a sword named like a tag could show text that ran a command when clicked. Last words had the same hole behind a leading backslash. Both are now shown exactly as written.

### Fixed
- **`/deaths top` shows names for players who are offline** instead of the first characters of their id.
- **Two deaths at the same moment no longer damage `deaths.json`,** and a damaged file is set aside with a warning instead of stopping the plugin from starting.

## [1.2.2] — 2026-09-27

### Fixed
- **Death messages no longer fail on 1.21.3 and newer** when the biome is read (the biome type became an interface and the old call broke).
- **Last words on Paper.** Since 1.2.0 the quote in a death message came out as a block of
  internal text instead of what the player said. Spigot was not affected.

## [1.2.1] — 2026-09-26

### Fixed
- **On Spigot 1.20.5 and newer, `/deaths` and `/bdm` replies never reached the player**, and a
  death message with a hover card was lost too. The plugin carried its own copy of a JSON library
  that the server did not recognise. It now uses the server's own, and every message arrives.
  Paper was not affected.

## [1.2.0] — 2026-09-26

### Added
- **Runs on Paper and Spigot, from 1.17 to 26.x.** It used to need Paper 1.21 and Java 21.
  Tested on a clean server with a player joining, chatting and dying on Paper 1.17.1, 1.20.4,
  1.21.11, 26.1.2 and 26.2 and on Spigot 1.21.11. On Paper everything looks exactly as before;
  on Spigot the same messages, hover cards and colours are sent through a bundled chat library.

## [1.1.7] — 2026-09-25

### Fixed
- **Cactus, lightning and other deaths no longer fall into "died in mysterious ways".** Damage
  dealt by the `/damage` command or by other plugins arrives with cause `CUSTOM`, so only the
  generic lines were used. The death type is now read from the damage itself (cactus,
  lightning, freezing, explosions, the Warden…) and each one gets its own message.
- **Arrows are credited to whoever shot them.** A skeleton's arrow used to say "slain by a
  Arrow"; it now uses the Skeleton, Stray or Bogged lines, and `{distance}` is how far away
  the shooter stood instead of how far the victim had fallen.
- **"an Iron sword", not "a Iron sword".** In English, the "a" before `{weapon}` and `{mob}`
  now changes to "an" when the name starts with a vowel.
- **Biomes read as names.** `{biome}` shows "Lukewarm Ocean" instead of `lukewarm_ocean`.
- Sweet berry bushes no longer use the cactus lines.
- **Last words work with channel chat plugins.** A message only counted as public if the
  console was among its readers, and plugins like ChattyChannels hand the chat to players
  only, so nothing was ever captured. Public now means every online player can read it;
  local, staff and private channels are still left out.
- **No empty quotes or empty weapons.** "X's last words: — then Y arrived" came up when the
  player hadn't said anything, and "defeated by Y wielding" when Y fought bare-handed. Lines
  that need a value that isn't there are skipped.

## [1.1.5] — 2026-09-20

### Changed
- **Menu titles no longer repeat the plugin name.** `◈ Plugin › Section` is now
  `◈ Section`: measured with the game's own font, the name alone ate more than half
  of the 160 px a chest title has, so any real section came out cut off with an
  ellipsis. The diamond keeps the plugin's colour, and anything still too wide is
  trimmed to fit.

---

## [1.1.6] — 2026-09-21

### Fixed
- **Numbers no longer follow the server's system language.** `String.format` was used
  without a `Locale`, so the very same jar printed `$12.50` on an English machine and
  `$12,50` on a Spanish or French one, and the thousands separator flipped with it. Every
  player-facing number is written with `Locale.ROOT` now, and a test keeps it that way.

---

## [1.1.4] — 2026-09-12

### Fixed
- 🚨 **The plugin did not start on a server without Vault.** `VaultHook.setup()`
  read `Economy.class` with no guard, and resolving that class without Vault
  installed is a `NoClassDefFoundError` in `onEnable`: BetterDeathMessages
  disabled itself on any server that never asked for Vault. Vault is listed as a
  **soft** dependency and the resource sells it as optional — it was mandatory.
  The lookup now happens only after `isPluginEnabled("Vault")`.
- **How it came out:** booting every plugin of the suite on a bare Paper 1.21.11
  with none of the optional dependencies installed. It is the same family as the
  one caught in TradeForge the same day, and `paper-test` hides both because it
  has Vault and PlaceholderAPI installed.

---

All notable changes to BetterDeathMessages are documented here.

## [1.1.2] — 2026-08-26

### Changed
- **Default interface language is English** (`language: en`), and every `config.yml` comment is
  now in English. The build published on BuiltByBit was still shipping the Spanish default.

## [1.1.0] - 2026-05-14

### Added
- **Last Words** — captures each player's last public chat message and
  injects it into death templates via the new `{last_words}` token.
- `LastWordsCache` with configurable TTL (default 60s) and periodic
  janitor that purges expired entries every minute.
- `ChatCaptureListener` for `AsyncChatEvent` (priority `MONITOR`,
  `ignoreCancelled = true`) with a conservative public-chat policy:
  if any other plugin has narrowed the audience (whispers, channel
  chat, party chat, etc.), the message is silently discarded.
- New `last-words` section in `config.yml` (`enabled`, `cache-seconds`,
  `fallback`).
- Banner hook label `LastWords` shown on `onEnable` when the feature
  is active.

### Privacy
- Last Words only captures messages whose final audience is the full
  online roster + console. Whispers, `/msg`, and channel-scoped chat
  (e.g. ChatChannels `#trade`) never enter the cache.
- Cached messages are dropped after `cache-seconds` and on `onDisable`
  (the cache lives only in memory).

### Changed
- `MessagePicker` now performs `{last_words}` substitution. Player
  chat content is escaped for MiniMessage (`<` → `\<`) before
  insertion so user input cannot break or inject formatting.

## [1.0.0] - 2026-05-10

### Added
- Initial release with 110+ narrative death message templates across
  17 cause categories (fall, lava, fire, drown, void, explosion, magic,
  lightning, and more) plus 42 per-mob categories with a DEFAULT fallback.
- Random template selection per category.
- Hover stats on notable deaths (ENDER_DRAGON, WITHER, pvp) showing
  total deaths, kills, KDR, and longest killstreak.
- First-death-of-day broadcast with automatic midnight reset.
- Persistent per-player stats via Gson (`deaths.json`).
- `/deaths [player]` command.
- `/bdm reload` admin command.
- PlaceholderAPI expansion (`%bdm_deaths_total%`, `%bdm_kills%`,
  `%bdm_kdr%`, `%bdm_killstreak%`).
- bStats metrics (plugin ID 31359).
