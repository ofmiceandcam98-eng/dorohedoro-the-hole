#!/usr/bin/env python3
"""Integration assertions; these are not evidence of a working NeoForge runtime."""
import json,re
from pathlib import Path
r=Path(__file__).resolve().parents[1]
p=lambda path:(r/path).read_text()
city='src/main/java/com/dorohedoro/hole/worldgen/city/'
box=p(city+'VanillaStructureBuffer64.java')
lookup=p(city+'VanillaStructureLookup64.java')
canvas=p(city+'Canvas.java')
painter=p(city+'CityPainter.java')
gen=p('src/main/java/com/dorohedoro/hole/worldgen/HoleChunkGenerator.java')
settings=p(city+'CitySettings.java')
old=p(city+'DeepDarkBuffer63.java')
vanilla_preset=json.loads(p('src/main/resources/data/minecraft/worldgen/world_preset/normal.json'))
conf=vanilla_preset['dimensions']['minecraft:overworld']['generator']['city']
checks={
 'version 46 fresh-world default':conf['world_detail_version']>=46,
 'world record validation is versioned':'worldDetailVersion > 52' in settings and 'Codec.intRange(0, 52)' in settings,
 'older saves retain v0 default':'optionalFieldOf("world_detail_version", 0)' in settings,
 'structure references queried for actual current chunk':'manager.startsForStructure(chunk,' in lookup,
 'respects doGenerateStructures gamerule':'manager.shouldGenerateStructures()' in lookup,
 'structure registry lookup uses vanilla namespace':'"minecraft".equals(id.getNamespace())' in lookup,
 'critical strongholds protected':'"stronghold"' in lookup,
 'critical mineshafts protected':'"mineshaft"' in lookup and '"mineshaft_mesa"' in lookup,
 'critical trial chambers protected':'"trial_chambers"' in lookup,
 'critical ancient cities protected':'"ancient_city"' in lookup,
 'valid start check performed':'start.isValid()' in lookup,
 'actual bounding boxes used':'start.getBoundingBox()' in lookup,
 'avoids off-chunk boxes':'box.nearChunk(chunk.getMinBlockX(), chunk.getMinBlockZ())' in lookup,
 'immutable snapshot list from lookup':'List.copyOf(result)' in lookup,
 'horizontal buffer finite':'HORIZONTAL_MARGIN = 4' in box,
 'vertical buffer finite':'VERTICAL_MARGIN = 6' in box,
 'overlays above underground boxes permitted':'(long) y <= (long) maxY + VERTICAL_MARGIN' in box,
 'negative coordinate safe checks':'(long) minX - HORIZONTAL_MARGIN' in box,
 'old generations not affected':'worldDetailVersion < 46' in box,
 'surface painter requests structure references before painting':gen.index('VanillaStructureLookup64.forChunk') < gen.index('CityPainter.paint(cityPlan, chunk);'),
 'new path exists and old path still exists':'if (citySettings.worldDetailVersion() >= 46)' in gen and 'CityPainter.paint(cityPlan, chunk);' in gen,
 'painter passes guard to canvas':'new Canvas(chunk, plan, structures)' in painter,
 'old painter overload remains':'paint(plan, chunk, List.of())' in painter,
 'Canvas copy isolates producer list':'this.protectedVanillaStructures64 = List.copyOf(structures)' in canvas,
 'all 4 writes check guard':canvas.count('if (protectsVanillaWrite(x, adjustedY, z)) return;')==4,
 'both buffers used in one guard':'DeepDarkBuffer63.protects(worldDetailVersion, adjustedY, protectDeepDark63)' in canvas and 'VanillaStructureBuffer64.protects(worldDetailVersion, x, adjustedY, z, protectedVanillaStructures64)' in canvas,
 'no direct chunk writes outside Canvas':not any('chunk.setBlockState' in x.read_text() for x in (r/'src/main/java/com/dorohedoro/hole/worldgen/city').glob('*.java') if x.name != 'Canvas.java'),
 'vanilla nether and end preserved':all(x in vanilla_preset['dimensions'] for x in ('minecraft:the_nether','minecraft:the_end')),
 'deep dark separate legacy guard preserved':'worldDetailVersion >= 45' in old or 'worldDetailVersion < 45' in old,
 'cave outlet percentage retained':conf['sewer_cave_frequency']==30,
 'new unit harness compiled from actual source':'VanillaStructureLookup64.java' in p('tools/qa-pass64-buffer.sh') and 'VanillaStructureBuffer64.java' in p('tools/qa-pass64-buffer.sh'),
 'no new separate Forgotten City':not (r/'src/main/java/com/dorohedoro/hole/worldgen/city/ForgottenCity.java').exists(),
 'JSON parseable':all(json.loads(x.read_text()) is not None for x in (r/'src').rglob('*.json'))
}
for k,v in checks.items(): print(('PASS' if v else 'FAIL')+' '+k)
print(f'Pass64 source: {sum(checks.values())}/{len(checks)} checks')
if not all(checks.values()): raise SystemExit(1)
