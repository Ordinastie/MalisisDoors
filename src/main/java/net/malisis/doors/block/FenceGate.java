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

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.malisis.core.MalisisCore;
import net.malisis.core.block.IComponent;
import net.malisis.core.block.IComponentProvider;
import net.malisis.core.block.IRegisterable;
import net.malisis.core.inventory.MalisisTab;
import net.malisis.core.renderer.MalisisRendered;
import net.malisis.core.renderer.icon.Icon;
import net.malisis.core.renderer.icon.provider.IIconProvider;
import net.malisis.core.util.EntityUtils;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.core.util.clientnotif.ClientNotification;
import net.malisis.doors.DoorState;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.iconprovider.CamoFenceGateIconProvider;
import net.malisis.doors.renderer.FenceGateRenderer;
import net.malisis.doors.tileentity.FenceGateTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockPlanks.EnumType;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
@MalisisRendered(FenceGateRenderer.class)
public class FenceGate extends BlockFenceGate implements IComponentProvider, IRegisterable<Block>
{
	public static enum Type
	{
		OAK("minecraft:fence_gate", "fenceGate", EnumType.OAK),
		ACACIA("minecraft:acacia_fence_gate", "acaciaFenceGate", EnumType.ACACIA),
		BIRCH("minecraft:birch_fence_gate", "birchFenceGate", EnumType.BIRCH),
		DARK_OAK("minecraft:dark_oak_fence_gate", "darkOakFenceGate", EnumType.DARK_OAK),
		JUNGLE("minecraft:jungle_fence_gate", "jungleFenceGate", EnumType.JUNGLE),
		SPRUCE("minecraft:spruce_fence_gate", "spruceFenceGate", EnumType.SPRUCE),
		CAMO(MalisisDoors.modid + ":camoFenceGate", "camoFenceGate", EnumType.OAK); //OAK to prevent NPE

		private EnumType type;
		private String registry;
		private String unlocalized;

		private Type(String registry, String unlocalized, EnumType type)
		{
			this.registry = registry;
			this.unlocalized = unlocalized;
			this.type = type;
		}
	}

	protected final List<IComponent> components = Lists.newArrayList();

	public FenceGate(Type type)
	{
		super(type.type);
		setHardness(2.0F);
		setResistance(5.0F);
		setSoundType(SoundType.WOOD);
		setName(type.registry);
		setUnlocalizedName(type.unlocalized);

		if (type == Type.CAMO)
			setCreativeTab(MalisisDoors.tab);

		if (MalisisCore.isClient())
		{
			if (type == Type.CAMO)
				addComponent(CamoFenceGateIconProvider.get());
			else
			{
				Icon icon = Icon.from(Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT, type.type));
				addComponent((IIconProvider) () -> icon);
			}
		}
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
	public FenceGate setCreativeTab(CreativeTabs tab)
	{
		super.setCreativeTab(tab);
		if (tab instanceof MalisisTab)
			((MalisisTab) tab).addItem(this);
		return this;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		super.onBlockPlacedBy(world, pos, state, placer, stack);
		FenceGateTileEntity te = TileEntityUtils.getTileEntity(FenceGateTileEntity.class, world, pos);
		if (te == null)
			return;

		te.updateAll();
		te = te.getDoubleDoor();
		if (te != null)
			te.updateAll();
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
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
	@ClientNotification
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos from)
	{
		FenceGateTileEntity te = TileEntityUtils.getTileEntity(FenceGateTileEntity.class, world, pos);
		if (te == null)
			return;

		if (!world.isRemote) //server
		{
			if (world.isBlockIndirectlyGettingPowered(pos) != 0 || neighborBlock.getDefaultState().canProvidePower())
				te.setPowered(te.isPowered());
		}
		else
		{
			te.updateAll();
		}
	}

	@Override
	@Nullable
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		FenceGateTileEntity te = TileEntityUtils.getTileEntity(FenceGateTileEntity.class, world, pos);
		if (te == null || te.isMoving() || te.isOpened())
			return null;

		return super.getCollisionBoundingBox(state, world, pos);
	}

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		FenceGateTileEntity te = new FenceGateTileEntity();
		if (te.isOpened(state))
			te.setState(DoorState.OPENED);
		return te;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}
}
