# Rebuilds src/main/resources/data/dorohedoro/worldgen/noise_settings/hole.json.
#
# The Hole keeps the vanilla 1.21.1 underground (caves, aquifers, ore veins, bedrock, deepslate) and swaps the
# terrain shape for a flat ground plane, with caves capped below the sewers. The fluid and vein functions are read
# from the local client jar so they always match the pinned Minecraft version.
param(
    [string]$ClientJar = "$env:USERPROFILE\.gradle\caches\neoformruntime\artifacts\minecraft_1.21.1_client.jar",
    [string]$Out = "src/main/resources/data/dorohedoro/worldgen/noise_settings/hole.json"
)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.IO.Compression.FileSystem

$zip = [IO.Compression.ZipFile]::OpenRead($ClientJar)
try {
    $entry = $zip.GetEntry('data/minecraft/worldgen/noise_settings/overworld.json')
    $reader = New-Object IO.StreamReader($entry.Open())
    $vanilla = $reader.ReadToEnd() | ConvertFrom-Json
    $reader.Close()
} finally {
    $zip.Dispose()
}

function Gradient([int]$fromY, [int]$toY, [double]$fromValue, [double]$toValue) {
    [ordered]@{ type = 'minecraft:y_clamped_gradient'; from_y = $fromY; to_y = $toY; from_value = $fromValue; to_value = $toValue }
}
function Op([string]$type, $a, $b) { [ordered]@{ type = "minecraft:$type"; argument1 = $a; argument2 = $b } }
function Noise([string]$name, [double]$xz, [double]$y) { [ordered]@{ type = 'minecraft:noise'; noise = "minecraft:$name"; xz_scale = $xz; y_scale = $y } }

# Mirrors NoiseRouterData.underground, without the surface-relative sloped cheese term.
$caveLayer = Op 'mul' 4.0 ([ordered]@{ type = 'minecraft:square'; argument = (Noise 'cave_layer' 1.0 8.0) })
$cheese = [ordered]@{ type = 'minecraft:clamp'; input = (Op 'add' 0.27 (Noise 'cave_cheese' 1.0 0.6666666666666666)); min = -1.0; max = 1.0 }
$caves = Op 'min' (Op 'min' (Op 'add' $caveLayer $cheese) 'minecraft:overworld/caves/entrances') (Op 'add' 'minecraft:overworld/caves/spaghetti_2d' 'minecraft:overworld/caves/spaghetti_roughness_function')
$pillars = [ordered]@{
    type = 'minecraft:range_choice'; input = 'minecraft:overworld/caves/pillars'
    min_inclusive = -1000000.0; max_exclusive = 0.03
    when_in_range = -1000000.0; when_out_of_range = 'minecraft:overworld/caves/pillars'
}
$underground = Op 'max' $caves $pillars

# Above y=26 caves fade out so nothing natural breaches the sewers. Near bedrock they fade out the same way.
$capTop = Gradient 26 44 -1.5 1.5
$capBottom = Gradient -64 -56 1.5 -1.5
$cappedCaves = Op 'max' (Op 'max' $underground $capTop) $capBottom
$ground = Gradient 58 66 2.0 -2.0
$solid = Op 'min' $ground $cappedCaves

$finalDensity = Op 'min' ([ordered]@{
    type = 'minecraft:squeeze'
    argument = (Op 'mul' 0.64 ([ordered]@{ type = 'minecraft:interpolated'; argument = [ordered]@{ type = 'minecraft:blend_density'; argument = $solid } }))
}) (Op 'max' (Op 'max' 'minecraft:overworld/caves/noodle' $capTop) $capBottom)

$bedrock = [ordered]@{
    type = 'minecraft:condition'
    if_true = [ordered]@{ type = 'minecraft:vertical_gradient'; random_name = 'minecraft:bedrock_floor'; true_at_and_below = @{ above_bottom = 0 }; false_at_and_above = @{ above_bottom = 5 } }
    then_run = [ordered]@{ type = 'minecraft:block'; result_state = @{ Name = 'minecraft:bedrock' } }
}
$deepslate = [ordered]@{
    type = 'minecraft:condition'
    if_true = [ordered]@{ type = 'minecraft:vertical_gradient'; random_name = 'minecraft:deepslate'; true_at_and_below = @{ absolute = 0 }; false_at_and_above = @{ absolute = 8 } }
    then_run = [ordered]@{ type = 'minecraft:block'; result_state = [ordered]@{ Name = 'minecraft:deepslate'; Properties = @{ axis = 'y' } } }
}

$router = [ordered]@{
    barrier = $vanilla.noise_router.barrier
    continents = 0.0
    depth = 0.0
    erosion = 0.0
    final_density = $finalDensity
    fluid_level_floodedness = $vanilla.noise_router.fluid_level_floodedness
    fluid_level_spread = $vanilla.noise_router.fluid_level_spread
    initial_density_without_jaggedness = (Gradient 58 66 1.0 -1.0)
    lava = $vanilla.noise_router.lava
    ridges = 0.0
    temperature = $vanilla.noise_router.temperature
    vegetation = $vanilla.noise_router.vegetation
    vein_gap = $vanilla.noise_router.vein_gap
    vein_ridged = $vanilla.noise_router.vein_ridged
    vein_toggle = $vanilla.noise_router.vein_toggle
}

$settings = [ordered]@{
    aquifers_enabled = $true
    default_block = $vanilla.default_block
    default_fluid = $vanilla.default_fluid
    disable_mob_generation = $false
    legacy_random_source = $false
    noise = $vanilla.noise
    noise_router = $router
    ore_veins_enabled = $true
    sea_level = 63
    spawn_target = $vanilla.spawn_target
    surface_rule = [ordered]@{ type = 'minecraft:sequence'; sequence = @($bedrock, $deepslate) }
}

$dir = Split-Path $Out -Parent
New-Item -ItemType Directory -Force $dir | Out-Null
$json = $settings | ConvertTo-Json -Depth 60
[IO.File]::WriteAllText((Join-Path (Get-Location) $Out), $json + "`n", (New-Object Text.UTF8Encoding($false)))
"wrote $Out ($($json.Length) chars)"
