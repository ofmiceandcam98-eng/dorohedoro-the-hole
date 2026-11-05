#!/usr/bin/env python3
"""Source-only regression for spawn egg registrations; no claim of Minecraft runtime verification."""
from pathlib import Path
import json,re
root=Path(__file__).resolve().parents[1]
items=(root/'src/main/java/com/dorohedoro/hole/registry/ModItems.java').read_text()
entities=(root/'src/main/java/com/dorohedoro/hole/registry/ModEntities.java').read_text()
models=(root/'src/main/java/com/dorohedoro/hole/datagen/ModModels.java').read_text()
langs=json.loads((root/'src/main/resources/assets/dorohedoro/lang/en_us.json').read_text())
assert 'TABS.register("creatures"' in items
assert 'CreativeModeTabs.SPAWN_EGGS' in items
assert 'addListener(ModItems::addToVanillaSpawnEggs)' in items
assert langs['itemGroup.dorohedoro.creatures']=='The Hole: Creatures'
check=0
mobs=[('SEWER_ROACH','sewer_roach'),('SCRAP_RAT','scrap_rat'),('SEWER_LEECH','sewer_leech'),('GLOW_MOTH','glow_moth'),('SLUDGE_CRAWLER','sludge_crawler'),('RUST_EATER_BEETLE','rust_eater_beetle'),('CAVERN_BURROWER','cavern_burrower'),('SPORE_CRAWLER','spore_crawler')]
for holder,mid in mobs:
    itemid=mid+'_spawn_egg'
    assert f'{holder} = ENTITIES.register(' in entities,mid
    assert re.search(r'\b'+holder+r'_SPAWN_EGG = ITEMS\.register\(\s*"'+itemid+r'", \(\) -> new DeferredSpawnEggItem\(\s*ModEntities\.'+holder+r', 0x[0-9A-F]{6}, 0x[0-9A-F]{6}, new Item\.Properties\(\)\)\);',items),mid
    check+=1
    assert items.count('output.accept('+holder+'_SPAWN_EGG.get())')==1,mid
    assert items.count('addEggIfMissing(event, '+holder+'_SPAWN_EGG.get())')==1,mid
    check+=1
    assert langs['item.dorohedoro.'+itemid].endswith('Spawn Egg'),mid
    check+=1
    p=root/'src/main/resources/assets/dorohedoro/models/item'/f'{itemid}.json'
    assert json.loads(p.read_text())=={'parent':'minecraft:item/template_spawn_egg'},p
    assert f'withExistingParent("{itemid}", "minecraft:item/template_spawn_egg");' in models,mid
    check+=1
    # no Survival recipe for Creative-only test devices
    assert not any((root/'src/main/resources/data/dorohedoro/recipe').glob(itemid+'*.json'))
    check+=1
print(f'PASS: {check+4} spawn-egg source/asset checks for {len(mobs)} mobs, Creative tabs, models, and names')
