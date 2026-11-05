#!/usr/bin/env python3
"""Compile production policy with javac and verify real new/legacy behavior.
Does not substitute for NeoForge integration or world performance profiling.
"""
from pathlib import Path
from tempfile import TemporaryDirectory
import subprocess, json, re
r=Path(__file__).resolve().parents[1]
city=r/'src/main/java/com/dorohedoro/hole/worldgen/city'
policy=city/'CityEcologyPolicy32.java'
pop=(city/'CityPopulator.java').read_text()
canal=(city/'CanalEcology24.java').read_text()
settings=(city/'CitySettings.java').read_text()
preset=json.loads((r/'src/main/resources/data/minecraft/worldgen/world_preset/normal.json').read_text())
assert 'CityEcologyPolicy32.keepStarterFishPersistent(plan.settings.worldDetailVersion())' in pop
assert 'CityEcologyPolicy32.canPlaceCanalSupply(' in canal
assert 'c.contains(bx, bz)' in canal and '!c.get(bx,64,bz).isAir()' in canal
assert 'if (CityEcologyPolicy32.keepStarterFishPersistent' in pop
assert 'Codec.intRange(0, 19).optionalFieldOf("world_detail_version", 0)' in settings
assert preset['dimensions']['minecraft:overworld']['generator']['city']['world_detail_version']==19
assert 'EntityType.SALMON' in pop and 'EntityType.COD' in pop
# No accidental change to the original entity-persistence policy for fixed residents.
assert pop.count('mob.setPersistenceRequired();')==2
with TemporaryDirectory(prefix='doro_ecology32_') as tmp:
    d=Path(tmp);test=d/'CityEcologyPolicyTest.java'
    test.write_text('''package com.dorohedoro.hole.worldgen.city;
public final class CityEcologyPolicyTest {
    public static void main(String[] args) {
        for(int v=0;v<=24;v++) {
            boolean legacy=v<19;
            if(CityEcologyPolicy32.keepStarterFishPersistent(v)!=legacy) throw new AssertionError("fish v"+v);
            if(!CityEcologyPolicy32.canPlaceCanalSupply(v,true)) throw new AssertionError("solid quay v"+v);
            if(CityEcologyPolicy32.canPlaceCanalSupply(v,false)!=legacy) throw new AssertionError("floating barrel v"+v);
        }
        System.out.println("PASS 25 profile versions: old-world fish and supply choices preserved; v19+ transient fish and supported barrels");
    }
}
''')
    c=subprocess.run(['javac','--release','21','-d',tmp,str(policy),str(test)],capture_output=True,text=True)
    assert c.returncode==0,c.stderr
    a=subprocess.run(['java','-cp',tmp,'com.dorohedoro.hole.worldgen.city.CityEcologyPolicyTest'],capture_output=True,text=True)
    assert a.returncode==0,a.stderr
    print(a.stdout.strip())
