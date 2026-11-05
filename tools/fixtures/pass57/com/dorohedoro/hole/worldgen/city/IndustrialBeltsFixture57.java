package com.dorohedoro.hole.worldgen.city;
import java.util.EnumMap;
/** Executable test of the real land-use policy with no Minecraft runtime dependency. */
public final class IndustrialBeltsFixture57 {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        int cases=0, small=0, matched=0;
        int[][] count = new int[3][LotType.values().length];
        EnumMap<IndustrialBelts57.Belt, Integer> regions = new EnumMap<>(IndustrialBelts57.Belt.class);
        for (IndustrialBelts57.Belt belt: IndustrialBelts57.Belt.values()) regions.put(belt,0);
        for (int seed=0;seed<1200;seed++) {
          long worldSeed=CityHash.of(0xBEEFL,seed);
          for (int i=0;i<240;i++) {
            int x=(i%24)*160 - 1450 + (seed%11);
            int z=(i/24)*160 - 720 - (seed%7);
            Rect site=new Rect(x,z,x+11+(i%33),z+11+((i*7)%35));
            IndustrialBelts57.Belt belt=IndustrialBelts57.belt(worldSeed,site.centerX(),site.centerZ());
            regions.put(belt,regions.get(belt)+1);
            float roll=CityHash.unit(CityHash.of(worldSeed,site.centerX(),site.centerZ(),i));
            for(int f=0;f<3;f++){
                UrbanPattern52.Fabric fabric=UrbanPattern52.Fabric.values()[f];
                LotType selected=IndustrialBelts57.choose(worldSeed,site,fabric,roll);
                check(selected==IndustrialBelts57.choose(worldSeed,site,fabric,roll),"Nondeterministic lot");
                if (selected==LotType.FACTORY) check(site.width()>=20 && site.depth()>=20,"Unsafe tiny factory");
                if (selected==LotType.WAREHOUSE) check(site.width()>=13 && site.depth()>=13,"Unsafe tiny depot");
                if (selected==LotType.SHOP && site.width()<20) small++;
                count[f][selected.ordinal()]++;
                cases++;
            }
          }
        }
        for (int i=0;i<3;i++) {
          int factory=count[i][LotType.FACTORY.ordinal()], warehouse=count[i][LotType.WAREHOUSE.ordinal()];
          int salvage=count[i][LotType.JUNKYARD.ordinal()], shops=count[i][LotType.SHOP.ordinal()];
          int tenements=count[i][LotType.TENEMENT.ordinal()], empty=count[i][LotType.VACANT.ordinal()]+count[i][LotType.RUBBLE.ordinal()];
          check(factory>1000 && warehouse>1000 && salvage>1000 && shops>1000 && tenements>1000,"Industrial ecosystem missing a real land use "+i);
          check(empty>1000,"Needs some nonfunctional/broken spaces "+i);
          if(i>0) check(count[i][LotType.FACTORY.ordinal()]<count[0][LotType.FACTORY.ordinal()],"Sparse fabric contains too many plants");
          System.out.printf("  %s factories=%d depots=%d repair shops=%d salvage=%d housing=%d open=%d%n",UrbanPattern52.Fabric.values()[i],factory,warehouse,shops,salvage,tenements,empty);
        }
        check(small>1000,"Small parcels not getting small workshops");
        for(var entry:regions.entrySet()) check(entry.getValue()>10000,"Missing neighborhood belt: "+entry);
        boolean rejected=false;
        try { IndustrialBelts57.choose(1L,new Rect(0,0,22,22),UrbanPattern52.Fabric.OPEN_COUNTRY,0.5f); }
        catch(IllegalArgumentException expected){rejected=true;}
        check(rejected,"Open country must remain on original Minecraft policy");
        rejected=false;
        try { IndustrialBelts57.choose(1L,new Rect(0,0,22,22),UrbanPattern52.Fabric.DENSE,1.0f); }
        catch(IllegalArgumentException expected){rejected=true;}
        check(rejected,"Invalid RNG accepted");
        for(int x=-1600;x<1600;x+=40)for(int z=-1600;z<1600;z+=40){
            int cx=(Math.floorDiv(x,320)*320)+75, cz=(Math.floorDiv(z,320)*320)+88;
            if(IndustrialBelts57.belt(42L,cx,cz)==IndustrialBelts57.belt(42L,cx+80,cz+80))matched++;
            else throw new AssertionError("Belts not stable inside architecture neighborhood");
        }
        System.out.printf("PASS Pass57 actual Java industrial policy: %d occupied/ruined/dense parcel selections, %d small repair shops, all 4 belts and %d neighborhood cohesion checks%n",cases,small,matched);
    }
}
