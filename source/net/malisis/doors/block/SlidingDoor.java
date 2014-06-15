package net.malisis.doors.block;

import static net.malisis.doors.block.DoorHandler.*;

import java.util.Random;

import net.malisis.doors.MalisisDoors;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SlidingDoor extends Door
{

	public SlidingDoor(Material material)
	{
		super(material);
		float f = 0.5F;
		float f1 = 1.0F;
		this.setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f1, 0.5F + f);
		this.setHardness(2.0F);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(int side, int metadata)
	{
		// return icons[(metadata & flagTopBlock) >> 3];
		if (side == 1 || side == 0)
			return iconSide;

		int dir = metadata & 3;
		boolean topBlock = (metadata & flagTopBlock) != 0;

		if ((dir == DIR_NORTH || dir == DIR_SOUTH) && (side == 4 || side == 5))
			return iconSide;
		if ((dir == DIR_EAST || dir == DIR_WEST) && (side == 2 || side == 3))
			return iconSide;

		return topBlock ? iconTop[0] : iconBottom[0];
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister register)
	{
		iconTop = new IIcon[1];
		iconBottom = new IIcon[1];
		iconSide = register.registerIcon(MalisisDoors.modid + ":" + getTextureName() + "_side");
		iconTop[0] = register.registerIcon(MalisisDoors.modid + ":sliding_" + getTextureName() + "_upper");
		iconBottom[0] = register.registerIcon(MalisisDoors.modid + ":sliding_" + getTextureName() + "_lower");
	}

	@Override
	public Item getItemDropped(int metadata, Random par2Random, int par3)
	{
		if ((metadata & 8) != 0)
			return null;

		if (this.blockMaterial == Material.iron)
			return MalisisDoors.Items.ironSlidingDoorItem;
		else
			return MalisisDoors.Items.woodSlidingDoorItem;
	}

	@Override
	public Item getItem(World world, int x, int y, int z)
	{
		return this.blockMaterial == Material.iron ? MalisisDoors.Items.ironSlidingDoorItem : MalisisDoors.Items.woodSlidingDoorItem;
	}

}
