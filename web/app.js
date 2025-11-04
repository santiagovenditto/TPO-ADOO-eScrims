// Frontend que consume la API Java en localhost:8080
const API = '/api'; // relative to server
const $ = id => document.getElementById(id);

const loginForm = $('loginForm');
const registerForm = $('registerForm');
const showRegister = $('showRegister');
const showLogin = $('showLogin');
const loginNotice = $('loginNotice');
const regNotice = $('regNotice');
const appArea = $('appArea');
const userLabel = $('userLabel');
const logoutBtn = $('logoutBtn');
const simOutput = document.createElement('pre');

simOutput.style.maxHeight = '300px'; simOutput.style.overflow = 'auto'; simOutput.style.background = 'rgba(0,0,0,0.12)'; simOutput.style.padding = '12px'; simOutput.style.borderRadius = '8px';
if(!document.querySelector('.app-area pre')) $('appArea').appendChild(simOutput);

function showForm(id){
  loginForm.classList.remove('active');
  registerForm.classList.remove('active');
  appArea.classList.add('hidden');
  if(id==='login') loginForm.classList.add('active');
  if(id==='register') registerForm.classList.add('active');
}

function setLogged(username, token){
  localStorage.setItem('session', JSON.stringify({username, token}));
  userLabel.textContent = username;
  loginForm.classList.remove('active');
  registerForm.classList.remove('active');
  appArea.classList.remove('hidden');
  // apply saved preferences (or defaults) to UI for this user
  try{ applyPrefsToUI(username); }catch(e){}
}

function applyPrefsToUI(username){
  const prefs = loadPrefsForUser(username) || {};
  const def = {push:true, email:false, discord:false, reminderHours:2};
  const merged = Object.assign({}, def, prefs);
  if(!localStorage.getItem('prefs_'+username)) savePrefsForUser(username, merged);
  const elPush = document.getElementById('prefPush'); if(elPush) elPush.checked = !!merged.push;
  const elEmail = document.getElementById('prefEmail'); if(elEmail) elEmail.checked = !!merged.email;
  const elDiscord = document.getElementById('prefDiscord'); if(elDiscord) elDiscord.checked = !!merged.discord;
  const elRem = document.getElementById('prefReminder'); if(elRem) elRem.value = (merged.reminderHours!=null?merged.reminderHours:2);
}

function getSession(){
  try{ return JSON.parse(localStorage.getItem('session')||'null'); }catch(e){ return null }
}

function logout(){
  const s = getSession();
  if(s && s.token){ fetch(API + '/logout', {method:'POST', body: JSON.stringify({token: s.token}), headers:{'Content-Type':'application/json'}}).catch(()=>{}); }
  localStorage.removeItem('session');
  showForm('login');
}

async function postJson(path, body){
  const res = await fetch(API + path, {method:'POST', body: JSON.stringify(body), headers:{'Content-Type':'application/json'}});
  return res.json();
}

// init: check session
(async ()=>{
  const s = getSession();
  if(s && s.token){
    try{
      const r = await fetch(API + '/session?token=' + encodeURIComponent(s.token));
      const j = await r.json();
      if (j.ok) { setLogged(s.username, s.token); return; }
    }catch(e){ /* ignore */ }
  }
  showForm('login');
})();

showRegister.addEventListener('click', ()=>{ loginNotice.textContent=''; regNotice.textContent=''; showForm('register'); });
showLogin.addEventListener('click', ()=>{ loginNotice.textContent=''; regNotice.textContent=''; showForm('login'); });

registerForm.addEventListener('submit', async e =>{
  e.preventDefault(); regNotice.textContent='';
  const user = $('regUser').value.trim();
  const email = $('regEmail').value.trim().toLowerCase();
  const p1 = $('regPassword').value;
  const p2 = $('regPassword2').value;
  if(p1!==p2){ regNotice.textContent='Las contraseñas no coinciden.'; return; }
  if(p1.length<6){ regNotice.textContent='La contraseña debe tener al menos 6 caracteres.'; return; }
  try{
    const j = await postJson('/register', {username: user, email, password: p1});
    if(j.ok){
      // auto-login: call login endpoint
      const login = await postJson('/login', {email, password: p1});
      if(login.ok){ setLogged(login.username, login.token); simOutput.textContent = '' } else { regNotice.textContent = login.message || 'Error al loguear'; }
    } else { regNotice.textContent = j.message || 'Error registro'; }
  }catch(err){ regNotice.textContent='Error de red'; }
});

loginForm.addEventListener('submit', async e =>{
  e.preventDefault(); loginNotice.textContent='';
  const email = $('loginEmail').value.trim().toLowerCase();
  const p = $('loginPassword').value;
  try{
    const j = await postJson('/login', {email, password: p});
    if(j.ok){ setLogged(j.username, j.token); simOutput.textContent = '' } else { loginNotice.textContent = j.message || 'Credenciales inválidas'; }
  }catch(err){ loginNotice.textContent='Error de red'; }
});

logoutBtn.addEventListener('click', ()=>{ logout(); });

// run simulation button in app area
const runBtn = document.createElement('button'); runBtn.className='btn ghost'; runBtn.textContent='Ejecutar simulación (server)';
runBtn.addEventListener('click', async ()=>{
  simOutput.textContent = 'Ejecutando...';
  const s = getSession();
  if(!s || !s.token){ simOutput.textContent = 'No estás autenticado.'; return; }
  try{
    const j = await postJson('/run', {token: s.token});
    if(j.ok){ simOutput.textContent = j.output || '(sin salida)'; } else { simOutput.textContent = j.message || 'Error'; }
  }catch(e){ simOutput.textContent = 'Error de red'; }
});
$('appArea').querySelector('.actions').appendChild(runBtn);

// small UX: focus first input on show
document.addEventListener('click', ()=>{ const f = document.querySelector('.form.active input'); if(f) f.focus(); });

// ------------------- Scrims (localStorage) -------------------
const SCRIM_KEY = 'scrims_v1';
const scrimListContainer = $('scrimListContainer');
const createScrimBtn = $('createScrimBtn');

function loadScrims(){
  try{ return JSON.parse(localStorage.getItem(SCRIM_KEY) || '[]'); }catch(e){ return []; }
}
function saveScrims(arr){ localStorage.setItem(SCRIM_KEY, JSON.stringify(arr)); }

async function refreshScrimsFromServer(){
  try{
    const r = await fetch(API + '/scrims');
    if(r.ok){ const j = await r.json(); if(Array.isArray(j)){ localStorage.setItem(SCRIM_KEY, JSON.stringify(j)); return j; } }
  }catch(e){ /* ignore */ }
  return loadScrims();
}

function newScrimObject(title, format, region, owner){
  return { id: 's_'+Date.now(), title, format, region, owner, state: 'Buscando', created: Date.now(),
    playersPerSide: 5, date: null, mode: 'Ranked-like', minMMR: null, maxMMR: null, latency: 100,
    participants: [], confirmations: {}, strategy: 'ByMMR', rolesRequired: [], waitlist: [], results: null };
}

function renderScrims(){
  const list = loadScrims();
  scrimListContainer.innerHTML = '';
  if(list.length===0){ scrimListContainer.innerHTML = '<div class="scrim-meta">No hay scrims. Crea el primero.</div>'; return; }
  list.forEach(s => {
    const el = document.createElement('div'); el.className='scrim-card';
    const left = document.createElement('div');
  const dateStr = s.date ? (new Date(s.date)).toLocaleString() : '';
    left.innerHTML = `<div style="font-weight:700">${escapeHtml(s.title)}</div><div class="scrim-meta">${s.format} • ${s.region} • creado por ${escapeHtml(s.owner)}</div><div class="scrim-details">${dateStr?'<div>📅 '+escapeHtml(dateStr)+'</div>':''}<div>👥 ${s.playersPerSide} /lado</div><div>⚙️ ${escapeHtml(s.mode)}</div><div>⏱ ${s.latency}ms</div><div>${s.minMMR||''}${s.maxMMR?(' - '+s.maxMMR):''}</div></div>`;
    // roles and strategy
    if(s.rolesRequired && s.rolesRequired.length>0){
      const rbox = document.createElement('div'); rbox.style.marginTop='8px';
      s.rolesRequired.forEach(r=>{ const rb=document.createElement('span'); rb.className='role-badge'; rb.textContent=r; rbox.appendChild(rb); });
      left.appendChild(rbox);
    }
    const strat = document.createElement('div'); strat.style.marginTop='8px'; strat.style.fontSize='12px'; strat.style.color='var(--muted)'; strat.textContent = 'Estrategia: ' + (s.strategy||'ByMMR'); left.appendChild(strat);
    const right = document.createElement('div'); right.style.display='flex'; right.style.alignItems='center'; right.style.gap='10px';
    const badge = document.createElement('div'); badge.className='badge ' + stateBadgeClass(s.state);
    badge.textContent = s.state;
    right.appendChild(badge);
    const actions = document.createElement('div'); actions.className='scrim-actions';
    // participant info and actions
    const sCapacity = (s.playersPerSide||5) * 2;
    const partInfo = document.createElement('div'); partInfo.style.fontSize='12px'; partInfo.style.color='var(--muted)';
    partInfo.textContent = `${s.participants.length}/${sCapacity} participantes`;
    left.appendChild(partInfo);

    // participant list with report links and strikes
    if(s.participants && s.participants.length>0){
      const ul = document.createElement('div'); ul.style.marginTop='6px'; ul.style.fontSize='13px';
      s.participants.forEach(p=>{
        const item = document.createElement('div'); item.style.display='flex'; item.style.alignItems='center'; item.style.gap='8px';
        const name = document.createElement('span'); name.textContent = p; item.appendChild(name);
  // strikes count
  const strikes = JSON.parse(localStorage.getItem('strikes_'+p)||'[]').length;
  if(strikes>0){ const sb = document.createElement('span'); sb.className='badge'; sb.textContent = '⚠️ '+strikes; sb.style.marginLeft='6px'; item.appendChild(sb); }
  // banned badge
  const banned = !!localStorage.getItem('banned_'+p);
  if(banned){ const bb = document.createElement('span'); bb.className='badge danger'; bb.textContent = 'BANEADO'; bb.style.marginLeft='6px'; item.appendChild(bb); }
        // report link
        const rpt = document.createElement('a'); rpt.href='#'; rpt.style.fontSize='12px'; rpt.textContent='Reportar'; rpt.addEventListener('click', (ev)=>{ ev.preventDefault(); const reason = prompt('Motivo del reporte para '+p+'?'); if(reason) { reportPlayer(p, reason, (getSession()||{}).username); } });
        item.appendChild(rpt);
        ul.appendChild(item);
      });
      left.appendChild(ul);
    }

    // actions depending on state
    const me = (getSession() && getSession().username) || 'anon';
    if(s.state==='Buscando'){
      // if not already participant, show Postular
      if(!s.participants.includes(me)){
        const b = createActionBtn('Postular', ()=>postular(s.id, me)); actions.appendChild(b);
      } else {
        const b = createActionBtn('Retirarme', ()=>unpostular(s.id, me), 'ghost'); actions.appendChild(b);
      }
    }
    if(s.state==='LobbyArmado'){
      // participants can confirm
      if(s.participants.includes(me) && !s.confirmations[me]){
        const b = createActionBtn('Confirmar asistencia', ()=>confirmar(s.id, me)); actions.appendChild(b);
      }
      // organizer quick confirm-all
      if(me===s.owner){ const b2 = createActionBtn('Forzar confirmar todo', ()=>forceConfirmAll(s.id)); actions.appendChild(b2); }
    }
    if(s.state==='Confirmado'){
      if(me===s.owner){ const b = createActionBtn('Iniciar', ()=>changeState(s.id,'EnJuego')); actions.appendChild(b); }
    }
    if(s.state==='EnJuego'){
      if(me===s.owner){ const b = createActionBtn('Finalizar', ()=>changeState(s.id,'Finalizado')); actions.appendChild(b); }
    }
    // cancel available for owner
    if(me===s.owner && s.state!=='Finalizado'){
      const c = createActionBtn('Cancelar', ()=>{ if(confirm('Cancelar scrim?')) changeState(s.id,'Cancelado'); }, 'ghost'); actions.appendChild(c);
    }
    // always allow delete
    const del = createActionBtn('Eliminar', ()=>{ if(confirm('Eliminar scrim?')) removeScrim(s.id); }, 'ghost');
    actions.appendChild(del);

  // if has waitlist, show small indicator
  if(s.waitlist && s.waitlist.length>0){ const wl=document.createElement('div'); wl.style.fontSize='12px'; wl.style.color='var(--muted)'; wl.textContent = 'Suplentes: '+s.waitlist.length; left.appendChild(wl); }

    right.appendChild(actions);

    el.appendChild(left);
    el.appendChild(right);
    scrimListContainer.appendChild(el);
  // schedule reminder for this scrim (if applicable)
  try{ scheduleRemindersForScrim(s); }catch(e){}
  });
}

function createActionBtn(text, handler, kind='primary'){
  const btn = document.createElement('button'); btn.className = 'btn ' + (kind==='ghost'?'ghost':'primary'); btn.textContent = text; btn.addEventListener('click', handler);
  return btn;
}

// Notifications
const NOTIF_KEY = 'escrim_notifs_v1';
function pushNotif(text){
  const arr = JSON.parse(localStorage.getItem(NOTIF_KEY)||'[]');
  arr.unshift({id:'n_'+Date.now(), text, time:Date.now()});
  localStorage.setItem(NOTIF_KEY, JSON.stringify(arr));
  renderNotifs();
}
function renderNotifs(){
  const list = JSON.parse(localStorage.getItem(NOTIF_KEY)||'[]');
  const el = document.getElementById('notifList'); if(!el) return; el.innerHTML='';
  list.slice(0,50).forEach(n=>{ const d=document.createElement('div'); d.className='notif-item'; d.textContent = new Date(n.time).toLocaleString() + ' — ' + n.text; el.appendChild(d); });
  const c = document.getElementById('notifCount'); if(c) c.textContent = list.length?('('+list.length+')'):'';
}

// Results / feedback
function showResultsForm(scrim){
  const area = document.getElementById('resultsArea'); if(!area) return;
  area.innerHTML = '';
  const f = document.createElement('div'); f.className='results-form';
  f.innerHTML = `<label>Ganador (equipo)</label><input id="resWinner" placeholder="Equipo A / Equipo B" />
    <label>Notas / MVP</label><textarea id="resNotes" rows="3"></textarea>
    <div class="submit-row"><button id="resSubmit" class="btn primary">Guardar</button><button id="resClear" class="btn ghost">Cancelar</button></div>`;
  area.appendChild(f);
  document.getElementById('resSubmit').addEventListener('click', ()=>{
    const winner = document.getElementById('resWinner').value.trim(); const notes = document.getElementById('resNotes').value.trim();
    const arr = loadScrims(); const s = arr.find(x=>x.id===scrim.id); if(!s) return;
    s.results = {winner, notes, recordedBy: (getSession()||{}).username || 'anon', time: Date.now()}; saveScrims(arr); renderScrims(); pushNotif('Resultados cargados para '+s.title);
    area.innerHTML = 'Resultados guardados.';
  });
  document.getElementById('resClear').addEventListener('click', ()=>{ area.innerHTML = 'Cancelado.'; });
}

// render notifs on load
document.addEventListener('DOMContentLoaded', ()=>{ renderNotifs(); });

// Preferences per user
function savePrefsForUser(username, prefs){
  const key = 'prefs_'+username;
  localStorage.setItem(key, JSON.stringify(prefs));
}
function loadPrefsForUser(username){
  const key = 'prefs_'+username; try{ return JSON.parse(localStorage.getItem(key)||'{}'); }catch(e){ return {}; }
}

document.getElementById('savePrefs')?.addEventListener('click', ()=>{
  const s = getSession(); if(!s||!s.username) return alert('Inicia sesión');
  const prefs = {push: !!document.getElementById('prefPush').checked, email: !!document.getElementById('prefEmail').checked, discord: !!document.getElementById('prefDiscord').checked, reminderHours: parseInt(document.getElementById('prefReminder').value)||0};
  savePrefsForUser(s.username, prefs); alert('Preferencias guardadas');
});

// simulate channel dispatch
function dispatchToChannels(username, text){
  const prefs = loadPrefsForUser(username||'anon');
  if(prefs.push) pushNotif('[PUSH] '+text);
  if(prefs.email) pushNotif('[EMAIL] '+text);
  if(prefs.discord) pushNotif('[DISCORD] '+text);
}

// schedule reminders when scrim is created or updated
function scheduleRemindersForScrim(scrim){
  // clear existing timers (we won't persist timers across reloads in this demo)
  // for demo: when a scrim is Confirmado we create a timeout to push reminder at scrim.date - reminderHours
  try{
    if(!scrim.date) return;
    const when = new Date(scrim.date).getTime(); if(isNaN(when)) return;
    const prefsKeyPrefix = 'prefs_';
    const remHours = (scrim.reminderHours!=null)?scrim.reminderHours:2; // default
    const remindAt = when - (remHours*3600*1000);
    const delay = remindAt - Date.now();
    if(delay<=0) return; // past
    setTimeout(()=>{
      // send reminder to all participants according to their prefs
      (scrim.participants||[]).forEach(p=> dispatchToChannels(p, 'Recordatorio: tu scrim "'+scrim.title+'" comienza en '+remHours+'h'));
      pushNotif('Recordatorios enviados para '+scrim.title);
    }, Math.max(1000, delay));
  }catch(e){console.error(e)}
}

// moderation: report player -> strikes
async function reportPlayer(player, reason, reporter){
  const r = { id: 'r_'+Date.now(), reported: player, reason: reason||'', by: reporter||'anon', time: Date.now() };
  try{
    await fetch(API + '/report', {method:'POST', body: JSON.stringify(r), headers:{'Content-Type':'application/json'}});
    pushNotif('Reporte enviado al servidor para '+player);
    // refresh UI (server may have applied ban)
    await refreshScrimsFromServer(); renderScrims();
    return;
  }catch(e){ /* fallback to local */ }
  const key = 'strikes_'+player; const arr = JSON.parse(localStorage.getItem(key)||'[]');
  arr.push({by: reporter||'anon', reason: reason||'', time:Date.now()});
  localStorage.setItem(key, JSON.stringify(arr));
  pushNotif('Jugador '+player+' reportado (local)');
}

// small UI: add report button when rendering scrim participants (we'll add as text link)
// update renderScrims to show participant list with report links


function stateBadgeClass(state){
  if(state==='Buscando') return 'state-buscando';
  if(state==='LobbyArmado') return 'state-lobby';
  if(state==='Confirmado') return 'state-confirmado';
  if(state==='EnJuego') return 'state-confirmado';
  return 'state-lobby';
}

function changeState(id, to){
  const arr = loadScrims();
  const s = arr.find(x=>x.id===id); if(!s) return;
  s.state = to; saveScrims(arr); renderScrims();
}

function postular(id, user){
  const arr = loadScrims(); const s = arr.find(x=>x.id===id); if(!s) return;
  // disallow banned users
  if(!!localStorage.getItem('banned_'+user)) return alert('Estás baneado y no puedes postular');
  const cap = (s.playersPerSide||5)*2;
  if(s.participants.includes(user)) return;
  if(s.participants.length>=cap) return alert('Cupo completo');
  s.participants.push(user); saveScrims(arr);
  // if reached capacity -> move to LobbyArmado
  if(s.participants.length>=cap){ s.state='LobbyArmado'; }
  saveScrims(arr); renderScrims();
}

function unpostular(id, user){
  const arr = loadScrims(); const s = arr.find(x=>x.id===id); if(!s) return;
  s.participants = s.participants.filter(p=>p!==user); delete s.confirmations[user];
  // if removed, revert to Buscando
  s.state='Buscando'; saveScrims(arr); renderScrims();
}

function confirmar(id, user){
  const arr = loadScrims(); const s = arr.find(x=>x.id===id); if(!s) return;
  if(!s.participants.includes(user)) return;
  s.confirmations[user] = true;
  // check if all confirmed
  const all = s.participants.length>0 && s.participants.every(p=>s.confirmations[p]);
  if(all) s.state='Confirmado'; saveScrims(arr); renderScrims();
}

function forceConfirmAll(id){
  const arr = loadScrims(); const s = arr.find(x=>x.id===id); if(!s) return;
  s.participants.forEach(p=> s.confirmations[p]=true);
  s.state='Confirmado'; saveScrims(arr); renderScrims();
}

// scheduler: check scrims periodically to auto-transition Confirmado -> EnJuego when date reached
setInterval(()=>{
  const now = Date.now(); const arr = loadScrims(); let changed=false;
  arr.forEach(s=>{
    if(s.state==='Confirmado' && s.date){
      const t = new Date(s.date).getTime(); if(!isNaN(t) && t<=now){ s.state='EnJuego'; changed=true; }
    }
  });
  if(changed) { saveScrims(arr); renderScrims(); }
}, 5000);

async function removeScrim(id){
  try{
    await fetch(API + '/scrims/delete', {method:'POST', body: JSON.stringify({id}), headers:{'Content-Type':'application/json'}});
    await refreshScrimsFromServer(); renderScrims(); return;
  }catch(e){ /* fallback */ }
  const arr = loadScrims(); saveScrims(arr.filter(x=>x.id!==id)); renderScrims();
}

function escapeHtml(s){ return (s+'').replace(/[&<>"']/g, c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c])); }

createScrimBtn.addEventListener('click', async ()=>{
  const title = $('scrimTitle').value.trim() || 'Scrim';
  const format = $('scrimFormat').value;
  const region = $('scrimRegion').value.trim() || 'Any';
  const players = parseInt($('scrimPlayers').value) || 5;
  const date = $('scrimDate').value || null;
  const mode = $('scrimMode').value || 'Ranked-like';
  const minMMR = $('scrimMinMMR').value ? parseInt($('scrimMinMMR').value) : null;
  const maxMMR = $('scrimMaxMMR').value ? parseInt($('scrimMaxMMR').value) : null;
  const latency = $('scrimLatency').value ? parseInt($('scrimLatency').value) : 100;
  const s = getSession();
  const owner = s && s.username ? s.username : 'anon';
  const obj = newScrimObject(title, format, region, owner);
  obj.playersPerSide = players; obj.date = date; obj.mode = mode; obj.minMMR = minMMR; obj.maxMMR = maxMMR; obj.latency = latency;
  // copy reminder hours from preferences input (per-scrim override)
  const rh = parseInt(document.getElementById('prefReminder')?.value || '0'); if(rh>0) obj.reminderHours = rh;
  try{ await fetch(API + '/scrims', {method:'POST', body: JSON.stringify(obj), headers:{'Content-Type':'application/json'}}); }catch(e){}
  await refreshScrimsFromServer(); renderScrims();
  // clear fields
  $('scrimTitle').value=''; $('scrimRegion').value='';
});

// initial render when app area shown
document.addEventListener('DOMContentLoaded', async ()=>{ await refreshScrimsFromServer(); renderScrims(); });

