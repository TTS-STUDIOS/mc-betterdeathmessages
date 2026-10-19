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

> 🌐 [English](README.md) · **Español**

> **Mensajes de muerte narrativos para Paper y Spigot, de la 1.17 a la 26.x** — Reemplaza los mensajes de una línea de la vanilla con una librería configurable de plantillas MiniMessage divididas por causa (caída, lava, ahogamiento, vacío, pvp, por mob y muchas más), más stats en hover, broadcasts diarios, stats persistentes y Últimas Palabras.

> 🏷️ Hecho por **[TTS-Studio](https://github.com/TTS-STUDIOS)** — parte de la suite unificada de plugins TTS-Studio.

---

## ✨ Funcionalidades

| Funcionalidad | Descripción |
|---------------|-------------|
| 📝 **110+ plantillas por defecto** | Distribuidas entre 17 categorías de causa (`fall`, `lava`, `fire`, `drown`, `void`, `explosion`, `magic`, `lightning`, `cramming`, `freeze`, `suffocation`, `cactus`, `sonic_boom`, `starvation`, `poison`, `wither_effect`, `pvp`) más 42 categorías por mob (`ZOMBIE`, `SKELETON`, `CREEPER`, `SPIDER`, `ENDER_DRAGON`, `WITHER`, …) con un fallback `DEFAULT`. |
| 🎲 **Selección aleatoria de plantillas** | Se elige una plantilla al azar de su categoría — ninguna muerte se leerá igual dos veces. |
| 💬 **Últimas Palabras** | El último mensaje público del jugador se incorpora a su mensaje de muerte mediante el token `{last_words}`. |
| 🖱️ **Stats en hover** | Las muertes notables (ENDER_DRAGON, WITHER, pvp) muestran una tarjeta hover con muertes totales, kills, KDR y racha máxima. |
| 📢 **Broadcast de primera muerte del día** | Anuncio especial para la primera muerte de cada día, con reset automático a medianoche. |
| 💾 **Stats persistentes** | Muertes, kills, rachas y KDR persistidos por UUID en un `deaths.json` local (Gson). |
| 📊 **PlaceholderAPI** | `%bdm_deaths_total%`, `%bdm_kills%`, `%bdm_kdr%`, `%bdm_killstreak%`. |
| 🎨 **Estilo TTS-Studio** | Prefijo de chat de la suite y banner de consola enmarcado — se siente como un solo producto. |
| 📈 **bStats** | Métricas de uso anónimas. |

---

## 🚀 Inicio rápido

```bash
# 1. Coloca el jar en tu carpeta de plugins
cp betterdeathmessages-1.1.1.jar plugins/

# 2. Reinicia el servidor
#    config.yml y los lang/<locale>.yml por defecto se generan al primer arranque.

# 3. Personaliza tus plantillas de mensajes de muerte
nano plugins/BetterDeathMessages/lang/es.yml

# 4. Recarga en caliente tras cualquier cambio en los YAML — sin reinicio
/bdm reload
```

> 💡 **Consejo:** Cada categoría acepta múltiples plantillas — el plugin elige una al azar en cada muerte, así tu servidor nunca repite el mismo mensaje dos veces seguidas.

---

## 💬 Últimas Palabras

El plugin recuerda la última línea de chat público de cada jugador durante una ventana corta y configurable. Cuando ese jugador muere, el token `{last_words}` en su plantilla de muerte se reemplaza por lo que dijo.

```yaml
# lang/<locale>.yml
messages:
  fall:
    - "<gray>{player} cayó a su perdición. Últimas palabras: <italic>\"{last_words}\"</italic></gray>"
```

> **Privacidad.** Últimas Palabras solo captura el chat que llega a todos los jugadores online — es decir, el chat público verdadero. Los mensajes enrutados a través de ChatChannels u otros plugins de canales (`#trade`, chat de staff, chat de party), los susurros y `/msg` **nunca** entran en la caché. El listener se ejecuta con prioridad `MONITOR` e `ignoreCancelled = true`, por lo que ve la audiencia final tras que todos los demás plugins hayan tenido su turno. Los mensajes en caché se eliminan tras `cache-seconds` (por defecto 60 s) y la caché solo vive en memoria — nada se escribe en disco.

Configúralo en `config.yml`:

```yaml
last-words:
  enabled: true        # pon false para deshabilitar la captura por completo
  cache-seconds: 60    # cuánto tiempo permanece usable un mensaje como últimas palabras
  fallback: ""         # sustituido cuando no hay ningún mensaje en caché reciente
```

---

## 📦 Qué incluye

- Un **`lang/<locale>.yml`** con 110+ plantillas de mensajes de muerte listas para usar en 17 categorías de causa más 42 categorías por mob, todas formateadas con MiniMessage (incluye `es` y `en`).
- Un **`config.yml`** con comentarios en línea que cubren Últimas Palabras, stats en hover, broadcasts de primera muerte, rachas de kills, primera sangre, sonidos de muerte, radio de broadcast y opt-out de bStats.
- **Stats de jugador persistentes** rastreados por UUID en un `deaths.json` local (Gson) — sin configuración manual necesaria.

---

## 🎮 Comandos

| Comando | Descripción | Permiso | Por defecto |
|---------|-------------|---------|-------------|
| `/deaths [jugador]` | Ver las stats de muerte de un jugador | `bdm.use` | `true` |
| `/bdm reload` | Recarga en caliente `lang/<locale>.yml` y `config.yml` | `bdm.admin` | `op` |

---

## 🛠️ Permisos

| Permiso | Descripción | Por defecto |
|---------|-------------|-------------|
| `bdm.use` | Ver stats de muerte con `/deaths` | `true` |
| `bdm.admin` | Recargar config y messages | `op` |

---

## 🔗 Integraciones

| Plugin | ¿Requerido? | Qué hace |
|--------|-------------|----------|
| **PlaceholderAPI** | Opcional | Expone placeholders `%bdm_*%` para scoreboards, TAB, chat |

PlaceholderAPI se detecta automáticamente al arrancar; los placeholders no hacen nada silenciosamente cuando el plugin está ausente.

---

## ⚙️ Resumen de configuración

### Tokens disponibles

Estos se sustituyen dentro de cualquier plantilla en `lang/<locale>.yml`:

| Token | Se reemplaza por |
|-------|-----------------|
| `{player}` | Nombre de la víctima |
| `{killer}` | Nombre del asesino (solo PvP; vacío en caso contrario) |
| `{weapon}` | Arma usada (nombre de display personalizado si está definido) |
| `{mob}` | Tipo de mob (solo muertes por mob) |
| `{distance}` | Distancia de caída en bloques |
| `{biome}` | Bioma en la ubicación de la muerte |
| `{x}` / `{y}` / `{z}` | Coordenadas de la muerte |
| `{dimension}` | Dimensión (`overworld`, `nether`, `the_end`) |
| `{last_words}` | Último mensaje de chat público del jugador (o `fallback`) |

### Extracto de lang/<locale>.yml

```yaml
messages:
  pvp:
    - "<red>{player}</red> fue derrotado por <gold>{killer}</gold> usando <aqua>{weapon}</aqua>"
    - "<gray>{player} encontró su fin a manos de <gold>{killer}</gold></gray>"
  fall:
    - "<gray>{player} cayó a su perdición. Últimas palabras: <italic>\"{last_words}\"</italic></gray>"
    - "<yellow>{player} no encajó bien el aterrizaje.</yellow>"
  mob:
    ENDER_DRAGON:
      - "<dark_purple>{player}</dark_purple> se atrevió a desafiar al dragón... y perdió"
    DEFAULT:
      - "<red>{player}</red> fue derrotado por un {mob}"

global-broadcast:
  enabled-causes: [ENDER_DRAGON, WITHER, pvp]
  hover-stats: true

first-death-of-day:
  enabled: true
  message: "<gold><player></gold> tiene el honor de morir primero hoy!"
```

---

## 📊 PlaceholderAPI

| Placeholder | Devuelve |
|-------------|----------|
| `%bdm_deaths_total%` | Muertes totales |
| `%bdm_kills%` | Kills totales |
| `%bdm_kdr%` | Ratio kill/muerte |
| `%bdm_killstreak%` | Racha de kills actual |

---

## 🐛 Reportar bugs

Abre un issue con:

- Versión de BetterDeathMessages (`/version BetterDeathMessages`)
- Tipo y versión del servidor (build de Paper, versión de Java)
- Un extracto mínimo de `lang/<locale>.yml` que reproduzca el problema
- Extracto del log del servidor — especialmente el stack trace si hay uno

> Nota interna: el soporte de producción se gestiona en el tablero de issues de TTS-Studio.

---

## 📜 Licencia

BetterDeathMessages se distribuye bajo la **licencia open-source de TTS-Studio** como parte de la suite gratuita de SpigotMC. Consulta `LICENSE` para los términos completos.

---

<sub>BetterDeathMessages es un plugin de TTS-Studio · © TTS-Studio · haciendo que cada muerte valga la pena leerla desde 2024.</sub>
