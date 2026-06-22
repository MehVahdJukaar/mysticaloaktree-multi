# Mystical Oak Tree — Digested Ideas

Distilled from the Discord `#WiseEmoji` brainstorm dump (`ideas.txt`, Aug 2023 → 2024).
Shitposts and off-topic tangents have been dropped. Status tags compare each idea
against the current codebase.

**Status legend:** ✅ implemented · 🟡 partial · ⬜ not built

---

## 1. Dialogue / trigger rework
- ⬜ Each line is **server-side** with one or more **activation reasons**:
  block placed, entity walks by, talked to, hit, woken up, **given an item**.
  *(Most triggers exist in `dialogues/types/`: TalkedTo, Hurt, OnBroken, WokenUp,
  EntityAround, HeardSound, ProjectileHit. "Given item" trigger is missing.)*
- ⬜ Lines carry **stat requirements** + **game conditions**: time of day, weather,
  gamemode, player equipment, which block was placed.
- ⬜ Some conditions only available under certain triggers.
- ⬜ Weather/time are **non-persistent conditions** — they gate lines but never trigger
  one on their own.

## 2. Stat system (core rework)
Per-player stats saved in tile data, 0–100, plus one global. (Mood was rejected —
too many mood-specific lines required.)
- 🟡 **Trust** — happiness vs. anger toward you; low = angry. *(Only stat that exists today,
  `Relationship.java`: +5/conversation, −20 on hurt; thresholds angry/friendly/in-confidence.)*
- ⬜ **Familiarity** — whether he knows/acknowledges you. Only grows... but **decays**
  ("treementia") if you don't visit — he becomes distant / forgets you.
- ⬜ **Romance** — decays much faster than the others.
- ⬜ **Fatigue / Sleepiness** — ticks down; controls **how many lines he tells before sleeping**.
  Any romance-giving interaction also adds fatigue (anti-abuse).
- ⬜ **"Talked to" count** — **global**, not per-player.

## 3. Interaction & progression mechanics
- ⬜ **Items given** affect stats, with a **per-day limit** (anti-abuse).
- ⬜ Block-tag idea (placing certain blocks nearby raises/lowers trust) — considered,
  leaning toward tying it to dialogue instead.
- ⬜ **Shift-right-click = "romantic interaction"**; rejected if trust/mood is low.
  Romance only reacts once normal stats are maxed.
- ⬜ **Romance arc**: max romance → flushed/blushing tree; romance him then trust drops
  to 0 → **"tree baby" / Groot**.
- ⬜ Talking to **another tree while he watches** breaks romance.

## 4. Dialogue delivery / UI
- 🟡 Delivers **one line per ~second**, auto-splitting long text; **click to speed up / advance**.
- ⬜ **Click = yes, ignore = "I don't care"** — binary input for branching lines.
- ⬜ **Question face**: expression changes when he wants player input.
- 🟡 Different **faces per dialogue option** (eyebrow raise, blush, question, etc.).
- ⬜ *(Unsure / parked)* tree **scanning chat** for input.

## 5. Visual / seasonal flavor
- ⬜ **Beard mechanic** — grows a **moss beard** (reuses vanilla moss). Implemented as a
  **separate block placed above** (2-tall caveat noted), with random-texture variants/styles.
  **Shearable** for a new item at a cost (~−50 trust, he gets MAD). Triggers: **No-Shave
  November**, winter, or a rare lucky variant. Appears only once he knows you.
- ⬜ Other flavor: **smoke rings**, ivy beard, **scarf / winter clothing**.

## 6. LLM / "ChatGPT" integration
- 🟡 Original plan: **fetch random lines at boot via AI, cache to file.**
  *(Current code goes further — a real local LLM runner + .gguf model, see `llm/LLMManager.java`.)*
- ⬜ Community **Discord "wise tree" bot** (murao volunteered) — possibly with bug-report flow
  and meme answers. A separate companion project, not the mod itself.

## 7. Audio
- ⬜ **Wooden-voice SFX** — generic NPC-mumble but "made of wood," Groot-like.
  A user (Wofrion) made sample sounds; long-wanted feature.

## 8. Misc / lore
- ⬜ Two oaks placed near each other → **duel/fight**, loser destroyed
  (doubles as anti-lag for placing many).
- ⬜ Trees **communicate via roots / telepathically** (lore reason they talk slowly).
- ⬜ He's an **immortal being**, talks slowly.

## 9. Salvageable in-game lines (from the noise)
- "What's that behind you?" → (player turns) → "**Thousand years of misfortune.**"
- Fake wisdom: "**If you put a container inside a container, you will have twice the storage.**"
</content>
</invoke>
