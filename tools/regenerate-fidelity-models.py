#!/usr/bin/env python3
"""Regenerate Java and PNG assets from the AUTHORED docs/model_geometry JSON; requires Pillow. Do not overwrite approved geometry."""
from pathlib import Path
from PIL import Image, ImageDraw
import math, json, random, re, shutil

BASE=Path(__file__).resolve().parents[1]
GEO=BASE/'docs/model_geometry'
SRC=BASE/'src/main/java/com/dorohedoro/hole/client/entity'
TEX=BASE/'src/main/resources/assets/dorohedoro/textures/entity'
colors={
 'roach_shell':'#583a29','roach_high':'#91613b','roach_dark':'#34231f','roach_edge':'#bc8d52','roach_legs':'#51392c','roach_eye':'#e04a32','roach_mouth':'#b49d7e',
 'rat_fur':'#86766c','rat_dark':'#574d48','rat_light':'#b5a49a','rat_pink':'#b77e81','rat_inner':'#d9a19c','rat_eye':'#1b1b19','rat_whisker':'#dcd2ba',
 'leech_body':'#652d3b','leech_bright':'#a25465','leech_dark':'#331b29','leech_mouth':'#bb727a','leech_tooth':'#e0b9a8','leech_slime':'#9d8391',
 'moth_fur':'#a1ac90','moth_light':'#dfdeb5','moth_green':'#6f8769','moth_wing':'#8ba988','moth_pattern':'#edf4b6','moth_eye':'#fff3b2','moth_legs':'#536250',
 'sludge_body':'#34362e','sludge_ooze':'#66684b','sludge_sheen':'#9c9a6c','sludge_eye':'#ccdb7b','sludge_tentacle':'#454738',
 'beetle_shell':'#754b36','beetle_rust':'#b77748','beetle_dark':'#332725','beetle_plate':'#b0986c','beetle_eye':'#efc874',
 'burrower_fur':'#73635a','burrower_dark':'#423a37','burrower_pale':'#b6a292','burrower_claw':'#e4ddbe','burrower_nose':'#b77f7c','burrower_eye':'#211f1d',
 'spore_skin':'#574c46','spore_pod':'#81725e','spore_cap':'#a59368','spore_gill':'#cfc19a','spore_glow':'#b5d977','spore_claw':'#4b4035',
}

ORIGINS={'sewer_roach':21,'scrap_rat':21,'sewer_leech':22,'glow_moth':17,'sludge_crawler':21,'rust_eater_beetle':21,'cavern_burrower':20,'spore_crawler':21}

def atlas(m):
 # Every box gets a unique, nonoverlapping six-face UV island. Fractional box sizes round up for safe allocation.
 cubes=[c for group in m['groups'].values() for c in group]; width=512
 x=2;y=2;row_h=0
 for c in cubes:
  w,h,d=map(lambda v:max(1,math.ceil(v)),c['dim'])
  iw=2*(w+d)+2;ih=h+d+2
  if x+iw+2>width: x=2;y+=row_h+2;row_h=0
  c['uv']=[x,y];x+=iw+2;row_h=max(row_h,ih)
 height=256
 while height<y+row_h+3:height*=2
 return width,height

def rgb(hex):return tuple(int(hex[i:i+2],16) for i in (1,3,5))
def shade(t,off):return tuple(max(0,min(255,int(k+off))) for k in t)

def texture(m,dim):
 im=Image.new('RGBA',dim,(0,0,0,0)); dr=ImageDraw.Draw(im)
 for cs in m['groups'].values():
  for c in cs:
   base=rgb(colors[c['material']]); u,v=c['uv'];w,h,d=map(lambda z:max(1,math.ceil(z)),c['dim'])
   key=sum(ord(ch)*(i+11) for i,ch in enumerate(c['name']))
   rng=random.Random(11209+key)
   faces=[(u,v+d,d,h,-12),(u+d,v+d,w,h,0),(u+d+w,v+d,d,h,-19),(u+2*d+w,v+d,w,h,-8),(u+d,v,w,d,20),(u+d+w,v,w,d,-17)]
   for fx,fy,fw,fh,illum in faces:
    dr.rectangle((fx,fy,fx+fw-1,fy+fh-1),fill=shade(base,illum)+(255,))
    for yy in range(fy,fy+fh):
     for xx in range(fx,fx+fw):
      delta=rng.choices([-24,-15,-8,0,5,12,20],[5,10,18,35,17,10,5])[0]
      # mottled pixel clusters rather than perfectly uniform faces
      if (xx+yy+key)%5==0:delta+=rng.choice([-7,7])
      if yy==fy:delta+=12
      if xx==fx:delta+=4
      dr.point((xx,yy),fill=shade(base,illum+delta)+(255,))
    if fw>=4 and fh>=3:
     dr.line((fx,fy,fx+fw-1,fy),fill=shade(base,illum+24)+(255,))
     dr.line((fx+fw-1,fy,fx+fw-1,fy+fh-1),fill=shade(base,illum-19)+(255,))
 return im

def to_java(m,dim):
 cls=m['class']; id=m['mob']; entity=cls+'Entity'; anim=m['animations']
 animated=sorted(set(k.split('.')[0] for k in anim))
 s=f'''package com.dorohedoro.hole.client.entity;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.entity.{entity};
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Approved Dorohedoro-inspired layered creature geometry. Generated from docs/model_geometry/{id}.json. */
public final class {cls}Model<T extends {entity}> extends HierarchicalModel<T> {{
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "{id}"), "main");
    private final ModelPart root;
'''
 for part in animated:s+=f'    private final ModelPart {part};\n'
 s+=f'    public {cls}Model(ModelPart root) {{\n        this.root = root;\n'
 for part in animated:s+=f'        this.{part} = root.getChild("{part}");\n'
 s+='    }\n\n    public static LayerDefinition createBodyLayer() {\n        MeshDefinition mesh = new MeshDefinition();\n        PartDefinition root = mesh.getRoot();\n'
 for gr,cubes in m['groups'].items():
  pivot=m.get('part_pivots',{}).get(gr,[0,0,0])
  s+=f'        root.addOrReplaceChild("{gr}", CubeListBuilder.create()\n'
  for c in cubes:
   vals=[round(c['xyz'][i]-pivot[i],3) for i in range(3)]+c['dim']; txt=', '.join(f'{round(float(v),3)}F' for v in vals)
   s+=f'                .texOffs({c["uv"][0]}, {c["uv"][1]}).addBox({txt})\n'
  s+=f'                , PartPose.offset({pivot[0]}F, {round(ORIGINS[id]+pivot[1],3)}F, {pivot[2]}F));\n'
 s+=f'        return LayerDefinition.create(mesh, {dim[0]}, {dim[1]});\n    }}\n\n    @Override public ModelPart root() {{ return root; }}\n\n    @Override public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {{\n'
 s+='        float motion = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);\n        float attack = entity.swinging ? (0.5F + 0.5F * Mth.sin(ageInTicks * 1.1F)) : 0.0F;\n        float recoil = entity.hurtTime > 0 ? (entity.hurtTime / 10.0F) : 0.0F;\n'
 for part,expr in anim.items():
  s+=f'        {part} = {expr};\n'
 s+='    }\n}\n'
 (SRC/(cls+'Model.java')).write_text(s)


if __name__ == '__main__':
    for mob_id in ORIGINS:
        geom_file = GEO/(mob_id+'.json')
        model = json.loads(geom_file.read_text())
        size = atlas(model)
        texture(model, size).save(TEX/(mob_id+'.png'))
        model['texture_size'] = list(size)
        geom_file.write_text(json.dumps(model, indent=2)+'\n')
        to_java(model, size)
        print(f"{mob_id}: {sum(len(g) for g in model['groups'].values())} cuboids, atlas {size[0]}x{size[1]}")
