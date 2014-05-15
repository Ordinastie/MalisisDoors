package net.malisis.doors.block;

import java.util.Random;

import net.malisis.doors.MalisisDoors;
import net.malisis.doors.MalisisItems;
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
		
		if((dir == DIR_NORTH || dir == DIR_SOUTH) && (side == 4 || side == 5))
			return iconSide;
		if((dir == DIR_EAST || dir == DIR_WEST) && (side == 2 || side == 3))
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
	public void playSound(World world, int x, int y, int z, int state)
	{
		if(state == stateOpening || state == stateClosing)
			world.playSoundEffect(x, y, z, MalisisDoors.modid + ":slidingdooro", 1F, 1F);
	}

	@Override
	public float[][] calculateBlockBounds(int metadata, boolean selBox)
	{
		float f = DOOR_WIDTH;
		int dir = metadata & 3;
		boolean topBlock = (metadata & flagTopBlock) != 0;
		boolean opened = (metadata & flagOpened) != 0;
		boolean reversed = (metadata & flagReversed) != 0;
		float left = -1 + f;
		float right = 1 - f;

		float x = 0;
		float y = 0;
		float z = 0;
		float X = 1;
		float Y = 1;
		float Z = 1;

		if (selBox)
		{
			y -= (topBlock ? 1 : 0);
			Y += (topBlock ? 0 : 1);
		}

		if (dir == DIR_NORTH)
		{
			z = 0.01F;
			Z = f;
			if (opened)
			{
				x += reversed ? left : right;
				X += reversed ? left : right;
			}
		}
		if (dir == DIR_SOUTH)
		{
			z = 1 - f;
			Z = 0.99F;
			if (opened)
			{
				x += reversed ? right : left;
				X += reversed ? right : left;
			}
		}
		if (dir == DIR_WEST)
		{
			x = 0.01F;
			X = f;
			if(opened)
			{
				z += reversed ? right : left;
				Z += reversed ? right : left;
			}
		}
		if (dir == DIR_EAST)
		{
			x = 1 - f;
			X = 0.99F;
			if(opened)
			{
				z += reversed ? left : right;
				Z += reversed ? left : right;
			}
		}

		return new float[][] { { x, y, z }, { X, Y, Z } };
	}

	@Override
	public Item getItemDropped(int metadata, Random par2Random, int par3)
	{
		if ((metadata & 8) != 0)
			return null;

		if (this.blockMaterial == Material.iron)
			return MalisisItems.ironSlidingDoorItem;
		else
			return MalisisItems.woodSlidingDoorItem;
	}

	@Override
	public Item getItem(World world, int x, int y, int z)
	{
		return this.blockMaterial == Material.iron ? MalisisItems.ironSlidingDoorItem : MalisisItems.woodSlidingDoorItem;
	}

}
