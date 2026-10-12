> 🌐 **English** · [Español](es/Configuracion.md)

# ⚙️ Configuration

All death templates and broadcast settings live in **`plugins/BetterDeathMessages/messages.yml`**. After editing, apply changes with `/bdm reload` — no restart required.

`config.yml` exists only as a version pin and is intentionally minimal; you'll rarely touch it.

---

## 📁 File Layout

```
plugins/BetterDeathMessages/
├── config.yml      ← config-version pin (don't edit)
├── messages.yml    ← all templates + broadcast settings — edit this
└── deaths.json     ← persistent player stats (don't edit by hand)
```

---

## 🧱 messages.yml Structure

The file has three top-level sections:

| Section | Purpose |
|---------|---------|
| `messages.*` | Death-message templates grouped by category |
| `global-broadcast.*` | Which categories trigger server-wide broadcasts + hover toggle |
| `first-death-of-day.*` | Special prominent broadcast for the first death each server day |

---

## 📜 Death Categories

The plugin auto-resolves each death into one of these categories, then picks a random template from the matching list.

| Category | Triggered by | YAML path |
|----------|--------------|-----------|
| `fall` | Fall damage | `messages.fall` |
| `lava` | Lava / fire / fire-tick / hot-floor | `messages.lava` |
| `drown` | Drowning | `messages.drown` |
| `void` | The void | `messages.void` |
| `pvp` | Killed by another player (direct or projectile) | `messages.pvp` |
| `mob.<TYPE>` | Killed by a mob — `TYPE` is the Bukkit `EntityType` name | `messages.mob.ZOMBIE`, `messages.mob.ENDER_DRAGON`, etc. |
| `mob.DEFAULT` | Fallback for any mob with no specific list | `messages.mob.DEFAULT` |
| `unknown` | Anything else (custom damage, suffocation, etc.) | `messages.unknown` |

> 📝 **Note:** Category resolution lives in [`DeathCategoryResolver.java`](../src/main/java/com/ttsstudio/betterdeathmessages/message/DeathCategoryResolver.java). PvP takes priority over mob kills; mob kills take priority over environmental causes.

---

## 🏷️ Template Placeholders

Within any template string, the following tokens are replaced before MiniMessage parsing:

| Token | Replaced With | Example |
|-------|---------------|---------|
| `{player}` | Victim's name | `Steve` |
| `{killer}` | Player killer's name (empty for non-PvP) | `Alex` |
| `{weapon}` | The weapon's own name with its colours and symbols (a gradient stays a gradient). Without a name, the item's name **in each player's game language** ("Espada de diamante" for a Spanish client). A line with `{weapon}` is skipped when the killer's hand is empty | `Diamond sword` / `[⚔] Excalibur [⚔]` |
| `{mob}` | The mob's own name when it has one (a MythicMobs boss, a name tag, an NPC), else its type in each player's language (mob deaths only) | `Ender Dragon` / `Anzu` |
| `{killer_papi:<placeholder>}` | A PlaceholderAPI value of the killer, written without `%`. A line whose value cannot be read (no PAPI, no killer) is skipped | `{killer_papi:lifestealz_hearts}` → `14` |
| `{player_papi:<placeholder>}` | The same, for the victim | |
| `{distance}` | Fall distance in blocks (integer) | `47` |
| `{biome}` | Biome at the death location, in each player's language | `Plains` |

---

## 📝 Full Default `messages.yml`

```yaml
messages:
  fall:
    - "<red>{player}</red> <gray>descubrió que la gravedad no negocia</gray>"
    - "<red>{player}</red> <gray>falló su examen de paracaidismo desde</gray> <yellow>{distance}</yellow> <gray>bloques</gray>"
  lava:
    - "<red>{player}</red> <gray>se convirtió en parte del paisaje volcánico</gray>"
    - "<red>{player}</red> <gray>decidió nadar en lava... una sola vez</gray>"
  drown:
    - "<red>{player}</red> <gray>recordó tarde que no es un pez</gray>"
    - "<red>{player}</red> <gray>olvidó que respirar bajo el agua no es una habilidad humana</gray>"
  void:
    - "<red>{player}</red> <gray>exploró la nada eterna</gray>"
    - "<red>{player}</red> <gray>se convenció de que el abismo era una piscina</gray>"
  pvp:
    - "<red>{player}</red> <gray>fue derrotado por</gray> <gold>{killer}</gold> <gray>usando</gray> <aqua>{weapon}</aqua>"
    - "<gold>{killer}</gold> <gray>demostró su superioridad sobre</gray> <red>{player}</red>"
    - "<red>{player}</red> <gray>subestimó el poder de</gray> <gold>{killer}</gold>"
  # Optional: lines for a killer with an empty hand. With it, pvp can be all {weapon} lines.
  pvp-unarmed:
    - "<red>{player}</red> <gray>fue asesinado por</gray> <gold>{killer}</gold> <gray>a puñetazos</gray>"
  # Optional (1.4.2): a kill the killer did not land himself (an end crystal, TNT, a fall or lava
  # after a hit) is still credited to him. pvp-<cause> first, then pvp-indirect, then pvp.
  pvp-explosion:
    - "<red>{player}</red> <gray>saltó por los aires por culpa de</gray> <gold>{killer}</gold>"
  pvp-indirect:
    - "<red>{player}</red> <gray>murió intentando escapar de</gray> <gold>{killer}</gold>"
  # Optional (1.4.2): a mob with its own name. Without it, the mob's type list is used.
  mob-named:
    - "<red>{player}</red> <gray>fue abatido por</gray> {mob}"
  mob:
    ZOMBIE:
      - "<red>{player}</red> <gray>alimentó a un zombie hambriento</gray>"
    SKELETON:
      - "<red>{player}</red> <gray>recibió un flechazo... pero no de amor</gray>"
    CREEPER:
      - "<red>{player}</red> <gray>hizo amistad con un creeper. Solo una vez</gray>"
    SPIDER:
      - "<red>{player}</red> <gray>olvidó que las arañas también pican</gray>"
    ENDER_DRAGON:
      - "<dark_purple>{player}</dark_purple> <gray>osó desafiar al dragón... y perdió</gray>"
      - "<dark_purple>El Dragón del End</dark_purple> <gray>reclamó el alma de</gray> <dark_purple>{player}</dark_purple>"
    WITHER:
      - "<dark_gray>{player}</dark_gray> <gray>fue consumido por el Wither</gray>"
    DEFAULT:
      - "<red>{player}</red> <gray>fue derrotado por un</gray> <gray>{mob}</gray>"
  unknown:
    - "<red>{player}</red> <gray>murió de formas misteriosas</gray>"
    - "<red>{player}</red> <gray>encontró el fin de su aventura</gray>"

global-broadcast:
  enabled-causes: [ENDER_DRAGON, WITHER, pvp]
  hover-stats: true

first-death-of-day:
  enabled: true
  message: "<gold>¡{player}</gold> <yellow>tiene el honor de morir primero hoy!</yellow>"
```

---

## ➕ Adding Custom Variants

The plugin picks templates **uniformly at random** from each category's list — so every entry you add has equal weight.

### Add a new fall-death variant

```yaml
messages:
  fall:
    - "<red>{player}</red> <gray>descubrió que la gravedad no negocia</gray>"
    - "<red>{player}</red> <gray>cayó <yellow>{distance}</yellow> bloques. Sin paracaídas.</gray>"
    - "<red>{player}</red> <gray>aterrizó como un saco de patatas en <green>{biome}</green></gray>"  # new
```

### Add a new mob category

To handle blazes specifically, add a `BLAZE` key under `messages.mob`:

```yaml
messages:
  mob:
    BLAZE:
      - "<red>{player}</red> <gray>fue carbonizado por un Blaze</gray>"
      - "<gold>{player}</gold> <gray>aprendió que el Nether no perdona</gray>"
```

The key must match the [Bukkit `EntityType` name](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html) exactly (uppercase). If you spell it wrong, the plugin falls back to `messages.mob.DEFAULT`.

### Add a custom unknown-cause variant

```yaml
messages:
  unknown:
    - "<red>{player}</red> <gray>murió de formas misteriosas</gray>"
    - "<red>{player}</red> <gray>fue víctima de la entropía cósmica</gray>"
```

After any edit:

```
/bdm reload
```

---

## 📣 Global Broadcast Settings

```yaml
global-broadcast:
  enabled-causes: [ENDER_DRAGON, WITHER, pvp]
  hover-stats: true
```

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `enabled-causes` | list | `[ENDER_DRAGON, WITHER, pvp]` | Which categories broadcast server-wide with hover stats. Use the literal `pvp` token for player kills, or a mob `EntityType` name for boss-kills |
| `hover-stats` | bool | `true` | Attach a hover panel with victim's lifetime stats to broadcasts |

> 📝 **Note:** Death messages always go out via vanilla broadcast — the `enabled-causes` list only controls **which ones get the hover-stats panel attached.** Non-broadcast deaths still appear in chat, just without the hover overlay.

---

## 🌅 First Death of the Day

```yaml
first-death-of-day:
  enabled: true
  message: "<gold>¡{player}</gold> <yellow>tiene el honor de morir primero hoy!</yellow>"
```

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| `enabled` | bool | `true` | Set `false` to disable the feature |
| `message` | string | `"<gold>¡{player}</gold>..."` | The MiniMessage template. Supports `{player}` only |

The day boundary uses the server's local timezone. The tracker is in-memory and resets on plugin reload **or** at midnight (whichever comes first).

---

## 🎨 MiniMessage Primer

[MiniMessage](https://docs.advntr.dev/minimessage/format.html) is Adventure's modern chat formatting syntax. A quick cheat-sheet:

| Tag | Effect |
|-----|--------|
| `<red>...</red>` | Red text |
| `<#B33A3A>...</#B33A3A>` | Custom hex color (TTS-Studio crimson) |
| `<bold>` `<italic>` `<underlined>` `<strikethrough>` | Decorations |
| `<gradient:red:gold>...</gradient>` | Linear gradient |
| `<rainbow>...</rainbow>` | Rainbow gradient |
| `<click:run_command:'/spawn'>...</click>` | Clickable text |
| `<hover:show_text:'<gold>Hover!'>...</hover>` | Hover text |

### Example: a dramatic Wither death

```yaml
messages:
  mob:
    WITHER:
      - "<gradient:dark_gray:black><bold>{player}</bold></gradient> <gray>fue consumido por el</gray> <dark_red><bold>Wither</bold></dark_red>"
```

### Example: TTS-Studio crimson PvP variant

```yaml
messages:
  pvp:
    - "<#B33A3A>{player}</#B33A3A> <gray>cayó ante</gray> <#FFD700>{killer}</#FFD700>"
```

---

## 🔄 Reload Workflow

1. Edit `plugins/BetterDeathMessages/messages.yml`
2. Run `/bdm reload` in-game or console
3. The plugin re-reads the YAML and rebuilds the `MessagePicker`
4. The next death uses your new templates immediately

> 💡 **Tip:** If a template renders blank or shows the raw `{player}` token, you likely have invalid YAML — check the console for parse errors after reload.

---

🏠 [Home](Home) · 📦 [Installation](Installation) · 🎮 [Commands & Permissions](Commands-and-Permissions) · 📊 [PlaceholderAPI](PlaceholderAPI)
