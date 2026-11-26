#!/usr/bin/env python3
"""Port of source's integer hash and smooth stations for geometry-only river sampling."""
mask=(1<<64)-1
u=lambda x:x&mask

def mix(z):
    z=u(u(z^(z>>30))*0xBF58476D1CE4E5B9)
    z=u(u(z^(z>>27))*0x94D049BB133111EB)
    return u(z^(z>>31))
def h1(seed,a):return mix(u(seed+0x9E3779B97F4A7C15 + u(a*0xC2B2AE3D27D4EB4F)))
def h2(seed,a,b):return mix(u(h1(seed,a)+u(b*0x165667B19E3779F9)))
def h3(seed,a,b,c):return mix(u(h2(seed,a,b)+u(c*0x27D4EB2F165667C5)))
def unit_station(seed,salt,index,along,period):
    section=along//period; t=(along%period)/period;ease=t*t*(3-2*t)
    a=h3(seed,u(salt+index*9973),section,0)
    b=h3(seed,u(salt+index*9973),section+1,0)
    v0=(a%2001-1000)/1000
    v1=(b%2001-1000)/1000
    return v0+(v1-v0)*ease

def offset(seed,index,along,new):
    salt=0x714EA003
    if not new:return round(3.0*unit_station(seed,salt,index,along,80))
    broad=unit_station(seed,salt,index,along,112)
    local=unit_station(seed,salt^0x36B1EC0,index,along,47)
    return max(-10,min(10,round(8.4*broad+2*local)))

def width(seed,index,along,new):
    salt=0x714EA005
    broad=unit_station(seed,salt,index,along,144)
    local=unit_station(seed,salt ^ 0x5E6D123,index,along,40)
    radius=max(3,min(8 if new else 7,5+round(2.3*broad+.7*local)))
    reserve=17 if new else 9
    limit=reserve-2-abs(offset(seed,index,along,new))
    return max(3,min(radius,limit))

def main():
    for seed in (12345,8675309,-987654321,42):
        for new in (False,True):
            o=[offset(seed,4,z,new) for z in range(-1024,1025)]
            w=[width(seed,4,z,new) for z in range(-1024,1025)]
            assert all(abs(a-b)<=2 for a,b in zip(o,o[1:])), 'abrupt channel center change'
            assert all(3<=b<=8 for b in w), 'channel width outside bounds'
            assert all(abs(a)+b<= (17 if new else 9)-2 for a,b in zip(o,w)), 'water cuts into lot zone'
            print(f'seed {seed:>11} {"new v21" if new else "old v20"} '
                f'center={min(o):>3}..{max(o):<3} width={min(w)}..{max(w)} '
                f'bank clearance>={2}')
    print('PASS: smooth bends, minimum width, and 2-block river-edge reservation for four sample seeds')
if __name__=='__main__':main()
