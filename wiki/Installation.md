> 🌐 **English** · [Español](es/Instalacion.md)

# 📦 Installation

This page walks you through getting **BetterDeathMessages** running on your Paper server from zero to operational.

---

## 🔧 Requirements

| Software | Minimum Version | Required? |
|----------|-----------------|-----------|
| [Paper](https://papermc.io/) or Spigot | **1.17 – 26.x** | ✅ Yes |
| Java | **16+** | ✅ Yes |
| [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) | 2.11.6+ | ⚡ Optional |

> ℹ️ Runs on Paper, Spigot and Folia. Minecraft 1.16 and older are not supported: the plugin refuses to load there.

---

## 🚀 Step-by-Step Installation

### Step 1 — Get the JAR

Build from source or grab `betterdeathmessages-1.0.0.jar` from the official release.

```bash
# To build from source:
git clone https://github.com/TTS-STUDIOS/mc-betterdeathmessages.git
cd mc-betterdeathmessages
mvn clean package
# JAR lands in target/betterdeathmessages-1.0.0.jar
```

### Step 2 — Deploy the JAR

```bash
cp betterdeathmessages-1.0.0.jar /your/server/plugins/

# Your plugins/ directory should now contain:
# plugins/
# ├── BetterDeathMessages-1.0.0.jar
# └── ...other plugins...
```

### Step 3 — First Start

Start or restart your server. BetterDeathMessages generates its data directory automatically:

```
plugins/BetterDeathMessages/
├── config.yml      ← language, last words, broadcasts, kill streaks, sounds, radius
├── lang/
│   ├── es.yml       ← 110+ death templates (Spanish) — edit this
│   └── en.yml       ← 110+ death templates (English)
└── deaths.json     ← auto-created on first death; persistent stats
```

You should see the TTS-Studio banner in console:

```
╔════════════════════════════════════════════════════════════╗
║                       T T S - S T U D I O                  ║
║   BetterDeathMessages v1.0.0                               ║
║   21 death message templates loaded · PAPI ✓ · ready in 84ms ║
╚════════════════════════════════════════════════════════════╝
```

### Step 4 — (Optional) Install PlaceholderAPI

If you want `%bdm_*%` placeholders in scoreboards, TAB lists, or chat formats:

```
/papi reload
```

PlaceholderAPI auto-detects BetterDeathMessages — there's no separate expansion to download. The plugin registers its own `bdm` expansion on enable.

### Step 5 — (Optional) Customize Messages

Open `plugins/BetterDeathMessages/messages.yml` and tweak templates to taste. See [Configuration](Configuration) for the full reference.

Apply changes with:

```
/bdm reload
```

No server restart needed.

---

## ✅ Post-Install Checklist

- [ ] JAR is in `plugins/`
- [ ] Server started at least once to generate `messages.yml`
- [ ] (Optional) PlaceholderAPI installed and `/papi reload` run
- [ ] (Optional) Templates customized in `messages.yml`
- [ ] `/bdm reload` works for admins (`bdm.admin` perm or OP)
- [ ] `/deaths <player>` shows the stats panel

---

## 🔄 Updating BetterDeathMessages

1. Stop the server
2. Replace the old JAR with the new one — **keep** `messages.yml` and `deaths.json`
3. Start the server
4. (If the changelog mentions new templates) merge new entries from the default `messages.yml` into yours

> 💡 **Tip:** Back up `plugins/BetterDeathMessages/deaths.json` before updating — it contains every player's lifetime kill/death stats.

---

## 🗑️ Uninstalling

1. Remove `BetterDeathMessages-*.jar` from `plugins/`
2. (Optional) delete `plugins/BetterDeathMessages/` to remove all configs and stats

> ⚠️ **Warning:** Deleting `deaths.json` is irreversible — there's no rebuild from server logs.

---

## 🆘 Troubleshooting

| Symptom | Likely Cause | Fix |
|---------|--------------|-----|
| Plugin not loading | Server older than 1.17 or Java older than 16 | Use Minecraft 1.17 or newer |
| Vanilla death messages still appear | Plugin disabled or `/reload` used | Check console; use `/bdm reload`, not `/reload` |
| Placeholders show `%bdm_kills%` literally | PAPI not installed or not reloaded | Install PAPI, run `/papi reload` |
| Hover stats missing on broadcast | `global-broadcast.hover-stats: false` | Set to `true` in `messages.yml` |
| First-death-of-day broadcast not firing | Feature disabled | Set `first-death-of-day.enabled: true` |

---

🏠 [Home](Home) · 🎮 [Commands & Permissions](Commands-and-Permissions) · ⚙️ [Configuration](Configuration) · 📊 [PlaceholderAPI](PlaceholderAPI)
