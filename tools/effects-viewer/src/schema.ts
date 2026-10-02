import { z } from 'zod';
import data from './catalog.json';
const spell = z.object({id:z.string(),name:z.string(),rarity:z.enum(['common','uncommon','rare','mythic']),origin:z.enum(['Pathfinder','Iron','Native']),description:z.string(),notes:z.string(),traits:z.array(z.string()),costs:z.array(z.object({type:z.string(),amount:z.number().optional(),ticks:z.number().optional()}))});
export type Spell=z.infer<typeof spell>;
export const catalog=z.array(spell).length(214).parse(data);
export const visibleSpells=(query:string,origin:string,rarity:string)=>catalog.filter(s => (origin==='All' || s.origin===origin) && (rarity==='All' || s.rarity===rarity) && `${s.name} ${s.id} ${s.traits.join(' ')}`.toLowerCase().includes(query.trim().toLowerCase()));
