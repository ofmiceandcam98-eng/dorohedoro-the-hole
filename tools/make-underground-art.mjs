// Original 16px item/block sprites and 64px voxel-model UV textures; no external packages.
// Run from repository root: node tools/make-underground-art.mjs
import { deflateSync } from 'node:zlib';
import { mkdirSync, writeFileSync } from 'node:fs';
import { dirname } from 'node:path';
const table = Array.from({length:256},(_,n)=>{let c=n;for(let i=0;i<8;i++)c=(c&1)?(0xedb88320^(c>>>1)):(c>>>1);return c>>>0;});
function crc(buf){let c=0xffffffff;for(let n of buf)c=table[(c^n)&255]^(c>>>8);return(c^0xffffffff)>>>0;}
function png(size,pixel){const rows=[];for(let y=0;y<size;y++){let r=Buffer.alloc(size*4+1);for(let x=0;x<size;x++)r.set(pixel(x,y),1+x*4);rows.push(r);}let hdr=Buffer.alloc(13);hdr.writeUInt32BE(size,0);hdr.writeUInt32BE(size,4);hdr[8]=8;hdr[9]=6;let chk=(type,p)=>{let l=Buffer.alloc(4);l.writeUInt32BE(p.length);let b=Buffer.concat([Buffer.from(type),p]);let c=Buffer.alloc(4);c.writeUInt32BE(crc(b));return Buffer.concat([l,b,c]);};return Buffer.concat([Buffer.from([137,80,78,71,13,10,26,10]),chk('IHDR',hdr),chk('IDAT',deflateSync(Buffer.concat(rows))),chk('IEND',Buffer.alloc(0))]);}
function hash(x,y,s){let v=Math.imul(x*73856093^y*19349663^s*83492791,0x5bd1e995);v^=v>>>13;return((v^(v>>>15))>>>0)/4294967295;}
function mix(a,b,t){return a.map((v,i)=>Math.round(v*(1-t)+b[i]*t));}
function save(path,size,pixel){mkdirSync(dirname(path),{recursive:true});writeFileSync(path,png(size,pixel));}
const root='src/main/resources/assets/dorohedoro/textures';
function stone(name,low,hi,seed,vein){save(`${root}/block/${name}.png`,16,(x,y)=>{const n=hash(x>>1,y>>1,seed);let c=mix(low,hi,n*0.85);if((hash(x,y,seed+3)>0.78&&hash(x>>2,y>>2,seed+8)>0.25)||((x+2*y+seed)%17===0))c=mix(c,vein,0.88);if((x*2+y)%13===0)c=mix(c,[27,26,25],0.25);return [...c,255];});}
stone('sulfur_ore',[70,74,69],[116,111,98],211,[222,194,57]);
stone('rock_salt_ore',[70,72,71],[118,116,108],223,[225,192,185]);
stone('limestone',[104,104,87],[172,164,130],233,[195,187,149]);
stone('chitin_plating',[58,42,35],[124,82,51],244,[160,113,66]);
function sprite(name,draw){const cells=Array.from({length:16},()=>Array.from({length:16},()=>[0,0,0,0]));const dot=(x,y,c)=>{x=Math.round(x);y=Math.round(y);if(x>=0&&x<16&&y>=0&&y<16)cells[y][x]=[...c,255];};const line=(x1,y1,x2,y2,c)=>{let n=Math.max(Math.abs(x2-x1),Math.abs(y2-y1));for(let i=0;i<=n;i++){let t=n?i/n:0;dot(x1+(x2-x1)*t,y1+(y2-y1)*t,c);}};const rect=(x0,y0,x1,y1,c)=>{for(let y=y0;y<=y1;y++)for(let x=x0;x<=x1;x++)dot(x,y,c);};draw({dot,line,rect});save(`${root}/item/${name}.png`,16,(x,y)=>cells[y][x]);}
const edge=[37,33,33],light=[228,213,167],deep=[92,57,43],gold=[232,187,55],pink=[235,192,184],copper=[168,98,58];
sprite('raw_sulfur',({line,rect,dot})=>{rect(4,8,11,12,[141,104,35]);rect(5,6,7,11,gold);rect(8,3,10,12,[208,168,50]);rect(10,7,12,12,[172,133,40]);line(8,3,9,5,[247,222,80]);line(5,6,6,8,[249,210,77]);});
sprite('rock_salt',({rect,line,dot})=>{rect(3,5,11,13,[127,102,104]);rect(5,3,13,11,pink);rect(6,4,11,9,[242,223,218]);line(5,3,3,5,light);line(11,12,13,10,[160,120,122]);});
sprite('lime_powder',({rect,line})=>{rect(3,10,13,12,[122,120,104]);rect(5,8,11,10,[215,211,189]);rect(7,6,9,8,[244,234,207]);rect(6,4,7,5,[203,195,175]);});
sprite('reinforced_chitin',({rect,line,dot})=>{rect(3,5,12,12,deep);rect(4,4,11,10,[114,76,49]);rect(5,3,10,5,[173,117,68]);line(4,7,11,7,edge);line(4,10,11,10,edge);dot(6,4,light);});
sprite('roach_antenna',({line,dot})=>{line(3,13,5,8,deep);line(5,8,4,4,[126,84,58]);line(4,4,6,2,light);line(7,13,10,9,[78,62,45]);line(10,9,12,4,[161,115,68]);line(12,4,11,2,light);});
sprite('rat_fur',({line,rect,dot})=>{rect(4,5,11,12,[93,80,74]);rect(6,4,10,7,[154,132,107]);for(let i=4;i<12;i+=2)line(i,5,i-2,3,[109,95,83]);line(5,12,3,14,[143,112,94]);});
sprite('raw_rat_meat',({rect,line,dot})=>{rect(3,7,12,11,[111,43,48]);rect(5,5,11,12,[164,70,78]);rect(7,6,10,10,[208,120,114]);dot(5,11,[238,203,159]);});
sprite('cooked_rat_meat',({rect,line,dot})=>{rect(3,7,12,11,[92,52,29]);rect(5,5,11,12,[155,96,46]);rect(7,6,10,10,[201,137,64]);line(6,8,11,8,[99,46,24]);});
sprite('salted_rat_ration',({rect,line})=>{rect(2,5,13,12,[110,74,47]);rect(3,4,12,6,[209,165,92]);rect(3,7,12,10,[163,82,64]);line(4,8,11,8,[235,215,170]);rect(3,11,12,13,[214,164,89]);});
sprite('sewer_echo_probe',({rect,line,dot})=>{rect(7,7,9,14,[86,75,69]);rect(6,6,10,9,copper);rect(5,3,11,6,[80,94,91]);rect(6,2,10,4,[131,164,145]);line(4,4,2,2,[158,113,75]);line(12,4,14,2,[158,113,75]);dot(8,5,[177,242,185]);});
// Deliberately handmade pixel textures for two DIFFERENT articulated voxel silhouettes.
// UVs: roach shell, head, abdomen, legs, feelers; rat body, head, ears, paws, tail.
function creature(name,base,light,seed,eyes){save(`${root}/entity/${name}.png`,64,(x,y)=>{let rough=hash(x>>2,y>>2,seed);let freckles=hash(x,y,seed+1);let c=mix(base,light,rough*0.55+(freckles>0.90?0.25:0));if(name==='sewer_roach'){
  if(x>=42&&x<61&&y<16)c=mix([66,46,37],light,rough*0.25);
  if(x>=45&&x<=59&&y>=9&&y<=12)c=[47,36,32];
  if((x===47||x===54)&&y===6)c=eyes;
  if(x>=0&&x<=40&&y>=21&&y<=32){c=(x+y)%9<2?[93,61,43]:[126,85,52];}
  if(y>=35&&y<50)c=mix([48,37,31],[139,93,62],rough*0.45);
} else {
  if(x<36&&y<31)c=mix(base,light,0.4+rough*0.35);
  if(x>=26&&x<37&&y>=18&&y<30)c=[159,104,110];
  if((x===3||x===13)&&y===21)c=eyes;
  if(y>=30)c=mix([104,67,69],[179,114,110],rough*0.4);
}return [...c,255];});}
creature('sewer_roach',[70,47,36],[159,105,61],301,[213,174,71]);
creature('scrap_rat',[88,78,76],[183,154,137],313,[229,62,52]);
console.log('2 original entity UV textures, 4 blocks, and 10 item sprites written');
