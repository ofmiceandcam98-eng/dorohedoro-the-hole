#!/usr/bin/env python3
"""Offline source/resource consistency audit; DOES NOT compile or launch NeoForge."""
from pathlib import Path
import struct
import re, json, sys
root = Path(__file__).resolve().parents[1]
java = root/'src/main/java/com/dorohedoro/hole'
assets = root/'src/main/resources/assets/dorohedoro'

def require(condition, message):
    if not condition:
        raise AssertionError(message)
    print('PASS:',message)

skills = (java/'magic/SmokeSkill.java').read_text()
names = re.findall(r'^\s*([A-Z_]+)\(MagicType\.',skills,flags=re.M)
visuals = (java/'magic/SmokeVisuals.java').read_text()
pattern = visuals[visuals.index('private static CastPattern castPattern'):visuals.index('private static void castPatternShape')]
groups = re.findall(r'case\s+(.+?)\s*->',pattern,flags=re.S)
assigned = [entry.strip() for group in groups for entry in group.replace('\n',' ').split(',') if entry.strip()]
require(len(names)==64 and len(set(names))==64, '64 technique declarations preserved')
require(len(assigned)==64 and len(set(assigned))==64 and set(assigned)==set(names),'every technique has exactly one explicit cast pattern')
require('MushroomSpellVisuals.animate' in visuals and 'phase < 2' in visuals, 'physical mushroom stage remains connected at phase 2')
for family, count in [('soot_plume',8),('smoke_filament',8),('healing_fiber',6),('fungal_spore',6)]:
    doc=json.loads((assets/'particles'/f'{family}.json').read_text())
    require(len(doc['textures'])==count,f'{family} has {count} registered frames')
    for i,entry in enumerate(doc['textures']):
        require(entry==f'dorohedoro:{family}_{i}',f'{family}_{i} reference matches')
        raw=(assets/'textures/particle'/f'{family}_{i}.png').read_bytes()
        require(raw.startswith(bytes.fromhex('89504e470d0a1a0a')) and
                struct.unpack('>II',raw[16:24])==(48,48) and raw[25]==6 and len(raw)>150,
                f'{family}_{i} is a 48x48 RGBA PNG')
registry=(java/'registry/ModSmokeParticles.java').read_text()
client=(java/'client/magic/SmokeParticleClient.java').read_text()
for reg in ['SOOT_PLUME','SMOKE_FILAMENT','HEALING_FIBER','FUNGAL_SPORE']:
    require(reg in registry and reg in client and reg in visuals, f'{reg} registered, rendered and emitted')
require('MAX_PACKET_CALLS_PER_FRAME' in visuals, 'visual packet emission cap present')
require('SLOTS=3' in (java/'magic/SmokeState.java').read_text().replace(' ',''), 'three active slots preserved')
portal=(java/'portal/PortalService.java')
if not portal.exists():
    portal=next(java.rglob('PortalService.java'))
s=portal.read_text()
require('Frame' in s and 'stand(' in s and 'core.offset(0, 0, sign)' in s, 'fixed portal stand offset preserved')
print('OFFLINE SOURCE CHECKS PASSED; NOT COMPILED OR RUN IN MINECRAFT')
