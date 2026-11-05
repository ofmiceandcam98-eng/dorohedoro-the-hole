#!/usr/bin/env python3
"""Pass53 offline source/contracts, not a Minecraft or NeoForge build."""
from pathlib import Path
import json, re
root=Path(__file__).resolve().parents[1]
b=root/'src/main/java/com/dorohedoro/hole/worldgen/city'
read=lambda f:(b/f).read_text()
settings=read('CitySettings.java'); sb=read('Superblock.java'); chooser=read('LotChooser.java')
parcels=read('UrbanParcels53.java'); irregular=read('IrregularBlocks14.java')
preset=json.loads((root/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())
city=preset['dimensions']['minecraft:overworld']['generator']['city']
checks={
    'new world explicitly uses v36':city['world_detail_version']>=36,
    'historical saves default to v0':'optionalFieldOf("world_detail_version", 0)' in settings,
    'codec range and constructor agree':'Codec.intRange(0, 52)' in settings and 'worldDetailVersion > 52' in settings,
    'new geometry gated at v36 only':sb.count('worldDetailVersion() >= 36')>=2,
    'historical split unchanged for v0-v35':'morphology != null ? morphology' in sb and 'morphology != null ? morphology.streetGap()' in sb,
    'urban shape driven by independent fabric':'UrbanPattern52.at(plan.seed, rect.centerX(), rect.centerZ())' in sb,
    'offset alleys still preserve historical entry':'IrregularBlocks14.parcels(area, hash, compact)' in sb and 'IrregularBlocks14.parcelsV36(area, hash, compact)' in sb,
    'new irregular frontage requires buildable plots':'parcelsWithMinParcel(area, hash, dense, 11)' in irregular,
    'original irregular method retains historic MIN_PARCEL':'parcelsWithMinParcel(area, hash, dense, dense ? 11 : MIN_PARCEL)' in irregular,
    'non-dense districts permitted compact buildings':'case DENSE -> new Profile(' in parcels and 'District.GRAVEYARD ? 24 : 22' in parcels,
    'inhabited derelict wilderness sizes differentiated':all(x in parcels for x in ('case INHABITED', 'case DERELICT', 'case OPEN_COUNTRY')),
    'all local routes remain at least 3 wide':'streetGap < 3' in parcels,
    'dense fringe uses occupied building mix':all(x in parcels for x in ('LotType.TENEMENT','LotType.SHOP','LotType.WAREHOUSE','LotType.FACTORY')),
    'new occupied fringe mix does not change old saves':'worldDetailVersion() >= 36 && district == District.OUTSKIRTS' in chooser,
    'climate remains independent of urban pattern':'HoleRegions42.Region' not in parcels and 'HoleRegions42.Region' not in read('UrbanPattern52.java').split('/** Used only for new worlds.')[0],
    'regional spawn quarters protected':'buildRegionalStartingQuarter' in sb and 'buildStartingQuarter' in sb,
    '30 percent sewer cave links unchanged':city['sewer_cave_frequency']==30,
    'nighttime lighting remains independently suppressed':'UrbanPattern52.Fabric.OPEN_COUNTRY' in read('CityPainter.java'),
    'new streets do not add lights or chest spam':not any(x in parcels for x in ('Blocks.LANTERN','Blocks.CHEST','c.chest(')),
    'all resource json parses':all(json.loads(f.read_text()) is not None for f in (root/'src').rglob('*.json')),
}
for label,ok in checks.items():print(('PASS' if ok else 'FAIL')+' '+label)
print(f'Pass53 source checks: {sum(checks.values())}/{len(checks)}')
assert all(checks.values())
