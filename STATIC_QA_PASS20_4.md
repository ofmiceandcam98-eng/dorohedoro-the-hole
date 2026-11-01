# Pass 20.4 — Static QA

Two static inspection scripts (no Gradle or Minecraft run):

- `python tools/qa-magic-injuries-pass20-4.py` — 20/20 checks passed at packaging.
- `python tools/qa-magic-damage-pass20-4.py` — confirmed exactly 64 skill declarations; 33 offensive branches call real direct-impact damage; damage credited to Sorcerer; PvP checks; curse-source attribution; explosion remains intact.

These checks assert source patterns and valid JSON; they **do not prove** NeoForge compilation, world loading, rendering, or gameplay behavior.

**NOT COMPILED / NOT TESTED IN MINECRAFT.**
