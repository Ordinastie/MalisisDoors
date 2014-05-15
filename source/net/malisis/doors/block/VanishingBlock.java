package net.malisis.doors.block;

import java.util.List;
import java.util.Random;

import net.malisis.doors.MalisisDoors;
import net.malisis.doors.entity.VanishingTileEntity;
import net.malisis.doors.renderer.VanishingBlockRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class VanishingBlock extends BlockContainer
{
    public static final int typeWoodFrame = 0;
    public static final int typeIronFrame = 1;
    public static final int typeGoldFrame = 2;

    public static final int flagPowered = 1 << 2;
    public static final int flagInTransition = 1 << 3;

    private IIcon[] icons = new IIcon[3];

    public VanishingBlock()
    {
        super(Material.wood);
        setCreativeTab(CreativeTabs.tabRedstone);
        setHardness(0.5F);
    }

    // #region Icons
    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister register)
    {
        icons[typeWoodFrame] = register.registerIcon(MalisisDoors.modid + ":vanishing_block_wood");
        icons[typeIronFrame] = register.registerIcon(MalisisDoors.modid + ":vanishing_block_iron");
        icons[typeGoldFrame] = register.registerIcon(MalisisDoors.modid + ":vanishing_block_gold");
    }

    @Override
    public IIcon getIcon(int side, int frameType)
    {
        frameType &= 3;
        if(frameType == typeIronFrame)
            return icons[typeIronFrame];
        else if(frameType == typeGoldFrame)
            return icons[typeGoldFrame];
        else
            return icons[typeWoodFrame];
    }

    // #end Icons

    /**
     * Check if block at x, y, z is a powered VanishingBlock
     */
    public boolean isPowered(World world, int x, int y, int z)
    {
        return world.getBlock(x, y, z) == this && (world.getBlockMetadata(x, y, z) & flagPowered) != 0;
    }

    /**
     * Set the power state for the block at x, y, z
     */
    public void setPowerState(World world, int x, int y, int z, boolean powered)
    {
        if (world.getBlock(x, y, z) != this) // block is VanishingBlock ?
            return;
        if (isPowered(world, x, y, z) == powered) // same power state?
            return;

        VanishingTileEntity te = (VanishingTileEntity) world.getTileEntity(x, y, z);
        te.setPowerState(powered);

        if(powered)
            world.setBlockMetadataWithNotify(x, y, z, te.blockMetadata | flagPowered, 2);
        else
            world.setBlockMetadataWithNotify(x, y, z, te.blockMetadata & ~flagPowered, 2);

        world.scheduleBlockUpdate(x, y, z, this, 1);
    }

    /**
     * Check if the block is available for propagation of power state
     */
    public boolean shouldPropagate(World world, int x, int y, int z, VanishingTileEntity source)
    {
        if (world.getBlock(x, y, z) != this) // block is VanishingBlock ?
            return false;

        if ((source.getBlockMetadata() & 3) == typeWoodFrame)
            return true;

        VanishingTileEntity dest = (VanishingTileEntity) world.getTileEntity(x, y, z);
        if(source.copiedBlock == null || dest.copiedBlock == null)
            return true;

        if ((source.getBlockMetadata() & 3) == typeIronFrame && source.copiedBlock== dest.copiedBlock)
            return true;

        if ((source.getBlockMetadata() & 3) == typeGoldFrame && source.copiedBlock == dest.copiedBlock && source.copiedMetadata == dest.copiedMetadata)
            return true;

        return false;
    }

    /**
     * Propagate power state in all six direction
     */
    public void propagateState(World world, int x, int y, int z)
    {
        VanishingTileEntity te = (VanishingTileEntity) world.getTileEntity(x, y, z);
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
        {
            if (shouldPropagate(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, te))
                this.setPowerState(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, te.powered);
        }
    }

    //#region Events
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer p, int par6, float par7, float par8, float par9)
    {
//		if(world.isRemote)
//			return true;

        ItemStack is = p.getHeldItem();
        if (is == null)
            return false;
        Block block = Block.getBlockFromItem(is.getItem());
        if (!block.isNormalCube() || block == this)
            return false;

        VanishingTileEntity te = (VanishingTileEntity) world.getTileEntity(x, y, z);
        if (te.copiedBlock != null)
            return false;

        te.setBlock(block, is.getItemDamage());
        setHardness(block.getBlockHardness(world, x, y, z));
        setStepSound(block.stepSound);
        slipperiness = block.slipperiness;

        //if(!world.isRemote && !((EntityPlayerMP)p).theItemInWorldManager.isCreative())
        if(!p.capabilities.isCreativeMode)
            is.stackSize--;

        world.markBlockForUpdate(x, y, z);
        return true;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
    {
        if (world.isRemote)
            return;

        boolean powered = world.isBlockIndirectlyGettingPowered(x, y, z);
        if (powered || (block.canProvidePower() && block != this))
        {
            if (isPowered(world, x, y, z) != powered)
                world.playSoundEffect((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, MalisisDoors.modid + ":portal", 0.3F, 0.5F);
            this.setPowerState(world, x, y, z, powered);
        }
    }

    @Override
    public void updateTick(World world, int x, int y, int z, Random rand)
    {
        this.propagateState(world, x, y, z);
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int j)
    {
        VanishingTileEntity te = (VanishingTileEntity) world.getTileEntity(x, y, z);
        if(te.copiedBlock != null)
        	te.copiedBlock.dropBlockAsItem(world, x, y, z, te.copiedMetadata, 0);

         world.removeTileEntity(x, y, z);
    }
    //#end Events


    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
    {
        if ((world.getBlockMetadata(x, y, z) & (flagPowered | flagInTransition)) != 0)
            return null;
        else
            return super.getCollisionBoundingBoxFromPool(world, x, y, z);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
    {
        if ((world.getBlockMetadata(x, y, z) & (flagPowered | flagInTransition)) != 0)
            this.setBlockBounds(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        else
            this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list)
    {
        list.add(new ItemStack(item, 1, typeWoodFrame));
        list.add(new ItemStack(item, 1, typeIronFrame));
        list.add(new ItemStack(item, 1, typeGoldFrame));
    }

    @Override
    public int damageDropped(int metadata)
    {
        return metadata;
    }

    @Override
    public boolean isNormalCube()
    {
        return false;
    }

    public float getAmbientOcclusionLightValue(IBlockAccess world, int x, int y, int z)
    {
    	return (world.getBlockMetadata(x, y, z) & (flagPowered | flagInTransition)) == 0 ? 0.2F : 1.0F;
    }

    @Override
    public int getRenderType()
    {
        return VanishingBlockRenderer.renderId;
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    // @Override
    // public boolean isOpaqueCube(IBlockAccess world, int x, int y, int z)
    // {
    // return (world.getBlockMetadata(x, y, z) & flagPowered) == 0;
    // }
    //
    // @Override
    // public boolean renderAsNormalBlock(World world, int x, int y, int z)
    // {
    // return renderAsNormalBlock();
    // }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata)
    {
        return new VanishingTileEntity(metadata);
    }

}

