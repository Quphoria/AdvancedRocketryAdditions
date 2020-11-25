package uk.co.quphoria.advancedrocketryadditions.blocks;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

public abstract class GuiMachine extends GuiContainer {
	
	protected InventoryPlayer playerInv;
	protected ResourceLocation BG_TEXTURE;
	private TileEntityMachine tile;
	protected Rectangle energyBar = new Rectangle(0, 0, 0, 0);
	protected Rectangle progressBar = new Rectangle(0, 0, 0, 0);
	protected int energyBarTextureX = 0;
	protected int energyBarTextureY = 0;
	protected int progressBarTextureX = 0;
	protected int progressBarTextureY = 0;

	public GuiMachine(Container container, InventoryPlayer playerInv, TileEntityMachine tileEntity) {
		super(container);
		tile = tileEntity;
		this.playerInv = playerInv;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1, 1, 1, 1);
		mc.getTextureManager().bindTexture(BG_TEXTURE);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
		if(tile != null) {
			int energy_level = (int) Math.round(energyBar.width * tile.getEnergyFraction());
			drawTexturedModalRect(x+energyBar.x, y+energyBar.y, energyBarTextureX, energyBarTextureY, energy_level, energyBar.height);
			int progress_level = (int) Math.round(progressBar.width * tile.getProgressFraction());
			drawTexturedModalRect(x+progressBar.x, y+progressBar.y, progressBarTextureX, progressBarTextureY, progress_level, progressBar.height);
		}
	}
	
	@Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        mouseX -= guiLeft;
        mouseY -= guiTop;
        List<String> tooltip = new ArrayList<String>();
        drawTooltips(tooltip, mouseX, mouseY);
        if(!tooltip.isEmpty())
            drawHoveringText(tooltip, mouseX, mouseY);
    }
	
	protected String addDigitGrouping(int number)
    {
        String output = Integer.toString(number);
        for(int i = output.length() - 3; i > 0; i -= 3)
            output = output.substring(0, i) + "," + output.substring(i);
        return output;
    }

    protected void drawTooltips(List<String> tooltip, int mouseX, int mouseY)
    {
    	if (tile != null) {
	        if(energyBar.contains(mouseX, mouseY))
	        {
	    		tooltip.add("Energy: " + addDigitGrouping(tile.getEnergy()) + " RF");
	            tooltip.add("Max: " + addDigitGrouping(tile.getMaxEnergy()) + " RF");
	        }
	        if(progressBar.contains(mouseX, mouseY))
	        {
	    		tooltip.add("Progress: " + (int)Math.round(100*tile.getProgressFraction()) + "%");
	        }
    	}
    }
}
