# Wise Oak dialogue rework — skeleton

Status: **skeleton landed** (no LLM yet). This is the extensible foundation the old
single-`trust` system is replaced by. Goal of the rework: deeper NPC interaction with
pre-made lines organized modularly, translation-driven, and a stat model that's trivial to
extend later.

## The one-sentence model

> **The client directs, the server witnesses.** The client owns the entry assets, the
> locale and (later) the LLM, so it *selects and renders* dialogue. The server owns the
> persistent stats and applies their changes. The block entity is synced both ways, so the
> client already has the stats it needs — no per-line netcode for the existing triggers.

Why client-side selection: dialogue entries are client assets (`assets/.../tree_wisdom`),
translation is a client concern, and the future LLM runs on the player's machine. The
server doesn't have the entry table, so it *can't* pick. It doesn't need to: interaction
callbacks (`useItemOn`/`attack`) already fire on both sides, and `WiseOakTile` is synced via
`getUpdatePacket`, so the client reads the same stats the server persists.

## Packages

```
dialogue/                      (common — the model)
├─ stat/      Stat, Stats (registry), StatMap        ← add a stat = one line in Stats
├─ condition/ Requirement (+dispatch), Stat/Time/Weather/ModLoaded/Compound
├─ effect/    Effect (+dispatch), StatEffect
├─ Trigger, Triggers                                  ← why a line fires
├─ DialogueContext                                    ← stats + world, fed to requirements
├─ Face                                               ← expression carried per line
├─ DialoguePart, DialogueEntry                        ← the line + its gating/effects
client/
├─ TreeLoreManager   loads entries, selects by requirement filter + weighted pick
└─ dialogues/DialogueInstance   streams the chosen entry's parts
block/
├─ Relationship      now a thin layer over StatMap (per player)
└─ WiseOakTile       builds a DialogueContext and drives selection
```

## How a line is chosen (e.g. player talks to the tree)

1. `useItemOn` fires on client + server. Server mutates stats (`Relationship`, deterministic
   baseline: +trust/+familiarity on talk, −trust on hurt) and syncs the tile.
2. Client builds a `DialogueContext` (trigger + synced stats + live world state).
3. `TreeLoreManager.getRandomDialogue` filters that trigger's entries by their
   `Requirement`s against the context, then picks one weighted-randomly.
4. `DialogueInstance` streams the parts; each line's text is resolved as **lang override or
   inline default**, then `$placeholders` are substituted.

## Authoring entries

New format (`assets/<ns>/tree_wisdom/<name>.json`, see `example_new_format.json`):

```jsonc
{
  "trigger": "talked_to",
  "require":  [ {"type":"stat","stat":"familiarity","min":5}, {"type":"time","value":"night"} ],
  "effects":  [ {"type":"stat","stat":"romance","delta":1} ],   // parsed; see "deferred" below
  "weight":   1.0,
  "lines": [
    "We have spoken a few times now, $player_name.",            // bare string part
    {"text":"...did I say that out loud?","face":"blush","requires_interaction":true}
  ]
}
```

**Translation:** the inline `text` doubles as en_us. A lang pack overrides any line by id:
`tree.line.<entry-path>.<line-index>` (path = file path with `/`→`.`). Resolved via
`Component.translatableWithFallback`-style lookup in `DialogueInstance`.

**Back-compat:** the ~286 existing files keep working unchanged. `DialogueEntry.CODEC` reads
the legacy `type`/`trust_required`/`text`(+`required_interactions`) shape and maps it to the
new model (`trust_required` → a `stat` requirement on trust). The top-level `mod_loaded` gate
is still honored at load (new entries should prefer a `mod_loaded` requirement instead).

## Extending

- **New stat** (e.g. mood): one `register(...)` line in `Stats`. Storage, save data, json
  requirements and effects all pick it up by id; old saves are forward-compatible (unknown
  ids are dropped, missing ids fall back to the stat default).
- **New condition** (e.g. held item, biome, date/November): implement `Requirement`, register
  its `MapCodec` in `Requirements`. No change to selection or the entry codec.
- **New trigger** (e.g. `item_given`, `block_placed` — already stubbed in `Triggers`): add the
  server-side hook that builds a `DialogueContext` with that trigger and calls selection.

## Deferred (intentionally out of skeleton)

- **LLM**: a per-entry `generated` flavor (prompt instead of `text`) on the same pipeline,
  rolled on a dice. Selection/translation seams are already in place.
- **Per-entry effects → stats**: `effects` are parsed but not yet applied, because selection
  is client-side while stats are server-authoritative. Applying them needs a small "I played
  entry X" packet so the server applies them from its own copy (anti-cheat). Baseline
  per-trigger stat changes live in `Relationship` meanwhile.
- **Not-yet-wired triggers**: `projectile_hit`/`entity_around`/`heard_sound`/`on_broken`
  exist as ids but nothing fires them (they had no data files either). `heard_sound`/
  `entity_around` will need MC game-event listeners (`WiseOakBlock.getListener`).
- **Stat decay** (familiarity slow, romance fast) and the **global "times talked" stat**
  (plumbed as `WiseOakTile.globalStats`, not yet written).
- **Faces**: carried through the model; not yet mapped to block state / overlay visuals.

## Behavior note

Old selection used a trust "band" so high-trust trees rarely showed low-trust lines. The new
selector shows any line whose requirements pass, weighted uniformly. If the old feel matters,
add a soft weight toward entries near the current stat value.


LLM output:


Refactors — Relationship→StatMap-backed (legacy-int save compat), TreeLoreManager→requirement-filter+weighted selection (dropped the binary-search/hardcoded hacks; kept the online flavor lines),        
WiseOakTile→builds a DialogueContext. Deleted the 9 obsolete classes (ITreeDialogue, TreeDialogueTypes, 7 types/*, BinarySearch).

The client/server answer, baked into the design: client directs (assets+locale+future LLM → selects & renders), server witnesses (owns/persists stats); the synced tile means no per-line netcode for     
current triggers. Documented in REWORK.md.

Left for later (per your "skeleton, no LLM")

- LLM — slots in as a per-entry generated flavor on the same pipeline, dice-rolled.
- Per-entry effects → stats — needs a tiny "played entry X" packet (server applies from its own copy, anti-cheat); baseline per-trigger changes work now.
- The 4 unwired triggers, stat decay, the global "times talked" stat, and mapping Face to visuals.

Two notes worth a glance in REWORK.md: the linter swapped DialoguePart's codec to Moonlight's CodecUtils.bestAlternative (kept it), and selection no longer uses the old trust "band" (any                
requirement-passing line is eligible, weighted) — easy to re-add a soft weight if you miss that feel.

Want me to wire one deferred piece next — e.g. the item_given trigger end-to-end, or Face→block-state so expressions actually show?
  
