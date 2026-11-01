#!/usr/bin/env python3
"""Portable bootstrap for Dorohedoro Phase 3 noise assets (Python 3, stdlib only).

Prefer the official pinned Minecraft 1.21.1 client jar for the exact vanilla overworld
noise router. A jar is optional: in offline/source-checkout environments we use the
registered vanilla density functions and the Hole's already extracted noise settings.
Do not mistake a generated JSON file for a runtime or visual verification.
"""
import argparse
import copy
import json
from pathlib import Path
import zipfile

ROOT = Path(__file__).resolve().parents[1]
NOISE_SETTINGS = ROOT / 'src/main/resources/data/dorohedoro/worldgen/noise_settings'
NOISE = NOISE_SETTINGS.parent / 'noise'


def op(kind, a, b):
    return dict(type='minecraft:' + kind, argument1=a, argument2=b)

def grad(a, b, c, d):
    return {'type':'minecraft:y_clamped_gradient','from_y':a,'to_y':b,'from_value':c,'to_value':d}

def noise(name, xz=1.0, y=0.0):
    return {'type':'minecraft:noise','noise':name,'xz_scale':xz,'y_scale':y}

def clamp(value, lower, upper):
    return {'type':'minecraft:clamp','input':value,'min':lower,'max':upper}

def square(arg):
    return {'type':'minecraft:square','argument':arg}

def write(path, content):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(content,indent=2,ensure_ascii=False)+'\n',encoding='utf-8')
    print('wrote',path.relative_to(ROOT))


def density_base(vanilla):
    """Find vanilla pre-cave 'blended' density when a real 1.21.1 jar is supplied."""
    # Vanilla: min(squeeze(mul(0.64, interpolated(blend_density(EXPR)))), noodles).
    return vanilla['noise_router']['final_density']['argument1']['argument']['argument2']['argument']['argument']


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--vanilla-jar', type=Path, help='Official Minecraft 1.21.1 client jar (optional)')
    a=parser.parse_args()
    base = json.loads((NOISE_SETTINGS/'hole.json').read_text(encoding='utf-8'))
    vanilla = None
    if a.vanilla_jar:
        with zipfile.ZipFile(a.vanilla_jar) as z:
            vanilla=json.loads(z.read('data/minecraft/worldgen/noise_settings/overworld.json'))
        print('Using vanilla overworld noise from',a.vanilla_jar)
    else:
        print('Offline fallback: using existing Hole settings + registered vanilla sloped_cheese density')
    settings = vanilla or base

    for name,octaves,amplitudes in [
        ('city_basin', -9, [1.0,1.0,0.5]),
        ('hell_horns', -6, [1.0,0.5]),
        ('hell_regions', -9, [1.0,0.5]),
        ('hell_mesas', -7, [1.0,0.75,0.5]),
        ('hell_ground', -5, [1.0,0.5,0.25]),
    ]:
        write(NOISE/(name+'.json'), {'firstOctave':octaves, 'amplitudes':amplitudes})

    cave_layer = op('mul',4.0,square(noise('minecraft:cave_layer',1.0,8.0)))
    cheese = clamp(op('add',0.27,noise('minecraft:cave_cheese',1.0,2.0/3.0)),-1.0,1.0)
    caves = op('min',op('min',op('add',cave_layer,cheese),'minecraft:overworld/caves/entrances'),
              op('add','minecraft:overworld/caves/spaghetti_2d','minecraft:overworld/caves/spaghetti_roughness_function'))
    basin = noise('dorohedoro:city_basin')
    mask = clamp(op('mul',op('add',basin,-0.30),7.0),0.0,1.0)
    inverse = op('add',1.0,op('mul',-1.0,mask))
    city = op('min',grad(68,76,2.0,-2.0),op('max',op('max',caves,grad(30,48,-1.5,1.5)),grad(-64,-56,1.5,-1.5)))
    # Use the actual unmodified vanilla density when available. In offline fallback,
    # use the registered 1.21.1 sloped-cheese field so land isn't a flat copy of the Hole.
    natural = density_base(vanilla) if vanilla else op('min',
        'minecraft:overworld/sloped_cheese',
        op('max', caves,grad(30,48,-1.5,1.5)))
    blended=op('add',op('mul',mask,city),op('mul',inverse,natural))
    sw=copy.deepcopy(settings)
    if vanilla:
        sw['noise_router']['final_density']['argument1']['argument']['argument2']['argument']['argument'] = blended
    else:
        # Existing Hole final-density has the same vanilla outer wrapping: replace
        # only its inner expression, not its noise/ore/aquifer or noodle handling.
        sw['noise_router']['final_density']['argument1']['argument']['argument2']['argument']['argument']=blended
    sw['noise_router']['depth']=basin
    sw['sea_level']=63
    sw['disable_mob_generation']=False
    write(NOISE_SETTINGS/'sorcerers_world.json',sw)

    slope=op('add',grad(30,270,1.0,-1.0),-0.667)
    ground=op('mul',0.05,noise('dorohedoro:hell_ground'))
    mesa=op('mul',0.30,clamp(op('mul',op('add',noise('dorohedoro:hell_mesas'),-0.32),9.0),0.0,1.0))
    regions=noise('dorohedoro:hell_regions')
    horn_field=clamp(op('mul',op('add',regions,0.05),6.0),0.0,1.0)
    shifted={'type':'minecraft:shifted_noise','noise':'dorohedoro:hell_horns',
             'shift_x':grad(70,280,0.0,34.0),'shift_y':0.0,'shift_z':grad(90,280,0.0,-18.0),
             'xz_scale':1.0,'y_scale':0.0}
    horn_core=clamp(op('mul',op('add',shifted,-0.40),5.0),0.0,1.0)
    horn=op('mul',1.50,op('mul',horn_field,square(horn_core)))
    surface=op('add',slope,op('add',ground,op('add',mesa,horn)))
    cavern=op('add',0.22,noise('minecraft:cave_cheese',0.5,0.3))
    cavern_cap=op('max',cavern,grad(24,54,-1.5,1.5))
    cavern_floor=op('max',cavern_cap,grad(-64,-50,1.5,-1.5))
    solid=op('min',surface,cavern_floor)
    router={
        'barrier':0.0, 'continents':0.0, 'depth':regions,'erosion':0.0,
        'final_density':{'type':'minecraft:squeeze','argument':op('mul',0.64,{'type':'minecraft:interpolated','argument':solid})},
        'fluid_level_floodedness':0.0,'fluid_level_spread':0.0,
        'initial_density_without_jaggedness':slope,'lava':0.0,'ridges':0.0,
        'temperature':settings['noise_router']['temperature'],
        'vegetation':settings['noise_router']['vegetation'],
        'vein_gap':0.0,'vein_ridged':0.0,'vein_toggle':0.0
    }
    bedrock={'type':'minecraft:condition','if_true':{
        'type':'minecraft:vertical_gradient','random_name':'minecraft:bedrock_floor',
        'true_at_and_below':{'above_bottom':0},'false_at_and_above':{'above_bottom':5}},
        'then_run':{'type':'minecraft:block','result_state':{'Name':'minecraft:bedrock'}}}
    ash={'type':'minecraft:condition','if_true':{
        'type':'minecraft:stone_depth','offset':0,'add_surface_depth':True,
        'secondary_depth_range':0,'surface_type':'floor'},
        'then_run':{'type':'minecraft:block','result_state':{'Name':'dorohedoro:ash_soil'}}}
    hell={'aquifers_enabled':False,'default_block':{'Name':'dorohedoro:hell_rock'},
          'default_fluid':{'Name':'minecraft:air'},'disable_mob_generation':True,
          'legacy_random_source':False,'noise':settings['noise'],'noise_router':router,
          'ore_veins_enabled':False,'sea_level':-64,'spawn_target':[],
          'surface_rule':{'type':'minecraft:sequence','sequence':[bedrock,ash]}}
    write(NOISE_SETTINGS/'hell.json',hell)
    print('Realm settings are DATA GENERATED, NOT verified by a Minecraft runtime.')

if __name__ == '__main__':
    main()
