# FastEdit

Async WorldEdit-style plugin for PowerNukkitX (Minecraft Bedrock servers).
Wand, brushes, schematics, undo/redo — without the lag.

---

## Install

1. Drop `FastEdit-1.0.0.jar` into your server's `plugins/` folder.
2. Restart the server.
3. In-game: `//wand` to get the wooden axe.

That's it. No config file, no permissions to set up (commands default to op).

> Every command works with both syntaxes: `/wand` *and* `//wand`. The double
> slash is rewritten to single slash transparently, so muscle-memory from
> Java's WorldEdit still works.

---

## Quick start

```
//wand              # gives you the FastEdit wooden axe
[left-click]        # sets position 1 on the clicked block
[right-click]       # sets position 2
//size              # shows the size of your selection
//set stone         # fills it with stone
//undo              # changed your mind
```

You can also stand somewhere and use `//pos1` / `//pos2` instead of the wand.

---

## Selecting

| Command | What it does |
|---|---|
| `//wand` | Get the wooden axe. Break = pos1, right-click = pos2. |
| `//pos1` | Set pos1 to your current block. |
| `//pos2` | Set pos2 to your current block. |
| `//sel` | Show the current selection. |
| `//sel clear` | Clear it. |
| `//size` | Show width × height × length and the block count. |
| `//expand <n> [dir]` | Grow the selection (see below). |
| `//expandrod` | Get the blaze rod that grows it by clicking. |

---

## Building shapes

Everything below operates on your current selection (or for `//sphere`,
`//cyl`, `//pyramid`, at your feet):

```
//set <pattern>                          fill the selection
//replace <mask> <pattern>               replace blocks matching the mask
//walls <pattern>                        outer walls of the selection
//sphere <pattern> <radius> [hollow]     sphere at your feet
//cyl <pattern> <radius> <height> [hollow]
//pyramid <pattern> <size> [hollow]
```

Examples:

```
//set oak_planks
//set 70%stone,30%cobblestone            ~70% stone, ~30% cobble
//replace dirt grass_block               only swap dirt blocks
//replace #air glass                     fill air with glass
//walls bricks
//sphere glowstone 6
//cyl water 4 12 hollow                  hollow water cylinder
//pyramid sandstone 8
```

### Patterns

What you write as `<pattern>`:

| Syntax | Meaning |
|---|---|
| `stone` | a single block |
| `minecraft:stone` | same, fully qualified |
| `50%stone,50%dirt` | weighted random |
| `stone,dirt,grass_block` | equal-weight random |

### Masks

What you write as `<mask>` (e.g. for `//replace`):

| Syntax | Meaning |
|---|---|
| `*` | any block |
| `#air` | only air |
| `#solid` | anything that isn't air |
| `stone` | only this block |
| `stone,dirt` | any of these |
| `!stone` | anything *except* stone |

---

## Clipboard

```
//copy                       copy the selection (anchor = where you stand)
//cut                        same as copy, then clears the selection
//paste                      paste at your feet
//paste -noair               paste but keep existing blocks where the clip is air
//rotate <90|180|270>        rotate clipboard around Y
//flip <x|y|z>               mirror the clipboard
```

Default `//paste` *overwrites everything* in the paste box, including with
air blocks from the clipboard. Use `-noair` if you want to overlay the
clipboard on existing terrain without clearing the gaps.

---

## Move & stack

```
//move <amount> [direction]      move the selection (and update pos1/pos2)
//stack <count> [direction]      duplicate the selection N times in a row
```

`direction` is one of: `up`, `down`, `north`, `south`, `east`, `west`, or
`me` (the direction you're facing — default).

```
//stack 5 east        copy your selection 5 times to the east
//move 10 up          push the selection 10 blocks up
```

---

## Expand the selection

Grow your current selection without re-selecting it — same idea as Java
WorldEdit's `//expand`.

```
//expand <amount> [direction]            grow one side
//expand <amount> <reverse> [direction]  grow both opposite sides at once
//expand <amount> n,w                    several directions in one go
//expand vert                            stretch to the world's build limits
```

`direction` is the same set as `//move`: `up`, `down`, `north`, `south`,
`east`, `west`, or `me` / `back` (relative to where you're looking — `me`
is the default if you omit it, and it accounts for looking up or down).

```
//expand 10 up        raise the top of the selection by 10
//expand 5 5 up       add 5 above and 5 below
//expand 8 me         push it 8 blocks the way you're facing
//expand vert         floor-to-ceiling on the same footprint
```

### Expand rod

```
//expandrod
[click any block face]
```

`//expandrod` gives you a **blaze rod**. Left- or right-click a block and
the selection grows **1 block toward the face you clicked** — top face
grows it up, north face grows it north, and so on. Handy for nudging a
selection out by hand instead of re-typing coordinates. (You need a
selection first; set it with `//wand`, `//pos1`/`//pos2`.)

---

## Undo / Redo

```
//undo [count]        undo the last N edits (default: 1)
//redo [count]        redo
```

Each player keeps the last **20 edits** in memory. Cleared on server restart.

---

## Schematics

FastEdit reads three formats:

| Extension | Origin | Notes |
|---|---|---|
| `.mcstructure` | Bedrock native | The default save format. |
| `.schem` | Sponge (WorldEdit / FAWE / Axiom) | Java-edition blocks are translated. |
| `.schematic` | Legacy MCEdit | Older format; numeric IDs. |

Two folders are scanned, in this order:

- `plugins/FastEdit/schematics/` — drop your downloaded `.schem` files here
- The world's `structures/` folder — anything saved via vanilla `/structure
  save`, SimpleWorldEdit, or FastEdit's own `//schem save`

```
//schem list                 list everything FastEdit can see
//schem load <name>          load it into your clipboard
//schem save <name>          save the clipboard as .mcstructure
//paste                      drop it in the world
```

### Java → Bedrock blocks

Schematics made on Java Edition reference block names that don't always exist
on Bedrock (`minecraft:stonebrick`, `minecraft:terracotta`, `minecraft:wool`,
etc.). FastEdit translates the common ones automatically.

For anything genuinely unknown, FastEdit places **`minecraft:magenta_wool`**
as a visible placeholder and remembers the original Java ID. After pasting:

```
//paste                      "FastEdit | pasted 8421 blocks. (37 placeholders)"
/inspect                     gives you the FastEdit Inspector stick
[right-click a pink block]   shows "current: minecraft:magenta_wool"
                                  "original (Java): minecraft:stonebrick"
```

You can then fix the placeholders with a regular `//replace`:

```
//pos1 ... //pos2 ...        select the area you pasted into
//replace minecraft:magenta_wool minecraft:stone_bricks
```

The mapping is persisted to `plugins/FastEdit/unknown_blocks.dat`, so the
inspector still works after a restart.

---

## Brushes (paint with a shovel)

Brushes are stored **inside** your shovel — give different shovels different
brushes and switch between them.

```
[hold any shovel: wooden, stone, iron, golden, diamond, netherite]
//brush sphere stone 5
[right-click anywhere — even at the sky, raycast finds the block in front]
```

Available brushes:

```
//brush sphere   <pattern> <radius>
//brush cube     <pattern> <radius>
//brush cyl      <pattern> <radius> <height>
//brush smooth   <fillPattern> <radius> [iterations]
//brush clipboard [-noair]
//brush none                          unbind the brush from this shovel
```

Add a mask so the brush only affects matching blocks:

```
//brush sphere stone 6
//mask #air                           only paint into air
//mask !bedrock                       paint anywhere except on bedrock
//mask none                           clear the mask
```

Right-click triggers a brush stroke. There's a **500 ms cooldown** to prevent
spam from Bedrock's hold-to-attack behaviour, and the raycast reaches up to
256 blocks so you can sculpt from a distance.

---

## Inspector

```
/inspect
[right-click a block]
```

Shows the block's coordinates, its current ID, and — if it's a placeholder
from a schematic load — its original Java ID. Useful for cleaning up
schematics that referenced Bedrock-unknown blocks.

---

## All commands at a glance

| Command | What it does |
|---|---|
| `//wand` | Give the wooden axe |
| `//pos1` `//pos2` | Set positions manually |
| `//sel [clear]` | Show / clear the selection |
| `//size` | Show selection volume |
| `//expand <n> [rev] [dir]` `//expand vert` | Grow the selection |
| `//expandrod` | Blaze rod: click a face to grow the selection |
| `//set <pat>` | Fill |
| `//replace <mask> <pat>` | Conditional fill |
| `//walls <pat>` | Outline |
| `//sphere <pat> <r> [hollow]` | Sphere at your feet |
| `//cyl <pat> <r> <h> [hollow]` | Cylinder |
| `//pyramid <pat> <size> [hollow]` | Pyramid |
| `//copy` `//cut` `//paste [-noair]` | Clipboard |
| `//rotate <90\|180\|270>` `//flip <x\|y\|z>` | Transform clipboard |
| `//move <n> [dir]` `//stack <n> [dir]` | Move / stack |
| `//undo [n]` `//redo [n]` | Undo / redo |
| `//schem <save\|load\|list> [name]` | Schematics |
| `//brush <kind> <args>` `//mask <mask>` | Brushes |
| `/inspect` | Block info stick |

---

## Under the hood

A short note for the curious — you don't need any of this to use the plugin.

- **Async planning, sliced apply.** Block coordinates are computed on a
  virtual thread (no main-thread cost), then written on the main thread in
  60 000-block slices per tick, so the server keeps a healthy TPS even on
  huge edits.
- **Batched writes.** Each tick's slice goes through PowerNukkitX's
  `BlockManager.applySubChunkUpdate()`, which sends one packet per sub-chunk
  instead of one per block and writes chunks in parallel.
- **No physics / redstone updates** are triggered by edits — exactly the
  behaviour world-edit operations expect.
- **Per-player session** keeps pos1, pos2, the clipboard, and a 20-deep
  undo/redo stack in memory.

The full architecture lives in
`src/main/java/fr/fastedit/edit/EditEngine.java` if you want to read it.

---

## Building from source

```bash
./gradlew shadowJar
# produces build/libs/FastEdit-1.0.0.jar
```

If your machine can't reach the PowerNukkitX maven repo, point at a local
jar instead:

```bash
FASTEDIT_PNX_JAR=/path/to/powernukkitx.jar ./gradlew shadowJar
```

---

## License

MIT. Do whatever you want with it.
