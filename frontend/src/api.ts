export type Row=Record<string,any>;
let token='';
export async function api<T=any>(path:string,method='GET',body?:unknown):Promise<T>{
 if(method!=='GET'&&!token){const r=await fetch('/api/auth/csrf');token=(await r.json()).token;}
 const headers:Record<string,string>={};if(token)headers['X-XSRF-TOKEN']=token;
 if(!(body instanceof FormData))headers['Content-Type']='application/json';
 const r=await fetch('/api'+path,{method,headers,body:body===undefined?undefined:body instanceof FormData?body:JSON.stringify(body)});
 const data=await r.json().catch(()=>({message:'Unexpected response'}));if(!r.ok){if(data.code==='CSRF_DENIED')token='';throw new Error(data.message||'Request failed');}return data;
}
export async function checkout(body:unknown,key:string){if(!token)await api('/auth/csrf').then(r=>token=r.token);const r=await fetch('/api/checkout',{method:'POST',headers:{'Content-Type':'application/json','X-XSRF-TOKEN':token,'Idempotency-Key':key},body:JSON.stringify(body)});const data=await r.json();if(!r.ok)throw new Error(data.message||'Checkout failed');return data;}
export const money=(x:unknown)=>new Intl.NumberFormat('en-US',{style:'currency',currency:'USD'}).format(Number(x||0));
export const date=(x:unknown)=>new Date(String(x)).toLocaleDateString('en-US',{month:'short',day:'numeric',year:'numeric'});
