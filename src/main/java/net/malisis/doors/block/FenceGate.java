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

package net.malisis.doors.block;

import java.util.List;

import net.malisis.core.MalisisCore;
import net.malisis.core.block.IComponent;
import net.malisis.core.block.IComponentProvider;
import net.malisis.core.block.IRegisterable;
import net.malisis.core.renderer.MalisisRendered;
import net.malisis.core.renderer.icon.Icon;
import net.malisis.core.renderer.icon.provider.IIconProvider;
import net.malisis.core.util.EntityUtils;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.iconprovider.CamoFenceGateIconProvider;
import net.malisis.doors.renderer.FenceGateRenderer;
import net.malisis.doors.tileentity.FenceGateTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockPlanks.EnumType;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.google.common.collect.Lists;

/**
 * @author Ordinastie
 *
 */
@MalisisRendered(FenceGateRenderer.class)
public class FenceGate extends BlockFenceGate implements ITileEntityProvider, IComponentProvider, IRegisterable
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
		CAMO("camoFenceGate", EnumType.OAK); //OAK to prevent NPE
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
	protected final List<IComponent> components = Lists.newArrayList();

	public FenceGate(Type type)
	{
		super(type.type);
		this.type = type;
		setHardness(2.0F);
		setResistance(5.0F);
		setSoundType(SoundType.WOOD);
		setUnlocalizedName(type.name);

		if (type == Type.CAMO)
			setCreativeTab(MalisisDoors.tab);

		if (MalisisCore.isClient())
		{
			if (type == Type.CAMO)
				addComponent(CamoFenceGateIconProvider.get());
			else
			{
				Icon icon = Icon.from(Blocks.planks.getDefaultState().withProperty(BlockPlanks.VARIANT, type.type));
				addComponent((IIconProvider) () -> icon);
			}
		}
	}

	@Override
	public String getName()
	{
		return type.name;
	}

	@Override
	public void addComponent(IComponent component)
	{
		components.add(component);
	}

	@Override
	public List<IComponent> getComponents()
	{
		return components;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		super.onBlockPlacedBy(world, pos, state, placer, stack);
		FenceGateTileEntity te = TileEntityUtils.getTileEntity(FenceGateTileEntity.class, world, pos);
		if (te != null)
			te.updateAll();
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (world.isRemote)
			return true;

		FenceGateTileEntity te = TileEntityUtils.getTileEntity(FenceGateTileEntity.class, world, pos);
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
		FenceGateTileEntity te = TileEntityUtils.getTileEntity(FenceGateTileEntity.class, world, pos);
		if (te == null)
			return;

		te.updateAll();

		if (world.isBlockIndirectlyGettingPowered(pos) != 0 || neighborBlock.getDefaultState().canProvidePower())
			te.setPowered(te.isPowered());
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, World world, BlockPos pos)
	{
		FenceGateTileEntity te = TileEntityUtils.getTileEntity(FenceGateTileEntity.class, world, pos);
		if (te == null || te.isMoving() || te.isOpened())
			return null;

		return super.getCollisionBoundingBox(state, world, pos);
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2)
	{
		return new FenceGateTileEntity();
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}
}
