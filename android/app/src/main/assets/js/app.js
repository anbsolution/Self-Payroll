const KEY="selfPayrollWebV1";
const defaultData={profile:{name:"",employeeId:"",company:"",basicSalary:0,allowance:0,bonus:0,advance:0,deduction:0},attendance:{}};
let data=JSON.parse(localStorage.getItem(KEY)||"null")||structuredClone(defaultData);
const $=id=>document.getElementById(id);
const pad=n=>String(n).padStart(2,"0");
const money=n=>"₹"+Number(n||0).toLocaleString("en-IN");
const todayKey=()=>{const d=new Date();return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}`};
const nowHM=()=>{const d=new Date();return pad(d.getHours())+":"+pad(d.getMinutes())};
function save(){localStorage.setItem(KEY,JSON.stringify(data));renderAll()}
function toast(msg){const e=$("toast");e.textContent=msg;e.classList.add("show");setTimeout(()=>e.classList.remove("show"),1800)}
function formatDate(k){return new Date(k+"T00:00:00").toLocaleDateString("en-IN",{weekday:"short",day:"2-digit",month:"short",year:"numeric"})}
function duration(a,b){if(!a||!b)return 0;let x=new Date("1970-01-01T"+a),y=new Date("1970-01-01T"+b);if(y<x)y.setDate(y.getDate()+1);return Math.max(0,y-x)}
function hm(ms){const m=Math.floor(ms/60000);return `${Math.floor(m/60)}h ${m%60}m`}
function statusClass(s){return s==="Present"?"present":s==="Absent"?"absent":s==="Leave"?"leave":"off"}
function navigate(page){document.querySelectorAll(".page").forEach(x=>x.classList.toggle("active",x.dataset.page===page));document.querySelectorAll(".nav-item").forEach(x=>x.classList.toggle("active",x.dataset.nav===page));if(page==="monthly")renderMonthly();if(page==="salary")renderSalary();if(page==="attendance")renderAttendance();if(page==="profile")renderProfile()}
document.querySelectorAll("[data-nav]").forEach(b=>b.addEventListener("click",()=>navigate(b.dataset.nav)));
function renderAttendance(){const now=new Date(),k=todayKey(),r=data.attendance[k];$("todayText").textContent=now.toLocaleDateString("en-IN",{weekday:"long",day:"numeric",month:"long"});$("clockText").textContent=now.toLocaleTimeString("en-IN",{hour:"2-digit",minute:"2-digit"});$("inTime").textContent=r?.in||"--:--";$("outTime").textContent=r?.out||"--:--";$("workTime").textContent=r?hm(duration(r.in,r.out||nowHM())):"0h 0m";$("todayStatus").textContent=r?.status||"Not punched";const btn=$("punchBtn");btn.disabled=false;if(!r){btn.textContent="Punch IN";btn.onclick=punchIn}else if(r.in&&!r.out){btn.textContent="Punch OUT";btn.onclick=punchOut}else{btn.textContent="Completed";btn.disabled=true}$("editTodayBtn").classList.toggle("hidden",!r);const vals=Object.values(data.attendance);$("presentCount").textContent=vals.filter(x=>x.status==="Present").length;$("absentCount").textContent=vals.filter(x=>x.status==="Absent").length;$("leaveCount").textContent=vals.filter(x=>x.status==="Leave").length;$("lateCount").textContent=vals.filter(x=>x.late).length}
function punchIn(){data.attendance[todayKey()]={in:nowHM(),out:"",status:"Present",late:false};save();toast("Punch IN recorded")}
function punchOut(){const r=data.attendance[todayKey()];if(!r)return;r.out=nowHM();save();toast("Punch OUT recorded")}
$("editTodayBtn").onclick=()=>openManualAttendance(todayKey());
function daysInMonth(ym){const [y,m]=ym.split("-").map(Number),n=new Date(y,m,0).getDate();return Array.from({length:n},(_,i)=>`${y}-${pad(m)}-${pad(i+1)}`)}
function renderMonthly(){const ym=$("monthPicker").value||todayKey().slice(0,7);$("monthPicker").value=ym;const rows=daysInMonth(ym).map(k=>({k,r:data.attendance[k]}));$("attendanceBody").innerHTML=rows.map(({k,r})=>{const st=r?.status||"Absent";const hrs=r?hm(duration(r.in,r.out)):"0h 0m";return `<tr><td>${formatDate(k)}</td><td><span class="badge ${statusClass(st)}">${st}</span></td><td>${r?.in||"—"}</td><td>${r?.out||"—"}</td><td>${hrs}</td><td><button class="secondary-btn edit-row-btn" data-date="${k}">✏️ Edit</button></td></tr>`}).join("");document.querySelectorAll(".edit-row-btn").forEach(btn=>btn.onclick=()=>openManualAttendance(btn.dataset.date));const present=rows.filter(x=>x.r?.status==="Present").length,leave=rows.filter(x=>x.r?.status==="Leave").length,absent=rows.filter(x=>x.r?.status==="Absent").length,ms=rows.reduce((a,x)=>a+duration(x.r?.in,x.r?.out),0);$("mPresent").textContent=present;$("mAbsent").textContent=absent;$("mLeave").textContent=leave;$("mHours").textContent=hm(ms)}
$("monthPicker").value=todayKey().slice(0,7);$("monthPicker").onchange=renderMonthly;
function renderSalary(){const p=data.profile,total=Number(p.basicSalary||0)+Number(p.allowance||0)+Number(p.bonus||0),net=total-Number(p.advance||0)-Number(p.deduction||0);$("salaryMonth").value=$("salaryMonth").value||todayKey().slice(0,7);$("sBasic").textContent=money(p.basicSalary);$("sAllowance").textContent=money(p.allowance);$("sBonus").textContent=money(p.bonus);$("sAdvance").textContent=money(p.advance);$("sDeduction").textContent=money(p.deduction);$("netSalary").textContent=money(net)}
$("salaryMonth").value=todayKey().slice(0,7);$("salaryMonth").onchange=renderSalary;
function renderProfile(){const p=data.profile;["name","employeeId","company","basicSalary","allowance","bonus","advance","deduction"].forEach(k=>$(k).value=p[k]??"")}
$("profileForm").onsubmit=e=>{e.preventDefault();["name","employeeId","company","basicSalary","allowance","bonus","advance","deduction"].forEach(k=>data.profile[k]=["basicSalary","allowance","bonus","advance","deduction"].includes(k)?Number($(k).value||0):$(k).value.trim());save();toast("Profile saved")};
$("exportBtn").onclick=()=>{const blob=new Blob([JSON.stringify(data,null,2)],{type:"application/json"}),a=document.createElement("a");a.href=URL.createObjectURL(blob);a.download=`self-payroll-backup-${todayKey()}.json`;a.click();URL.revokeObjectURL(a.href);toast("Backup exported")};
$("importInput").onchange=e=>{const f=e.target.files[0];if(!f)return;const rd=new FileReader();rd.onload=()=>{try{const x=JSON.parse(rd.result);if(!x.profile||!x.attendance)throw Error();data=x;save();toast("Backup restored")}catch{toast("Invalid backup file")}};rd.readAsText(f)};
$("resetBtn").onclick=()=>{if(confirm("Reset all local attendance and salary data?")){data=structuredClone(defaultData);save();toast("Data reset")}};
$("printBtn").onclick=()=>window.print();
$("shareBtn").onclick=async()=>{const text=`Self Payroll Salary Slip\n${$("salaryMonth").value}\nNet Salary: ${$("netSalary").textContent}`;if(navigator.share)await navigator.share({title:"Self Payroll Salary Slip",text});else{await navigator.clipboard?.writeText(text);toast("Salary summary copied")}};
$("themeBtn").onclick=()=>{document.body.classList.toggle("dark");localStorage.setItem("spTheme",document.body.classList.contains("dark")?"dark":"light")};
if(localStorage.getItem("spTheme")==="dark")document.body.classList.add("dark");
function openManualAttendance(date){const r=data.attendance[date]||{status:"Present",in:"",out:"",note:""};$("editDate").value=date;$("editDateLabel").textContent=formatDate(date);$("editStatus").value=r.status||"Present";$("editIn").value=r.in||"";$("editOut").value=r.out||"";$("editNote").value=r.note||"";$("attendanceModal").classList.remove("hidden");document.body.classList.add("modal-open")}
function closeManualAttendance(){$("attendanceModal").classList.add("hidden");document.body.classList.remove("modal-open")}
window.openManualAttendance=openManualAttendance;
document.querySelectorAll("[data-close-modal]").forEach(x=>x.addEventListener("click",closeManualAttendance));
$("attendanceEditForm").onsubmit=e=>{e.preventDefault();const date=$("editDate").value,status=$("editStatus").value;data.attendance[date]={in:status==="Present"?$("editIn").value:"",out:status==="Present"?$("editOut").value:"",status,late:false,note:$("editNote").value.trim(),manual:true};save();closeManualAttendance();toast("Attendance updated")};
if("serviceWorker" in navigator)navigator.serviceWorker.register("sw.js").catch(()=>{});
function renderAll(){renderAttendance();renderMonthly();renderSalary();renderProfile()}
renderAll();setInterval(renderAttendance,30000);

/* APP_LOCK_THEME_V2 */
(function(){
  const THEME_KEY="selfPayrollTheme";
  const LOCK_KEY="selfPayrollLock";
  const PIN_HASH_KEY="selfPayrollPinHash";
  const $q=(id)=>document.getElementById(id);

  async function hashPin(pin){
    const buf=await crypto.subtle.digest("SHA-256",new TextEncoder().encode(pin));
    return Array.from(new Uint8Array(buf)).map(b=>b.toString(16).padStart(2,"0")).join("");
  }
  function applyTheme(mode){
    document.documentElement.dataset.theme=mode;
    if(mode==="dark") document.documentElement.classList.add("dark");
    else if(mode==="light") document.documentElement.classList.remove("dark");
    else document.documentElement.classList.toggle("dark",matchMedia("(prefers-color-scheme: dark)").matches);
  }
  function themeInit(){
    const mode=localStorage.getItem(THEME_KEY)||"system";
    applyTheme(mode);
    const sel=$q("themeMode"); if(sel) sel.value=mode;
    if(sel) sel.onchange=()=>{localStorage.setItem(THEME_KEY,sel.value);applyTheme(sel.value);};
    matchMedia("(prefers-color-scheme: dark)").addEventListener?.("change",()=>{if((localStorage.getItem(THEME_KEY)||"system")==="system")applyTheme("system");});
  }
  async function setPin(){
    let p=prompt("Set a new 4–6 digit PIN:");
    if(p===null)return;
    p=p.trim();
    if(!/^\d{4,6}$/.test(p)){alert("PIN must be 4–6 digits.");return;}
    localStorage.setItem(PIN_HASH_KEY,await hashPin(p));
    localStorage.setItem(LOCK_KEY,"1");
    syncLockUI();
    alert("App Lock enabled.");
  }
  async function changePin(){
    if(!localStorage.getItem(PIN_HASH_KEY)){return setPin();}
    let old=prompt("Enter current PIN:");
    if(old===null)return;
    if(await hashPin(old)!==localStorage.getItem(PIN_HASH_KEY)){alert("Incorrect PIN.");return;}
    let p=prompt("Enter new 4–6 digit PIN:");
    if(p===null)return;
    p=p.trim();
    if(!/^\d{4,6}$/.test(p)){alert("PIN must be 4–6 digits.");return;}
    localStorage.setItem(PIN_HASH_KEY,await hashPin(p));
    alert("PIN changed.");
  }
  function syncLockUI(){
    const enabled=localStorage.getItem(LOCK_KEY)==="1";
    const cb=$q("lockEnabled"), row=$q("pinRow");
    if(cb) cb.checked=enabled;
    if(row) row.style.display=enabled?"flex":"none";
  }
  function showLock(){
    const modal=$q("appLockModal"); if(!modal)return;
    modal.style.display="flex";
    const input=$q("unlockPin"); if(input){input.value="";setTimeout(()=>input.focus(),50);}
  }
  async function unlock(){
    const input=$q("unlockPin"), err=$q("unlockError");
    if(!input)return;
    if(await hashPin(input.value.trim())===localStorage.getItem(PIN_HASH_KEY)){
      sessionStorage.setItem("selfPayrollUnlocked","1");
      $q("appLockModal").style.display="none";
      if(err)err.textContent="";
    }else if(err) err.textContent="Incorrect PIN";
  }
  function lockIfNeeded(){
    if(localStorage.getItem(LOCK_KEY)==="1" && sessionStorage.getItem("selfPayrollUnlocked")!=="1") showLock();
  }
  document.addEventListener("DOMContentLoaded",()=>{
    themeInit();syncLockUI();
    const cb=$q("lockEnabled");
    if(cb) cb.onchange=async()=>{
      if(cb.checked) await setPin();
      else {localStorage.removeItem(LOCK_KEY);localStorage.removeItem(PIN_HASH_KEY);syncLockUI();alert("App Lock disabled.");}
    };
    const cp=$q("changePinBtn");if(cp)cp.onclick=changePin;
    const ub=$q("unlockBtn");if(ub)ub.onclick=unlock;
    const inp=$q("unlockPin");if(inp)inp.addEventListener("keydown",e=>{if(e.key==="Enter")unlock();});
    lockIfNeeded();
  });
  document.addEventListener("visibilitychange",()=>{
    if(document.visibilityState==="visible") lockIfNeeded();
  });
})();

/* BIOMETRIC_LOCK_V1 */
(function(){
  const KEY="selfPayrollBiometric";
  function nativeBridge(method){
    try {
      if(window.Android && typeof window.Android[method]==="function"){
        window.Android[method](); return true;
      }
    } catch(e){}
    return false;
  }
  window.showPinLockFromNative=function(){
    // Native biometric cancellation falls back to the existing web PIN flow.
    if(typeof window.openManualAttendance==="function"){} // keep global scope alive
    const m=document.getElementById("appLockModal");
    if(m){m.style.display="flex";const i=document.getElementById("unlockPin");if(i)i.focus();}
  };
  document.addEventListener("DOMContentLoaded",function(){
    const cb=document.getElementById("biometricEnabled");
    if(!cb)return;
    cb.checked=localStorage.getItem(KEY)==="1";
    cb.onchange=function(){
      if(cb.checked){
        const ok=nativeBridge("enableBiometricLock");
        if(ok) localStorage.setItem(KEY,"1");
        else {
          cb.checked=false;
          alert("Fingerprint lock is available in the Android APK on phones with biometric support. The PWA uses PIN lock.");
        }
      }else{
        nativeBridge("disableBiometricLock");
        localStorage.removeItem(KEY);
      }
    };
  });
})();

/* DARKNESS_PERCENT_V1 */
(function(){
  const KEY="selfPayrollDarkness";
  function applyDarkness(v){
    v=Math.max(0,Math.min(100,Number(v)||0));
    document.documentElement.style.setProperty("--darkness-pct",v+"%");
    document.documentElement.style.setProperty("--darkness",String(v/100));
    const out=document.getElementById("darknessValue");
    if(out) out.textContent=Math.round(v)+"%";
  }
  function initDarkness(){
    const range=document.getElementById("darknessRange");
    if(!range)return;
    const saved=localStorage.getItem(KEY);
    const value=saved===null?85:Number(saved);
    range.value=value;
    applyDarkness(value);
    range.oninput=function(){
      localStorage.setItem(KEY,range.value);
      applyDarkness(range.value);
    };
  }
  document.addEventListener("DOMContentLoaded",initDarkness);
})();
