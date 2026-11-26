# Run Gradle/NeoForge verification on Windows using Java 21.
# Requires internet access, and may modify src/generated/resources via runData.
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
Set-Location $root
$expected = '423cb469ccc0ecc31f0e4e1c309976198ccb734cdcbb7029d4bda0f18f57e8d9'
$wrapper = Join-Path $root 'gradle\wrapper\gradle-wrapper.jar'
$logs = Join-Path $root 'build\verification'
New-Item -ItemType Directory -Force -Path $logs | Out-Null

$javaOutput = (& cmd /c 'java -version 2>&1') -join "`n"
if ($LASTEXITCODE -ne 0 -or $javaOutput -notmatch 'version "21(?:[.\"]|$)') {
    throw 'Java 21 required. Install JDK 21 and configure JAVA_HOME.'
}
if (-not (Test-Path $wrapper)) {
    & (Join-Path $PSScriptRoot 'restore-official-gradle-wrapper.ps1')
}
$actual = (Get-FileHash -Algorithm SHA256 $wrapper).Hash.ToLowerInvariant()
if ($actual -ne $expected) { throw 'Gradle wrapper checksum does not match official version 9.2.1.' }
$props = Get-Content -Raw 'gradle\wrapper\gradle-wrapper.properties'
if (-not $props.Contains('distributionSha256Sum=72f44c9f8ebcb1af43838f45ee5c4aa9c5444898b3468ab3f4af7b6076c5bc3f')) {
    throw 'Gradle distribution checksum is missing or incorrect.'
}

$steps = @('help', 'runData', 'build')
foreach ($step in $steps) {
    Write-Host "Running NeoForge Gradle task: $step"
    & (Join-Path $root 'gradlew.bat') --no-daemon --stacktrace $step *>&1 | Tee-Object -FilePath (Join-Path $logs "$step.log")
    if ($LASTEXITCODE -ne 0) {
        throw "Gradle $step FAILED. Check build\verification\$step.log"
    }
}
Write-Host 'REAL GRADLE BUILD PASSED. In-game client/server testing still required.'
Get-ChildItem -Path 'build\libs' -Filter '*.jar' -ErrorAction SilentlyContinue | Select-Object FullName,Length
