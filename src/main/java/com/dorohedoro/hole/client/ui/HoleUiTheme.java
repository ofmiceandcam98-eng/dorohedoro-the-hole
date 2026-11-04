package com.dorohedoro.hole.client.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** Shared Dorohedoro UI palette and scale-independent, vanilla-compatible panels.
 * Uses Minecraft's own GUI text and widgets; no copied third-party assets. */
public final class HoleUiTheme {
    private HoleUiTheme() {}
    public static final int TEXT=0xFFF0E5D2, MUTED=0xFF9BAEAA, ACCENT=0xFF69CFC2,
        WARNING=0xFFF2A77C, BACK=0xFF151D21, PANEL=0xFF202A2E, LINE=0xFF506568;

    public static void backdrop(GuiGraphics g,int screenW,int screenH,int x,int y,int w,int h) {
        g.fill(0,0,screenW,screenH,0xB4080D10);
        g.fill(x-4,y-4,x+w+4,y+h+4,0xFF0A0E10);
        g.fill(x-2,y-2,x+w+2,y+h+2,LINE);
        g.fill(x,y,x+w,y+h,BACK);
        g.fill(x+2,y+2,x+w-2,y+32,PANEL);
        g.fill(x+2,y+32,x+w-2,y+34,0xFF446E6A);
        g.fill(x+10,y+8,x+14,y+25,ACCENT);
    }
    public static void section(GuiGraphics g,int x,int y,int w,int h) {
        g.fill(x,y,x+w,y+h,0xFF263136);
        g.fill(x,y,x+w,y+1,0xFF43575A);
        g.fill(x,y,x+1,y+h,0xFF43575A);
    }
    public static void meter(GuiGraphics g, Font font,String name,int value,int max,int x,int y,int w,int color) {
        int safeMax=Math.max(1,max);
        int safe=Math.clamp(value,0,safeMax);
        g.drawString(font,name,x,y,TEXT);
        String numbers=safe+" / "+safeMax;
        g.drawString(font,numbers,x+w-font.width(numbers),y,MUTED);
        g.fill(x,y+12,x+w,y+18,0xFF0B1417);
        if(safe>0)g.fill(x+1,y+13,x+1+(w-2)*safe/safeMax,y+17,color);
    }
    public static String fit(Font f,String text,int maxWidth) {
        return f.width(text)<=maxWidth?text:f.plainSubstrByWidth(text,Math.max(0,maxWidth-f.width("...")))+"...";
    }
}
