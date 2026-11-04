package com.dorohedoro.hole.client.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
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
        boolean hot=active&&isHoveredOrFocused();
        int x=getX(),y=getY(),w=getWidth(),h=getHeight();
        g.blitSprite(!active?HoleUiTheme.BUTTON_DISABLED:hot?HoleUiTheme.BUTTON_HOVER:HoleUiTheme.BUTTON,x,y,w,h);
        var f=Minecraft.getInstance().font;
        String label=HoleUiTheme.fit(f,getMessage().getString(),Math.max(4,w-10));
        int color=!active?0xFF6E7A78:hot?0xFFFFFFFF:HoleUiTheme.TEXT;
        g.drawString(f,label,x+(w-f.width(label))/2,y+(h-8)/2,color,true);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }
}
