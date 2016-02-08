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

import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.block.component.DirectionalComponent;
import net.malisis.core.block.component.PowerComponent;
import net.malisis.core.block.component.PowerComponent.Type;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.malisis.core.renderer.icon.provider.IBlockIconProvider;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.doors.DoorState;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.tileentity.GarageDoorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 *
 */
public class GarageDoor extends MalisisBlock implements ITileEntityProvider
{
	public GarageDoor()
	{
		super(Material.wood);
		setName("garage_door");
		setCreativeTab(MalisisDoors.tab);
		setHardness(2.0F);
		setStepSound(soundTypeWood);

		addComponent(new DirectionalComponent());
		addComponent(new PowerComponent(Type.REDSTONE));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void createIconProvider(Object object)
	{
		iconProvider = new GarageDoorIconProvider();
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
		if ((powered || neighborBlock.canProvidePower()) && neighborBlock != this)
			te.getTopDoor().setPowered(powered);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockAccess world, BlockPos pos, BoundingBoxType type)
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
	public boolean isNormalCube()
	{
		return false;
	}

	@Override
	public boolean isFullCube()
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public int getRenderType()
	{
		return -1;
	}

	@SideOnly(Side.CLIENT)
	public static class GarageDoorIconProvider implements IBlockIconProvider
	{
		private MalisisIcon topIcon = new MalisisIcon(MalisisDoors.modid + ":blocks/garage_door_top");
		private MalisisIcon baseIcon = new MalisisIcon(MalisisDoors.modid + ":blocks/garage_door");

		@Override
		public void registerIcons(TextureMap textureMap)
		{
			topIcon = topIcon.register(textureMap);
			baseIcon = baseIcon.register(textureMap);
		}

		@Override
		public MalisisIcon getIcon()
		{
			return baseIcon;
		}

		@Override
		public MalisisIcon getIcon(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side)
		{
			GarageDoorTileEntity te = TileEntityUtils.getTileEntity(GarageDoorTileEntity.class, world, pos);
			return te != null && te.isTop() ? getIcon(side) : baseIcon;
		}

		@Override
		public MalisisIcon getIcon(ItemStack itemStack, EnumFacing side)
		{
			return getIcon(side);
		}

		private MalisisIcon getIcon(EnumFacing side)
		{
			return side == EnumFacing.SOUTH || side == EnumFacing.NORTH ? topIcon : baseIcon;
		}
	}
}
