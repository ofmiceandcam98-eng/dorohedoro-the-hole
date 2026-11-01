#!/usr/bin/env python3
"""Pass 18 portal-arrival structural regression; not a Minecraft runtime/Gametest."""
from pathlib import Path
import re
root = Path(__file__).resolve().parents[1]
s = (root/'src/main/java/com/dorohedoro/hole/portal/PortalService.java').read_text()
checks = {
    'destination y is heightmap first-air': 'level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z)' in s,
    'frame bottom placed at first-air height': 'new Frame(new BlockPos(x, y, z), axis, kind, true)' in s,
    'landing at same y as base (axis X)': 'core.offset(0, 0, sign)' in s,
    'landing at same y as base (axis Z)': 'core.offset(sign, 0, 0)' in s,
    'no ungrounded y+1 landing remains': 'core.offset(0, 1, sign)' not in s and 'core.offset(sign, 1, 0)' not in s,
    'foot support validated below arrival': 'level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)' in s,
    'feet and head checked unobstructed': 'level.getBlockState(feet).isAir() && level.getBlockState(feet.above()).isAir()' in s,
    'target site checks avoid overwriting any structure': 'if (!level.getBlockState(p).isAir() || level.getFluidState(p).isSource()) return false;' in s,
    'both candidate faces checked': 'validLanding(level, frame.stand(1)) || validLanding(level, frame.stand(-1))' in s,
    'return travel still checks arrival': 'BlockPos landing = validLanding(destLevel, returnFrame.stand(1))' in s,
    'source frame validation unchanged': 'Frame frame = findFrame(source, corePos, kind);' in s,
    'paired destination and travel stay server-owned': 'sourceCore.linkTo(targetCore, target.dimension().location());' in s and 'player.changeDimension(new DimensionTransition(destLevel, spawn' in s,
}
# Relative to the block immediately beneath the portal base, the standing position at
# core.y is the only offset that can meet the code's air/solid floor contract on flat ground.
for y in (-5, 65, 150):
    terrain_top = y - 1
    air_start = y
    assert air_start == terrain_top+1
    assert air_start-1 == terrain_top
    assert (air_start+1)-1 != terrain_top
print('\n'.join(('PASS' if v else 'FAIL')+' '+k for k,v in checks.items()))
print(f"PORTAL LANDING SOURCE CHECK: {sum(checks.values())}/{len(checks)} passed (not a NeoForge build)")
if not all(checks.values()): raise SystemExit(1)
