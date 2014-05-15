package net.malisis.doors.gui;

import net.malisis.doors.MalisisDoors;
import net.malisis.doors.entity.BlockMixerContainer;
import net.malisis.doors.entity.BlockMixerTileEntity;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

public class BlockMixerGui extends GuiContainer
{
	ResourceLocation texture = new ResourceLocation(MalisisDoors.modid, "textures/gui/block_mixer.png");
	BlockMixerTileEntity te;
	
	public BlockMixerGui(InventoryPlayer inventoryPlayer, BlockMixerTileEntity tileEntity)
	{
		super(new BlockMixerContainer(inventoryPlayer, tileEntity));
		te = tileEntity;
//		xSize = 156;
//		ySize = 62;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int param1, int param2)
	{
		fontRendererObj.drawString(StatCollector.translateToLocal("tile.block_mixer.name"), 8, 6, 4210752);
		fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, ySize - 96 + 2, 4210752);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int mouseX, int mouseY)
	{
		ResourceLocation texture = new ResourceLocation(MalisisDoors.modid, "textures/gui/block_mixer.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(texture);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
		
		float timer = te.getMixTimer();
        this.drawTexturedModalRect(x + 46, y + 35, 176, 14, (int) (timer * 24) + 1, 16);
        timer = 1 - timer;
        this.drawTexturedModalRect(x + 107 + (int) (timer * 24), y + 35, 176 + (int) (timer * 24), 31, 24 - (int) (timer * 24) - 1, 16);
	}

}