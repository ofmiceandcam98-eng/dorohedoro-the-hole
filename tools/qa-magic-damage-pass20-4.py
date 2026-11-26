#!/usr/bin/env python3
"""Checks complete offensive-spell damage mapping. Static inspection only: NOT a compile."""
from pathlib import Path
import re
root=Path(__file__).resolve().parents[1]
engine=(root/'src/main/java/com/dorohedoro/hole/magic/SmokeEngine.java').read_text()
mechanics=(root/'src/main/java/com/dorohedoro/hole/magic/OtherMagicMechanics.java').read_text()
skills=(root/'src/main/java/com/dorohedoro/hole/magic/SmokeSkill.java').read_text()
# Intentionally distinguish support/utility powers from attacks.
attacks='''SPORE_PUFF SPORE_CLOUD FUNGAL_BIND FUNGAL_BLOOM SPORE_DOMINION
TAIL_LASH
SEVERING_TOUCH CLEAN_CUT DISMEMBER_STRIKE PRECISION_NERVE LIMB_LOCK DISARMING_SMOKE SEVERING_WAVE ANATOMY_REND
CURSE_MARK HEX_BURDEN WITHER_SEAL MARK_OF_DREAD PERSISTENT_HEX MISFORTUNE CURSE_PULSE RUINOUS_CURSE
DILATED_MOMENT SLOWED_MOMENT CHRONO_BURST
SMOKE_SHOT DETONATION SHOCKWAVE BREACH_BOLT RUPTURE_ARMOR CONCUSSIVE_WAVE SHRAPNEL_SMOKE ANNIHILATION_STREAM'''.split()
spells=re.findall(r'^\s*([A-Z][A-Z0-9_]+)\(MagicType\.(\w+),',skills,re.M)
cases=re.findall(r'(?m)^\s*case\s+(\w+)\s*->',engine)
fail=[]
for spell in attacks:
    m=re.search(r'(?m)^\s*case '+spell+r'\s*->\s*(.*?)(?=^\s*case\s+|\n\s*}\n\s*// Persistent|\Z)',engine,re.S|re.M)
    if not m or 'impact(p,' not in m.group(1):
        fail.append(spell)
print('64 spells catalogued:',len(spells)==64)
print('All 33 selected offensive spells call direct damage:',len(attacks)==33 and not fail)
print('Missing:',fail)
print('Caster-owned damage source:', 'caster.damageSources().indirectMagic(caster, caster)' in engine)
print('PvP restriction at hit:', 'caster.canHarmPlayer(player)' in engine)
print('Curse DOT attribution and PvP:', all(t in mechanics for t in ['putUUID(CURSE_CASTER, caster.getUUID())','owner.damageSources().indirectMagic(owner, owner)','owner.canHarmPlayer(opponent)']))
print('Direct damage scales:', 'baseDamage * multiplier' in engine and 'mastery' in engine and 'rank' in engine)
print('Healing and Protection support stay separate:', 'case MEND_ALLY ->' in engine and 'case SMOKE_WARD ->' in engine)
print('Destructive spell blast still actual:', 'level.explode(caster' in mechanics)
passed=(len(spells)==64 and len(attacks)==33 and not fail
    and 'caster.damageSources().indirectMagic(caster, caster)' in engine
    and 'caster.canHarmPlayer(player)' in engine
    and all(t in mechanics for t in ['putUUID(CURSE_CASTER, caster.getUUID())','owner.damageSources().indirectMagic(owner, owner)','owner.canHarmPlayer(opponent)'])
    and 'baseDamage * multiplier' in engine
    and 'level.explode(caster' in mechanics)
print('RESULT:', 'PASS static checks' if passed else 'FAILED', '— NO COMPILE, NO MINECRAFT TEST')
raise SystemExit(0 if passed else 1)
