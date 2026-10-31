# Rebuilds the noise settings for the Sorcerers' World and Hell from the pinned client jar.
#
# Sorcerers' World keeps vanilla's natural terrain, caves and aquifers, and blends it into flat basins where cities sit.
# The basin noise is also routed through the climate "depth" slot, which terrain does not read, so the biome source can
# find city land without any shared state.
#
# Hell is built from scratch and deliberately avoids the Nether's shape: open wastes under an open sky, tablelands,
# caverns, and huge curved horns. There is no lava sea and no ceiling.
param(
    [string]$ClientJar = "$env:USERPROFILE\.gradle\caches\neoformruntime\artifacts\minecraft_1.21.1_client.jar",
    [string]$OutDir = "src/main/resources/data/dorohedoro/worldgen/noise_settings"
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
function Noise([string]$name, [double]$xz, [double]$y) { [ordered]@{ type = 'minecraft:noise'; noise = $name; xz_scale = $xz; y_scale = $y } }
function Clamp($input, [double]$min, [double]$max) { [ordered]@{ type = 'minecraft:clamp'; input = $input; min = $min; max = $max } }
function Square($a) { [ordered]@{ type = 'minecraft:square'; argument = $a } }

$utf8 = New-Object Text.UTF8Encoding($false)
function Save($object, [string]$path) {
    $full = Join-Path (Get-Location) $path
    New-Item -ItemType Directory -Force (Split-Path $full -Parent) | Out-Null
    $json = $object | ConvertTo-Json -Depth 80
    [IO.File]::WriteAllText($full, $json + "`n", $utf8)
    "wrote $path ($($json.Length) chars)"
}

# Noise parameters. firstOctave sets feature size: -9 is roughly 512 blocks, -6 roughly 64.
Save ([ordered]@{ firstOctave = -9; amplitudes = @(1.0, 1.0, 0.5) }) 'src/main/resources/data/dorohedoro/worldgen/noise/city_basin.json'
Save ([ordered]@{ firstOctave = -6; amplitudes = @(1.0, 0.5) }) 'src/main/resources/data/dorohedoro/worldgen/noise/hell_horns.json'
Save ([ordered]@{ firstOctave = -9; amplitudes = @(1.0, 0.5) }) 'src/main/resources/data/dorohedoro/worldgen/noise/hell_regions.json'
Save ([ordered]@{ firstOctave = -7; amplitudes = @(1.0, 0.75, 0.5) }) 'src/main/resources/data/dorohedoro/worldgen/noise/hell_mesas.json'
Save ([ordered]@{ firstOctave = -5; amplitudes = @(1.0, 0.5, 0.25) }) 'src/main/resources/data/dorohedoro/worldgen/noise/hell_ground.json'

# Caves shared by city basins: vanilla's underground, capped so nothing natural opens under foundations.
$caveLayer = Op 'mul' 4.0 (Square (Noise 'minecraft:cave_layer' 1.0 8.0))
$cheese = Clamp (Op 'add' 0.27 (Noise 'minecraft:cave_cheese' 1.0 0.6666666666666666)) -1.0 1.0
$caves = Op 'min' (Op 'min' (Op 'add' $caveLayer $cheese) 'minecraft:overworld/caves/entrances') (Op 'add' 'minecraft:overworld/caves/spaghetti_2d' 'minecraft:overworld/caves/spaghetti_roughness_function')

# --- Sorcerers' World ---------------------------------------------------------------------------------------------
$basin = Noise 'dorohedoro:city_basin' 1.0 0.0
$mask = Clamp (Op 'mul' (Op 'add' $basin -0.30) 7.0) 0.0 1.0
$inverse = Op 'add' 1.0 (Op 'mul' -1.0 $mask)
$cityGround = Op 'min' (Gradient 68 76 2.0 -2.0) (Op 'max' (Op 'max' $caves (Gradient 30 48 -1.5 1.5)) (Gradient -64 -56 1.5 -1.5))

$natural = $vanilla.noise_router.final_density.argument1.argument.argument2.argument.argument
$blended = Op 'add' (Op 'mul' $mask $cityGround) (Op 'mul' $inverse $natural)

$finalDensity = $vanilla.noise_router.final_density | ConvertTo-Json -Depth 80 | ConvertFrom-Json
$finalDensity.argument1.argument.argument2.argument.argument = $blended

$router = [ordered]@{}
foreach ($name in $vanilla.noise_router.PSObject.Properties.Name) { $router[$name] = $vanilla.noise_router.$name }
$router['final_density'] = $finalDensity
$router['depth'] = $basin

Save ([ordered]@{
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
    surface_rule = $vanilla.surface_rule
}) "$OutDir/sorcerers_world.json"

# --- Hell ---------------------------------------------------------------------------------------------------------
# Column height comes from three layers: gently broken ground around y=70, flat-topped mesas, and horns.
$slope = Op 'add' (Gradient 30 270 1.0 -1.0) -0.667
$ground = Op 'mul' 0.05 (Noise 'dorohedoro:hell_ground' 1.0 0.0)
$mesa = Op 'mul' 0.30 (Clamp (Op 'mul' (Op 'add' (Noise 'dorohedoro:hell_mesas' 1.0 0.0) -0.32) 9.0) 0.0 1.0)

# Regions decide where horns stand. Everywhere else stays open waste.
$regions = Noise 'dorohedoro:hell_regions' 1.0 0.0
$hornField = Clamp (Op 'mul' (Op 'add' $regions 0.05) 6.0) 0.0 1.0

# Shifting the sample point with height bends a straight spike into a curved horn.
$hornNoise = [ordered]@{
    type = 'minecraft:shifted_noise'; noise = 'dorohedoro:hell_horns'
    shift_x = (Gradient 70 280 0.0 34.0); shift_y = 0.0; shift_z = (Gradient 90 280 0.0 -18.0)
    xz_scale = 1.0; y_scale = 0.0
}
$hornCore = Clamp (Op 'mul' (Op 'add' $hornNoise -0.40) 5.0) 0.0 1.0
$horn = Op 'mul' 1.50 (Op 'mul' $hornField (Square $hornCore))

$surface = Op 'add' $slope (Op 'add' $ground (Op 'add' $mesa $horn))

# Vast caverns below the wastes, closed off from bedrock and the surface.
$cavern = Op 'add' 0.22 (Noise 'minecraft:cave_cheese' 0.5 0.3)
$cavernCap = Op 'max' $cavern (Gradient 24 54 -1.5 1.5)
$cavernFloor = Op 'max' $cavernCap (Gradient -64 -50 1.5 -1.5)
$solid = Op 'min' $surface $cavernFloor

$hellRouter = [ordered]@{
    barrier = 0.0
    continents = 0.0
    depth = $regions
    erosion = 0.0
    final_density = [ordered]@{ type = 'minecraft:squeeze'; argument = (Op 'mul' 0.64 ([ordered]@{ type = 'minecraft:interpolated'; argument = $solid })) }
    fluid_level_floodedness = 0.0
    fluid_level_spread = 0.0
    initial_density_without_jaggedness = $slope
    lava = 0.0
    ridges = 0.0
    temperature = $vanilla.noise_router.temperature
    vegetation = $vanilla.noise_router.vegetation
    vein_gap = 0.0
    vein_ridged = 0.0
    vein_toggle = 0.0
}

$bedrock = [ordered]@{
    type = 'minecraft:condition'
    if_true = [ordered]@{ type = 'minecraft:vertical_gradient'; random_name = 'minecraft:bedrock_floor'; true_at_and_below = @{ above_bottom = 0 }; false_at_and_above = @{ above_bottom = 5 } }
    then_run = [ordered]@{ type = 'minecraft:block'; result_state = @{ Name = 'minecraft:bedrock' } }
}
$ash = [ordered]@{
    type = 'minecraft:condition'
    if_true = [ordered]@{ type = 'minecraft:stone_depth'; offset = 0; add_surface_depth = $true; secondary_depth_range = 0; surface_type = 'floor' }
    then_run = [ordered]@{ type = 'minecraft:block'; result_state = @{ Name = 'dorohedoro:ash_soil' } }
}

Save ([ordered]@{
    aquifers_enabled = $false
    default_block = @{ Name = 'dorohedoro:hell_rock' }
    default_fluid = @{ Name = 'minecraft:air' }
    disable_mob_generation = $true
    legacy_random_source = $false
    noise = $vanilla.noise
    noise_router = $hellRouter
    ore_veins_enabled = $false
    sea_level = -64
    spawn_target = @()
    surface_rule = [ordered]@{ type = 'minecraft:sequence'; sequence = @($bedrock, $ash) }
}) "$OutDir/hell.json"
