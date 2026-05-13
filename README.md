# FastEdit

Async WorldEdit-style plugin for **PowerNukkitX** (Bedrock servers).
MIT-licensed, single-jar, no native deps. Inspired by SimpleWorldEdit,
FastAsyncWorldEdit-PNX, EasyEdit, and Axiom.

## What you get

- **Selection** with a wooden axe (`//wand`) — left-click sets pos1,
  right-click sets pos2. Or `//pos1` / `//pos2` at your feet.
- **Shapes**: `//set`, `//replace`, `//walls`, `//sphere`, `//cyl`,
  `//pyramid`.
- **Clipboard**: `//copy`, `//cut`, `//paste`, `//rotate`, `//flip`.
- **Schematics**: `//schem save/load/list` — gzipped NBT, plain
  `.fschem` files under `plugins/FastEdit/schematics/`.
- **Brushes**: `//brush sphere|cube|cyl|smooth|clipboard …`, with an
  optional `//mask …`. Right-click anywhere to apply.
- **Move / stack**: `//move <n> [dir]`, `//stack <n> [dir]`.
- **Undo / redo**: `//undo [count]`, `//redo [count]` — keeps the last
  20 edits per player.

## How "async" works

Block setting on PowerNukkitX has to happen on the main thread, so we
split every edit in two phases:

1. **Plan** — runs on a virtual thread. We just compute the list of
   `(position, target state)` tuples. No world I/O.
2. **Apply** — runs on the main thread, but only `BLOCKS_PER_TICK = 8000`
   blocks per tick. Big edits stretch over several ticks and never
   stall the server. Each applied tuple captures the previous state
   into the undo buffer before writing.

Writes go through `Level#setBlockStateAt`, which skips physics/redstone
updates — exactly what world-edit operations want.

See `src/main/java/fr/fastedit/edit/EditEngine.java` for the engine.

## Patterns

Anywhere a `<pattern>` is expected:

```
stone                   single block
minecraft:stone         single block (namespaced)
50%stone,50%dirt        weighted random
stone,dirt,grass_block  equal-weight random
```

## Masks

Anywhere a `<mask>` is expected (`//replace`, `//mask`):

```
*               any block (default)
#air            air only
#solid          any non-air block
stone           only this block id
stone,dirt      any of these ids
!stone          anything except stone
```

## Build

```bash
./gradlew shadowJar
# produces build/libs/FastEdit-1.0.0.jar
```

If `repo.powernukkitx.cn` is unreachable, point at a local PNX jar:

```bash
FASTEDIT_PNX_JAR=/path/to/powernukkitx.jar ./gradlew shadowJar
```

Drop the resulting jar in your `plugins/` folder and restart.

## Permissions

```
fastedit.use         — every command and the wand (default: op)
fastedit.schematic   — //schem save|load|list      (default: op)
```

## Layout

```
fr.fastedit
├── FastEdit              — plugin entry point
├── math/                 — Vec3, Region (cuboid + iteration)
├── session/              — per-player Session, SessionManager
├── edit/                 — EditEngine, EditSession, UndoBuffer
├── block/                — Blocks, Pattern, Mask
├── shape/                — pure shape generators
├── clipboard/            — Clipboard, .fschem reader/writer
├── brush/                — Brush base + sphere/cube/cyl/smooth/clipboard
├── listener/             — WandListener (left-click pos1 / right-click pos2 / brush)
└── command/              — one class per command, all extend Cmd
```

Each command is a tiny class that parses its args, builds a planner
lambda, and hands it to `EditEngine.submit(...)`. That's the only
"hard" part — everything else is data.
