package net.malisis.doors.block;

import java.util.ArrayList;

import net.malisis.core.renderer.IBaseRendering;
import net.malisis.doors.MalisisDoorsSettings;
import net.malisis.doors.entity.MixedBlockTileEntity;
import net.malisis.doors.item.MixedBlockBlockItem;
import net.malisis.doors.renderer.block.MixedBlockRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MixedBlock extends BlockContainer implements IBaseRendering
{
	private int renderType = -1;

	public MixedBlock()
	{
		super(Material.rock);
		setHardness(0.7F);
	}

	@Override
	public void registerBlockIcons(IIconRegister p_149651_1_)
	{}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack)
	{
		if (!(itemStack.getItem() instanceof MixedBlockBlockItem))
			return;

		int side = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		int metadata = 0;
		if (side == 0)
			metadata = 2;
		if (side == 1)
			metadata = 5;
		if (side == 2)
			metadata = 3;
		if (side == 3)
			metadata = 4;
		world.setBlockMetadataWithNotify(x, y, z, metadata, 2);

		MixedBlockTileEntity te = (MixedBlockTileEntity) world.getTileEntity(x, y, z);
		Block block1 = Block.getBlockById(itemStack.stackTagCompound.getInteger("block1"));
		Block block2 = Block.getBlockById(itemStack.stackTagCompound.getInteger("block2"));
		int metadata1 = itemStack.stackTagCompound.getInteger("metadata1");
		int metadata2 = itemStack.stackTagCompound.getInteger("metadata2");
		te.setBlocks(block1, metadata1, block2, metadata2);
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
		MixedBlockTileEntity te = (MixedBlockTileEntity) world.getTileEntity(x, y, z);
		return MixedBlockBlockItem.fromTileEntity(te);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean addHitEffects(World world, MovingObjectPosition target, EffectRenderer effectRenderer)
	{
		int x = target.blockX;
		int y = target.blockY;
		int z = target.blockZ;

		MixedBlockTileEntity te = (MixedBlockTileEntity) world.getTileEntity(x, y, z);
		if (te == null)
			return true;

		Block[] blocks = { te.block1, te.block2 };
		int[] metadata = { te.metadata1, te.metadata2 };

		float f = 0.1F;
		ForgeDirection side = ForgeDirection.getOrientation(target.sideHit);

		double fxX = x + world.rand.nextDouble();
		double fxY = y + world.rand.nextDouble();
		double fxZ = z + world.rand.nextDouble();

		switch (side)
		{
			case DOWN:
			case UP:
				fxY = y + side.offsetY * f;
				break;
			case NORTH:
			case SOUTH:
				fxZ = z + side.offsetZ * f;
				break;
			case EAST:
			case WEST:
				fxX = x + side.offsetX * f;
				break;
			default:
				break;
		}

		int i = world.rand.nextBoolean() ? 0 : 1;

		EntityDiggingFX fx = new EntityDiggingFX(world, fxX, fxY, fxZ, 0.0D, 0.0D, 0.0D, blocks[i], metadata[i]);
		fx.multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F);
		effectRenderer.addEffect(fx);

		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean addDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer effectRenderer)
	{
		byte nb = 4;
		EntityDiggingFX fx;

		MixedBlockTileEntity te = (MixedBlockTileEntity) world.getTileEntity(x, y, z);
		if (te == null)
			return true;

		Block[] blocks = { te.block1, te.block2 };
		int[] metadata = { te.metadata1, te.metadata2 };

		for (int i = 0; i < nb; ++i)
		{
			for (int j = 0; j < nb; ++j)
			{
				for (int k = 0; k < nb; ++k)
				{
					double fxX = x + (i + 0.5D) / nb;
					double fxY = y + (j + 0.5D) / nb;
					double fxZ = z + (k + 0.5D) / nb;
					int l = (i + j + k) % 2;
					fx = new EntityDiggingFX(world, fxX, fxY, fxZ, fxX - x - 0.5D, fxY - y - 0.5D, fxZ - z - 0.5D, blocks[l], metadata[l]);
					effectRenderer.addEffect(fx);
				}
			}
		}

		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata)
	{
		return new MixedBlockTileEntity();
	}

	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z)
	{
		if (!player.capabilities.isCreativeMode)
		{
			MixedBlockTileEntity te = (MixedBlockTileEntity) world.getTileEntity(x, y, z);
			dropBlockAsItem(world, x, y, z, MixedBlockBlockItem.fromTileEntity(te));
		}
		return super.removedByPlayer(world, player, x, y, z);
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
	{
		return new ArrayList<ItemStack>();
	}

	@Override
	public void setRenderId(int id)
	{
		renderType = id;
	}

	@Override
	public int getRenderType()
	{
		return renderType;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderBlockPass()
	{
		return MalisisDoorsSettings.simpleMixedBlockRendering.get() ? 0 : 1;
	}

	@Override
	public boolean canRenderInPass(int pass)
	{
		MixedBlockRenderer.setRenderPass(pass);
		return MalisisDoorsSettings.simpleMixedBlockRendering.get() ? pass == 0 : true;
	}
}
