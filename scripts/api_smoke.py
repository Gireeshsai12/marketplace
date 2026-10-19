#!/usr/bin/env python3
"""Run against an already running demo; creates isolated test customer and orders."""
import urllib.request,urllib.error,http.cookiejar,json,uuid,os
BASE=os.environ.get('API_URL','http://127.0.0.1:8080')+'/api'
passed=0
def check(ok,msg):
 global passed
 assert ok,msg
 passed+=1;print('PASS',msg,flush=True)
class Client:
 def __init__(self):self.jar=http.cookiejar.CookieJar();self.o=urllib.request.build_opener(urllib.request.ProxyHandler({}),urllib.request.HTTPCookieProcessor(self.jar));self.token=''
 def req(self,path,method='GET',body=None,key=None,expect=200):
  if method!='GET' and not self.token:self.token=self.req('/auth/csrf')['token']
  h={'Content-Type':'application/json','X-XSRF-TOKEN':self.token}
  if key:h['Idempotency-Key']=key
  req=urllib.request.Request(BASE+path,data=None if body is None else json.dumps(body).encode(),headers=h,method=method)
  try:
   with self.o.open(req,timeout=30) as r:status=r.status;data=json.load(r)
  except urllib.error.HTTPError as e:status=e.code;data=json.load(e)
  assert status==expect,(path,status,expect,data)
  return data
 def login(self,role):return self.req('/auth/login','POST',{'email':role+'@marketplace.local','password':'MarketDemo!2026'})
a=Client();a.req('/cart',expect=401);check(True,'anonymous cart denied')
products=a.req('/products');check(products['total']==48,'48 deterministic catalog products')
p=a.req('/products?q=headphones')['items'];check(len(p)==1 and p[0]['title']=='Studio Wireless Headphones','PostgreSQL full-text title search')
v=a.req('/products/1')['variants'][0];vid=v['id']
a.req('/products?min=-1',expect=400);check(True,'bounded filter validation')
a.login('customer');seller=Client();seller.login('seller');admin=Client();admin.login('admin')
other=Client();email='qa-'+uuid.uuid4().hex[:10]+'@example.com';other.req('/auth/register','POST',{'email':email,'password':'QaPassword!2026','name':'QA Customer'});other.req('/auth/login','POST',{'email':email,'password':'QaPassword!2026'});check(other.req('/auth/me')['email']==email,'registration, BCrypt login, session identity')
a.req('/cart','POST',{'variantId':vid,'quantity':1});a.req('/cart/'+str(vid),'PUT',{'variantId':vid,'quantity':-1},expect=400);check(True,'cart rejects negative quantities')
quote=a.req('/checkout/quote','POST',{'shipping':'STANDARD','coupon':'WELCOME10'});check(float(quote['discount'])==13 and float(quote['total'])==124.30,'server quote uses decimal-safe discount and demo tax')
key=uuid.uuid4().hex;body={'addressId':1,'shipping':'STANDARD','coupon':'WELCOME10','mode':'success'};o=a.req('/checkout','POST',body,key);check(o['payment_state']=='PAID','successful payment creates paid order')
replay=a.req('/checkout','POST',body,key);check(replay['id']==o['id'],'duplicate checkout returns same order')
oid=o['id'];a.req('/orders/'+str(oid)+'/demo-payment','POST',{'eventId':'initial-'+str(oid),'outcome':'success'});check(True,'duplicate payment event is accepted idempotently')
other.req('/orders/'+str(oid),expect=404);check(True,'customer order ownership enforced')
a.req('/seller/inventory',expect=403);check(True,'customer cannot access seller inventory')
seller2=Client();seller2.login('seller2');before=seller.req('/inventory') if False else seller.req('/seller/inventory');stock=next(x for x in before if x['id']==vid)['stock'];seller2.req('/seller/inventory/'+str(vid),'POST',{'delta':1,'reason':'Ownership test'},expect=404);check(True,'seller cannot adjust another seller inventory')
a.req('/cart','POST',{'variantId':vid,'quantity':1});a.req('/checkout/quote','POST',{'shipping':'STANDARD','coupon':'WELCOME10'},expect=409);check(True,'per-customer coupon usage limit enforced')
# Paid cancellation, repeated cancellation, inventory restored exactly once.
a.req('/orders/'+str(oid)+'/cancel','POST',{});a.req('/orders/'+str(oid)+'/cancel','POST',{});after=next(x for x in seller.req('/seller/inventory') if x['id']==vid)['stock'];check(after==stock+1,'refund and cancellation restock exactly once')
# Declined and delayed payment flows.
for mode in ['decline','delay']:
 k=uuid.uuid4().hex;before=next(x for x in seller.req('/seller/inventory') if x['id']==vid)
 if not a.req('/cart'):a.req('/cart','POST',{'variantId':vid,'quantity':1})
 o=a.req('/checkout','POST',{'addressId':1,'shipping':'STANDARD','coupon':'','mode':mode},k)
 if mode=='decline':
  after=next(x for x in seller.req('/seller/inventory') if x['id']==vid);check(o['payment_state']=='FAILED' and after['reserved']==before['reserved'],'decline releases reservation without reducing stock')
 else:
  check(o['payment_state']=='PENDING','delayed payment holds pending order')
  o=a.req('/orders/'+str(o['id'])+'/demo-payment','POST',{'eventId':'qa-'+str(o['id']),'outcome':'success'});check(o['payment_state']=='PAID','delayed callback commits reservation')
  detail=a.req('/orders/'+str(o['id']));portion=detail['portions'][0]['id'];seller.req('/seller/orders/'+str(portion)+'/fulfillment','POST',{'status':'DELIVERED'},expect=409);check(True,'invalid fulfillment transition rejected')
  seller.req('/seller/orders/'+str(portion)+'/fulfillment','POST',{'status':'SHIPPED','tracking':'SIM-QA'});check(a.req('/orders/'+str(o['id']))['portions'][0]['fulfillment']=='SHIPPED','customer sees seller shipment update')
  a.req('/orders/'+str(o['id'])+'/cancel','POST',{},expect=409);check(True,'shipped order cancellation rejected')
  seller.req('/seller/orders/'+str(portion)+'/fulfillment','POST',{'status':'DELIVERED','tracking':'SIM-QA'})
  a.req('/products/1/reviews','POST',{'rating':5,'title':'Excellent everyday audio','body':'Comfortable and dependable. Demo verified purchase review.'});check(a.req('/products/1')['review_count']==1,'verified-purchase review updates rating aggregate')
  a.req('/orders/'+str(o['id'])+'/returns/'+str(portion),'POST',{'reason':'Testing the documented demo return workflow'});r=seller.req('/seller/returns')[0];seller.req('/seller/returns/'+str(r['id'])+'/approve','POST',{});seller.req('/seller/returns/'+str(r['id'])+'/approve','POST',{});check(a.req('/orders/'+str(o['id']))['payment_state']=='REFUNDED','return approval refunds idempotently')
other.req('/products/2/reviews','POST',{'rating':5,'title':'Not purchased','body':'Unverified attempt'},expect=403);check(True,'review requires actual paid purchase')
# Leave one paid order visible for portfolio screenshots.
a.req('/cart','POST',{'variantId':vid,'quantity':1});o=a.req('/checkout','POST',{'addressId':1,'shipping':'STANDARD','coupon':'','mode':'success'},uuid.uuid4().hex)
check(float(seller.req('/seller/overview')['revenue'])==129.99,'seller revenue excludes refunded portions')
check(admin.req('/admin/orders/'+str(o['id']))['id']==o['id'],'administrator can inspect order snapshots')
check(len(admin.req('/admin/audit'))>0,'sensitive seller operations recorded in audit log')
# CSRF, administration, catalog management, and upload validation.
raw=urllib.request.Request(BASE+'/auth/logout',data=b'{}',headers={'Content-Type':'application/json'},method='POST')
try:a.o.open(raw);raise AssertionError('CSRF-free POST accepted')
except urllib.error.HTTPError as e:check(e.code==403,'CSRF-free state-changing request rejected')
a.req('/admin/users',expect=403);check(True,'customer cannot access administrator resources')
admin.req('/admin/users/'+str(other.req('/auth/me')['id'])+'/active','POST',{'active':False});other.req('/auth/me',expect=403);check(True,'administrator suspension invalidates account access')
# New product metadata and version protection; archive it so active demo catalog remains unchanged.
product={'title':'QA Portable Speaker','brand':'QA','description':'A deterministic catalog management test listing.','categoryId':1,'image':'/assets/product-4.svg','price':39.99,'originalPrice':49.99,'stock':10,'color':'Slate','active':True}
pid=seller.req('/seller/products','POST',product)['id'];product.update(version=0,title='QA Updated Speaker',active=False);seller.req('/seller/products/'+str(pid),'PUT',product);seller.req('/seller/products/'+str(pid),'PUT',product,expect=409);check(True,'seller catalog create/edit/archive and optimistic conflict protection')
seller.req('/seller/products/'+str(pid)+'/variants','POST',{'color':'Forest','price':44.99,'originalPrice':49.99,'stock':5});check(True,'seller can add a priced inventory variant')
seller.req('/seller/inventory/'+str(vid),'POST',{'delta':-100000,'reason':'Invalid negative stock test'},expect=409);check(True,'inventory adjustment cannot make available stock negative')
# Generate a valid 2x2 PNG using only the Python standard library.
import zlib,struct
chunk=lambda t,data:struct.pack('!I',len(data))+t+data+struct.pack('!I',zlib.crc32(t+data)&0xffffffff)
png=b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('!IIBBBBB',2,2,8,2,0,0,0))+chunk(b'IDAT',zlib.compress(b'\x00'+b'\x90\xa0\xb0'*2+b'\x00'+b'\x90\xa0\xb0'*2))+chunk(b'IEND',b'')
def upload(data,filename):
 boundary='qa-boundary';body=(f'--{boundary}\r\nContent-Disposition: form-data; name="file"; filename="{filename}"\r\nContent-Type: image/png\r\n\r\n').encode()+data+f'\r\n--{boundary}--\r\n'.encode()
 req=urllib.request.Request(BASE+'/seller/uploads',data=body,headers={'Content-Type':'multipart/form-data; boundary='+boundary,'X-XSRF-TOKEN':seller.token},method='POST')
 try:
  with seller.o.open(req,timeout=30) as r:return r.status,json.load(r)
 except urllib.error.HTTPError as e:return e.code,json.load(e)
status,data=upload(png,'../../sample.png');check(status==200 and data['url'].startswith('/uploads/') and '..' not in data['url'],'image upload re-encodes valid PNG and ignores client filename')
status,data=upload(b'<script>alert(1)</script>','fake.png');check(status==400,'disguised non-image upload rejected')
a.req('/auth/logout','POST',{});a.req('/auth/me',expect=401);check(True,'logout invalidates session')
print(f'API SMOKE: {passed} checks passed',flush=True)
