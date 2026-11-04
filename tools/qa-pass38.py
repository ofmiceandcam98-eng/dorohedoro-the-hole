from pathlib import Path
from PIL import Image
from collections import Counter
import re,hashlib,json,sys
root=Path(__file__).resolve().parents[1]
src=root/'src/main/java/com/dorohedoro/hole'
res=root/'src/main/resources/assets/dorohedoro'
icons=res/'textures/gui/techniques'
s=(src/'magic/SmokeSkill.java').read_text()
skills=[(a.lower(),b) for a,b in re.findall(r'\s+([A-Z_]+)\(MagicType\.([A-Z_]+),"[^"\n]+"',s)]
assert len(skills)==64
assert len({skill for skill,_ in skills})==64
files=sorted(icons.glob('*.png'))
assert len(files)==64, len(files)
assert {f.stem for f in files}=={id for id,_ in skills}
hashes=[]; errors=[]; pixels=[]
for f in files:
 with Image.open(f) as im:
  assert im.size==(24,24),f
  assert im.mode=='RGBA',f
  assert im.getpixel((0,0))[3]==0, f
  assert im.getbbox(),f
  alpha=im.getchannel('A'); filled=sum(1 for v in list(alpha.getdata()) if v>0)
  assert 28<filled<560,(f,filled)
  nearest=im.resize((16,16),Image.Resampling.NEAREST)
  assert nearest.getbbox(),f
  hashes.append(hashlib.sha256(im.tobytes()).hexdigest());pixels.append(filled)
assert len(set(hashes))==64,'icon duplicates'
client=(src/'client/magic/SmokeClient.java').read_text()
menu=(src/'client/magic/SmokeLedgerScreen.java').read_text()
bridge=(src/'client/ui/SmokeSkillIcon.java').read_text()
assert 'SmokeSkillIcon.draw(g,skill,x+2,yy+2,23)' in client
assert 'onVanillaHudLayer' in client and 'VanillaGuiLayers.FOOD_LEVEL' in client
assert '"SMOKE",' not in client and '"FOOD",' not in client
assert 'g.fill(x-2,y-2,x+w+2,y+h+2,' not in client
assert 'SmokeSkillIcon.draw(g,s,' in menu
assert 'perPage=h<260?1:h<320?2:3' in menu
assert 'ICONS.put(skill,' in bridge
blocks=['smoke_stained_concrete','seepage_brick','patchwork_masonry','rusted_industrial_sheet']
for name in blocks:
 with Image.open(res/f'textures/block/{name}.png') as im:
  assert im.size==(32,32) and im.mode in ('RGB','RGBA'),name
item_names=['scrap_metal','smoke_residue','zinc_ingot','brass_ingot','rock_salt','raw_sulfur','medical_salve','leech_mucus']
for name in item_names:
 with Image.open(res/f'textures/item/{name}.png') as im:
  assert im.size==(32,32) and im.mode in ('RGB','RGBA'),name
# Static checks, not an assessment of in-game appearance or worldgen.
checks=dict(skills=len(skills),unique_icons=len(set(hashes)),total_visible_icon_pixels=sum(pixels),hud_labels_removed=True,gameplay_uses_unlabeled_slender_bars=True,vanilla_survival_hearts_food_avoids_duplicates=True,small_screen_skill_pagination=True,icons_render_in_menu=True,modified_existing_block_textures=len(blocks),modified_existing_item_textures=len(item_names),full_client_compile='NOT_RUN',in_game_test='NOT_RUN',technique_crash_root_cause='UNCONFIRMED')
out=root/'docs/PASS38_QA_RESULTS.json';out.write_text(json.dumps(checks,indent=2))
print(json.dumps(checks,indent=2))
