import { z } from 'zod';
import data from './native-clips.json';
const subject=z.object({role:z.string(),before:z.number().nonnegative(),after:z.number().nonnegative(),alive:z.boolean(),displacement:z.number().nonnegative().optional()});
const clip=z.object({spell:z.string(),phase:z.literal(0),label:z.string(),kind:z.literal('cast'),engine:z.string(),outcome:z.enum(['COMPLETED','AWAITING_RECAST','RUNNING','CHARGING']),definitionSha256:z.string().regex(/^[0-9a-f]{64}$/),width:z.number().int().positive(),height:z.number().int().positive(),url:z.string().regex(/^\/native\/vestige-[a-z0-9_]+-cast-\d+\.mp4$/),seconds:z.number().positive(),frames:z.number().int().min(2),audio:z.literal(false),videoSha256:z.string().regex(/^[0-9a-f]{64}$/),captureFps:z.number().positive(),castContext:z.object({scenario:z.string(),subjects:z.array(subject),observations:z.array(z.string()).default([])})});
export const nativeClips=z.array(clip).parse(data);
export const nativeClip=(spell:string)=>nativeClips.find(c=>c.spell===spell);
export const nativeMediaUrl=(clip:{url:string;videoSha256:string})=>`${clip.url}?v=${clip.videoSha256}`;
/** Only clips that executed a spell count as cast demonstrations. */
export const recordedCastIds:ReadonlySet<string>=new Set(nativeClips.map(c=>c.spell));
