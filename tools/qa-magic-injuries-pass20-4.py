#!/usr/bin/env python3
"""Static, dependency-free Pass 20.4 source assertions; NOT a build or gameplay test."""
from collections import Counter
from pathlib import Path
import json
import re

ROOT=Path(__file__).resolve().parents[1]
JAVA=ROOT/'src/main/java/com/dorohedoro/hole'
read=lambda p:(JAVA/p).read_text(encoding='utf-8')
engine=read('magic/SmokeEngine.java')
mechanics=read('magic/OtherMagicMechanics.java')
skills=read('magic/SmokeSkill.java')
state=read('magic/SmokeState.java')
network=read('magic/SmokeNetwork.java')
client=read('client/magic/SmokeClient.java')
portal=read('portal/PortalService.java')
checks={}

def check(name,passed):
    checks[name] = bool(passed)
    print(('PASS' if passed else 'FAIL') + ' ' + name)

enum=skills.split('public enum SmokeSkill {',1)[1].split('    private final MagicType magic;',1)[0]
entries=re.findall(r'(?m)^\s*([A-Z][A-Z0-9_]+)\(MagicType\.([A-Z_]+),',enum)
counts=Counter(family for _,family in entries)
check('64 skill definitions',len(entries)==64 and len(set(n for n,_ in entries))==64)
check('8 innate types, 8 techniques each',len(counts)==8 and all(n==8 for n in counts.values()))
check('3 active slots, 9 saved loadouts','PRESETS=9, SLOTS=3' in state)
check('server-owned casting with learning/mastery','public static void cast(ServerPlayer p,int slot,boolean focused)' in engine and 's.learned(skill)' in engine)
check('new arm/leg/body injuries all present',all(t in mechanics for t in ['"arm"','"leg"','"body"','SEVER_ZONE','SEVER_MODERN']))
check('permanent injury only from advanced attacks','skill == SmokeSkill.ANATOMY_REND && mastery >= 650' in mechanics and 'skill == SmokeSkill.DISMEMBER_STRIKE && mastery >= 900' in mechanics)
check('injury persists in entity NBT and can be healed',all(t in mechanics for t in ['getPersistentData()','putLong(SEVER_UNTIL, expiry)','healDismemberment','clearInjuryData(data)']))
check('unique injury attribute IDs and named modifier removal',all(t in mechanics for t in ['SEVER_LEG_SPEED','SEVER_ARM_ATTACK','SEVER_ARM_SWING','instance.removeModifier(id)']))
check('injury Healing does not remove independent potions for new wounds','if (oldStyle)' in mechanics and 'patient.removeEffect(MobEffects.MOVEMENT_SLOWDOWN)' in mechanics)
check('Healing can repair another player',all(t in engine for t in ['case CLEANSE_ALLY','case LIFEWEAVE','OtherMagicMechanics.healDismemberment(ally)','OtherMagicMechanics.healDismemberment(friend)']))
check('ordinary support effects are temporary','Math.min(1800L' in mechanics and 'Math.min(340' in mechanics and 'Long.MAX_VALUE' not in mechanics[mechanics.index('public static void raiseBarrier('):mechanics.index('private static boolean isOpenForBarrier(')])
check('physical mushrooms preserved',(JAVA/'magic/MushroomGrowthSavedData.java').exists() and (JAVA/'magic/MushroomSpellVisuals.java').exists())
check('destructive spell PvP preserved','destructiveBlast' in engine and 'TERRAIN_DAMAGE.get()' in mechanics and 'canHarmPlayer(opponent)' in engine)
check('direct spell damage attributed to player','indirectMagic(caster, caster)' in engine and 'indirectMagic(owner, owner)' in mechanics and 'p.damageSources().magic()' not in engine)
check('wound HUD server/client sync','injuryStatus(player)' in network and 'parts.length!=10&&parts.length!=11' in client and 'INJURY:' in client)
check('Human injury countdown refresh','if(OtherMagicMechanics.hasActiveDismemberment(p))SmokeNetwork.sync(p);' in engine)
check('lore-themed injury visual present','private static void injuryMark' in mechanics and 'level.sendParticles(SOOT' in mechanics)
check('portal landing fix retained','core.offset(0, 0, sign) : core.offset(sign, 0, 0)' in portal)
js_files=list(ROOT.rglob('*.json'))
errors=[]
for p in js_files:
    try: json.loads(p.read_text(encoding='utf-8'))
    except Exception as exc: errors.append((str(p.relative_to(ROOT)),str(exc)))
check(f'JSON parse ({len(js_files)} files)', not errors)
if errors: print('JSON errors:',errors[:8])
forbidden=[p.relative_to(ROOT) for p in ROOT.rglob('*') if any(a in {'.gradle','.git','build'} for a in p.relative_to(ROOT).parts)]
check('no build or .gradle/.git folders',not forbidden)
if forbidden: print('forbidden:',forbidden[:10])
print(f'STATIC QA: {sum(checks.values())}/{len(checks)} checks passed. NOT COMPILED. NOT TESTED IN MINECRAFT.')
raise SystemExit(0 if all(checks.values()) else 1)
