// Cavern Burrower: a sorcerer's failed experiment that crawled into the sewers.
// One cube list drives both the Java model and the 128x64 texture, so every box's UVs always match its paint.
//   node tools/ui/gen-burrower.mjs [previewDir]
import { Img, noise, mix } from './png.mjs';
import { writeFileSync, mkdirSync } from 'node:fs';
import { join } from 'node:path';

const TEX_W = 128, TEX_H = 64;
const preview = process.argv[2];

// ---------------------------------------------------------------- palettes
const FLESH = { hi: '#a99a9d', base: '#8a7b7f', mid: '#7c6e72', lo: '#6f6265', deep: '#4f4346' };
const DARK_FLESH = { hi: '#8e8084', base: '#76686c', mid: '#6a5d61', lo: '#5d5154', deep: '#43393b' };
// Gray sewer iron; the rust is painted on as spots and streaks so the plates read as metal, not wood.
const IRON = { hi: '#8d8781', base: '#6b635d', mid: '#5b544e', lo: '#4a443f', deep: '#2e2926' };
const BONE = { hi: '#f4eee0', base: '#e8e1cf', mid: '#dcd4bf', lo: '#cfc6b0', deep: '#a89f88' };
const STRAP = { hi: '#5c4532', base: '#3e2d23', mid: '#35261e', lo: '#2a1e17', deep: '#1c140f' };
const CLAW = { hi: '#9a8466', base: '#6e5843', mid: '#614d3a', lo: '#4f3e30', deep: '#30251c' };
const STITCH = '#2b1a1d';
const THREAD = '#b98f86';

// ---------------------------------------------------------------- the model
// Model space: y grows downward, the ground is y = 24, the head faces -z. Boxes are [x, y, z, w, h, d] relative to
// the part's pivot. `uv` is the box-UV origin in the texture; `mat` and `detail` choose the paint.
const PARTS = [
  {
    name: 'body', pivot: [0, 14, 0],
    cubes: [
      { box: [-7, -5, -9, 14, 10, 18], uv: [0, 0], mat: FLESH, detail: 'torso' },
      { box: [-5, -7, -7, 10, 2, 5], uv: [64, 16], mat: IRON, detail: 'plate' },
      { box: [-5, -7, 1, 10, 2, 5], uv: [64, 16], mat: IRON, detail: 'plate' },
    ],
    children: [
      {
        name: 'tail', pivot: [0, -1, 9], rot: [-0.45, 0, 0],
        cubes: [{ box: [-1.5, -1, 0, 3, 3, 6], uv: [48, 28], mat: FLESH, detail: 'tail' }],
      },
    ],
  },
  {
    name: 'head', pivot: [0, 15, -8],
    cubes: [
      { box: [-4.5, -4, -7, 9, 8, 7], uv: [64, 0], mat: FLESH, detail: 'skull' },
      { box: [-4, -3.5, -8, 8, 7, 1], uv: [96, 0], mat: BONE, detail: 'mask' },
      { box: [-4.5, -2, -7, 9, 1, 7], uv: [64, 24], mat: STRAP, detail: 'strap', inflate: 0.15 },
    ],
  },
  // Front legs carry three oversized digging claws; back legs two small ones. Right legs mirror the left.
  ...[['paw_l_0', 5, -5, false], ['paw_r_0', -5, -5, true]].map(([name, x, z, mirror]) => ({
    name, pivot: [x, 19, z], mirror,
    cubes: [
      { box: [-2, 0, -2, 4, 5, 4], uv: [0, 28], mat: DARK_FLESH, detail: 'leg' },
      { box: [-2, 4, -5, 1, 1, 3], uv: [32, 28], mat: CLAW, detail: 'claw' },
      { box: [-0.5, 4, -5.5, 1, 1, 3], uv: [32, 28], mat: CLAW, detail: 'claw' },
      { box: [1, 4, -5, 1, 1, 3], uv: [32, 28], mat: CLAW, detail: 'claw' },
    ],
  })),
  ...[['paw_l_1', 5, 6, false], ['paw_r_1', -5, 6, true]].map(([name, x, z, mirror]) => ({
    name, pivot: [x, 19, z], mirror,
    cubes: [
      { box: [-2, 0, -2, 4, 5, 4], uv: [16, 28], mat: DARK_FLESH, detail: 'leg' },
      { box: [-1.5, 4, -3.5, 1, 1, 2], uv: [40, 28], mat: CLAW, detail: 'claw' },
      { box: [0.5, 4, -3.5, 1, 1, 2], uv: [40, 28], mat: CLAW, detail: 'claw' },
    ],
  })),
];

// ---------------------------------------------------------------- texture painting
const img = new Img(TEX_W, TEX_H);
const painted = new Set();

/** Box-UV face rectangles for a cube with integer UV dimensions, named by the direction each face looks. */
function faces(u, v, w, h, d) {
  return {
    up: [u + d, v, w, d], down: [u + d + w, v, w, d],
    east: [u, v + d, d, h], north: [u + d, v + d, w, h], west: [u + d + w, v + d, d, h], south: [u + 2 * d + w, v + d, w, h],
  };
}

function shade(mat, face, x, y, fh, seed) {
  // Lit from above and the front-left: tops brightest, undersides darkest, sides graded top to bottom.
  const n = noise(x, y, seed);
  const t = fh > 1 ? y / (fh - 1) : 0;
  let c;
  if (face === 'up') c = n > 0.8 ? mat.hi : n < 0.15 ? mat.mid : mat.base;
  else if (face === 'down') c = n > 0.85 ? mat.lo : mat.deep;
  else {
    c = t < 0.25 ? mat.base : t < 0.75 ? mat.mid : mat.lo;
    if (n > 0.9) c = mat.hi;
    else if (n < 0.08) c = mat.deep;
    if (face === 'west' || face === 'south') c = mix(c, '#000000', 0.08);
  }
  return c;
}

function paintCube(cube) {
  const [, , , w, h, d] = cube.box;
  const [u, v] = cube.uv;
  const key = u + ',' + v;
  if (painted.has(key)) return; // shared UVs (plates, claws) are painted once
  painted.add(key);
  const W = Math.ceil(w), H = Math.ceil(h), D = Math.ceil(d);
  const seed = u * 31 + v * 7;
  for (const [face, [fx, fy, fw, fh]] of Object.entries(faces(u, v, W, H, D))) {
    for (let y = 0; y < fh; y++) for (let x = 0; x < fw; x++) img.px(fx + x, fy + y, shade(cube.mat, face, fx + x, fy + y, fh, seed));
    detail(cube, face, fx, fy, fw, fh);
  }
}

/** A cross-stitched seam from (x0, y0) along a direction, the way the Hole's back-alley doctors close a wound. */
function seam(x0, y0, len, horizontal) {
  for (let i = 0; i < len; i++) {
    const x = horizontal ? x0 + i : x0, y = horizontal ? y0 : y0 + i;
    img.px(x, y, STITCH);
    if (i % 2 === 0) {
      if (horizontal) { img.px(x, y - 1, THREAD); img.px(x, y + 1, STITCH); }
      else { img.px(x - 1, y, THREAD); img.px(x + 1, y, STITCH); }
    }
  }
}

function rivet(x, y) {
  img.px(x, y, '#d0b394');
  img.px(x + 1, y + 1, '#2a1810');
}

function detail(cube, face, fx, fy, fw, fh) {
  switch (cube.detail) {
    case 'torso':
      if (face === 'up') {
        seam(fx + Math.floor(fw / 2), fy + 1, fh - 2, false); // spine seam, nose to tail
        seam(fx + 2, fy + 6, 4, true);
        seam(fx + fw - 6, fy + 11, 4, true);
      }
      if (face === 'east' || face === 'west') {
        seam(fx + 2, fy + 3, fw - 4, true); // flank seam
        // Bruised, smoke-stained patches where the experiment went wrong.
        for (let i = 0; i < 9; i++) {
          const x = fx + 4 + Math.floor(noise(i, fx, 5) * (fw - 8)), y = fy + 5 + Math.floor(noise(i, fy, 6) * (fh - 7));
          img.px(x, y, '#5e4a5c');
          img.px(x + 1, y, '#6b5668');
        }
      }
      if (face === 'north' || face === 'south') seam(fx + 2, fy + 2, fw - 4, true);
      break;
    case 'plate':
      for (let y = 0; y < fh; y++) for (let x = 0; x < fw; x++) {
        const n = noise(fx + x, fy + y, 41);
        if (n > 0.78) img.px(fx + x, fy + y, n > 0.93 ? '#b0652f' : '#8a4a26');
      }
      if (face === 'up') {
        rivet(fx, fy); rivet(fx + fw - 2, fy); rivet(fx, fy + fh - 2); rivet(fx + fw - 2, fy + fh - 2);
        for (let x = 2; x < fw - 2; x += 3) img.px(fx + x, fy + 2, IRON.deep); // weld bead
      }
      if (face !== 'up' && face !== 'down') {
        // Rust running down the plate edges.
        for (let x = 0; x < fw; x += 2) if (noise(x, fy, 9) > 0.5) img.px(fx + x, fy + fh - 1, '#a0552c');
      }
      break;
    case 'skull':
      if (face === 'up') seam(fx + 1, fy + Math.floor(fh / 2), fw - 2, true);
      if (face === 'east' || face === 'west') { seam(fx + 1, fy + 5, fw - 2, true); img.px(fx + 3, fy + 2, '#5e4a5c'); }
      break;
    case 'mask':
      if (face === 'north') {
        // A cracked bone mask: two dark eyeholes, a breathing grille and a crack from the brow.
        for (let x = 0; x < fw; x++) { img.px(fx + x, fy, BONE.hi); img.px(fx + x, fy + fh - 1, BONE.deep); }
        for (let y = 0; y < fh; y++) { img.px(fx, fy + y, BONE.hi); img.px(fx + fw - 1, fy + y, BONE.lo); }
        const eye = (x, y) => {
          img.rect(fx + x, fy + y, 2, 2, '#120c0c');
          img.px(fx + x, fy + y - 1, BONE.deep);
          img.px(fx + x + 1, fy + y - 1, BONE.deep);
        };
        eye(1, 2); eye(5, 2);
        for (let x = 2; x <= 5; x += 1) img.px(fx + x, fy + 5, x % 2 === 0 ? '#2a2220' : BONE.deep);
        img.px(fx + 3, fy + 6, '#2a2220'); img.px(fx + 4, fy + 6, '#2a2220');
        img.px(fx + 5, fy + 1, '#7d7462'); img.px(fx + 6, fy + 2, '#7d7462'); img.px(fx + 6, fy + 3, '#7d7462');
        img.px(fx + 3, fy + 1, BONE.hi);
      }
      break;
    case 'strap':
      if (face === 'east' || face === 'west' || face === 'north') {
        for (let x = 0; x < fw; x++) img.px(fx + x, fy, STRAP.hi);
        if (face === 'east') { img.px(fx + 2, fy, '#b8a27a'); img.px(fx + 3, fy, '#8c7a58'); } // buckle
      }
      break;
    case 'leg':
      if (face !== 'up' && face !== 'down') seam(fx, fy + 1, fw, true);
      break;
    case 'claw':
      // Rusted claws with worn, bone-pale tips at the front.
      if (face === 'north') img.rect(fx, fy, fw, fh, '#d8cdb4');
      if (face === 'east' || face === 'west' || face === 'up') img.px(fx, fy, '#c4b69a');
      break;
    case 'tail':
      if (face !== 'up' && face !== 'down') seam(fx, fy + 1, fw, true);
      break;
  }
}

for (const part of PARTS) {
  for (const c of part.cubes) paintCube(c);
  for (const child of part.children || []) for (const c of child.cubes) paintCube(c);
}

const OUT_TEX = 'src/main/resources/assets/dorohedoro/textures/entity/cavern_burrower.png';
img.save(OUT_TEX);

// ---------------------------------------------------------------- the Java model
const f = n => (Number.isInteger(n) ? n.toFixed(1) : String(n)) + 'F';
function cubes(list, mirror) {
  return list.map(c => {
    const [x, y, z, w, h, d] = c.box;
    const def = c.inflate ? `, new CubeDeformation(${f(c.inflate)})` : '';
    return `\n                .texOffs(${c.uv[0]}, ${c.uv[1]})${mirror ? '.mirror()' : ''}.addBox(${[x, y, z, w, h, d].map(f).join(', ')}${def})`;
  }).join('');
}
function pose(p) {
  const [x, y, z] = p.pivot;
  return p.rot ? `PartPose.offsetAndRotation(${f(x)}, ${f(y)}, ${f(z)}, ${p.rot.map(f).join(', ')})` : `PartPose.offset(${f(x)}, ${f(y)}, ${f(z)})`;
}
let defs = '';
for (const p of PARTS) {
  const v = p.children ? `PartDefinition ${p.name} = ` : '';
  defs += `        ${v}parts.addOrReplaceChild("${p.name}", CubeListBuilder.create()${cubes(p.cubes, p.mirror)},\n                ${pose(p)});\n`;
  for (const c of p.children || []) {
    defs += `        ${p.name}.addOrReplaceChild("${c.name}", CubeListBuilder.create()${cubes(c.cubes, c.mirror)},\n                ${pose(c)});\n`;
  }
}

const java = `package com.dorohedoro.hole.client.entity;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.entity.CavernBurrowerEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * The Cavern Burrower: a sorcerer's failed experiment that crawled into the sewers. A hulking stitched body, a cracked
 * bone mask strapped over its face, rusted plates bolted to its back and oversized digging claws.
 *
 * <p>Generated by {@code tools/ui/gen-burrower.mjs}, which also paints the 128x64 texture from the same cube list.
 * Edit the generator, not this file.
 */
public final class CavernBurrowerModel<T extends CavernBurrowerEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "cavern_burrower"), "main");
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart head;
    private final ModelPart paw_l_0;
    private final ModelPart paw_l_1;
    private final ModelPart paw_r_0;
    private final ModelPart paw_r_1;

    public CavernBurrowerModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.tail = body.getChild("tail");
        this.head = root.getChild("head");
        this.paw_l_0 = root.getChild("paw_l_0");
        this.paw_l_1 = root.getChild("paw_l_1");
        this.paw_r_0 = root.getChild("paw_r_0");
        this.paw_r_1 = root.getChild("paw_r_1");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();
${defs}        return LayerDefinition.create(mesh, ${TEX_W}, ${TEX_H});
    }

    @Override public ModelPart root() { return root; }

    @Override public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        float motion = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
        float attack = entity.swinging ? (0.5F + 0.5F * Mth.sin(ageInTicks * 1.1F)) : 0.0F;
        float recoil = entity.hurtTime > 0 ? (entity.hurtTime / 10.0F) : 0.0F;
        float gait = limbSwing * 2.08F;
        // Diagonal pairs move together, like a heavy four-legged digger.
        leg(paw_l_0, gait, motion, 0.62F, 1.1F, -attack * 0.9F);
        leg(paw_r_1, gait, motion, 0.48F, 0.8F, attack * 0.15F);
        leg(paw_r_0, gait + Mth.PI, motion, 0.62F, 1.1F, -attack * 0.9F);
        leg(paw_l_1, gait + Mth.PI, motion, 0.48F, 0.8F, attack * 0.15F);
        body.y = 14.0F + motion * 0.35F * Mth.cos(gait * 2.0F);
        body.xRot = motion * Mth.cos(gait + 0.6F) * 0.06F + recoil * 0.13F - attack * 0.08F;
        body.zRot = motion * Mth.sin(gait) * 0.07F;
        tail.yRot = Mth.sin(ageInTicks * 0.12F) * 0.18F + motion * Mth.sin(gait) * 0.25F;
        tail.xRot = -0.45F + Mth.sin(ageInTicks * 0.09F) * 0.05F;
        // The masked head tracks its target and lunges on a swipe.
        head.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.6F + Mth.sin(ageInTicks * 0.13F) * 0.04F;
        head.xRot = headPitch * Mth.DEG_TO_RAD * 0.5F + Mth.sin(ageInTicks * 0.08F) * 0.035F - attack * 0.35F;
        head.y = 15.0F + motion * Mth.sin(gait + 0.9F) * 0.25F;
        head.z = -8.0F - attack * 1.2F;
    }

    private static void leg(ModelPart leg, float phase, float motion, float swing, float lift, float extra) {
        leg.xRot = motion * Mth.sin(phase) * swing + extra;
        leg.y = 19.0F - motion * Math.max(0.0F, Mth.sin(phase)) * lift;
    }
}
`;
writeFileSync('src/main/java/com/dorohedoro/hole/client/entity/CavernBurrowerModel.java', java);
console.log('wrote model and', OUT_TEX);

if (preview) {
  mkdirSync(preview, { recursive: true });
  img.scaled(8).save(join(preview, 'burrower-texture-8x.png'));
  console.log('preview', join(preview, 'burrower-texture-8x.png'));
}
