> 🌐 [English](../Configuration.md) · **Español**

# ⚙️ Configuración

Todas las plantillas de muerte y los ajustes de transmisión se encuentran en **`plugins/BetterDeathMessages/messages.yml`**. Después de editar, aplica los cambios con `/bdm reload` — sin reinicio necesario.

`config.yml` existe solo como fijador de versión y es intencionalmente mínimo; rara vez necesitarás tocarlo.

---

## 📁 Estructura de Archivos

```
plugins/BetterDeathMessages/
├── config.yml      ← fijador de versión de config (no editar)
├── messages.yml    ← todas las plantillas + ajustes de transmisión — edita este
└── deaths.json     ← estadísticas persistentes de jugadores (no editar a mano)
```

---

## 🧱 Estructura de messages.yml

El archivo tiene tres secciones de nivel superior:

| Sección | Propósito |
|---------|-----------|
| `messages.*` | Plantillas de mensajes de muerte agrupadas por categoría |
| `global-broadcast.*` | Qué categorías activan transmisiones a todo el servidor + control del hover |
| `first-death-of-day.*` | Transmisión especial y prominente para la primera muerte de cada día de servidor |

---

## 📜 Categorías de Muerte

El plugin resuelve automáticamente cada muerte en una de estas categorías y luego elige una plantilla aleatoria de la lista correspondiente.

| Categoría | Activada por | Ruta YAML |
|-----------|--------------|-----------|
| `fall` | Daño por caída | `messages.fall` |
| `lava` | Lava / fuego / tick de fuego / suelo caliente | `messages.lava` |
| `drown` | Ahogamiento | `messages.drown` |
| `void` | El vacío | `messages.void` |
| `pvp` | Muerto por otro jugador (directo o proyectil) | `messages.pvp` |
| `mob.<TIPO>` | Muerto por un mob — `TIPO` es el nombre `EntityType` de Bukkit | `messages.mob.ZOMBIE`, `messages.mob.ENDER_DRAGON`, etc. |
| `mob.DEFAULT` | Fallback para cualquier mob sin lista específica | `messages.mob.DEFAULT` |
| `unknown` | Cualquier otra cosa (daño personalizado, sofocación, etc.) | `messages.unknown` |

> 📝 **Nota:** La resolución de categorías vive en [`DeathCategoryResolver.java`](../src/main/java/com/ttsstudio/betterdeathmessages/message/DeathCategoryResolver.java). El PvP tiene prioridad sobre los kills de mob; los kills de mob tienen prioridad sobre las causas ambientales.

---

## 🏷️ Placeholders de Plantilla

Dentro de cualquier cadena de plantilla, los siguientes tokens se reemplazan antes del análisis MiniMessage:

| Token | Reemplazado por | Ejemplo |
|-------|-----------------|---------|
| `{player}` | Nombre de la víctima | `Steve` |
| `{killer}` | Nombre del jugador asesino (vacío para muertes sin PvP) | `Alex` |
| `{weapon}` | El nombre propio del arma con sus colores y símbolos (un degradado sigue siendo degradado), o el material en title-case. Una frase con `{weapon}` se salta si el asesino va con la mano vacía | `Diamond sword` / `[⚔] Excalibur [⚔]` |
| `{mob}` | Tipo del mob asesino en title-case (solo muertes por mob) | `Ender dragon` |
| `{distance}` | Distancia de caída en bloques (entero) | `47` |
| `{biome}` | Clave del bioma en la ubicación de la muerte | `minecraft:plains` |

---

## 📝 `messages.yml` Completo por Defecto

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
  # Opcional: frases para un asesino con la mano vacía. Con ellas, pvp puede llevar solo frases con {weapon}.
  pvp-unarmed:
    - "<red>{player}</red> <gray>fue asesinado por</gray> <gold>{killer}</gold> <gray>a puñetazos</gray>"
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

## ➕ Añadir Variantes Personalizadas

El plugin elige plantillas **uniformemente al azar** de la lista de cada categoría — por lo tanto, cada entrada que añadas tiene el mismo peso.

### Añadir una nueva variante de muerte por caída

```yaml
messages:
  fall:
    - "<red>{player}</red> <gray>descubrió que la gravedad no negocia</gray>"
    - "<red>{player}</red> <gray>cayó <yellow>{distance}</yellow> bloques. Sin paracaídas.</gray>"
    - "<red>{player}</red> <gray>aterrizó como un saco de patatas en <green>{biome}</green></gray>"  # nueva
```

### Añadir una nueva categoría de mob

Para gestionar blazes específicamente, añade una clave `BLAZE` bajo `messages.mob`:

```yaml
messages:
  mob:
    BLAZE:
      - "<red>{player}</red> <gray>fue carbonizado por un Blaze</gray>"
      - "<gold>{player}</gold> <gray>aprendió que el Nether no perdona</gray>"
```

La clave debe coincidir exactamente con el [nombre `EntityType` de Bukkit](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html) (en mayúsculas). Si lo escribes mal, el plugin usa como respaldo `messages.mob.DEFAULT`.

### Añadir una variante de causa desconocida personalizada

```yaml
messages:
  unknown:
    - "<red>{player}</red> <gray>murió de formas misteriosas</gray>"
    - "<red>{player}</red> <gray>fue víctima de la entropía cósmica</gray>"
```

Después de cualquier edición:

```
/bdm reload
```

---

## 📣 Ajustes de Transmisión Global

```yaml
global-broadcast:
  enabled-causes: [ENDER_DRAGON, WITHER, pvp]
  hover-stats: true
```

| Clave | Tipo | Por Defecto | Descripción |
|-------|------|-------------|-------------|
| `enabled-causes` | lista | `[ENDER_DRAGON, WITHER, pvp]` | Qué categorías transmiten a todo el servidor con stats en hover. Usa el token literal `pvp` para kills de jugador, o un nombre `EntityType` de mob para kills de jefe |
| `hover-stats` | bool | `true` | Adjuntar un panel de hover con las estadísticas de por vida de la víctima a las transmisiones |

> 📝 **Nota:** Los mensajes de muerte siempre salen mediante la transmisión vainilla — la lista `enabled-causes` solo controla **cuáles reciben el panel de hover adjunto**. Las muertes que no se transmiten globalmente siguen apareciendo en el chat, solo sin el overlay de hover.

---

## 🌅 Primera Muerte del Día

```yaml
first-death-of-day:
  enabled: true
  message: "<gold>¡{player}</gold> <yellow>tiene el honor de morir primero hoy!</yellow>"
```

| Clave | Tipo | Por Defecto | Descripción |
|-------|------|-------------|-------------|
| `enabled` | bool | `true` | Establece `false` para deshabilitar la función |
| `message` | string | `"<gold>¡{player}</gold>..."` | La plantilla MiniMessage. Solo soporta `{player}` |

El límite del día usa la zona horaria local del servidor. El rastreador está en memoria y se reinicia al recargar el plugin **o** a medianoche (lo que ocurra primero).

---

## 🎨 Introducción a MiniMessage

[MiniMessage](https://docs.advntr.dev/minimessage/format.html) es la sintaxis de formato de chat moderno de Adventure. Una guía rápida de referencia:

| Etiqueta | Efecto |
|---------|--------|
| `<red>...</red>` | Texto rojo |
| `<#B33A3A>...</#B33A3A>` | Color hex personalizado (carmesí de TTS-Studio) |
| `<bold>` `<italic>` `<underlined>` `<strikethrough>` | Decoraciones |
| `<gradient:red:gold>...</gradient>` | Gradiente lineal |
| `<rainbow>...</rainbow>` | Gradiente arcoíris |
| `<click:run_command:'/spawn'>...</click>` | Texto clicable |
| `<hover:show_text:'<gold>Hover!'>...</hover>` | Texto al pasar el cursor |

### Ejemplo: una dramática muerte por Wither

```yaml
messages:
  mob:
    WITHER:
      - "<gradient:dark_gray:black><bold>{player}</bold></gradient> <gray>fue consumido por el</gray> <dark_red><bold>Wither</bold></dark_red>"
```

### Ejemplo: variante PvP con carmesí de TTS-Studio

```yaml
messages:
  pvp:
    - "<#B33A3A>{player}</#B33A3A> <gray>cayó ante</gray> <#FFD700>{killer}</#FFD700>"
```

---

## 🔄 Flujo de Recarga

1. Edita `plugins/BetterDeathMessages/messages.yml`
2. Ejecuta `/bdm reload` en el juego o en la consola
3. El plugin relee el YAML y reconstruye el `MessagePicker`
4. La siguiente muerte usa tus nuevas plantillas inmediatamente

> 💡 **Consejo:** Si una plantilla se renderiza vacía o muestra el token `{player}` literal, probablemente tengas YAML inválido — comprueba la consola en busca de errores de análisis después de la recarga.

---

🏠 [Inicio](Home.md) · 📦 [Instalación](Instalacion.md) · 🎮 [Comandos y Permisos](Comandos-y-Permisos.md) · 📊 [PlaceholderAPI](PlaceholderAPI.md)
