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
const topLogoutBtn = $('topLogoutBtn');
const avatarImg = $('avatarImg');
const topUserLabel = $('topUserLabel');
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
  if(topLogoutBtn) topLogoutBtn.style.display = 'inline-block';
  if(avatarImg){
    // use DiceBear bottts for gamer-ish avatars
    avatarImg.src = 'https://api.dicebear.com/7.x/bottts/svg?seed='+encodeURIComponent(username);
    avatarImg.style.display = 'inline-block';
  }
  if(topUserLabel) { topUserLabel.textContent = username; topUserLabel.style.display='inline-block'; }
  // ensure the top controls container is visible
  // (we use the 'hidden' class to toggle visibility so CSS controls layout)
  const topControls = document.getElementById('topControls'); if(topControls) topControls.classList.remove('hidden');
  // also replace main avatar with a themed avatar based on user
  const main = document.getElementById('mainAvatar'); if(main) main.src = 'https://api.dicebear.com/7.x/adventurer/svg?seed='+encodeURIComponent(username);
  // apply saved preferences (or defaults) to UI for this user
  try{ applyPrefsToUI(username); }catch(e){}
}

function applyPrefsToUI(username){
  // preferences removed — no-op
}

function getSession(){
  try{ return JSON.parse(localStorage.getItem('session')||'null'); }catch(e){ return null }
}

function logout(){
  const s = getSession();
  if(s && s.token){ fetch(API + '/logout', {method:'POST', body: JSON.stringify({token: s.token}), headers:{'Content-Type':'application/json'}}).catch(()=>{}); }
  localStorage.removeItem('session');
  // Clear UI session and show login form. Also ensure SPA navigates to root so any internal state resets.
  // show login form in SPA
  showForm('login');
  // hide top controls and avatar by adding hidden class
  const topControls = document.getElementById('topControls'); if(topControls) topControls.classList.add('hidden');
  if(avatarImg) { avatarImg.style.display='none'; avatarImg.src=''; }
  const main = document.getElementById('mainAvatar'); if(main) main.src = 'https://api.dicebear.com/7.x/bottts/svg?seed=eScrims';
  if(topUserLabel) { topUserLabel.style.display='none'; topUserLabel.textContent=''; }
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

if(logoutBtn) logoutBtn.addEventListener('click', ()=>{ logout(); });
if(topLogoutBtn) topLogoutBtn.addEventListener('click', ()=>{ logout(); });

// small UX: focus first input on show
document.addEventListener('click', ()=>{ const f = document.querySelector('.form.active input'); if(f) f.focus(); });

// ------------------- Scrims (localStorage) -------------------
const SCRIM_KEY = 'scrims_v1';
const scrimListContainer = $('scrimListContainer');
const createScrimBtn = $('createScrimBtn');

function loadScrims(){
  try{ return JSON.parse(localStorage.getItem(SCRIM_KEY) || '[]'); }catch(e){ return []; }
}
function saveScrims(arr){ localStorage.setItem(SCRIM_KEY, JSON.stringify(arr)); try{ console.debug('[app] saveScrims -> count=', Array.isArray(arr)?arr.length:0, 'sampleIds=', Array.isArray(arr)?arr.slice(0,5).map(x=>x.id):[]); }catch(e){} }

async function refreshScrimsFromServer(){
  try{
    console.debug('[app] refreshScrimsFromServer -> fetching', API + '/scrims');
    const r = await fetch(API + '/scrims');
    if(r.ok){ const j = await r.json(); if(Array.isArray(j)){ try{ console.debug('[app] refreshScrimsFromServer -> fetched count=', j.length, 'sampleIds=', j.slice(0,6).map(x=>x.id)); }catch(e){}
        // merge optimistic items persisted with prefix optimistic_
        try{
          const merged = Array.isArray(j)? j.slice() : [];
          for(let i=0;i<localStorage.length;i++){
            const k = localStorage.key(i);
            if(k && k.startsWith && k.startsWith('optimistic_')){
              try{ const o = JSON.parse(localStorage.getItem(k)||'null'); if(o && o.id && !merged.some(x=>x && x.id===o.id)) merged.unshift(o); }catch(e){}
            }
          }
          localStorage.setItem(SCRIM_KEY, JSON.stringify(merged));
          try{ console.debug('[app] refreshScrimsFromServer -> merged optimistic count=', merged.length); }catch(e){}
          return merged;
        }catch(e){ localStorage.setItem(SCRIM_KEY, JSON.stringify(j)); return j; }
      } }
  }catch(e){ console.debug('[app] refreshScrimsFromServer -> fetch error', e); }
  return loadScrims();
}


// Sync a scrim object to server: prefer update endpoint, fallback to delete+post if not available.
async function syncScrimToServer(scrim){
  if(!scrim || !scrim.id) return;
  try{
    // prefer the update endpoint
    const up = await fetch(API + '/scrims/update', {method:'POST', body: JSON.stringify(scrim), headers:{'Content-Type':'application/json'}}).catch(()=>null);
    if (!up || !up.ok){
      // fallback: delete + post
      await fetch(API + '/scrims/delete', {method:'POST', body: JSON.stringify({id: scrim.id}), headers:{'Content-Type':'application/json'}}).catch(()=>{});
      await fetch(API + '/scrims', {method:'POST', body: JSON.stringify(scrim), headers:{'Content-Type':'application/json'}}).catch(()=>{});
    }
  }catch(e){ /* ignore network errors for now */ }
}

function newScrimObject(title, format, region, owner){
  return { id: 's_'+Date.now(), title, format, region, owner, state: 'Buscando', created: Date.now(),
    playersPerSide: 5, date: null, mode: 'Ranked-like', minMMR: null, maxMMR: null, latency: 100,
    participants: [], confirmations: {}, strategy: 'ByMMR', rolesRequired: [], waitlist: [], results: null };
}

function renderScrims(){
  const list = loadScrims();
  try{ console.debug('[app] renderScrims -> count=', Array.isArray(list)?list.length:0, 'ids=', Array.isArray(list)?list.slice(0,6).map(x=>x.id):[]); }catch(e){}
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
    // show reminder badge if configured for this scrim
    if (s.reminderHours && Number(s.reminderHours)>0) {
      const rBadge = document.createElement('div'); rBadge.className = 'badge reminder-badge'; rBadge.textContent = s.reminderHours + 'h'; right.appendChild(rBadge);
    }
    const actions = document.createElement('div'); actions.className='scrim-actions';
    // helper to create more menu
    function createMoreMenu(extraBtns){
      const wrap = document.createElement('div'); wrap.style.position='relative';
      const more = document.createElement('button'); more.className='btn ghost'; more.textContent='⋯'; more.title='Más acciones'; more.style.padding='8px 10px';
      const menu = document.createElement('div'); menu.style.position='absolute'; menu.style.right='0'; menu.style.top='36px'; menu.style.background='var(--card)'; menu.style.border='1px solid rgba(255,255,255,0.03)'; menu.style.padding='8px'; menu.style.borderRadius='8px'; menu.style.display='none'; menu.style.zIndex='50'; menu.style.minWidth='160px';
      extraBtns.forEach(b=>{ const r = document.createElement('div'); r.style.marginBottom='6px'; const cb = b.cloneNode(true); cb.style.display='block'; cb.style.width='100%'; r.appendChild(cb); menu.appendChild(r); });
      more.addEventListener('click', (ev)=>{ ev.stopPropagation(); menu.style.display = menu.style.display==='none' ? 'block' : 'none'; });
      document.addEventListener('click', ()=>{ menu.style.display='none'; });
      wrap.appendChild(more); wrap.appendChild(menu); return wrap;
    }
    // participant info and actions
    const sCapacity = (s.playersPerSide||5) * 2;
    const partInfo = document.createElement('div'); partInfo.style.fontSize='12px'; partInfo.style.color='var(--muted)';
    partInfo.textContent = `${s.participants.length}/${sCapacity} participantes`;
    left.appendChild(partInfo);

    // participant list with report links and strikes
    if(s.participants && s.participants.length>0){
      const ul = document.createElement('div'); ul.style.marginTop='6px'; ul.style.fontSize='13px';
      // ensure we have a per-scrim mapping for display names (so names "van con la partida")
      const arrAll = loadScrims();
      const localScrim = (arrAll||[]).find(x=>x && x.id===s.id) || s;
      localScrim.participantNames = localScrim.participantNames || {};
      // compute next Usuario index based on existing mappings
      let nextUserIndex = 1;
      try{
        Object.values(localScrim.participantNames).forEach(v=>{ const m = (v||'').toString().match(/^Usuario\s+(\d+)$/); if(m) nextUserIndex = Math.max(nextUserIndex, parseInt(m[1]) + 1); });
      }catch(e){}

      s.participants.forEach(p=>{
        const item = document.createElement('div'); item.style.display='flex'; item.style.alignItems='center'; item.style.gap='8px';
        const name = document.createElement('span');
        // If this is a bot id, assign or reuse a friendly display name "Usuario N"
        if(p && p.startsWith && p.startsWith('bot_')){
          name.className='bot-name';
          let display = localScrim.participantNames[p];
          if(!display){ display = 'Jugador ' + (nextUserIndex++); localScrim.participantNames[p] = display; }
          name.textContent = display;
          // attach small hover metadata if available
          let meta = null; try{ meta = JSON.parse(localStorage.getItem('bot_meta_'+p) || 'null'); }catch(e){ meta = null; }
          if(meta){ name.title = 'Role: '+(meta.role||'n/a')+' • Ranking: '+(meta.mmr||'n/a')+' • '+(meta.latency||'?')+'ms'; }
          item.appendChild(name);
          // append ranking display if available
          const rankingVal = getParticipantRanking(localScrim, p) || (meta && meta.mmr);
          if(rankingVal){ const rs = document.createElement('span'); rs.className='bot-mmr'; rs.textContent = ' • '+rankingVal; rs.style.marginLeft='8px'; rs.style.fontSize='12px'; rs.style.opacity='0.9'; item.appendChild(rs); }
        } else {
          name.textContent = p;
          item.appendChild(name);
          // show ranking for real users if we have it cached locally
          const userRank = getParticipantRanking(localScrim, p);
          if(userRank){ const rs = document.createElement('span'); rs.className='bot-mmr'; rs.textContent = ' • '+userRank; rs.style.marginLeft='8px'; rs.style.fontSize='12px'; rs.style.opacity='0.9'; item.appendChild(rs); }
        }
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
      // persist updated mapping into local scrim store so it "va con la partida"
      try{ if(localScrim && localScrim.id){ const all = loadScrims(); const target = all.find(x=>x && x.id===localScrim.id); if(target){ target.participantNames = localScrim.participantNames; saveScrims(all); } }
      }catch(e){}
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
  // owner helper: simulate filling the scrim to reach LobbyArmado
  if(me===s.owner){ const sim = createActionBtn('Simular llenar', ()=>simulateFill(s.id)); actions.appendChild(sim); }
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
    // add an advance-state simulation button for quick testing (visible to everyone)
    {
      const adv = createActionBtn('Avanzar estado', ()=>advanceStateSimulation(s.id)); adv.className = 'btn ghost'; actions.appendChild(adv);
    }
  // owner quick controls to simulate start and finish
  // removed per UX request: do not show Simular inicio / Simular fin buttons to owner
    // cancel available for owner
    if(me===s.owner && s.state!=='Finalizado'){
      const c = createActionBtn('Cancelar', ()=>{ if(confirm('Cancelar scrim?')) changeState(s.id,'Cancelado'); }, 'ghost'); actions.appendChild(c);
    }
  // owner helper: remove simulated bots
  // button hidden per UX request
    // always allow delete
    const del = createActionBtn('Eliminar', ()=>{ if(confirm('Eliminar scrim?')) removeScrim(s.id); }, 'ghost');
    actions.appendChild(del);

  // if has waitlist, show small indicator
  if(s.waitlist && s.waitlist.length>0){ const wl=document.createElement('div'); wl.style.fontSize='12px'; wl.style.color='var(--muted)'; wl.textContent = 'Suplentes: '+s.waitlist.length; left.appendChild(wl); }
    // if finalized, show brief results summary and a view button
    if(s.state==='Finalizado' && s.results){
      const mvpFriendly = getParticipantDisplayName(s, s.results.mvp);
  const mvpRankSummary = getParticipantRanking(s, s.results.mvp) || '';
  const sum = document.createElement('div'); sum.style.marginTop='8px'; sum.style.fontSize='13px'; sum.style.fontWeight='600'; sum.textContent = 'Resultado: ' + (s.results.winner || '') + (s.results.mvp?(' • MVP: '+mvpFriendly + (mvpRankSummary?(' • '+mvpRankSummary):'')):'');
      left.appendChild(sum);
      const view = createActionBtn('Ver resultados', ()=> showResultsDetail(s),'ghost'); view.style.marginTop='6px'; left.appendChild(view);
    }

    right.appendChild(actions);

    el.appendChild(left);
    el.appendChild(right);
    scrimListContainer.appendChild(el);
  // schedule reminder for this scrim (if applicable)
  try{ scheduleRemindersForScrim(s); }catch(e){}
  });
}

function showResultsDetail(s){
  const area = document.getElementById('resultsArea'); if(!area) return;
  if(!s || !s.results){ area.innerHTML = 'No hay resultados para esta partida.'; return; }
  const r = s.results;
    const mvpDisplay = getParticipantDisplayName(s, r.mvp || '');
    const mvpRank = getParticipantRanking(s, r.mvp) || '';
    area.innerHTML = `<div style="font-weight:700">Resultados — ${escapeHtml(s.title||s.id)}</div>
      <div style="margin-top:8px">Ganador: <b>${escapeHtml(r.winner||'')}</b></div>
      <div>MVP: ${escapeHtml(mvpDisplay||'')}${mvpRank?(' • '+escapeHtml(mvpRank)) : ''}</div>
      <div>Kills/Assists: ${escapeHtml(r.kills||'')}</div>
    <div>Ranking: ${escapeHtml(r.rating||'')}</div>`;
}

function createActionBtn(text, handler, kind='primary'){
  // Defensive: prevent older/cached render paths from showing the removed 'Limpiar bots' button
  if(text === 'Limpiar bots'){
    const hidden = document.createElement('button'); hidden.className = 'btn ghost'; hidden.style.display = 'none'; hidden.addEventListener('click', handler);
    return hidden;
  }
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
    <label>MVP</label><input id="resMVP" placeholder="Jugador destacado" />
    <label>Kills / Assists</label><input id="resKills" placeholder="ej. 12/8" />
  <label>Ranking (1-5)</label><select id="resRating"><option value="">(sin ranking)</option><option>1</option><option>2</option><option>3</option><option>4</option><option>5</option></select>
    <label>Comentarios</label><textarea id="resNotes" rows="3"></textarea>
    <div class="submit-row"><button id="resSubmit" class="btn primary">Guardar</button><button id="resClear" class="btn ghost">Cancelar</button></div>`;
  area.appendChild(f);
  document.getElementById('resSubmit').addEventListener('click', ()=>{
    const winner = document.getElementById('resWinner').value.trim(); const notes = document.getElementById('resNotes').value.trim();
    const mvp = document.getElementById('resMVP').value.trim(); const kills = document.getElementById('resKills').value.trim(); const rating = document.getElementById('resRating').value;
    const arr = loadScrims(); const s = arr.find(x=>x.id===scrim.id); if(!s) return;
    s.results = {winner, notes, mvp, kills, rating, recordedBy: (getSession()||{}).username || 'anon', time: Date.now()}; saveScrims(arr); renderScrims(); pushNotif('Resultados cargados para '+s.title);
  // moderation removed: feedback stored directly (no moderation queue)
    area.innerHTML = 'Resultados guardados.';
  });
  document.getElementById('resClear').addEventListener('click', ()=>{ area.innerHTML = 'Cancelado.'; });
}

// moderation UI
// Moderation removed (no moderation queue)

// render notifs on load
document.addEventListener('DOMContentLoaded', ()=>{ renderNotifs(); });

// Preferences per user
function savePrefsForUser(username, prefs){
  // preferences removed — noop
}
function loadPrefsForUser(username){
  return {};
}
// preferences UI removed — no handlers

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
    pushNotif('Reporte enviado al servidor para '+getFallbackDisplayName(player));
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
  s.state = to;
  if(to==='Finalizado' && !s.results){
    s.results = generateResultsForScrim(s);
  }
  saveScrims(arr); renderScrims();
  try{ syncScrimToServer(s).catch(()=>{}); }catch(e){}
  try{ notifyStateChange(id, to); }catch(e){}
}

// Advance the scrim state in a predefined flow for simulation/testing
function advanceStateSimulation(id){
  const arr = loadScrims(); const s = arr.find(x=>x.id===id); if(!s) return;
  const flow = ['Buscando','LobbyArmado','Confirmado','EnJuego','Finalizado'];
  const idx = flow.indexOf(s.state);
  const next = (idx===-1 || idx===flow.length-1) ? flow[0] : flow[idx+1];
  // if moving to LobbyArmado and capacity not full, fill with bots
  if(next==='LobbyArmado'){
    // fill with bots until capacity
    const cap = (s.playersPerSide||5)*2;
    let i=0; while(s.participants.length<cap){ const botId = 'bot_'+Date.now()+'_'+(i++); s.participants.push(botId); try{ localStorage.setItem('bot_meta_'+botId, JSON.stringify({role:'R', mmr:1000, latency:80})); }catch(e){} }
  }
  // if moving to Confirmado, mark confirmations for all participants
  if(next==='Confirmado'){
    s.participants.forEach(p=> s.confirmations[p]=true);
    // set scrim date to now+5s to test auto-start
    if(!s.date) s.date = new Date(Date.now()+5000).toISOString();
  }
  s.state = next; saveScrims(arr); renderScrims(); try{ syncScrimToServer(s).catch(()=>{}); }catch(e){}
  try{ notifyStateChange(id, next); }catch(e){}
}

// small helper to animate badge and push a notification
function notifyStateChange(id, newState){
  // try to include scrim title for better readability
  try{
    const arr = loadScrims(); const s = arr.find(x=>x.id===id);
    if(s && s.title){ pushNotif('Scrim "'+s.title+'" cambió a '+newState); }
    else { pushNotif('Scrim '+id+' cambió a '+newState); }
  }catch(e){ pushNotif('Scrim '+id+' cambió a '+newState); }
  // attempt to add pulse class to the visible badge for this scrim in DOM
  try{
    const cards = document.querySelectorAll('.scrim-card');
    cards.forEach(c=>{
      const b = c.querySelector('.badge');
      if(!b) return;
      // find matching card by title/id heuristic: check text inside
      if(c.innerText.includes(id) || c.innerText.includes(newState) || c.querySelector('div')?.innerText?.includes(id)){
        b.classList.add('pulse'); setTimeout(()=>b.classList.remove('pulse'),900);
      }
    });
  }catch(e){}
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
  try{ syncScrimToServer(s).catch(()=>{}); }catch(e){}
}

function unpostular(id, user){
  const arr = loadScrims(); const s = arr.find(x=>x.id===id); if(!s) return;
  s.participants = s.participants.filter(p=>p!==user); delete s.confirmations[user];
  // if removed, revert to Buscando
  s.state='Buscando'; saveScrims(arr); renderScrims();
  try{ syncScrimToServer(s).catch(()=>{}); }catch(e){}
}

function confirmar(id, user){
  const arr = loadScrims(); const s = arr.find(x=>x.id===id); if(!s) return;
  if(!s.participants.includes(user)) return;
  s.confirmations[user] = true;
  // check if all confirmed
  const all = s.participants.length>0 && s.participants.every(p=>s.confirmations[p]);
  if(all) s.state='Confirmado'; saveScrims(arr); renderScrims();
  try{ syncScrimToServer(s).catch(()=>{}); }catch(e){}
}

function forceConfirmAll(id){
  const arr = loadScrims(); const s = arr.find(x=>x.id===id); if(!s) return;
  s.participants.forEach(p=> s.confirmations[p]=true);
  s.state='Confirmado'; saveScrims(arr); renderScrims();
  try{ syncScrimToServer(s).catch(()=>{}); }catch(e){}
}

// Helpers for testing/owner convenience
function simulateFill(id){
  const arr = loadScrims(); const s = arr.find(x=>x.id===id); if(!s) return;
  const cap = (s.playersPerSide||5)*2;
  // add fake participants until capacity (use unique names)
  let i=0; while(s.participants.length<cap){ const botId = 'bot_'+Date.now()+'_'+(i++); s.participants.push(botId);
    // store small metadata for better UX
    const meta = { role: ['Top','Jg','Mid','Bot','Sup'][Math.floor(Math.random()*5)], mmr: Math.floor(800 + Math.random()*1200), latency: Math.floor(30+Math.random()*170) };
    try{ localStorage.setItem('bot_meta_'+botId, JSON.stringify(meta)); }catch(e){}
  }
  s.state = 'LobbyArmado'; saveScrims(arr); renderScrims();
  try{ syncScrimToServer(s).catch(()=>{}); }catch(e){}
}

function simulateFillAndConfirm(id){
  const arr = loadScrims(); const s = arr.find(x=>x.id===id); if(!s) return;
  simulateFill(id);
  s.participants.forEach(p=> s.confirmations[p]=true);
  s.state = 'Confirmado'; saveScrims(arr); renderScrims();
  try{ syncScrimToServer(s).catch(()=>{}); }catch(e){}
}

// owner helpers to simulate game start/finish
function simulateStart(id){
  const arr = loadScrims(); const s = arr.find(x=>x.id===id); if(!s) return;
  s.state = 'EnJuego'; saveScrims(arr); renderScrims();
  try{ syncScrimToServer(s).catch(()=>{}); }catch(e){}
}

function simulateFinish(id){
  const arr = loadScrims(); const s = arr.find(x=>x.id===id); if(!s) return;
  s.state = 'Finalizado';
  if(!s.results) s.results = generateResultsForScrim(s);
  saveScrims(arr); renderScrims();
  try{ syncScrimToServer(s).catch(()=>{}); }catch(e){}
}

function removeBots(id){
  const arr = loadScrims(); const s = arr.find(x=>x.id===id); if(!s) return;
  s.participants = s.participants.filter(p=> !(p && p.startsWith && p.startsWith('bot_')));
  // remove corresponding confirmations
  Object.keys(s.confirmations||{}).forEach(k=>{ if(k.startsWith('bot_')) delete s.confirmations[k]; });
  // if no participants, set to Buscando
  if(!s.participants || s.participants.length===0) s.state='Buscando';
  saveScrims(arr); renderScrims();
  try{ syncScrimToServer(s).catch(()=>{}); }catch(e){}
}

// scheduler: check scrims periodically to auto-transition Confirmado -> EnJuego when date reached
setInterval(()=>{
  const now = Date.now(); const arr = loadScrims(); let changed=false;
  arr.forEach(s=>{
    if(s.state==='Confirmado' && s.date){
      const t = new Date(s.date).getTime(); if(!isNaN(t) && t<=now){ s.state='EnJuego'; changed=true; }
    }
    // if scrim is EnJuego and date passed long enough, auto-finalize (demo: +10s)
    if(s.state==='EnJuego' && s.date){
      const t = new Date(s.date).getTime(); if(!isNaN(t) && t+10000<=now){ s.state='Finalizado'; if(!s.results) s.results = generateResultsForScrim(s);
        // attempt to sync and notify for this scrim
        try{ syncScrimToServer(s).catch(()=>{}); }catch(e){}
        try{ notifyStateChange(s.id,'Finalizado'); }catch(e){}
        changed=true; }
    }
  });
  if(changed) { saveScrims(arr); renderScrims(); }
}, 5000);

// generate plausible results for a scrim (winner, mvp, kills/assists, rating)
function generateResultsForScrim(s){
  try{
    const players = (s.participants||[]).filter(p=>p && !p.startsWith('bot_'));
    const bots = (s.participants||[]).filter(p=>p && p.startsWith && p.startsWith('bot_'));
    // pick winner randomly between Equipo A / Equipo B
    const winner = Math.random()>0.5 ? 'Equipo A' : 'Equipo B';
    // choose mvp from players if available, else from bots, else random name
    let mvp = null;
    if(players.length>0) mvp = players[Math.floor(Math.random()*players.length)];
    else if(bots.length>0) mvp = bots[Math.floor(Math.random()*bots.length)];
    else mvp = 'Jugador_'+Math.floor(1000+Math.random()*9000);
    // kills/assists plausible values
    const k = 8 + Math.floor(Math.random()*15); const a = 2 + Math.floor(Math.random()*10);
    const kills = k + '/' + a;
    const rating = 3 + Math.floor(Math.random()*3); // 3-5
    const notes = 'Resultados generados automáticamente';
  // moderation removed: results stored directly on scrim (no moderation queue)
    // push notification summary
  try{ pushNotif('Resultados generados para "'+(s.title||s.id)+'" — '+getParticipantDisplayName(s,mvp)+' ('+kills+')'); }catch(e){}
    return { winner, notes, mvp, kills, rating, recordedBy: 'system', time: Date.now() };
  }catch(e){ return { winner: 'Equipo A', notes: 'Simulación automática', recordedBy: 'system', time: Date.now() }; }
}

async function removeScrim(id){
  try{
    await fetch(API + '/scrims/delete', {method:'POST', body: JSON.stringify({id}), headers:{'Content-Type':'application/json'}});
    await refreshScrimsFromServer(); renderScrims(); return;
  }catch(e){ /* fallback */ }
  const arr = loadScrims(); saveScrims(arr.filter(x=>x.id!==id)); renderScrims();
}

function escapeHtml(s){ return (s+'').replace(/[&<>"']/g, c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c])); }

// Return the friendly display name for a participant id within a scrim
function getParticipantDisplayName(scrim, participantId){
  try{
    if(!participantId) return '';
    const all = loadScrims(); const local = (all||[]).find(x=>x && x.id===scrim.id) || scrim;
    if(local && local.participantNames && local.participantNames[participantId]) return local.participantNames[participantId];
    // if not found and it's a bot id, try to map quickly to a truncated friendly name
    if(participantId && participantId.startsWith && participantId.startsWith('bot_')){
      // fallback: show 'Jugador <suffix>' where suffix is last digits
      const m = participantId.match(/_(\d+)$/);
      return m? ('Jugador ' + (m[1].slice(-2)||m[1])) : participantId;
    }
    return participantId;
  }catch(e){ return participantId || ''; }
}

function getParticipantRanking(scrim, participantId){
  try{
    if(!participantId) return null;
    const meta = JSON.parse(localStorage.getItem('bot_meta_'+participantId) || 'null');
    if(meta && meta.mmr) return meta.mmr;
    // try to read from scrim participantNames mapping if we stored ranking there
    const all = loadScrims(); const local = (all||[]).find(x=>x && x.id===scrim.id) || scrim;
    if(local && local.participantRanks && local.participantRanks[participantId]) return local.participantRanks[participantId];
    return null;
  }catch(e){ return null; }
}

function getFallbackDisplayName(participantId){
  try{ if(!participantId) return ''; if(participantId.startsWith && participantId.startsWith('bot_')) return 'Jugador'; return participantId; }catch(e){ return participantId||''; }
}

// named handler so we can call it from delegated listeners if needed
async function handleCreateScrim(){
  // NOTE: window handle is assigned once below (outside) to avoid repeated rebindings
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
  // copy reminder hours: prefer scrim-specific value, fallback to user's global preference
  const scrRemVal = document.getElementById('scrimReminder')?.value;
  let rh = 0;
  if (scrRemVal && scrRemVal.trim()!=='') rh = parseInt(scrRemVal) || 0;
  if (rh<=0) rh = parseInt(document.getElementById('prefReminderGlobal')?.value || '0') || 0;
  if(rh>0) obj.reminderHours = rh;
  // show visible status while sending
  try{
    simOutput.textContent = 'Enviando scrim...';
    const resp = await fetch(API + '/scrims', {method:'POST', body: JSON.stringify(obj), headers:{'Content-Type':'application/json'}});
    if(!resp.ok){ const txt = await resp.text(); simOutput.textContent = 'Error al crear scrim: ' + resp.status + ' ' + txt; return; }
    const j = await resp.json().catch(()=>null);
  if (j && j.ok){
      simOutput.textContent = 'Scrim creado.';
      // auto-clear status after short delay so UI doesn't stay 'colgado'
      try{ setTimeout(()=>{ if(simOutput && simOutput.textContent==='Scrim creado.') simOutput.textContent=''; }, 2000); }catch(e){}
      // reconcile using server-returned scrim if available, otherwise use optimistic obj
      let created = obj;
      try{ if(j.scrim) created = j.scrim; }catch(e){}
      try{
        const arr = loadScrims();
        // remove any existing with same id, then unshift created to keep it at top
        const filtered = (arr||[]).filter(x=> !(x && x.id && created && created.id && x.id===created.id));
        filtered.unshift(created);
        saveScrims(filtered);
        // persist optimistic copy so future server snapshots merge it
        try{ localStorage.setItem('optimistic_'+(created.id||('tmp_'+Date.now())), JSON.stringify(created)); }catch(e){}
        console.log('handleCreateScrim: merged created scrim into localStorage', created.id || created.title);
        try{ renderScrims(); }catch(e){}
      }catch(e){ console.error('handleCreateScrim: merging failed', e); }
      // then schedule a gentle refresh to fetch any other server-side items but reconcile by id
      try{
        setTimeout(async ()=>{
          try{
            const serverList = await (await fetch(API + '/scrims')).json().catch(()=>null);
            if(Array.isArray(serverList)){
              try{
                // Merge strategy: preserve any local/optimistic items and prefer server objects when ids match.
                const localList = loadScrims();
                const serverMap = {};
                serverList.forEach(it=>{ if(it && it.id) serverMap[it.id] = it; });
                const merged = [];
                // keep local order but replace items with server version when available
                (localList||[]).forEach(it=>{
                  if(it && it.id && serverMap[it.id]) merged.push(serverMap[it.id]);
                  else merged.push(it);
                });
                // append any server items not already present
                serverList.forEach(it=>{ if(it && it.id && !(merged.some(m=>m && m.id===it.id))) merged.push(it); });
                // ensure created is present (fallback)
                try{ if(created && created.id && !merged.some(m=>m && m.id===created.id)) merged.unshift(created); }catch(e){}
                // also merge any optimistic saved scrims (persisted temporarily) to avoid losing them
                try{
                  for(let i=0;i<localStorage.length;i++){
                    const k = localStorage.key(i);
                    if(k && k.startsWith && k.startsWith('optimistic_')){
                      try{ const o = JSON.parse(localStorage.getItem(k)||'null'); if(o && o.id){ if(!merged.some(x=>x && x.id===o.id)) merged.unshift(o); } }catch(e){}
                    }
                  }
                }catch(e){}
                saveScrims(merged);
                // clear optimistic entry for created id (server acknowledged) if present
                try{ if(created && created.id) localStorage.removeItem('optimistic_'+created.id); }catch(e){}
                console.log('handleCreateScrim: reconciled with server list, merged count=', merged.length);
                try{ renderScrims(); }catch(e){}
              }catch(e){ console.error('handleCreateScrim: delayed reconcile failed', e); }
            }
          }catch(e){ console.error('handleCreateScrim: delayed reconcile failed', e); }
        }, 900);
      }catch(e){ console.error('handleCreateScrim: schedule reconcile failed', e); }
    } else {
      simOutput.textContent = 'Respuesta no OK del servidor: ' + (j && j.message? j.message : JSON.stringify(j));
    }
  }catch(e){ simOutput.textContent = 'Error de red al crear scrim: '+ (e && e.message ? e.message : String(e)); }
  // avoid unconditional overwrite; refresh is done above with reconciliation
  // clear fields
  $('scrimTitle').value=''; $('scrimRegion').value='';
}

// expose handler on window so external delegates can call it (assigned once)
try{ window.handleCreateScrim = handleCreateScrim; }catch(e){}
// attach normally if element exists; fallback to delegated listener only if the element is not present
if (createScrimBtn) {
  createScrimBtn.addEventListener('click', handleCreateScrim);
} else {
  // fallback: delegated listener in case the button is added later to the DOM
  document.addEventListener('click', (ev)=>{
    try{ if(ev.target && ev.target.id==='createScrimBtn'){ handleCreateScrim(); } }catch(e){}
  });
}

// initial render when app area shown
document.addEventListener('DOMContentLoaded', async ()=>{ await refreshScrimsFromServer(); renderScrims(); });

