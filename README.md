# Random Letter Survival

Paper-Plugin: Alle 3 Minuten wird ein zufälliger Buchstabe gezogen. Alle Blöcke, deren englischer Grundname mit diesem Buchstaben beginnt (z.B. `S` → Stone, Sand, Spruce Log …), verschwinden für 10 Sekunden – aber nur in dem Chunk, in dem ein Spieler steht.

Varianten zählen zum Grundblock: Grass Block → **D**irt, Polished Andesite → **A**ndesite, Mossy Cobblestone → **C**obblestone, Deepslate Iron Ore → **I**ron Ore. Gezogen werden nur Buchstaben, deren Blöcke in den Chunks der Spieler auch vorkommen.

## Voraussetzungen

- **Paper 26.3** (Spigot/Bukkit wird nicht unterstützt)
- **Java 25** auf dem Server
- Beim **ersten Start** braucht der Server Internet: Paper lädt die Kotlin-Bibliothek automatisch von Maven Central herunter (danach liegt sie im Ordner `libraries/`).

## Installation

### 1. Plugin-Datei bauen

```bash
git clone https://github.com/itsjefferyyyyy/random-letter-survival.git
cd random-letter-survival
./gradlew build          # Windows: gradlew.bat build
```

Die fertige Datei liegt danach unter `build/libs/random-letter-survival-1.0.0.jar`.

### 2. In den Server kopieren

1. Server stoppen.
2. Die `.jar`-Datei in den Ordner `plugins/` des Servers kopieren.
3. Server starten.

In der Konsole sollte erscheinen:

```
[RandomLetterSurvival] Enabling RandomLetterSurvival v1.0.0
```

Mit `/plugins` im Spiel kannst du prüfen, ob **RandomLetterSurvival** grün angezeigt wird.

Das Plugin startet den Timer automatisch – nach 3 Minuten läuft die erste Runde.

### 3. Einstellungen anpassen (optional)

Beim ersten Start wird `plugins/RandomLetterSurvival/config.yml` angelegt:

| Einstellung | Standard | Bedeutung |
|---|---|---|
| `interval-seconds` | `180` | Abstand zwischen zwei Runden |
| `duration-seconds` | `10` | Wie lange die Blöcke weg sind |
| `warning-seconds` | `10` | Vorwarnung im Chat (`0` = aus) |
| `blocks-per-tick` | `5000` | Wie viele Blöcke pro Tick entfernt/zurückgesetzt werden. Kleiner = weniger Lag, aber langsameres Verschwinden |
| `skip-block-entities` | `true` | Truhen, Öfen, Schilder usw. verschwinden nicht (kein Item-Verlust) |
| `worlds` | `[]` | Nur diese Welten, z.B. `[world, world_nether]`. Leer = alle |
| `excluded-materials` | Bedrock, Portale, Spawner … | Diese Blöcke verschwinden nie |
| `letter-aliases` | – | Eigene Sonderfälle, z.B. `MUD: DIRT` → Mud zählt als **D** |

Nach dem Ändern im Spiel `/rls reload` ausführen – ein Neustart ist nicht nötig.

## Befehle

Alle Befehle brauchen die Permission `randomletter.admin` (haben OPs automatisch).

| Befehl | Funktion |
|---|---|
| `/rls start` | Timer starten |
| `/rls stop` | Timer anhalten |
| `/rls trigger` | Sofort eine Runde mit zufälligem Buchstaben |
| `/rls trigger S` | Sofort eine Runde mit Buchstabe `S` (zum Testen) |
| `/rls reload` | `config.yml` neu laden |

Einem Nicht-OP die Rechte geben (z.B. mit LuckPerms):

```
/lp user <Name> permission set randomletter.admin true
```

## Gut zu wissen

- Steht ein Spieler beim Zurücksetzen dort, wo ein Block hinkommen würde, wird dieser Block **nicht** zurückgesetzt und bleibt weg.
- Blöcke, die Spieler in der Zwischenzeit in die Lücken setzen, werden nicht überschrieben.
- Beim normalen Stoppen des Servers werden alle verschwundenen Blöcke sofort zurückgesetzt. **Stürzt der Server** während der 10 Sekunden ab, bleiben die Blöcke weg.
- Kein `/reload` des ganzen Servers verwenden – lieber den Server neu starten.

## Probleme

| Problem | Lösung |
|---|---|
| `UnsupportedClassVersionError` in der Konsole | Server läuft nicht mit Java 25 → Java aktualisieren |
| `NoClassDefFoundError: kotlin/...` | Kotlin-Bibliothek konnte nicht geladen werden → Server beim ersten Start Internetzugang geben |
| Plugin taucht in `/plugins` nicht auf | Liegt die `.jar` direkt in `plugins/` (nicht in einem Unterordner)? Läuft der Server mit Paper? |
| Es passiert nichts | Ist ein Spieler online und nicht im Zuschauermodus? Steht er in einer Welt aus `worlds`? |
| Server laggt bei der Runde | `blocks-per-tick` in der Config verkleinern, z.B. auf `2000` |
