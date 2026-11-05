package com.dorohedoro.hole.worldgen.city;
/** Offline deterministic branch selection; District and LotType can be stubbed for local JVM tests. */
public class VerifyHoleTreePolicy16 {
    public static void main(String[] args) {
        long seeds[] = {1, 15, 42, 123, 2026, 20261031L, -888, 999999};
        int samples=0;
        for (District d : District.values()) {
            for (LotType t : new LotType[]{LotType.PARK, LotType.VACANT, LotType.RUBBLE}) {
                int[] counts = new int[4];
                for (long seed : seeds) {
                    for (int i=0; i<20000; i++) {
                        long h= CityHash.of(seed, i, 0x51L);
                        HoleTreePolicy.Form form=HoleTreePolicy.select(seed, h, d, t);
                        if (form != HoleTreePolicy.select(seed, h, d, t)) throw new AssertionError("Non deterministic");
                        counts[form.ordinal()]++;
                        samples++;
                    }
                }
                double living=100.0*(counts[HoleTreePolicy.Form.STUNTED.ordinal()]+counts[HoleTreePolicy.Form.CROOKED.ordinal()])/(seeds.length * 20000.0);
                double dead=100.0*counts[HoleTreePolicy.Form.DEAD.ordinal()]/(seeds.length * 20000.0);
                System.out.printf("%s/%s: living %.2f%%, dead %.2f%%, none %.2f%%%n", d,t,living,dead,100-living-dead);
                if(t==LotType.PARK && living > 14) throw new AssertionError("Park too green");
                if(t==LotType.VACANT && living > 4) throw new AssertionError("Vacant lot too green");
                if(t==LotType.RUBBLE && living > 0.01) throw new AssertionError("Rubble must not grow live tree");
                if(living+dead > 65) throw new AssertionError("Too many trees");
            }
        }
        System.out.println("PASS "+samples+" deterministic vegetation selections");
    }
}
