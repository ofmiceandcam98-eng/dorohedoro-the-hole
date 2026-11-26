package com.dorohedoro.hole.worldgen.city;
import net.minecraft.util.RandomSource;
final class LotChooser {
  static LotType choose(CityPlan p,District d,Rect r,RandomSource random){
    if(p.settings.worldDetailVersion()>=36 && UrbanPattern52.at(p.seed,r.centerX(),r.centerZ())==UrbanPattern52.Fabric.DENSE && d==District.OUTSKIRTS)
      return UrbanParcels53.denseFringeLot(random.nextFloat(),r);
    return LotType.TENEMENT;
  }
}
