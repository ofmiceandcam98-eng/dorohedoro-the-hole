// Generates the mod's block and item textures as 16x PNGs, using only Node built-ins.
// All art here is procedural and original. Re-run with: node tools/make-textures.mjs
import { deflateSync } from 'node:zlib';
import { mkdirSync, writeFileSync } from 'node:fs';
import { dirname } from 'node:path';

const crcTable = Array.from({ length: 256 }, (_, n) => {
  let c = n;
  for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
  return c >>> 0;
});
const crc = (buf) => {
  let c = 0xffffffff;
  for (const b of buf) c = crcTable[(c ^ b) & 255] ^ (c >>> 8);
  return (c ^ 0xffffffff) >>> 0;
};
function chunk(type, data) {
  const len = Buffer.alloc(4);
  len.writeUInt32BE(data.length);
  const body = Buffer.concat([Buffer.from(type), data]);
  const sum = Buffer.alloc(4);
  sum.writeUInt32BE(crc(body));
  return Buffer.concat([len, body, sum]);
}
function png(size, pixel) {
  const rows = [];
  for (let y = 0; y < size; y++) {
    const row = Buffer.alloc(1 + size * 4);
    for (let x = 0; x < size; x++) row.set(pixel(x, y), 1 + x * 4);
    rows.push(row);
  }
  const header = Buffer.alloc(13);
  header.writeUInt32BE(size, 0);
  header.writeUInt32BE(size, 4);
  header[8] = 8;
  header[9] = 6;
  return Buffer.concat([
    Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]),
    chunk('IHDR', header),
    chunk('IDAT', deflateSync(Buffer.concat(rows))),
    chunk('IEND', Buffer.alloc(0)),
  ]);
}
const noise = (x, y, s) => {
  let h = Math.imul((x * 73856093) ^ (y * 19349663) ^ (s * 83492791), 0x5bd1e995);
  h ^= h >>> 13;
  h = Math.imul(h, 0x5bd1e995);
  return ((h ^ (h >>> 15)) >>> 0) / 4294967295;
};
const mix = (a, b, t) => a.map((v, i) => Math.round(v + (b[i] - v) * t));
const save = (path, data) => {
  mkdirSync(dirname(path), { recursive: true });
  writeFileSync(path, data);
};
const root = 'src/main/resources/assets/dorohedoro/textures';

// Riveted sheet steel, streaked with rust running down from the seams.
save(`${root}/block/rusted_plating.png`, png(16, (x, y) => {
  const steel = [92, 74, 62];
  const rust = [146, 76, 38];
  const dark = [48, 38, 34];
  const streak = noise(x, 0, 5) * 0.7 + noise(x, y >> 2, 6) * 0.3;
  let c = mix(steel, rust, Math.min(1, streak * 1.1 + y / 40));
  c = mix(c, [c[0] + 14, c[1] + 10, c[2] + 8], noise(x, y, 7) * 0.5);
  if (y === 0 || y === 15 || x === 0 || x === 15) c = dark;
  if ((x === 2 || x === 13) && (y === 2 || y === 13)) c = [176, 150, 120];
  return [...c, 255];
}));

// A compacted heap of pipe ends, gears and sheet offcuts.
save(`${root}/block/industrial_debris.png`, png(16, (x, y) => {
  const cell = noise(x >> 2, y >> 2, 11);
  const palette = [[70, 62, 56], [112, 72, 44], [58, 66, 62], [134, 96, 58], [40, 36, 34]];
  let c = palette[Math.floor(cell * palette.length) % palette.length];
  c = mix(c, [24, 20, 18], noise(x, y, 12) * 0.45);
  if ((x & 3) === 0 && noise(x, y, 13) > 0.55) c = [22, 18, 16];
  return [...c, 255];
}));

// A twisted shard of salvaged metal.
save(`${root}/item/scrap_metal.png`, png(16, (x, y) => {
  const inShard = Math.abs(x - y) < 5 && x + y > 7 && x + y < 24 && noise(x, y, 21) > 0.12;
  if (!inShard) return [0, 0, 0, 0];
  const edge = Math.abs(x - y) === 4;
  const base = mix([120, 112, 104], [150, 84, 44], noise(x, y, 22));
  return [...(edge ? mix(base, [40, 34, 30], 0.6) : base), 255];
}));
console.log('textures written');

// Phase 3: Hell's ground blocks; standalone original 16x textures, not recycled Nether artwork.
save(`${root}/block/hell_rock.png`, png(16, (x, y) => {
  const veins = noise(x >> 1, y >> 1, 61);
  const cracks = (x + 2 * y) % 11 === 0 && noise(x, y, 62) > 0.30;
  const rock = mix([49, 43, 46], [83, 67, 65], veins * 0.75);
  return [...(cracks ? mix(rock, [23, 19, 24], 0.75) : rock), 255];
}));
save(`${root}/block/ash_soil.png`, png(16, (x, y) => {
  const grain = noise(x, y, 64);
  const soot = noise(x >> 2, y >> 2, 65);
  return [...mix([67, 60, 57], [120, 106, 100], grain * 0.55 + soot * 0.28), 255];
}));

// Phase 4 original industrial portal-component textures. No borrowed game/anime art.
for (const [name, palette, seed] of [
  ['reinforced_door_frame', [[68,66,67],[117,108,99]], 71],
  ['etched_gate_stone', [[38,32,40],[115,63,73]], 73],
  ['artificial_door_core', [[41,56,59],[127,154,132]], 75],
  ['infernal_door_core', [[33,28,37],[152,77,82]], 77],
  ['portal_surface', [[30,35,34],[77,95,86]], 79],
  ['generated_door_frame', [[68,66,67],[117,108,99]], 71],
  ['generated_gate_stone', [[38,32,40],[115,63,73]], 73],
  ['generated_artificial_core', [[41,56,59],[127,154,132]], 75],
  ['generated_infernal_core', [[33,28,37],[152,77,82]], 77],
]) {
  save(`${root}/block/${name}.png`, png(16,(x,y)=>{
    let c=mix(palette[0],palette[1],noise(x>>1,y>>1,seed)*0.85);
    if (name==='portal_surface') return [...mix(c,[16,21,18],Math.abs(x-8)/17),170];
    if (x===0||x===15||y===0||y===15) c=[19,21,21];
    if (name.includes('core') && (x===7||x===8||y===7||y===8)) c=palette[1];
    if (name.includes('frame') && ((x===3||x===12)&&(y===3||y===12))) c=[157,149,126];
    return [...c,255];
  }));
}
for (const [name,seed] of [
 ['smoke_residue',81],['smoke_condenser',83],['smoke_conduit',85],['preserved_smoke_conduit',87],
 ['door_activation_tool',89],['door_linking_key',91],['devil_sigil',93],['ritual_tool',95]]) {
  save(`${root}/item/${name}.png`, png(16,(x,y)=>{
    const shape = (x>3 && x<13 && y>2 && y<14 && Math.abs(x-8)+Math.abs(y-8)<10);
    if (!shape) return [0,0,0,0];
    const accent=(name.includes('devil')||name.includes('ritual'))?[174,70,67]:[131,155,139];
    const c=mix([36,40,42],accent,noise(x,y,seed)*0.7+(x===8||y===8?0.3:0));
    return [...c,255];
  }));
}

// Recognizable original Phase 4 item sprites. These override the preliminary geometric silhouettes
// above, so regenerating textures produces the reviewed, distinctive art every time.
function sprite(name, paint) {
  const cells = Array.from({length:16},()=>Array.from({length:16},()=>[0,0,0,0]));
  const color = (c) => [...c,255];
  const dot = (x,y,c) => { x=Math.round(x);y=Math.round(y); if(x>=0&&x<16&&y>=0&&y<16)cells[y][x]=color(c); };
  const rect = (x1,y1,x2,y2,c) => {for(let y=y1;y<=y2;y++)for(let x=x1;x<=x2;x++)dot(x,y,c);};
  const line = (x1,y1,x2,y2,c) => {
    const n=Math.max(Math.abs(x2-x1),Math.abs(y2-y1));for(let i=0;i<=n;i++){let t=n?i/n:0;dot(x1+(x2-x1)*t,y1+(y2-y1)*t,c);}
  };
  const circle = (cx,cy,r,c,filled=false) => {
    for(let y=0;y<16;y++)for(let x=0;x<16;x++){
      const d=Math.hypot(x-cx,y-cy);if(filled?d<=r:d>=r-0.6&&d<=r+0.6)dot(x,y,c);
    }
  };
  paint({dot,rect,line,circle});
  save(`${root}/item/${name}.png`,png(16,(x,y)=>cells[y][x]));
}
const iron=[91,101,104], steel=[154,159,149], shadow=[35,38,45], copper=[164,94,52], amber=[209,149,72];
const green=[105,185,148], glow=[182,232,192], crimson=[159,53,67], soot=[64,59,66];
sprite('smoke_soaked_filter',({dot,rect,line})=>{
  rect(3,2,12,13,shadow);rect(4,3,11,12,iron);rect(5,4,10,11,[49,51,56]);
  for(let v=5;v<12;v+=2){line(v,4,v,11,[92,83,85]);line(5,v,10,v,[78,77,80]);}
  rect(6,6,9,8,soot);dot(7,5,[30,26,34]);dot(9,9,[24,24,29]);
  for(const [x,y] of [[3,2],[12,2],[3,13],[12,13]])dot(x,y,amber);
});
sprite('smoke_residue',({dot,circle})=>{
  for(let y=7;y<=13;y++)for(let x=3;x<=12;x++)if((x-8)**2/32+(y-11)**2/10<1&&noise(x,y,135)>.13)dot(x,y,[45+Math.round(31*noise(x,y,136)),43,52]);
  for(const [x,y] of [[6,4],[9,3],[11,5],[4,6],[12,7]])dot(x,y,[115,108,120]);
  circle(8,9,2,[102,90,103]);
});
sprite('smoke_condenser',({dot,rect,circle})=>{
  rect(3,3,12,12,shadow);rect(4,4,11,11,copper);rect(5,5,10,10,iron);
  circle(8,8,3,shadow,true);circle(8,8,2,green,true);dot(8,8,glow);
  rect(6,1,9,3,steel);rect(6,12,9,14,copper);dot(3,4,amber);dot(12,11,amber);
});
sprite('smoke_conduit',({rect,line,dot})=>{
  line(4,12,4,5,shadow);line(4,5,11,5,shadow);rect(3,11,6,13,copper);rect(9,3,12,6,copper);
  line(5,11,5,6,copper);line(5,6,10,6,amber);line(6,7,10,7,steel);dot(5,5,steel);
});
sprite('preserved_smoke_conduit',({rect,line,dot})=>{
  line(4,12,4,5,shadow);line(4,5,11,5,shadow);rect(3,11,6,13,copper);rect(9,3,12,6,copper);
  line(5,11,5,6,copper);line(5,6,10,6,glow);line(6,7,10,7,green);dot(7,5,[245,239,188]);
});
sprite('door_activation_tool',({rect,line,circle,dot})=>{
  line(4,13,10,6,shadow);line(5,12,11,5,steel);line(4,12,10,5,iron);
  circle(11,4,3,iron);rect(11,1,15,4,[0,0,0]);line(10,3,11,5,steel);dot(4,13,copper);
});
sprite('door_linking_key',({rect,line,circle,dot})=>{
  circle(5,5,3,amber);circle(5,5,1,shadow,true);line(7,7,12,12,steel);
  line(7,8,11,12,iron);rect(10,10,13,12,copper);rect(12,11,14,13,amber);
});
sprite('devil_sigil',({rect,line,circle,dot})=>{
  rect(3,2,12,13,[169,141,103]);rect(4,3,11,12,[208,187,142]);
  circle(8,8,4,crimson);line(8,4,5,11,crimson);line(8,4,11,11,crimson);line(5,11,11,11,crimson);
  circle(8,8,1,shadow,true);dot(3,2,[106,78,59]);dot(12,13,[106,78,59]);
});
sprite('ritual_tool',({rect,line,circle,dot})=>{
  line(5,13,10,4,[65,53,57]);line(6,12,11,3,[201,184,143]);
  circle(11,4,2,crimson,true);dot(11,3,[248,158,145]);
  rect(4,11,6,13,iron);line(8,7,10,9,crimson);
});

// Layered flood-control masonry: original low-saturation pixel texture, playable as crafted block.
save(`${root}/block/sewer_brickwork.png`, png(16, (x, y) => {
  const mortar = [38, 45, 44];
  const shift = (y < 5 ? 0 : y < 11 ? 5 : 0);
  const seam = (y === 0 || y === 5 || y === 11) || ((x + shift) % 9 === 0);
  if (seam) return [...mortar, 255];
  const stain = noise(x, y, 467), rust = noise(x >> 1, y >> 1, 468);
  let c = mix([83, 90, 81], [47, 64, 57], stain * 0.6);
  if (rust > 0.81 && y > 4) c = mix(c, [101, 65, 51], 0.52);
  if (noise(x, y, 469) > 0.91) c = mix(c, [110, 110, 98], 0.35);
  return [...c, 255];
}));
