package com.dorohedoro.hole.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.network.chat.Component;

/** Small accessible vanilla GUI widget with original Hole industrial styling. */
public final class HoleMenuButton extends AbstractButton {
    private final Runnable action;
    public HoleMenuButton(String text,int x,int y,int width,int height,Runnable action) {
        super(x,y,width,height,Component.literal(text));
        this.action=action;
    }
    @Override public void onPress() {if(active)action.run();}
    @Override protected void renderWidget(GuiGraphics g,int mx,int my,float partial) {
        boolean hot=isHoveredOrFocused();
        int x=getX(),y=getY(),w=getWidth(),h=getHeight();
        g.fill(x,y,x+w,y+h,hot?0xFF79C6B6:0xFF435B5C);
        g.fill(x+1,y+1,x+w-1,y+h-1,hot?0xFF344D50:0xFF26373B);
        if(hot)g.fill(x+2,y+2,x+5,y+h-2,0xFF7BE6D3);
        var f=Minecraft.getInstance().font;
        String label=HoleUiTheme.fit(f,getMessage().getString(),Math.max(4,w-11));
        g.drawCenteredString(f,label,x+w/2,y+(h-8)/2,active?HoleUiTheme.TEXT:HoleUiTheme.MUTED);
    }
}
