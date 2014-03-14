package net.malisis.doors.item;

import net.malisis.doors.MalisisBlocks;
import net.malisis.doors.MalisisDoors;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class SlidingDoorItem extends Item
{
	private Material material;

    public SlidingDoorItem(int par1, Material material)
    {
        super(par1);
        this.maxStackSize = 16;
        this.material = material;
        this.setCreativeTab(CreativeTabs.tabRedstone);
    }

	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister)
	{
		this.itemIcon = iconRegister.registerIcon(MalisisDoors.modid + ":" + (this.getUnlocalizedName().substring(5)));
	}


    /**
     * Callback for item usage. If the item does something special on right clicking, he will have one of those. Return
     * True if something happen and false if it don't. This is for ITEMS, not BLOCKS
     */
    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
    {
        if (par7 != 1)
        {
            return false;
        }
        else
        {
            ++y;

            Block block;
            if (this.material == Material.wood)
                block = MalisisBlocks.woodSlidingDoor;
            else
            	block = MalisisBlocks.ironSlidingDoor;



            if (player.canPlayerEdit(x, y, z, par7, itemStack) && player.canPlayerEdit(x, y + 1, z, par7, itemStack))
            {
                if (!block.canPlaceBlockAt(world, x, y, z))
                    return false;
                else
                {
                    int i1 = MathHelper.floor_double((double)((player.rotationYaw + 180.0F) * 4.0F / 360.0F) - 0.5D) & 3;
                    placeDoorBlock(world, x, y, z, i1, block);
                    --itemStack.stackSize;
                    return true;
                }
            }
            else
                return false;
        }
    }

    public void placeDoorBlock(World world, int x, int y, int z, int dir, Block block)
    {
        byte b0 = 0;
        byte b1 = 0;

        if (dir == 0)
            b1 = 1;

        if (dir == 1)
            b0 = -1;

        if (dir == 2)
            b1 = -1;

        if (dir == 3)
            b0 = 1;

        int i1 = (world.isBlockNormalCube(x - b0, y, z - b1) ? 1 : 0) + (world.isBlockNormalCube(x - b0, y + 1, z - b1) ? 1 : 0);
        int j1 = (world.isBlockNormalCube(x + b0, y, z + b1) ? 1 : 0) + (world.isBlockNormalCube(x + b0, y + 1, z + b1) ? 1 : 0);
        boolean flag = world.getBlockId(x - b0, y, z - b1) == block.blockID || world.getBlockId(x - b0, y + 1, z - b1) == block.blockID;
        boolean flag1 = world.getBlockId(x + b0, y, z + b1) == block.blockID || world.getBlockId(x + b0, y + 1, z + b1) == block.blockID;
        boolean flag2 = false;

        if (flag && !flag1)
        	flag2 = true;
        else if (j1 > i1)
            flag2 = true;

        world.setBlock(x, y, z, block.blockID, dir, 2);
        world.setBlock(x, y + 1, z, block.blockID, 8 | (flag2 ? 1 : 0), 2);
        world.notifyBlocksOfNeighborChange(x, y, z, block.blockID);
        world.notifyBlocksOfNeighborChange(x, y + 1, z, block.blockID);
    }

}
