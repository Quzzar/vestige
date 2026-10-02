import { useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { catalog, visibleSpells } from './schema';
import * as c from './workshop.css';
import { nativeClip, nativeMediaUrl, recordedCastIds } from './native-media';

function Workshop(){
 const [query,setQuery]=useState(''),[origin,setOrigin]=useState('All'),[rarity,setRarity]=useState('All');
 const [selected,setSelected]=useState(catalog.find(s=>s.id==='vestige:pf2_wall_of_ice')??catalog[0]);
 const [castsOnly,setCastsOnly]=useState(false),[message,setMessage]=useState('');
 const footage=nativeClip(selected.id);
 const visible=useMemo(()=>visibleSpells(query,origin,rarity).filter(s=>!castsOnly||recordedCastIds.has(s.id)),[query,origin,rarity,castsOnly]);
 const mana=selected.costs.find(cost=>cost.type==='mana')?.amount??0,recovery=(selected.costs.find(cost=>cost.type==='cooldown')?.ticks??0)/20;
 async function copyCommand(){
  const command=`/vestige_magic cast ${selected.id}`;
  try{await navigator.clipboard.writeText(command);setMessage('Minecraft command copied.');}
  catch{setMessage(command);}
 }
 return <>
  <header className={c.header}><div className={c.brand}><span className={c.mark} aria-hidden="true">V</span><div><h1>Vestige</h1><p className={c.small}>Spell casting showcase</p></div></div><p className={c.small}>{recordedCastIds.size} cast videos · {catalog.length} spells</p></header>
  <div className={c.layout}>
   <aside className={c.sidebar} aria-label="Spell catalog"><div className={c.filters}><h3>Spell library</h3><input aria-label="Search spells" placeholder="Find a spell or trait…" value={query} onChange={e=>setQuery(e.target.value)}/><div className={c.filterRow}><select aria-label="Source" value={origin} onChange={e=>setOrigin(e.target.value)}>{['All','Pathfinder','Iron','Native'].map(s=><option key={s}>{s}</option>)}</select><select aria-label="Rarity" value={rarity} onChange={e=>setRarity(e.target.value)}>{['All','common','uncommon','rare','mythic'].map(s=><option key={s}>{s}</option>)}</select></div><p className={c.small}>{visible.length} {visible.length===1?'spell':'spells'}</p></div>
    <label className={c.coverageFilter}><input type="checkbox" checked={castsOnly} onChange={e=>setCastsOnly(e.target.checked)}/>Recorded casts only</label>
    <nav className={c.list} aria-label="Spells">{visible.map(s=><button key={s.id} className={c.item} aria-pressed={selected.id===s.id} onClick={()=>{setSelected(s);setMessage('');}}><span><span className={c.itemName}>{s.name}</span><span style={{display:'block'}} className={c.small}>{s.origin} · {s.rarity}</span></span><span className={c.small}>{recordedCastIds.has(s.id)?'Cast':'Awaiting recording'}</span></button>)}{!visible.length&&<p className={c.emptyState}>No spells match these filters.</p>}</nav>
   </aside>
   <main className={c.main}>
    <div className={c.titleRow}><div><p className={c.subtitle} style={{margin:'0 0 7px'}}>Spell {String(catalog.indexOf(selected)+1).padStart(3,'0')} / {catalog.length}</p><h2>{selected.name}</h2><p className={c.subtitle}><span className={c.rarityDot}/>{selected.rarity} · {selected.origin} adaptation</p></div><button onClick={copyCommand}>Copy cast command</button></div>
    <section aria-label="Animation preview">
      {footage?<>
       <div className={c.nativeStage}><video key={nativeMediaUrl(footage)} aria-label="Actual Minecraft spell cast" src={nativeMediaUrl(footage)} controls autoPlay={!window.matchMedia('(prefers-reduced-motion: reduce)').matches} muted loop playsInline preload="metadata" style={{width:'100%',height:'100%',objectFit:'contain'}}/><span className={c.stageCaption} style={{color:'#fff',background:'#131e26dd',padding:'5px 8px',borderRadius:4}}>ACTUAL MINECRAFT CAST · {footage.seconds.toFixed(1)}s</span></div>
       <p className={c.banner} style={{marginTop:10}}>Recorded inside Minecraft with the actual spell runtime. Silent recording.</p>
       {footage.castContext&&<p className={c.castOutcome}>{footage.castContext.scenario} · {footage.castContext.subjects.filter(s=>s.role!=='Caster'||s.before!==s.after).map(s=>`${s.role}: ${s.before} → ${s.after} HP${s.displacement && s.displacement>.2 ? ` · moved ${s.displacement.toFixed(1)} blocks` : ''}`).join(' · ')}</p>}
       {footage.castContext.observations.length>0&&<p className={c.small}>{footage.castContext.observations.join(' · ')}</p>}
       <p className={c.small} style={{marginTop:8}}><a href={nativeMediaUrl(footage)} download>Download Minecraft clip</a></p>
      </>:<div className={c.emptyState}><h3>Cast not recorded yet</h3><p className={c.banner}>This spell is implemented; its cast recording is still pending.</p></div>}
      <p className={c.small} role="status" style={{marginTop:7,minHeight:18}}>{message}</p>
    </section>
    <div className={c.details}><section><h3>What happens</h3><p className={c.body} style={{marginTop:10}}>{selected.description}</p><div className={c.chips}>{selected.traits.filter(t=>!['vestige:range','vestige:area','vestige:amplify'].includes(t)).map(t=><span className={c.chip} key={t}>{t.replace('vestige:','')}</span>)}</div><div className={c.stats}><div><p className={c.small}>Mana</p><p className={c.statNumber}>{mana}</p></div><div><p className={c.small}>Recovery</p><p className={c.statNumber}>{recovery}s</p></div></div><p className={c.notes}>{selected.notes}</p></section>

    </div>
   </main>
  </div>
 </>;
}
const root=document.getElementById('root');if(!root)throw new Error('Missing workshop root');createRoot(root).render(<Workshop/>);
