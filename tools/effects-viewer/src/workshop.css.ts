import { globalStyle, style } from '@vanilla-extract/css';
const t={ink:'#18242c',muted:'#60717c',line:'#d9dfdf',paper:'#f2f4f2',panel:'#fff',accent:'#b08b48',stage:'#131e26'};
const review={space:18,gap:8,type:12};
globalStyle('*',{boxSizing:'border-box'});
globalStyle('body',{margin:0,fontFamily:'"Avenir Next", Avenir, system-ui, sans-serif',background:t.paper,color:t.ink});
globalStyle('button, input, select',{font:'inherit'});globalStyle('button',{cursor:'pointer'});
globalStyle('button:disabled',{cursor:'default',opacity:.55});globalStyle('button:focus-visible, input:focus-visible, select:focus-visible',{outline:'3px solid #bc9552',outlineOffset:3});
globalStyle('button',{border:'1px solid '+t.line,borderRadius:6,background:t.panel,padding:'8px 12px',color:t.ink});
globalStyle('h1,h2,h3,p',{margin:0});globalStyle('h1',{fontSize:21,fontWeight:600,letterSpacing:'-.035em'});globalStyle('h2',{fontSize:30,fontWeight:600,letterSpacing:'-.035em',lineHeight:1.15});globalStyle('h3',{fontSize:12,textTransform:'uppercase',letterSpacing:'.1em',fontWeight:600});
globalStyle('input,select',{minWidth:0,border:'1px solid '+t.line,borderRadius:6,padding:'9px 10px',background:t.panel,color:t.ink});globalStyle('input[type="range"]',{padding:0,accentColor:t.accent});
export const header=style({height:78,padding:'0 26px',display:'flex',alignItems:'center',justifyContent:'space-between',borderBottom:'1px solid '+t.line,background:t.panel});
export const brand=style({display:'flex',gap:15,alignItems:'center'});
export const mark=style({width:32,height:36,border:'1px solid '+t.accent,display:'grid',placeItems:'center',color:t.accent,fontSize:22,transform:'rotate(-8deg)'});
export const small=style({fontSize:12,color:t.muted,lineHeight:1.5});
export const layout=style({display:'grid',gridTemplateColumns:'284px minmax(0,1fr)',height:'calc(100dvh - 78px)',minHeight:0,'@media':{'(max-width:800px)':{display:'block',height:'auto'}}});
export const sidebar=style({background:t.panel,borderRight:'1px solid '+t.line,display:'flex',flexDirection:'column',minHeight:0,'@media':{'(max-width:800px)':{maxHeight:350,borderBottom:'1px solid '+t.line}}});
export const filters=style({padding:18,display:'grid',gap:10,borderBottom:'1px solid '+t.line});
export const filterRow=style({display:'grid',gridTemplateColumns:'1fr 1fr',gap:8});
export const list=style({overflowY:'auto',padding:'8px 10px',display:'flex',flexDirection:'column',gap:2});
export const item=style({border:0,borderRadius:5,textAlign:'left',padding:'11px 12px',background:'transparent',display:'flex',justifyContent:'space-between',gap:8,alignItems:'center',selectors:{'&:hover':{background:'#f1f4f3'},'&[aria-pressed="true"]':{background:'#e9efed',boxShadow:'inset 3px 0 0 #b08b48'}}});
export const itemName=style({fontSize:14,fontWeight:500});export const rarityDot=style({width:6,height:6,borderRadius:'50%',display:'inline-block',marginRight:6,background:t.accent});
export const main=style({padding:24,overflowY:'auto',display:'flex',flexDirection:'column',gap:20,'@media':{'(max-width:800px)':{padding:16}}});
export const titleRow=style({display:'flex',justifyContent:'space-between',alignItems:'flex-start',gap:16});
export const subtitle=style({marginTop:7,fontSize:12,color:t.muted,textTransform:'uppercase',letterSpacing:'.06em'});
export const stage=style({position:'relative',width:'100%',height:'min(49vh,530px)',minHeight:330,background:t.stage,borderRadius:10,overflow:'hidden',border:'1px solid #283742','@media':{'(max-width:800px)':{height:350,minHeight:260}}});
export const nativeStage=style([stage,{height:'auto',minHeight:0,aspectRatio:'16 / 9','@media':{'(max-width:800px)':{height:'auto',minHeight:0}}}]);
export const stageCaption=style({position:'absolute',left:18,top:15,color:'#a3b4be',fontSize:12,pointerEvents:'none'});




export const details=style({display:'grid',gridTemplateColumns:'1fr',gap:28,'@media':{'(max-width:1050px)':{gridTemplateColumns:'1fr'}}});
export const body=style({fontSize:14,lineHeight:1.65,color:t.ink});export const notes=style({fontSize:12,lineHeight:1.6,color:t.muted,marginTop:14});
export const chips=style({display:'flex',flexWrap:'wrap',gap:6,margin:'12px 0'});export const chip=style({fontSize:11,background:'#e7ecea',borderRadius:4,padding:'4px 7px',color:'#435963'});
export const stats=style({display:'grid',gridTemplateColumns:'repeat(2,1fr)',gap:12,padding:'14px 0',borderTop:'1px solid '+t.line,borderBottom:'1px solid '+t.line});export const statNumber=style({fontSize:19,fontWeight:500,marginTop:4});



export const banner=style({color:t.muted,fontSize:12,lineHeight:1.6});

export const coverageFilter=style({display:'flex',gap:review.gap,alignItems:'center',padding:review.space,fontSize:review.type,color:t.ink,borderBottom:'1px solid '+t.line});
export const emptyState=style({display:'grid',gap:review.gap,padding:review.space,color:t.muted});
export const castOutcome=style({fontSize:review.type,color:t.ink,marginTop:review.gap,fontVariantNumeric:'tabular-nums'});
