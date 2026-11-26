# Restore Gradle 9.2.1 wrapper JAR from the official Gradle distribution service.
# No third-party mirrors, and NEVER execute the JAR without checking SHA-256.
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$destination = Join-Path $root 'gradle/wrapper/gradle-wrapper.jar'
$expected = '423cb469ccc0ecc31f0e4e1c309976198ccb734cdcbb7029d4bda0f18f57e8d9'
$url = 'https://services.gradle.org/distributions/gradle-9.2.1-wrapper.jar'
$temp = "$destination.download"
try {
    Invoke-WebRequest -Uri $url -OutFile $temp -MaximumRedirection 5
    $actual = (Get-FileHash -Algorithm SHA256 -Path $temp).Hash.ToLowerInvariant()
    if ($actual -ne $expected) {
        throw "Gradle wrapper SHA-256 MISMATCH! Expected $expected; got $actual. No unverified JAR installed."
    }
    Move-Item -Force $temp $destination
    Write-Host 'Official Gradle 9.2.1 wrapper verified and restored.'
    Write-Host 'Next: run .\\gradlew.bat --no-daemon help, runData, and build sequentially with Java 21.'
} finally {
    if (Test-Path $temp) { Remove-Item -Force $temp }
}
