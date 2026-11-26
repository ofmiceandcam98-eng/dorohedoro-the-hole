#!/usr/bin/env python3
"""Compile policy with minimal copies of Minecraft-dependent enums, then run the real policy."""
from pathlib import Path
import tempfile, subprocess, shutil
root=Path(__file__).resolve().parents[1]
city=root/'src/main/java/com/dorohedoro/hole/worldgen/city'
with tempfile.TemporaryDirectory() as temp:
    out=Path(temp); pkg=out/'com/dorohedoro/hole/worldgen/city';pkg.mkdir(parents=True)
    (pkg/'District.java').write_text('package com.dorohedoro.hole.worldgen.city; public enum District {RESIDENTIAL,MARKET,INDUSTRIAL,WATERFRONT,DERELICT,GRAVEYARD,OUTSKIRTS;}')
    (pkg/'LotType.java').write_text('package com.dorohedoro.hole.worldgen.city; public enum LotType {PARK,VACANT,RUBBLE;}')
    files=[pkg/'District.java',pkg/'LotType.java',city/'CityHash.java',city/'HoleTreePolicy.java',root/'tools/VerifyHoleTreePolicy16.java']
    subprocess.run(['javac','-d',str(out),*[str(f) for f in files]],check=True)
    subprocess.run(['java','-cp',str(out),'com.dorohedoro.hole.worldgen.city.VerifyHoleTreePolicy16'],check=True)
