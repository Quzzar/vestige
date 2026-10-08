import fs from 'node:fs/promises';
import path from 'node:path';
import {createRequire} from 'node:module';
import {fileURLToPath} from 'node:url';

const root=path.dirname(fileURLToPath(import.meta.url));
const require=createRequire(import.meta.url);
const {chromium}=require(process.env.VESTIGE_PLAYWRIGHT_PATH??'playwright');
const browser=await chromium.launch({headless:true,...(process.env.VESTIGE_BROWSER_PATH?{executablePath:process.env.VESTIGE_BROWSER_PATH}:{})});
const audit={kind:'browser-only fitted illustration-rim study',nativeVerified:false,placeholderOutputSprite:'minecraft:string',captures:[],materials:[]};
try{
 for(const [name,width,height] of [['desktop',1100,680],['narrow',390,1050]]){
  const page=await browser.newPage({viewport:{width,height},deviceScaleFactor:1});
  const errors=[];page.on('pageerror',e=>errors.push(e.message));
  await page.goto('file://'+path.join(root,'index.html'));
  await page.waitForFunction(()=>window.previewReady||window.previewError);
  const failure=await page.evaluate(()=>window.previewError);if(failure)throw new Error(failure);
  for(const mode of ['normal','reduced']){
   await page.locator('#'+mode).click();
   await page.locator('#thread').selectOption({label:'Smoldering Thread'});
   await page.screenshot({path:path.join(root,`${name}-${mode}.png`),fullPage:true});
   const geometry=await page.evaluate(()=>({viewport:innerWidth,scrollWidth:document.documentElement.scrollWidth,sockets:[...document.querySelectorAll('.socket')].map(e=>{
    const r=e.getBoundingClientRect();const surface=e.closest('.surface').getBoundingClientRect();const offering=e.parentElement.querySelector('.item:not(.socket)').getBoundingClientRect();
    const offeringOwnsCenter=document.elementFromPoint((offering.left+offering.right)/2,(offering.top+offering.bottom)/2)===e.parentElement.querySelector('.item:not(.socket)');
    return {variant:e.closest('.study').dataset.preview,width:r.width,height:r.height,insideDiagram:r.left>=surface.left&&r.right<=surface.right&&r.top>=surface.top&&r.bottom<=surface.bottom,offeringOwnsCenter};
   })}));
   if(geometry.scrollWidth>geometry.viewport||geometry.sockets.some(s=>!s.insideDiagram||!s.offeringOwnsCenter))throw new Error('Preview geometry does not fit: '+JSON.stringify(geometry));
   audit.captures.push({name,mode,filename:`${name}-${mode}.png`,...geometry});
  }
  if(name==='desktop'){
   await page.locator('#normal').click();await page.locator('#thread').selectOption({label:'Laced Thread'});
   const bounds=await page.locator('.material-frame').boundingBox();await page.mouse.move(bounds.x+bounds.width*627/1254,bounds.y+bounds.height*315/1254);
   if(!await page.locator('.frame-tooltip').isVisible())throw new Error('Material frame hover is not visible');
   await page.screenshot({path:path.join(root,'desktop-hover.png'),fullPage:true});
   await page.locator('[data-preview="frame"] .item:not(.socket)').first().hover();
   if(await page.locator('.frame-tooltip').isVisible()||!await page.locator('[data-preview="frame"] .item:not(.socket) .tooltip').first().isVisible())throw new Error('Offering hover must remain distinct from frame hover');
   await page.mouse.move(0,0);
   await page.locator('#reduced').focus();await page.keyboard.press('Tab');
   if(!await page.evaluate(()=>document.activeElement.matches('.material-frame:focus-visible'))||!await page.locator('.frame-tooltip').isVisible())throw new Error('Material frame must be reachable with visible keyboard focus');
   await page.screenshot({path:path.join(root,'desktop-focus.png'),fullPage:true});
   audit.keyboardFrameFocus=true;await page.locator('#thread').focus();
   for(const label of await page.locator('#thread option').allTextContents()){
    await page.locator('#thread').selectOption({label});
    const materials=await page.locator('.diagram').evaluateAll(es=>es.map(e=>e.dataset.material));
    await page.locator('[data-preview="frame"]').screenshot({path:path.join(root,`frame-${materials[0]}.png`)});
    await page.locator('[data-preview="frame"] .zoom-study').evaluate(e=>e.open=true);
    await page.locator('[data-preview="frame"] .detail').screenshot({path:path.join(root,`rim-detail-${materials[0]}.png`)});
    await page.locator('[data-preview="frame"] .zoom-study').evaluate(e=>e.open=false);
    audit.materials.push({thread:label,materials});
   }
  }
  if(errors.length)throw new Error(errors.join('\n'));await page.close();
 }
 await fs.writeFile(path.join(root,'inspection.json'),JSON.stringify(audit,null,2)+'\n');
 console.log(JSON.stringify(audit));
}finally{await browser.close()}
