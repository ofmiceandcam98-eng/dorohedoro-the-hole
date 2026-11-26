#!/usr/bin/env python3
"""Source-level assertions only. Does not build or exercise the mod."""
from pathlib import Path
import re
root = Path(__file__).resolve().parents[1]
magic = root/'src/main/java/com/dorohedoro/hole/magic'
o = (magic/'OtherMagicMechanics.java').read_text()
e = (magic/'SmokeEngine.java').read_text()
s = (magic/'SmokeState.java').read_text()
sk = (magic/'SmokeSkill.java').read_text()
p = (root/'src/main/java/com/dorohedoro/hole/portal/PortalService.java').read_text()
def technique(label):
    # Examine the actual switch arm, not an unrelated substring elsewhere.
    start = e.index('case ' + label + ' ->')
    end = e.find('\n            case ', start+1)
    return e[start:end if end >= 0 else None]

def compact(label):
    return re.sub(r'\s+', '', technique(label))

checks = {
    'no_permanent_barrier_flag': 'boolean permanent = mastery' not in o and 'next.permanent()' not in o,
    'all_new_barriers_tracked': 'if (!data.canTrack()) continue;' in o and 'data.track(next.pos(), next.expiry());' in o,
    'capped_barrier_lifetime': 'Math.min(1800L' in o,
    'time_field_expires': 'field.level().getGameTime() >= field.expires()' in o,
    'injury_state_cured': all(t in o for t in ('healDismemberment(LivingEntity patient)', 'data.remove(SEVER_UNTIL)', 'data.remove(SEVER_POWER)')),
    'cleanse_cannot_bypass_injury': 'if (!OtherMagicMechanics.hasActiveDismemberment(living))' in e,
    # Ordinary injuries can be healed at lower rank. Full missing-limb regrowth
    # must remain mastery-gated; don't assert an obsolete ungated method call.
    'direct_ally_injury_healing': (
        'OtherMagicMechanics.healDismemberment(ally,mastery>=900)' in compact('CLEANSE_ALLY')
        and 'if(mastery>=350)OtherMagicMechanics.healDismemberment(ally,mastery>=750)' in compact('MEND_ALLY')),
    'group_injury_healing': (
        'for(Playerfriend:nearPlayers(p,7))' in compact('LIFEWEAVE')
        and 'OtherMagicMechanics.healDismemberment(friend,mastery>=650)' in compact('LIFEWEAVE')
        and 'if(mastery>=500)OtherMagicMechanics.healDismemberment(friend,mastery>=850)' in compact('RESTORATIVE_FIELD')),
    'protection_cannot_heal_injury': 'case PURGE_BARRIER -> {effect(p,MobEffects.DAMAGE_RESISTANCE' in e,
    '64_techniques': len(re.findall(r'^\s*[A-Z_]+\(MagicType\.', sk, re.M)) == 64,
    '3_slots_9_loadouts': 'PRESETS=9, SLOTS=3;' in s,
    'portal_stand_fixed': 'core.offset(0, 0, sign) : core.offset(sign, 0, 0)' in p,
    'no_build_dirs': not any(q.is_dir() for q in (root/'build',root/'.gradle')),
}
for name, ok in checks.items(): print(('PASS' if ok else 'FAIL') + ' ' + name)
assert all(checks.values()), 'Source assertion failed'
