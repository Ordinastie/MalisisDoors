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

import net.malisis.core.MalisisCore;
import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.block.component.DirectionalComponent;
import net.malisis.core.block.component.PowerComponent;
import net.malisis.core.block.component.PowerComponent.Type;
import net.malisis.core.renderer.MalisisRendered;
import net.malisis.core.renderer.icon.Icon;
import net.malisis.core.renderer.icon.provider.IBlockIconProvider;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.doors.DoorState;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.renderer.GarageDoorRenderer;
import net.malisis.doors.tileentity.GarageDoorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 *
 */
@MalisisRendered(item = GarageDoorRenderer.class)
public class GarageDoor extends MalisisBlock implements ITileEntityProvider
{
	public GarageDoor()
	{
		super(Material.wood);
		setName("garage_door");
		setCreativeTab(MalisisDoors.tab);
		setHardness(2.0F);
		setSoundType(SoundType.WOOD);

		addComponent(new DirectionalComponent());
		addComponent(new PowerComponent(Type.REDSTONE));

		if (MalisisCore.isClient())
			addComponent(GarageDoorIconProvider.get());
	}

	@Override
	public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		IBlockState neighborState = world.getBlockState(pos.up());
		if (neighborState.getBlock() == this)
			return neighborState;

		neighborState = world.getBlockState(pos.down());
		if (neighborState.getBlock() == this)
			return neighborState;

		return super.onBlockPlaced(world, pos, facing, hitX, hitY, hitZ, meta, placer);
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock)
	{
		GarageDoorTileEntity te = TileEntityUtils.getTileEntity(GarageDoorTileEntity.class, world, pos);
		if (te == null || te.isMoving())
			return;

		boolean powered = world.isBlockIndirectlyGettingPowered(pos) != 0;
		if ((powered || neighborBlock.getDefaultState().canProvidePower()) && neighborBlock != this)
			te.getTopDoor().setPowered(powered);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockAccess world, BlockPos pos, IBlockState state, BoundingBoxType type)
	{
		GarageDoorTileEntity te = TileEntityUtils.getTileEntity(GarageDoorTileEntity.class, world, pos);
		if (te != null && (te.isMoving() || te.getState() != DoorState.CLOSED))
			return null;

		float w = Door.DOOR_WIDTH / 2;
		return new AxisAlignedBB(0, 0, 0.5F - w, 1, 1, 0.5F + w);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata)
	{
		return new GarageDoorTileEntity();
	}

	@Override
	public boolean isNormalCube(IBlockState state)
	{
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state)
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}

	@SideOnly(Side.CLIENT)
	public static class GarageDoorIconProvider implements IBlockIconProvider
	{
		private Icon topIcon = Icon.from(MalisisDoors.modid + ":blocks/garage_door_top");
		private Icon baseIcon = Icon.from(MalisisDoors.modid + ":blocks/garage_door");

		@Override
		public Icon getIcon()
		{
			return baseIcon;
		}

		@Override
		public Icon getIcon(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side)
		{
			GarageDoorTileEntity te = TileEntityUtils.getTileEntity(GarageDoorTileEntity.class, world, pos);
			return te != null && te.isTop() ? getIcon(state, side) : baseIcon;
		}

		@Override
		public Icon getIcon(IBlockState state, EnumFacing side)
		{
			return side.getAxis() == Axis.Z ? topIcon : baseIcon;
		}

		public static GarageDoorIconProvider get()
		{
			return new GarageDoorIconProvider();
		}
	}
}
