/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Ordinastie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.malisis.doors.door.block;

import net.malisis.core.MalisisCore;
import net.malisis.core.block.IRegisterable;
import net.malisis.core.renderer.MalisisRendered;
import net.malisis.core.renderer.icon.IIconProvider;
import net.malisis.core.renderer.icon.IMetaIconProvider;
import net.malisis.core.renderer.icon.VanillaIcon;
import net.malisis.core.renderer.icon.provider.DefaultIconProvider;
import net.malisis.core.util.EntityUtils;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.door.iconprovider.CamoFenceGateIconProvider;
import net.malisis.doors.door.renderer.FenceGateRenderer;
import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.malisis.doors.door.tileentity.FenceGateTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockPlanks.EnumType;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 *
 */
@MalisisRendered(FenceGateRenderer.class)
public class FenceGate extends BlockFenceGate implements ITileEntityProvider, IMetaIconProvider, IRegisterable
{
	public static enum Type
	{
		//@formatter:off
		OAK("fenceGate", EnumType.OAK),
		ACACIA("acaciaFenceGate", EnumType.ACACIA),
		BIRCH("birchFenceGate", EnumType.BIRCH),
		DARK_OAK("darkOakFenceGate", EnumType.DARK_OAK),
		JUNGLE("jungleFenceGate", EnumType.JUNGLE),
		SPRUCE("spruceFenceGate", EnumType.SPRUCE),
		CAMO("camoFenceGate", null);
		//@formatter:on
		private EnumType type;
		private String name;

		private Type(String name, EnumType type)
		{
			this.name = name;
			this.type = type;
		}
	}

	private Type type;
	@SideOnly(Side.CLIENT)
	private IIconProvider iconProvider;

	public FenceGate(Type type)
	{
		this.type = type;
		setHardness(2.0F);
		setResistance(5.0F);
		setStepSound(soundTypeWood);
		setUnlocalizedName(type.name);

		if (type == Type.CAMO)
			setCreativeTab(MalisisDoors.tab);
	}

	@Override
	public String getRegistryName()
	{
		return type.name;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void createIconProvider(Object object)
	{
		if (type == Type.CAMO)
			iconProvider = new CamoFenceGateIconProvider();
		else
			iconProvider = new DefaultIconProvider(new VanillaIcon(Blocks.planks.getDefaultState().withProperty(BlockPlanks.VARIANT,
					type.type)));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider()
	{
		return iconProvider;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (world.isRemote)
			return true;

		DoorTileEntity te = Door.getDoor(world, pos);
		if (te == null)
			return true;

		boolean opened = te.isOpened();

		te.openOrCloseDoor();
		if (opened)
			return true;

		EnumFacing facing = EntityUtils.getEntityFacing(player);
		if (state.getValue(FACING) != facing.getOpposite())
			return true;

		world.setBlockState(pos, state.withProperty(FACING, facing));

		te = te.getDoubleDoor();
		if (te != null)
			world.setBlockState(te.getPos(), state.withProperty(FACING, facing));

		return true;
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock)
	{
		DoorTileEntity te = Door.getDoor(world, pos);
		if (te == null)
			return;

		//	((FenceGateTileEntity) te).updateCamo(world, pos);

		if (world.isBlockIndirectlyGettingPowered(pos) != 0 || state.getBlock().canProvidePower())
			te.setPowered(te.isPowered());
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state)
	{
		DoorTileEntity te = Door.getDoor(world, pos);
		if (te == null || te.isMoving() || te.isOpened())
			return null;

		return super.getCollisionBoundingBox(world, pos, state);
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2)
	{
		return new FenceGateTileEntity();
	}

	@Override
	public int getRenderType()
	{
		return MalisisCore.malisisRenderType;
	}
}
