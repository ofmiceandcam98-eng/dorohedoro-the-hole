package com.dorohedoro.hole.worldgen.city;

/** What occupies a lot. */
public enum LotType {
    PLAZA(false),
    HUNGRY_BUG(true),
    STARTER_SHELTER(true),
    TENEMENT(true),
    APARTMENT(true),
    SHOP(true),
    CLINIC(true),
    GUARD_POST(true),
    WAREHOUSE(true),
    FACTORY(true),
    MARKET_STALLS(false),
    GREENHOUSE(true),
    LIVESTOCK_PEN(false),
    NURSERY(false),
    PARK(false),
    JUNKYARD(false),
    RUBBLE(false),
    VACANT(false),
    CEMETERY(false),
    HOSPITAL(true),
    DEPARTMENT_STORE(true),
    RESEARCH_MANOR(true),
    LAKE_OF_REFUSE(false);

    /** Whether the lot holds an enclosed structure with an interior. */
    public final boolean enclosed;

    LotType(boolean enclosed) {
        this.enclosed = enclosed;
    }
}
