package com.dorohedoro.hole.client.magic;

import com.dorohedoro.hole.character.MagicType;
import com.dorohedoro.hole.client.ui.HoleMenuButton;
import com.dorohedoro.hole.client.ui.HoleUiTheme;
import com.dorohedoro.hole.client.ui.SmokeSkillIcon;
import com.dorohedoro.hole.magic.SmokeSkill;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

/**
 * The character record, opened with B. Four tabs across the top of a riveted iron window, each with one job, stated
 * under its title so nobody has to guess what a tab is for. Your live model stands in the left panel. Every action
 * here is a request; the server stays authoritative over techniques, loadouts and injuries.
 */
public final class SmokeLedgerScreen extends Screen {
    private enum Tab {
        STATUS("Status", "status", "Who you are in the Hole: origin, smoke, rank and condition.", false),
        TECHNIQUES("Techniques", "techniques", "Practice your family's smoke techniques and equip them to your keys.", true),
        LOADOUTS("Loadouts", "loadouts", "Nine quick-cast sets of three techniques. R cycles them, Shift+R goes back.", true),
        BODY("Body", "body", "Your wounds and severed limbs, and what will put you back together.", false);

        final String title, icon, reason;
        final boolean sorcererOnly;

        Tab(String title, String icon, String reason, boolean sorcererOnly) {
            this.title = title;
            this.icon = icon;
            this.reason = reason;
            this.sorcererOnly = sorcererOnly;
        }
    }

    private static Tab remembered = Tab.STATUS;
    private Tab tab = remembered;
    private int chosenSlot, skillPage;
    private int x, y, w, h;

    public SmokeLedgerScreen() {
        super(Component.literal("The Hole - Character Record"));
    }

    // ---------------------------------------------------------------- layout

    private int portraitX() { return x + 8; }
    private int portraitY() { return y + 28; }
    private int portraitW() { return 88; }
    private int portraitH() { return h - 36; }
    private int mainX() { return x + 100; }
    private int mainY() { return y + 28; }
    private int mainW() { return w - 108; }
    private int mainH() { return h - 36; }
    /** Lines the purpose text wraps to at the current width. */
    private int reasonLines() { return Math.max(1, font.split(Component.literal(tab.reason), mainW() - 12).size()); }
    private int dividerY() { return mainY() + 6 + reasonLines() * 9; }
    /** Content starts below the tab's purpose line and its divider. */
    private int contentY() { return dividerY() + 8; }

    private int tabX(int i) { return x + 10 + i * 31; }

    @Override
    protected void init() {
        w = Math.min(420, width - 16);
        h = Math.min(246, height - 44);
        x = (width - w) / 2;
        y = (height - h) / 2 + 14;
        rebuild();
    }

    private boolean sorcerer() {
        return !"none".equals(SmokeClient.innate) && MagicType.fromId(SmokeClient.innate) != null;
    }

    private void add(String label, int px, int py, int pw, int ph, Runnable action) {
        addRenderableWidget(new HoleMenuButton(label, px, py, pw, ph, action));
    }

    public void refresh() {
        if (minecraft != null) rebuild();
    }

    private void select(Tab next) {
        tab = next;
        remembered = next;
        rebuild();
    }

    private void rebuild() {
        if (minecraft == null) return;
        clearWidgets();
        add("CLOSE  [B]", portraitX() + 4, portraitY() + portraitH() - 20, portraitW() - 8, 16, this::onClose);
        if (tab.sorcererOnly && !sorcerer()) return;
        int cx = mainX() + 6, cw = mainW() - 12, cy = contentY();
        switch (tab) {
            case TECHNIQUES -> {
                SmokeSkill[] skills = familySkills();
                int perPage = Math.max(1, (mainY() + mainH() - 30 - cy) / 34);
                int pages = Math.max(1, (skills.length + perPage - 1) / perPage);
                skillPage = Mth.clamp(skillPage, 0, pages - 1);
                for (int i = skillPage * perPage; i < Math.min(skills.length, (skillPage + 1) * perPage); i++) {
                    SmokeSkill skill = skills[i];
                    int ry = cy + (i % perPage) * 34;
                    boolean learned = skill.rankRequired() == 1 || SmokeClient.study(skill.id()) >= 100;
                    if (learned) {
                        add("EQUIP " + SmokeClient.keyLabel(chosenSlot), cx + cw - 58, ry + 7, 58, 18,
                                () -> SmokeClient.request("equip", chosenSlot + ":" + skill.id()));
                    } else if (SmokeClient.rank >= skill.rankRequired()) {
                        add("PRACTICE", cx + cw - 58, ry + 7, 58, 18, () -> SmokeClient.request("train", skill.id()));
                    }
                }
                int fy = mainY() + mainH() - 24;
                add("<", cx, fy, 20, 18, () -> { skillPage = Math.max(0, skillPage - 1); rebuild(); });
                add(">", cx + 24, fy, 20, 18, () -> { skillPage = Math.min(pages - 1, skillPage + 1); rebuild(); });
                for (int i = 0; i < 3; i++) {
                    int slot = i;
                    String key = SmokeClient.keyLabel(i);
                    add(chosenSlot == i ? "[" + key + "]" : key, cx + cw - 96 + i * 33, fy, 30, 18,
                            () -> { chosenSlot = slot; rebuild(); });
                }
            }
            case LOADOUTS -> {
                int gap = 3, bw = (cw - 8 * gap) / 9;
                for (int i = 0; i < 9; i++) {
                    int index = i;
                    add(SmokeClient.preset == i ? "[" + (i + 1) + "]" : Integer.toString(i + 1), cx + i * (bw + gap), cy,
                            bw, 18, () -> SmokeClient.request("preset", Integer.toString(index)));
                }
                for (int i = 0; i < 3; i++) {
                    int slot = i, ry = cy + 30 + i * 32;
                    add("CHOOSE", cx + cw - 110, ry + 5, 52, 18, () -> { chosenSlot = slot; select(Tab.TECHNIQUES); });
                    add("CLEAR", cx + cw - 54, ry + 5, 54, 18, () -> SmokeClient.request("equip", slot + ":-"));
                }
            }
            default -> {
            }
        }
    }

    private SmokeSkill[] familySkills() {
        MagicType family = MagicType.fromId(SmokeClient.innate);
        return Arrays.stream(SmokeSkill.values()).filter(s -> s.magic() == family).toArray(SmokeSkill[]::new);
    }

    // ---------------------------------------------------------------- input

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        for (Tab t : Tab.values()) {
            int tx = tabX(t.ordinal());
            if (mx >= tx && mx < tx + 28 && my >= y - 28 && my < y) {
                if (t != tab) {
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    select(t);
                }
                return true;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        if (key == GLFW.GLFW_KEY_B) {
            onClose();
            return true;
        }
        if (key >= GLFW.GLFW_KEY_1 && key <= GLFW.GLFW_KEY_4) {
            select(Tab.values()[key - GLFW.GLFW_KEY_1]);
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    // ---------------------------------------------------------------- drawing

    /** All frame geometry goes here, so vanilla's background blur never smears the window. */
    @Override
    public void renderBackground(GuiGraphics g, int mx, int my, float partial) {
        g.fill(0, 0, width, height, 0xB4080D10);
        for (Tab t : Tab.values()) {
            if (t != tab) g.blitSprite(HoleUiTheme.TAB, tabX(t.ordinal()), y - 26, 28, 25);
        }
        g.blitSprite(HoleUiTheme.WINDOW, x - 4, y - 4, w + 8, h + 8);
        g.blitSprite(HoleUiTheme.TAB_SELECTED, tabX(tab.ordinal()), y - 28, 28, 28);
        for (Tab t : Tab.values()) {
            int ix = tabX(t.ordinal()) + 6, iy = t == tab ? y - 22 : y - 21;
            g.blitSprite(HoleUiTheme.sprite("menu/icon_" + t.icon), ix, iy, 16, 16);
            if (t.sorcererOnly && !sorcerer()) g.fill(ix, iy, ix + 16, iy + 16, 0x90101416);
        }
        g.blitSprite(HoleUiTheme.TITLE_PLATE, x + 6, y + 5, w - 12, 20);
        HoleUiTheme.section(g, portraitX(), portraitY(), portraitW(), portraitH());
        HoleUiTheme.section(g, mainX(), mainY(), mainW(), mainH());
        HoleUiTheme.divider(g, mainX() + 6, dividerY(), mainW() - 12);
        if (!(tab.sorcererOnly && !sorcerer())) tabGeometry(g);
    }

    /** Panels, sockets and icons for the current tab. Text is drawn later, over the widgets. */
    private void tabGeometry(GuiGraphics g) {
        int cx = mainX() + 6, cw = mainW() - 12, cy = contentY();
        if (tab == Tab.TECHNIQUES) {
            SmokeSkill[] skills = familySkills();
            int perPage = Math.max(1, (mainY() + mainH() - 30 - cy) / 34);
            for (int i = skillPage * perPage; i < Math.min(skills.length, (skillPage + 1) * perPage); i++) {
                int ry = cy + (i % perPage) * 34;
                HoleUiTheme.section(g, cx, ry, cw, 31);
                g.blitSprite(HoleUiTheme.sprite("hud/slot"), cx + 3, ry + 3, 25, 25);
                SmokeSkillIcon.draw(g, skills[i], cx + 6, ry + 6, 19);
            }
        } else if (tab == Tab.LOADOUTS) {
            for (int i = 0; i < 3; i++) {
                int ry = cy + 30 + i * 32;
                HoleUiTheme.section(g, cx, ry, cw, 29);
                g.blitSprite(HoleUiTheme.sprite(SmokeClient.focused(i) ? "hud/slot_focused" : "hud/slot"), cx + 2, ry + 2, 25, 25);
                SmokeSkill s = SmokeSkill.fromId(SmokeClient.slot(i));
                if (s != null) SmokeSkillIcon.draw(g, s, cx + 5, ry + 5, 19);
                g.blitSprite(HoleUiTheme.sprite("hud/key_plate"), cx + 31, ry + 8, 12, 12);
            }
        }
    }

    @Override
    public void render(GuiGraphics g, int mx, int my, float partial) {
        super.render(g, mx, my, partial);
        // Title plate: the tab name and where you are.
        g.drawString(font, tab.title.toUpperCase(Locale.ROOT), x + 14, y + 11, HoleUiTheme.ACCENT, true);
        String where = "THE HOLE  /  CHARACTER RECORD";
        g.drawString(font, where, x + w - 14 - font.width(where), y + 11, HoleUiTheme.MUTED, true);
        // The purpose of this tab, always visible.
        int cx = mainX() + 6, cw = mainW() - 12;
        g.drawWordWrap(font, Component.literal(tab.reason), cx, mainY() + 5, cw, HoleUiTheme.MUTED);

        portrait(g, mx, my);
        if (tab.sorcererOnly && !sorcerer()) {
            g.drawWordWrap(font, Component.literal("Your origin is Human. Smoke techniques only awaken in sorcerers, "
                    + "so there is nothing to " + (tab == Tab.TECHNIQUES ? "practice" : "equip") + " here."),
                    cx, contentY() + 4, cw, HoleUiTheme.TEXT);
        } else {
            switch (tab) {
                case STATUS -> status(g, cx, cw);
                case TECHNIQUES -> techniques(g, cx, cw);
                case LOADOUTS -> loadouts(g, cx, cw);
                case BODY -> body(g, cx, cw);
            }
        }
        tabTooltip(g, mx, my);
    }

    private void portrait(GuiGraphics g, int mx, int my) {
        if (minecraft == null || minecraft.player == null) return;
        int px = portraitX(), py = portraitY(), pw = portraitW(), ph = portraitH();
        InventoryScreen.renderEntityInInventoryFollowsMouse(g, px + 4, py + 4, px + pw - 4, py + ph - 50, 46, 0.0625f,
                mx, my, minecraft.player);
        String name = HoleUiTheme.fit(font, minecraft.player.getGameProfile().getName(), pw - 8);
        g.drawString(font, name, px + (pw - font.width(name)) / 2, py + ph - 44, HoleUiTheme.TEXT, true);
        if (sorcerer()) {
            String familyName = MagicType.fromId(SmokeClient.innate).display();
            // "Dismemberment smoke" does not fit the panel; the family name alone always does.
            String family = font.width(familyName + " smoke") <= pw - 8 ? familyName + " smoke" : HoleUiTheme.fit(font, familyName, pw - 8);
            g.drawString(font, family, px + (pw - font.width(family)) / 2, py + ph - 33, HoleUiTheme.ACCENT, true);
        } else {
            g.drawString(font, "Human", px + (pw - font.width("Human")) / 2, py + ph - 33, HoleUiTheme.MUTED, true);
        }
    }

    private void row(GuiGraphics g, String label, String value, int cx, int ry, int cw, int color) {
        HoleUiTheme.section(g, cx, ry, cw, 16);
        g.drawString(font, label, cx + 5, ry + 4, HoleUiTheme.MUTED, true);
        String v = HoleUiTheme.fit(font, value, cw - 16 - font.width(label));
        g.drawString(font, v, cx + cw - 5 - font.width(v), ry + 4, color, true);
    }

    private void status(GuiGraphics g, int cx, int cw) {
        var player = minecraft.player;
        int ry = contentY();
        if (sorcerer()) {
            MagicType family = MagicType.fromId(SmokeClient.innate);
            row(g, "Origin", "Sorcerer, " + family.display() + " smoke", cx, ry, cw, HoleUiTheme.ACCENT);
            ry += 19;
            HoleUiTheme.meter(g, font, "Smoke reserve", SmokeClient.smoke, SmokeClient.maxSmoke, cx + 2, ry, cw - 4, 0xFF55B0A1);
            ry += 24;
            HoleUiTheme.meter(g, font, "Rank " + SmokeClient.rank + " progress", SmokeClient.xp,
                    Math.max(100, SmokeClient.rank * 100), cx + 2, ry, cw - 4, HoleUiTheme.BRASS);
            ry += 25;
        } else {
            row(g, "Origin", "Human", cx, ry, cw, HoleUiTheme.TEXT);
            ry += 19;
        }
        if (player != null) {
            int half = (cw - 3) / 2;
            row(g, "Health", Mth.ceil(player.getHealth()) + " / " + Mth.ceil(player.getMaxHealth()), cx, ry, half, 0xFFFF8F7A);
            row(g, "Hunger", player.getFoodData().getFoodLevel() + " / 20", cx + half + 3, ry, cw - half - 3, 0xFFF2BD62);
            ry += 19;
            row(g, "Armor", Integer.toString(player.getArmorValue()), cx, ry, half, HoleUiTheme.TEXT);
            row(g, "Level", Integer.toString(player.experienceLevel), cx + half + 3, ry, cw - half - 3, 0xFFB9EA58);
            ry += 19;
        }
        if (sorcerer()) {
            StringBuilder set = new StringBuilder("Set " + (SmokeClient.preset + 1) + ":");
            for (int i = 0; i < 3; i++) {
                SmokeSkill s = SmokeSkill.fromId(SmokeClient.slot(i));
                set.append("  ").append(SmokeClient.keyLabel(i)).append(" ").append(s == null ? "-" : s.display());
            }
            row(g, "Loadout", set.toString(), cx, ry, cw, HoleUiTheme.TEXT);
            ry += 19;
        }
        boolean hurt = !"none".equals(SmokeClient.injury);
        row(g, "Condition", hurt ? SmokeClient.injury : "Whole", cx, ry, cw, hurt ? HoleUiTheme.WARNING : HoleUiTheme.ACCENT);
    }

    private void techniques(GuiGraphics g, int cx, int cw) {
        SmokeSkill[] skills = familySkills();
        int cy = contentY();
        int perPage = Math.max(1, (mainY() + mainH() - 30 - cy) / 34);
        int pages = Math.max(1, (skills.length + perPage - 1) / perPage);
        for (int i = skillPage * perPage; i < Math.min(skills.length, (skillPage + 1) * perPage); i++) {
            SmokeSkill s = skills[i];
            int ry = cy + (i % perPage) * 34;
            boolean learned = s.rankRequired() == 1 || SmokeClient.study(s.id()) >= 100;
            boolean locked = SmokeClient.rank < s.rankRequired();
            int textW = cw - 100;
            g.drawString(font, HoleUiTheme.fit(font, s.display(), textW), cx + 32, ry + 4,
                    locked ? HoleUiTheme.MUTED : HoleUiTheme.TEXT, true);
            String info = locked ? "Unlocks at rank " + s.rankRequired()
                    : (learned ? "Mastery " + SmokeClient.mastery(s.id()) + "/1000" : "Study " + SmokeClient.study(s.id()) + "/100")
                    + "  |  " + s.smokeCost() + " smoke";
            g.drawString(font, HoleUiTheme.fit(font, info, textW), cx + 32, ry + 15, locked ? 0xFF6E7A78 : HoleUiTheme.MUTED, true);
            int bx = cx + 32, by = ry + 25, bw = Math.min(textW, 120);
            float progress = learned ? SmokeClient.mastery(s.id()) / 1000f : SmokeClient.study(s.id()) / 100f;
            g.fill(bx, by, bx + bw, by + 2, 0xFF0B1417);
            g.fill(bx, by, bx + Math.round(bw * Mth.clamp(progress, 0f, 1f)), by + 2, learned ? HoleUiTheme.ACCENT : HoleUiTheme.BRASS);
        }
        String page = (skillPage + 1) + " / " + pages;
        g.drawString(font, page, cx + 50, mainY() + mainH() - 19, HoleUiTheme.MUTED, true);
        g.drawString(font, "Equip to:", cx + cw - 96 - font.width("Equip to:") - 4, mainY() + mainH() - 19, HoleUiTheme.MUTED, true);
    }

    private void loadouts(GuiGraphics g, int cx, int cw) {
        int cy = contentY();
        for (int i = 0; i < 3; i++) {
            int ry = cy + 30 + i * 32;
            SmokeSkill s = SmokeSkill.fromId(SmokeClient.slot(i));
            String key = SmokeClient.keyLabel(i);
            g.drawString(font, key, cx + 37 - font.width(key) / 2, ry + 10, HoleUiTheme.TEXT, true);
            String label = s == null ? "Empty" : s.display();
            g.drawString(font, HoleUiTheme.fit(font, label, cw - 170), cx + 48, ry + 10,
                    s == null ? HoleUiTheme.MUTED : HoleUiTheme.TEXT, true);
        }
        g.drawWordWrap(font, Component.literal("Alt + a technique key toggles its focused form."), cx, cy + 128, cw,
                HoleUiTheme.MUTED);
    }

    private void body(GuiGraphics g, int cx, int cw) {
        String injury = SmokeClient.injury == null ? "none" : SmokeClient.injury;
        String lower = injury.toLowerCase(Locale.ROOT);
        boolean missing = lower.startsWith("missing");
        int cy = contentY();
        // A plain body chart: teal is whole, amber is wounded, an empty red outline is a severed limb.
        int bx = cx + 8, by = cy + 4;
        limb(g, bx + 8, by, 16, 16, part(lower, missing, "head"));
        limb(g, bx + 8, by + 17, 16, 24, part(lower, missing, "body"));
        limb(g, bx, by + 17, 7, 24, part(lower, missing, "left arm"));
        limb(g, bx + 25, by + 17, 7, 24, part(lower, missing, "right arm"));
        limb(g, bx + 8, by + 42, 7, 26, part(lower, missing, "left leg"));
        limb(g, bx + 17, by + 42, 7, 26, part(lower, missing, "right leg"));

        int tx = cx + 50, tw = cw - 50, ty = cy;
        boolean hurt = !"none".equals(injury);
        g.drawString(font, "Diagnosis", tx, ty, HoleUiTheme.ACCENT, true);
        ty += 11;
        g.drawWordWrap(font, Component.literal(hurt ? injury : "Whole. No wounds or missing limbs."), tx, ty, tw,
                hurt ? HoleUiTheme.WARNING : HoleUiTheme.TEXT);
        ty += hurt ? 22 : 13;
        HoleUiTheme.divider(g, tx, ty, tw);
        ty += 7;
        g.drawString(font, "Treatment", tx, ty, HoleUiTheme.ACCENT, true);
        ty += 11;
        List<String> lines = List.of(
                "Timed wounds close on their own.",
                "Surgical Reattachment Kit: another player sews your own recovered limb back on.",
                "Healing smoke: Mend Flesh treats wounds at 600 mastery and regrows limbs at 800.",
                "Antidote Poultice: cures poison.");
        for (String line : lines) {
            g.drawWordWrap(font, Component.literal("- " + line), tx, ty, tw, HoleUiTheme.TEXT);
            ty += font.split(Component.literal("- " + line), tw).size() * 9 + 3;
        }
    }

    /** 0 whole, 1 wounded, 2 severed. */
    private static int part(String injury, boolean missing, String part) {
        if ("none".equals(injury)) return 0;
        if (missing) return injury.contains(part) ? 2 : 0;
        // Timed wounds name a zone ("arm (12s)") without a side, so the whole zone is marked.
        String zone = part.endsWith("arm") ? "arm" : part.endsWith("leg") ? "leg" : part;
        return injury.startsWith(zone) ? 1 : 0;
    }

    private static void limb(GuiGraphics g, int x, int y, int w, int h, int state) {
        if (state == 2) {
            g.fill(x, y, x + w, y + h, 0x60200808);
            g.renderOutline(x, y, w, h, 0xFFE2473C);
            return;
        }
        int body = state == 1 ? 0xFFD48A2F : 0xFF3F7F76;
        g.fill(x, y, x + w, y + h, 0xFF0B0D0E);
        g.fill(x + 1, y + 1, x + w - 1, y + h - 1, body);
        g.fill(x + 1, y + 1, x + w - 1, y + 2, state == 1 ? 0xFFF2BD62 : 0xFF6FC1B2);
    }

    private void tabTooltip(GuiGraphics g, int mx, int my) {
        for (Tab t : Tab.values()) {
            int tx = tabX(t.ordinal());
            if (mx >= tx && mx < tx + 28 && my >= y - 28 && my < y) {
                String extra = t.sorcererOnly && !sorcerer() ? " (sorcerers only)" : "";
                g.renderComponentTooltip(font, List.of(Component.literal(t.title + extra),
                        Component.literal(t.reason).withStyle(s -> s.withColor(0x9BAEAA))), mx, my);
            }
        }
    }
}
