// Offline export of the preview's actual guide function and unchanged image assets.
import fs from 'node:fs';
import vm from 'node:vm';
import path from 'node:path';
import { createRequire } from 'node:module';

const root = path.dirname(new URL(import.meta.url).pathname);
const source = fs.readFileSync(path.join(root, 'index.html'), 'utf8');
const layouts = vm.runInNewContext(`(${source.match(/const layouts=([\s\S]*?);\nconst recipes=/)[1]})`);
const recipes = vm.runInNewContext(`(${source.match(/const recipes=([^\n]+);\n/)[1]})`);
const escape = value => String(value).replaceAll('&', '&amp;').replaceAll('"', '&quot;').replaceAll('<', '&lt;');
class Element {
  constructor(name) { this.name=name; this.attrs={}; this.dataset={}; this.children=[]; this.classList={add(){}}; }
  setAttribute(name,value) { this.attrs[name]=value; }
  append(child) { this.children.push(child); }
  xml() {
    const attrs={...this.attrs,...Object.fromEntries(Object.entries(this.dataset).map(([key,value])=>[`data-${key.replace(/[A-Z]/g,letter=>'-'+letter.toLowerCase())}`,value]))};
    return `<${this.name} ${Object.entries(attrs).map(([key,value])=>`${key}="${escape(value)}"`).join(' ')}>${this.children.map(child=>child.xml()).join('')}</${this.name}>`;
  }
}
const context = vm.createContext({document:{createElementNS:(_ns,name)=>new Element(name)}});
vm.runInContext(source.match(/function guides\([\s\S]*?(?=\nfunction illustration\()/)[0],context);
const guides = vm.runInContext('guides',context);
const imageData = filename => 'data:image/png;base64,'+fs.readFileSync(path.join(root,filename)).toString('base64');
const image = (filename,x,y,width,height,pixelated=false) => `<image href="${imageData(filename)}" x="${x}" y="${y}" width="${width}" height="${height}"${pixelated?' image-rendering="optimizeSpeed"':''}/>`;
const output=[];
output.push('<svg xmlns="http://www.w3.org/2000/svg" width="930" height="590" viewBox="0 0 930 590">');
output.push('<rect width="930" height="590" fill="#1c1b20"/>');
output.push('<text x="32" y="34" fill="#b9a5cc" font-family="sans-serif" font-size="11" letter-spacing="2">VESTIGE · PARCHMENT RITUAL DIAGRAMS</text>');
output.push('<text x="32" y="75" fill="#eee8da" font-family="Georgia,serif" font-size="29">Four inner. Four outer.</text>');
output.push('<text x="32" y="107" fill="#aaa59d" font-family="sans-serif" font-size="13">Just the paper and ritual sketch. Spell ingredients remain unknown.</text>');
const audit={kind:'offline SVG export of the live preview geometry',browserVerified:false,mockedViewerFrame:false,layers:[],assets:['parchment-v7.png','plinth-v7.png','spellstone-v7.png'],viewportScale:2};
for(const [index,type] of ['four','eight'].entries()) {
  const layout=layouts[type],recipe=recipes[type],drawingX=32+index*450,paperX=drawingX+52,paperY=221,zoom=2;
  output.push(`<text x="${drawingX}" y="160" fill="#eee8da" font-family="Georgia,serif" font-size="19">${escape(layout.name)}</text>`);
  output.push(`<text x="${drawingX}" y="190" fill="#aaa59d" font-family="sans-serif" font-size="12">${type==='four'?'Four inner plinths.':'Four inner plinths plus four outer plinths.'}</text>`);
  output.push(image('parchment-v7.png',paperX,paperY,300,300));
  let diagram;
  guides({append(value){diagram=value;}},layout);
  diagram.setAttribute('x',paperX);diagram.setAttribute('y',paperY);diagram.setAttribute('width',300);diagram.setAttribute('height',300);
  output.push(diagram.xml());
  const project=point=>[paperX+(point[0]-layout.crop[0])*150/layout.crop[2]*zoom,paperY+(point[1]-layout.crop[1])*150/layout.crop[3]*zoom];
  const center=project(layout.center);
  for(const point of layout.slots){const [x,y]=project(point);output.push(image('plinth-v7.png',x-34,y-34,68,68));}
  output.push(image('spellstone-v7.png',center[0]-33,center[1]-33,66,66));
  if(recipe.concealed)for(const point of layout.slots){const [x,y]=project(point);output.push(`<text x="${x}" y="${y+8}" text-anchor="middle" fill="#503722" font-family="monospace" font-weight="bold" font-size="22">?</text>`);}
  else for(const [seat,offering] of recipe.items.entries()){const [x,y]=project(layout.slots[seat]);output.push(image('icons/'+offering[0]+'.png',x-16,y-16,32,32,true));}
  output.push(image('icons/spell_scroll.png',center[0]-16,center[1]-16,32,32,true));
  for(const layer of diagram.children.filter(child=>child.dataset.layer)) {
    const seats=layout.layers[layer.dataset.layer==='inner'?0:1];
    const radius=Number(layer.dataset.radius)*zoom;
    const residuals=seats.map(seat=>{const point=project(layout.slots[seat]);return Math.abs(Math.hypot(point[0]-center[0],point[1]-center[1])-radius);});
    const maximum=Math.max(...residuals);
    if(maximum>1e-8)throw new Error('Circle/slot alignment drift');
    audit.layers.push({recipe:type,layer:layer.dataset.layer,slots:seats.length,radiusAtGameSize:radius/zoom,maxAlignmentError:maximum});
  }
}
output.push('<text x="32" y="566" fill="#aaa59d" font-family="sans-serif" font-size="11">Artwork export. JEI and EMI supply their own surrounding window and controls in-game.</text></svg>');
const svg=output.join('\n');
fs.writeFileSync(path.join(root,'paper-only-export-v9.svg'),svg);
fs.writeFileSync(path.join(root,'alignment-v9.json'),JSON.stringify(audit,null,2)+'\n');
const require=createRequire(import.meta.url);
const sharp=require(process.env.VESTIGE_SHARP_PATH??'sharp');
await sharp(Buffer.from(svg)).png().toFile(path.join(root,'paper-only-export-v9.png'));
process.stdout.write(JSON.stringify(audit)+'\n');
