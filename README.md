# 💀 BetterDeathMessages

```
  ██████╗ ███████╗████████╗████████╗███████╗██████╗
  ██╔══██╗██╔════╝╚══██╔══╝╚══██╔══╝██╔════╝██╔══██╗
  ██████╔╝█████╗     ██║      ██║   █████╗  ██████╔╝
  ██╔══██╗██╔══╝     ██║      ██║   ██╔══╝  ██╔══██╗
  ██████╔╝███████╗   ██║      ██║   ███████╗██║  ██║
  ╚═════╝ ╚══════╝   ╚═╝      ╚═╝   ╚══════╝╚═╝  ╚═╝

  ██████╗ ███████╗ █████╗ ████████╗██╗  ██╗
  ██╔══██╗██╔════╝██╔══██╗╚══██╔══╝██║  ██║
  ██║  ██║█████╗  ███████║   ██║   ███████║
  ██║  ██║██╔══╝  ██╔══██║   ██║   ██╔══██║
  ██████╔╝███████╗██║  ██║   ██║   ██║  ██║
  ╚═════╝ ╚══════╝╚═╝  ╚═╝   ╚═╝   ╚═╝  ╚═╝

  ███╗   ███╗███████╗███████╗███████╗ █████╗  ██████╗ ███████╗███████╗
  ████╗ ████║██╔════╝██╔════╝██╔════╝██╔══██╗██╔════╝ ██╔════╝██╔════╝
  ██╔████╔██║█████╗  ███████╗███████╗███████║██║  ███╗█████╗  ███████╗
  ██║╚██╔╝██║██╔══╝  ╚════██║╚════██║██╔══██║██║   ██║██╔══╝  ╚════██║
  ██║ ╚═╝ ██║███████╗███████║███████║██║  ██║╚██████╔╝███████╗███████║
  ╚═╝     ╚═╝╚══════╝╚══════╝╚══════╝╚═╝  ╚═╝ ╚═════╝ ╚══════╝╚══════╝
```

> 🌐 **English** · [Español](README.es.md)

> **Narrative death messages for Paper and Spigot, 1.17 to 26.x** — Replaces vanilla one-liners with a configurable library of MiniMessage templates split by cause (fall, lava, drown, void, pvp, per-mob, and many more), plus hover stats, daily broadcasts, persistent stats, and Last Words.

> 🏷️ Brought to you by **[TTS-Studio](https://github.com/TTS-STUDIOS)** — part of the unified TTS-Studio plugin suite.

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| 📝 **110+ default templates** | Spread across 17 cause categories (`fall`, `lava`, `fire`, `drown`, `void`, `explosion`, `magic`, `lightning`, `cramming`, `freeze`, `suffocation`, `cactus`, `sonic_boom`, `starvation`, `poison`, `wither_effect`, `pvp`) plus 42 per-mob categories (`ZOMBIE`, `SKELETON`, `CREEPER`, `SPIDER`, `ENDER_DRAGON`, `WITHER`, …) with a `DEFAULT` fallback. |
| 🎲 **Random template selection** | One template is picked at random from its category — no two deaths read the same. |
| 💬 **Last Words** | The player's last public chat message is woven into their death message via the `{last_words}` token. |
| 🖱️ **Hover stats** | Notable deaths (ENDER_DRAGON, WITHER, pvp) show a hover card with total deaths, kills, KDR, and longest killstreak. |
| 📢 **First-death-of-day broadcast** | Special announcement for the first death each day, with automatic midnight reset. |
| 💾 **Persistent player stats** | Deaths, kills, killstreaks, and KDR persisted per-UUID to a local `deaths.json` (Gson). |
| 📊 **PlaceholderAPI** | `%bdm_deaths_total%`, `%bdm_kills%`, `%bdm_kdr%`, `%bdm_killstreak%`. |
| 🎨 **TTS-Studio house style** | Suite-wide chat prefix and framed boot banner — feels like one product. |
| 📈 **bStats** | Anonymous usage metrics. |

---

## 🚀 Quick Start

```bash
# 1. Drop the jar into your plugins folder
cp betterdeathmessages-1.1.1.jar plugins/

# 2. Restart the server
#    Default config.yml and lang/<locale>.yml files are written on first enable.

# 3. Customize your death message templates
nano plugins/BetterDeathMessages/lang/en.yml

# 4. Hot-reload after any YAML edit — no restart needed
/bdm reload
```

> 💡 **Tip:** Each category accepts multiple template strings — the plugin picks one at random on every death, so your server never repeats the same message twice in a row.

---

## 💬 Last Words

The plugin remembers each player's most recent public chat line for a short, configurable window. When that player dies, the `{last_words}` token in their death template is replaced by what they said.

```yaml
# lang/<locale>.yml
messages:
  fall:
    - "<gray>{player} fell to their doom. Last words: <italic>\"{last_words}\"</italic></gray>"
```

> **Privacy.** Last Words only captures chat that reaches the full online roster — i.e. true public chat. Messages routed through ChatChannels or other channel plugins (`#trade`, staff chat, party chat), whispers, and `/msg` **never** enter the cache. The listener runs at `MONITOR` priority with `ignoreCancelled = true`, so it sees the final audience after every other plugin has had its say. Cached messages are dropped after `cache-seconds` (default 60s) and the cache lives only in memory — nothing is written to disk.

Tune it in `config.yml`:

```yaml
last-words:
  enabled: true        # set to false to disable capture entirely
  cache-seconds: 60    # how long a message remains usable as last words
  fallback: ""         # substituted when no recent message is cached
```

---

## 📦 What's in the box

- A **`lang/<locale>.yml`** with 110+ ready-to-use death message templates across 17 cause categories plus 42 per-mob categories, all MiniMessage-formatted (bundled `es` and `en`).
- A **`config.yml`** with inline comments covering Last Words, hover stats, first-death broadcasts, kill streaks, first blood, death sounds, broadcast radius, and bStats opt-out.
- **Persistent player stats** tracked per UUID in a local `deaths.json` (Gson) — no manual setup required.

---

## 🎮 Commands

| Command | Description | Permission | Default |
|---------|-------------|------------|---------|
| `/deaths [player]` | View a player's death stats | `bdm.use` | `true` |
| `/bdm reload` | Hot-reload `lang/<locale>.yml` and `config.yml` | `bdm.admin` | `op` |

---

## 🛠️ Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `bdm.use` | View death stats with `/deaths` | `true` |
| `bdm.admin` | Reload config and messages | `op` |

---

## 🔗 Integrations

| Plugin | Required? | What it does |
|--------|-----------|--------------|
| **PlaceholderAPI** | Optional | Exposes `%bdm_*%` placeholders for scoreboards, TAB, chat |

PlaceholderAPI is auto-detected on enable; placeholders silently no-op when the plugin is absent.

---

## ⚙️ Configuration overview

### Available tokens

These are substituted inside any template in `lang/<locale>.yml`:

| Token | Replaced with |
|-------|---------------|
| `{player}` | Victim's name |
| `{killer}` | Killer's name (PvP only; empty otherwise) |
| `{weapon}` | Weapon used (custom display name if set) |
| `{mob}` | Mob type (mob deaths only) |
| `{distance}` | Fall distance in blocks |
| `{biome}` | Biome at the death location |
| `{x}` / `{y}` / `{z}` | Death coordinates |
| `{dimension}` | Dimension (`overworld`, `nether`, `the_end`) |
| `{last_words}` | Player's last public chat message (or `fallback`) |

### lang/<locale>.yml excerpt

```yaml
messages:
  pvp:
    - "<red>{player}</red> was defeated by <gold>{killer}</gold> using <aqua>{weapon}</aqua>"
    - "<gray>{player} met their end at the hands of <gold>{killer}</gold></gray>"
  fall:
    - "<gray>{player} fell to their doom. Last words: <italic>\"{last_words}\"</italic></gray>"
    - "<yellow>{player} didn't stick the landing.</yellow>"
  mob:
    ENDER_DRAGON:
      - "<dark_purple>{player}</dark_purple> dared to challenge the dragon... and lost"
    DEFAULT:
      - "<red>{player}</red> was slain by a {mob}"

global-broadcast:
  enabled-causes: [ENDER_DRAGON, WITHER, pvp]
  hover-stats: true

first-death-of-day:
  enabled: true
  message: "<gold><player></gold> has the honour of dying first today!"
```

---

## 📊 PlaceholderAPI

| Placeholder | Returns |
|-------------|---------|
| `%bdm_deaths_total%` | Total deaths |
| `%bdm_kills%` | Total kills |
| `%bdm_kdr%` | Kill/death ratio |
| `%bdm_killstreak%` | Current killstreak |

---

## 🐛 Reporting bugs

Open an issue with:

- BetterDeathMessages version (`/version BetterDeathMessages`)
- Server type and version (Paper build, Java version)
- A minimal `lang/<locale>.yml` excerpt that reproduces the issue
- Server log excerpt — especially the stack trace if there is one

> Internal note: production support is tracked on the TTS-Studio issue board.

---

## 📜 License

Free to download and run on any server, under the **TTS-Studio Free License** (see `LICENSE`). Resale, rebranding and re-uploading elsewhere are not permitted — link to the official listing instead.

---

<sub>BetterDeathMessages is a TTS-Studio plugin · © TTS-Studio · making every death worth reading since 2024.</sub>
