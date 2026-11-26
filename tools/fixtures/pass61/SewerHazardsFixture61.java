package com.dorohedoro.hole.worldgen.city;

/** Offline seeded-policy validation. NOT Minecraft lighting/AI/runtime proof. */
public final class SewerHazardsFixture61 {
    private static int checks = 0;
    private static void test(boolean truth, String name) {
        checks++;
        if (!truth) throw new AssertionError(name);
    }
    public static void main(String[] args) {
        long seeds[] = {0L, 1L, -1L, 12345L, 0x13579BDF2468ACE0L, Long.MIN_VALUE};
        int inspected = 0, infested = 0, caves = 0;
        int totalLamps = 0, newLamps = 0;
        int darkSections = 0, litSections = 0;
        for (long seed : seeds) {
            for (int i = -80; i < 80; i++) {
                for (int j = -80; j < 80; j++) {
                    int kind = SewerBranchPolicy.kind(seed,i,j,30);
                    if (kind == 0) caves++;
                    if (kind == 1 || kind == 2) inspected++;
                    boolean nest = SewerHazards61.infested(seed,i,j,kind,44);
                    if (nest) infested++;
                    test(!nest || kind == 1 || kind == 2,"nest only optional inspection rooms");
                    test(!SewerHazards61.infested(seed,i,j,kind,43),"old saved worlds unchanged");
                    test(nest == SewerHazards61.infested(seed,i,j,kind,44),"repeatable nest choice");
                }
            }
            for (int axis = -4; axis <= 4; axis++) {
                for (int section = -70; section < 70; section++) {
                    int center = section*93 + 31;
                    // At the selected evenly spaced coordinate, either all three
                    // old lantern opportunities remain or all are dark in v44.
                    boolean old = SewerHazards61.lampAt(seed,axis,center,43);
                    boolean now = SewerHazards61.lampAt(seed,axis,center,44);
                    test(old,"old lantern at each 31-block station");
                    if (now) litSections++; else darkSections++;
                    for (int k = 0; k < 3; k++) {
                        int at = section*93 + 31*k;
                        // Call twice as if neighboring chunks examined the same column.
                        test(SewerHazards61.lampAt(seed,axis,at,44) ==
                             SewerHazards61.lampAt(seed,axis,at,44),"chunk-independent lighting");
                        if (SewerHazards61.lampAt(seed,axis,at,43)) totalLamps++;
                        if (SewerHazards61.lampAt(seed,axis,at,44)) newLamps++;
                    }
                }
            }
        }
        test(caves > 44000 && caves < 49000,"30 percent cave policy preserved");
        test(inspected > 26000 && inspected < 29000,"inspection probability preserved");
        test(infested > inspected * .20 && infested < inspected * .30,"quarter of inspection bays infested");
        test(darkSections > 2500 && litSections > 3900,"both dark and lit utility sections");
        test(newLamps > totalLamps * .50 && newLamps < totalLamps * .70,"roughly three-fifths lanterns remain");
        test(!SewerHazards61.lampAt(12345,0,5,43),"no lamps between legacy stations");
        test(!SewerHazards61.lampAt(12345,0,5,44),"no lamps between new stations");
        test(!SewerHazards61.infested(0,0,0,0,44),"starter junction cannot be nest");
        System.out.println("PASS Pass61 Java policy: " + checks + " checks; cave branches="+caves
                +", inspection rooms="+inspected+", infested rooms="+infested
                +", dark sections="+darkSections+", lit sections="+litSections
                +", old lamps="+totalLamps+", new lamps="+newLamps);
    }
}
