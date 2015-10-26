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
import net.malisis.core.block.IBlockDirectional;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.renderer.MalisisRendered;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.renderer.RustyLadderRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 *
 */
@MalisisRendered(RustyLadderRenderer.class)
public class RustyLadder extends MalisisBlock implements IBlockDirectional
{
	public RustyLadder()
	{
		super(Material.iron);
		setName("rustyLadder");
		setCreativeTab(MalisisDoors.tab);
		setTexture(MalisisDoors.modid + ":blocks/rusty_hatch_handle");
	}

	@Override
	public EnumFacing getPlacingDirection(EnumFacing side, EntityLivingBase placer)
	{
		return side;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockAccess world, BlockPos pos, BoundingBoxType type)
	{
		if (type == BoundingBoxType.COLLISION)
			return null;

		return new AxisAlignedBB(0, 0, 0, 1, 1, 0.125F);
	}

	@Override
	public boolean canPlaceBlockAt(World world, BlockPos pos)
	{
		return world.isSideSolid(pos.west(), EnumFacing.EAST, true) || world.isSideSolid(pos.east(), EnumFacing.WEST, true)
				|| world.isSideSolid(pos.north(), EnumFacing.SOUTH, true) || world.isSideSolid(pos.south(), EnumFacing.NORTH, true);
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock)
	{
		EnumFacing dir = IBlockDirectional.getDirection(world, pos);

		if (!this.canBlockStay(world, pos, dir))
		{
			this.dropBlockAsItem(world, pos, state, 0);
			world.setBlockToAir(pos);
		}

		super.onNeighborBlockChange(world, pos, state, neighborBlock);
	}

	protected boolean canBlockStay(World world, BlockPos pos, EnumFacing side)
	{
		return world.isSideSolid(pos.offset(side.getOpposite()), side, true);
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean isLadder(IBlockAccess world, BlockPos pos, EntityLivingBase entity)
	{
		return true;
	}

	@Override
	public boolean isFullCube()
	{
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public EnumWorldBlockLayer getBlockLayer()
	{
		return EnumWorldBlockLayer.CUTOUT;
	}
}
