package net.malisis.doors;

import net.malisis.doors.entity.BlockMixerContainer;
import net.malisis.doors.entity.BlockMixerTileEntity;
import net.malisis.doors.gui.BlockMixerGui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler
{
	// returns an instance of the Container you made earlier
	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity instanceof BlockMixerTileEntity)
		{
			return new BlockMixerContainer(player.inventory, (BlockMixerTileEntity) tileEntity);
		}
		return null;
	}

	// returns an instance of the Gui you made earlier
	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity instanceof BlockMixerTileEntity)
		{
			return new BlockMixerGui(player.inventory, (BlockMixerTileEntity) tileEntity);
		}
		return null;

	}
}
