# Dorohedoro: The Hole — Pass 33, real Minecraft UI test

## Scope and status

Source patch (NOT compiled or client-tested here). Apply to Pass 32E's Codespaces branch and run `bash tools/verify-neoforge-build.sh` before installing it. This is a new 0.1.1-alpha.1 **local test version**, NOT a CurseForge upload candidate.

## Reference and art rule

Reference: the *actual Minecraft* Tensura: Reincarnated 1.21.1 interface screenshot supplied by the user. Preserve its useful functional structure—upper-left vitals, three left-side technique squares, pop-up ability management—not its copyrighted textures, icons, fonts or assets. Separate third-party Tensura HUD/GUI addons are not assumed to be part of the base mod. No external assets copied.

## Implemented in this patch

1. Top-left mini vitals panel, with Minecraft HP, food/hunger, Sorcerer smoke, smoke rank, experience, current preset. Active injury label if applicable. Vanilla hearts/food bars are NOT disabled in this first test pass, to preserve safe fallback.
2. Left edge has three square cast slots (Z, X, C), name beside icon square, cooldown overlay/countdown, focused indicator. No bottom-center intrusive block or permanent R instructions. Skill casting and server authority are unchanged.
3. `B` opens the genuine clickable Character & Smoke pop-up; B or Esc closes it. Left navigation has Character, Techniques, Loadouts, Medical. Techniques show the unlocked/locked/practice states, mastery, rank requirements; equipping always targets the selected Z/X/C slot; nine server-owned presets are preserved.
4. First-join character questionnaire is an actual centered modal with dedicated readable question card, spaced answer buttons, progress indicator, permanently chosen affinity preserved. The question and labels now render AFTER `Screen.render`, avoiding the vanilla blur render order. Server-owned random question order and answers remain untouched.
5. Shared color/layout helper `HoleUiTheme.java` and accessible custom `HoleMenuButton.java` creates a consistent palette and clipping-aware label shortening using vanilla Minecraft GUI graphics. All visuals are newly drawn panels, no copying Tensura assets.

## NOT implemented / still blocked

- Technique activation crash: cause unknown until newest Minecraft crash-report and latest.log are inspected. No bogus broad try/catch was introduced into gameplay magic. DO NOT publish or resume the crashed save just to test this UI.
- Rendering and compatibility still require real 1.21.1 NeoForge client verification, including multiple GUI scales and different screen resolutions.
- Interactable in-game medical treatment and quest journal remain existing gameplay implementations and/or separate features; this tab displays the server diagnosis but does not invent new mechanics.
- No framework to suppress or move vanilla health/food bars yet. Test the new information before hiding native safety UI.
- No new content, deferred villager/faction overhaul, Ash-Snow, or world-generation modifications.

## Crash-first triage

In Windows: `C:\Users\Cam\Dorohedoro-Minecraft\crash-reports\` newest crash-*.txt. If no crash report, use `C:\Users\Cam\Dorohedoro-Minecraft\logs\latest.log`. Attach the report; include keybind Z/X/C used, technique name (Curse Mark), whether a target was hit, and whether single-player or a server. Identify the first exception and trace in Dorohedoro code, then fix only the proven cause. Inspect client/server threads, target selection, network cues, mesh rendering, and particle effects. Test after backup on *fresh* test world; close/reopen save after stable casting.

## Acceptance testing (new local test world)

- [ ] Real NeoForge `runData` and `build` succeed on Java 21; no compile failures.
- [ ] Original first-join question renders crisp at GUI scale 2, 3, 4 and at 1280x720.
- [ ] All eight quiz questions fit, no text obscured by buttons; back/next retain correct answers.
- [ ] Human and Sorcerer can complete origin without skipping server choice.
- [ ] 3 left-side slots work, Z/X/C still cast; Focus Alt mode and nine presets still work.
- [ ] B menu tabs switch, select slot, equip, practice, clear, choose presets; server snapshots update immediately.
- [ ] Top-left stats update correctly; no HUD over hotbar; menu not covered by world blur.
- [ ] Curse Mark no longer crashes **after separately repairing diagnosed root cause**.
- [ ] Save, quit, rejoin: identity, magic family, mastery, loadouts, health and inventory persist.
- [ ] Frame rate/playability, singleplayer + dedicated server if applicable.
- [ ] Only then decide on CurseForge Alpha upload and changelog.

## Install / rollback

Upload this zip to the Codespace repository root, then:

```
unzip -o Dorohedoro_Pass33_UI_Prototype_SOURCE_ONLY.zip -d . && bash tools/verify-neoforge-build.sh
```

On success download `build/libs/dorohedoro-0.1.1-alpha.1.jar`. Backup the previous Dorohedoro JAR and keep only ONE Dorohedoro JAR in the correct `mods` folder. Never publish until crash and save tests pass.
