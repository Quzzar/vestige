import { describe, expect, test } from 'bun:test';
import { catalog, visibleSpells } from './src/schema';
import { nativeClip, nativeClips, nativeMediaUrl, recordedCastIds } from './src/native-media';

describe('actual-cast library',()=>{
 test('contains all native definitions and uses consistent display titles',()=>{
  expect(new Set(catalog.map(s=>s.id)).size).toBe(214);
  expect(catalog.filter(s=>s.origin==='Pathfinder')).toHaveLength(100);
  expect(catalog.filter(s=>s.origin==='Iron')).toHaveLength(110);
  expect(catalog.find(s=>s.id==='vestige:ray_of_frost')?.name).toBe('Ray of Frost');
  expect(catalog.every(s=>!('phases' in s))).toBe(true);
 });
 test('search accepts names, IDs, traits and combined filters',()=>{
  const arc=catalog.find(s=>s.id==='vestige:pf2_electric_arc')!;
  expect(visibleSpells(' ELECTRIC ARC ','Pathfinder',arc.rarity)).toEqual([arc]);
  expect(visibleSpells('pf2_electric_arc','Iron','All')).toEqual([]);
  expect(visibleSpells('lightning','Pathfinder','All')).toContain(arc);
 });
 test('every published clip is an executed native cast with recorded subjects',()=>{
  expect([...recordedCastIds].sort()).toEqual(catalog.map(s=>s.id).sort());
  expect(recordedCastIds.size).toBe(nativeClips.length);
  for(const clip of nativeClips) {
   expect(clip.kind).toBe('cast');
   expect(clip.engine).toContain('Minecraft 1.21.1');
   expect(['COMPLETED','AWAITING_RECAST','RUNNING','CHARGING']).toContain(clip.outcome);
   expect(clip.castContext?.subjects.some(s=>s.role==='Caster')).toBe(true);
  }
 });
 test('recaptured footage gets a new playback URL',()=>{
  const original={url:'/native/vestige-pf2_wall_of_ice-cast-0.mp4',videoSha256:'a'.repeat(64)};
  expect(nativeMediaUrl(original)).not.toBe(nativeMediaUrl({...original,videoSha256:'b'.repeat(64)}));
 });
 test('fireball demonstrates damage inside its area while outsiders remain unharmed',()=>{
  const clip=nativeClip('vestige:pf2_fireball')!;
  expect(clip.castContext!.subjects.find(s=>s.role==='Inside A')!.after).toBeLessThan(20);
  for(const subject of clip.castContext!.subjects.filter(s=>s.role.startsWith('Outside')))expect(subject.after).toBe(subject.before);
 });
 test('lightning recordings demonstrate their different target counts',()=>{
  const damaged=(id:string)=>nativeClip(id)!.castContext!.subjects.filter(s=>s.after<s.before);
  expect(damaged('vestige:pf2_electric_arc')).toHaveLength(2);
  expect(damaged('vestige:pf2_chain_lightning')).toHaveLength(6);
 });
});
