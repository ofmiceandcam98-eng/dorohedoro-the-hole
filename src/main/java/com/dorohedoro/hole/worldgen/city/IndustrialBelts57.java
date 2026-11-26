package com.dorohedoro.hole.worldgen.city;

/**
 * Pass 57: industrial land use follows the independently seeded urban fabric,
 * instead of being overwritten by climate-themed settlement lotteries.
 *
 * The existing 320-block neighborhood identity supplies four recognizable
 * working belts: manufacturing, freight, salvage, and worker services. Each
 * contains complementary functions, never a monoculture of identical sheds.
 * Climate does not enter this selector, and the OPEN_COUNTRY policy remains
 * untouched so mining, exploration, rural fields and caves stay accessible.
 *
 * Pure seed/coordinates/parcel/roll: no chunk order, mutable RNG, or biome
 * lookups are involved. Invoked only by v40+ industrial lots.
 */
final class IndustrialBelts57 {
    enum Belt { MANUFACTURING, FREIGHT, RECLAMATION, WORKER_SERVICES }

    private IndustrialBelts57() {}

    static Belt belt(long seed, int x, int z) {
        // Reuse actual architectural neighborhood cells: adjacent buildings
        // already share this 320-block construction vocabulary.
        // Six inherited neighborhood palettes map into four work types. Salvage
        // and worker services each receive two palettes, avoiding an endless
        // factory belt and matching the Hole's mixed inhabited decay.
        return switch (CityArchitecture.neighborhood(seed, x, z)) {
            case 0 -> Belt.MANUFACTURING;
            case 1 -> Belt.FREIGHT;
            case 2, 4 -> Belt.RECLAMATION;
            default -> Belt.WORKER_SERVICES;
        };
    }

    private static LotType plant(Rect site) {
        // Small industrial lots become real staffed repair shops, not factories
        // without enough room for workstations and circulation.
        return site.width() >= 20 && site.depth() >= 20 ? LotType.FACTORY : LotType.SHOP;
    }

    private static LotType depot(Rect site) {
        return site.width() >= 13 && site.depth() >= 13 ? LotType.WAREHOUSE : LotType.SHOP;
    }

    static LotType choose(long seed, Rect site, UrbanPattern52.Fabric fabric, float roll) {
        if (fabric == UrbanPattern52.Fabric.OPEN_COUNTRY)
            throw new IllegalArgumentException("Open country keeps normal Minecraft exploration lots");
        if (!(roll >= 0.0f && roll < 1.0f))
            throw new IllegalArgumentException("roll must be in [0,1)");
        Belt belt = belt(seed, site.centerX(), site.centerZ());
        // Concentration decreases with the independently chosen urban fabric.
        // A ruined freight district might have a functioning depot, but mostly
        // salvage/reclaimed ground. Climate never forces a given settlement size.
        return switch (fabric) {
            case DENSE -> switch (belt) {
                case MANUFACTURING -> roll < .35f ? plant(site) : roll < .61f ? depot(site)
                        : roll < .78f ? LotType.SHOP : roll < .90f ? LotType.TENEMENT
                        : roll < .96f ? LotType.JUNKYARD : LotType.VACANT;
                case FREIGHT -> roll < .42f ? depot(site) : roll < .63f ? plant(site)
                        : roll < .78f ? LotType.SHOP : roll < .90f ? LotType.TENEMENT
                        : roll < .97f ? LotType.JUNKYARD : LotType.VACANT;
                case RECLAMATION -> roll < .36f ? LotType.JUNKYARD : roll < .58f ? depot(site)
                        : roll < .73f ? LotType.SHOP : roll < .84f ? plant(site)
                        : roll < .94f ? LotType.TENEMENT : LotType.RUBBLE;
                case WORKER_SERVICES -> roll < .31f ? LotType.TENEMENT : roll < .55f ? LotType.SHOP
                        : roll < .75f ? depot(site) : roll < .85f ? plant(site)
                        : roll < .92f ? LotType.CLINIC : LotType.COMMUNAL_COURT;
            };
            case INHABITED -> switch (belt) {
                case MANUFACTURING -> roll < .23f ? plant(site) : roll < .43f ? depot(site)
                        : roll < .60f ? LotType.SHOP : roll < .76f ? LotType.TENEMENT
                        : roll < .89f ? LotType.JUNKYARD : LotType.VACANT;
                case FREIGHT -> roll < .29f ? depot(site) : roll < .42f ? plant(site)
                        : roll < .61f ? LotType.SHOP : roll < .76f ? LotType.TENEMENT
                        : roll < .90f ? LotType.JUNKYARD : LotType.VACANT;
                case RECLAMATION -> roll < .25f ? LotType.JUNKYARD : roll < .44f ? depot(site)
                        : roll < .63f ? LotType.SHOP : roll < .76f ? LotType.TENEMENT
                        : roll < .87f ? LotType.RUBBLE : LotType.VACANT;
                case WORKER_SERVICES -> roll < .32f ? LotType.TENEMENT : roll < .58f ? LotType.SHOP
                        : roll < .70f ? depot(site) : roll < .80f ? LotType.CLINIC
                        : roll < .91f ? LotType.COMMUNAL_COURT : LotType.VACANT;
            };
            case DERELICT -> switch (belt) {
                case MANUFACTURING -> roll < .23f ? LotType.RUBBLE : roll < .44f ? LotType.JUNKYARD
                        : roll < .61f ? depot(site) : roll < .73f ? plant(site)
                        : roll < .84f ? LotType.SHOP : LotType.VACANT;
                case FREIGHT -> roll < .26f ? depot(site) : roll < .51f ? LotType.RUBBLE
                        : roll < .73f ? LotType.JUNKYARD : roll < .86f ? LotType.SHOP : LotType.VACANT;
                case RECLAMATION -> roll < .37f ? LotType.JUNKYARD : roll < .62f ? LotType.RUBBLE
                        : roll < .80f ? depot(site) : roll < .90f ? LotType.SHOP : LotType.VACANT;
                case WORKER_SERVICES -> roll < .22f ? LotType.TENEMENT : roll < .41f ? LotType.RUBBLE
                        : roll < .57f ? LotType.SHOP : roll < .70f ? depot(site)
                        : roll < .82f ? LotType.JUNKYARD : LotType.VACANT;
            };
            case OPEN_COUNTRY -> throw new IllegalArgumentException("Open country keeps existing policy");
        };
    }
}
