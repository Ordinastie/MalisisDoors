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

import net.malisis.core.block.IBlockDirectional;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.inventory.MalisisInventory;
import net.malisis.core.renderer.icon.provider.SidesIconProvider;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.tileentity.DoorFactoryTileEntity;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 *
 */
public class DoorFactory extends MalisisBlock implements ITileEntityProvider, IBlockDirectional
{
	public DoorFactory()
	{
		super(Material.iron);
		setCreativeTab(MalisisDoors.tab);
		setName("door_factory");
		setHardness(3.0F);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void createIconProvider(Object object)
	{
		SidesIconProvider ip = new SidesIconProvider(MalisisDoors.modid + ":blocks/door_factory_side");
		ip.setSideIcon(EnumFacing.SOUTH, MalisisDoors.modid + ":blocks/door_factory");
		iconProvider = ip;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (world.isRemote)
			return true;

		if (player.isSneaking())
			return false;

		DoorFactoryTileEntity te = TileEntityUtils.getTileEntity(DoorFactoryTileEntity.class, world, pos);
		MalisisInventory.open((EntityPlayerMP) player, te);
		return true;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		DoorFactoryTileEntity provider = TileEntityUtils.getTileEntity(DoorFactoryTileEntity.class, world, pos);
		if (provider != null)
			provider.breakInventories(world, pos);
		super.breakBlock(world, pos, state);
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2)
	{
		return new DoorFactoryTileEntity();
	}

}
