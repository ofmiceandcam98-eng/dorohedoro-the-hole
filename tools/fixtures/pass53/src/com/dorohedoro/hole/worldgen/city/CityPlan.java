package com.dorohedoro.hole.worldgen.city;
/** Minimal city geometry fixture, not a Minecraft runtime CityPlan. */
final class CityPlan {
    static final int LANDMARK_REGION=8;
    final long seed;
    final Settings settings;
    CityPlan(long seed, int version){this.seed=seed;this.settings=new Settings(version);}
    record Settings(int worldDetailVersion) {}
    Line xLine(int i){return new Line(i*128, 5);}
    Line zLine(int j){return new Line(j*128, 5);}
    long blockHash(int i,int j){return CityHash.of(seed, i, j);}
    boolean isRegionalStartingBlock(int i,int j){return false;}
    District districtAt(int x,int z){return Math.floorMod(x/240+z/230,3)==0?District.OUTSKIRTS:District.RESIDENTIAL;}
    LandmarkSite landmarkSite(int i,int j){return new LandmarkSite(Integer.MAX_VALUE, Integer.MAX_VALUE, LotType.HOSPITAL);}
    record LandmarkSite(int blockI,int blockJ,LotType type){}
}
