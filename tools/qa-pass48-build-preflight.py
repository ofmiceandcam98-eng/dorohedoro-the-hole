#!/usr/bin/env python3
"""Honest offline build-readiness preflight, not a Minecraft compiler.

Never automatically downloads or executes unknown binaries. The separate
verify-neoforge-build scripts download and hash-check the official wrapper.
"""
import hashlib
from pathlib import Path
import re
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]
EXPECTED_WRAPPER = "423cb469ccc0ecc31f0e4e1c309976198ccb734cdcbb7029d4bda0f18f57e8d9"
EXPECTED_DIST = "72f44c9f8ebcb1af43838f45ee5c4aa9c5444898b3468ab3f4af7b6076c5bc3f"
errors = []
notes = []

def check(ok, message):
    print(("PASS " if ok else "FAIL ") + message)
    if not ok:
        errors.append(message)

def get_java():
    try:
        p = subprocess.run(["java", "-version"], stdout=subprocess.PIPE,
                           stderr=subprocess.STDOUT, text=True, timeout=10)
    except (OSError, subprocess.TimeoutExpired):
        return ""
    return p.stdout if p.returncode == 0 else ""

props_path = ROOT / "gradle/wrapper/gradle-wrapper.properties"
props = props_path.read_text(encoding="utf8") if props_path.is_file() else ""
check("gradle-9.2.1-bin.zip" in props, "Gradle distribution pinned at 9.2.1")
check("distributionSha256Sum=" + EXPECTED_DIST in props, "Gradle distribution checksum pinned")
check((ROOT / "gradlew").is_file() and (ROOT / "gradlew.bat").is_file(), "Platform launchers present")
check((ROOT / "tools/verify-neoforge-build.ps1").is_file() and (ROOT / "tools/verify-neoforge-build.sh").is_file(), "Verified build scripts present")
check((ROOT / "BUILD_AND_TEST_WINDOWS.cmd").is_file(), "One-click Windows entrypoint present")
gradle_props = (ROOT / "gradle.properties").read_text(encoding="utf8")
check(re.search(r"(?m)^disable_recompilation=true\s*$", gradle_props) is not None, "Low-memory compilation pipeline selected by default")
check("minecraft_version=1.21.1" in gradle_props, "Minecraft 1.21.1 pinned")
check("neo_version=21.1." in gradle_props, "NeoForge 1.21.1 family pinned")
java_output = get_java()
check(re.search(r'version "21(?:\.|\")', java_output) is not None, "Java 21 available")
wrapper = ROOT / "gradle/wrapper/gradle-wrapper.jar"
if wrapper.is_file():
    digest = hashlib.sha256(wrapper.read_bytes()).hexdigest()
    check(digest == EXPECTED_WRAPPER, "Official Gradle wrapper SHA-256 verified")
else:
    notes.append("Wrapper JAR unavailable offline: network-enabled build script must retrieve and hash-check official wrapper")
    print("PENDING Gradle wrapper JAR is absent; official download required before compilation")

print(f"Preflight: {len(errors)} failed checks, {len(notes)} expected network prerequisite(s)")
print("This preflight does NOT run Gradle, NeoForge, runData, a game client or a dedicated server.")
sys.exit(1 if errors else 0)
