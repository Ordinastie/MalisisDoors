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

import net.malisis.core.inventory.IInventoryProvider;
import net.malisis.core.inventory.MalisisInventory;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.doors.entity.VanishingDiamondTileEntity;
import net.malisis.doors.entity.VanishingTileEntity;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
public class VanishingDiamondBlock extends VanishingBlock
{

	public VanishingDiamondBlock()
	{
		super();
		setUnlocalizedName("vanishing_block_diamond");
	}

	@Override
	public void setPowerState(World world, int x, int y, int z, boolean powered)
	{
		if (world.getBlock(x, y, z) != this) // block is VanishingBlock ?
			return;
		if (isPowered(world, x, y, z) == powered) // same power state?
			return;

		VanishingTileEntity te = TileEntityUtils.getTileEntity(VanishingTileEntity.class, world, x, y, z);
		if (te == null)
			return;

		te.setPowerState(powered);

		if (powered)
			world.setBlockMetadataWithNotify(x, y, z, te.blockMetadata | flagPowered, 2);
		else
			world.setBlockMetadataWithNotify(x, y, z, te.blockMetadata & ~flagPowered, 2);
	}

	@Override
	public boolean shouldPropagate(World world, int x, int y, int z, VanishingTileEntity source)
	{
		return false;
	}

	@Override
	public void propagateState(World world, int x, int y, int z)
	{}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		if (world.isRemote)
			return true;

		IInventoryProvider te = TileEntityUtils.getTileEntity(IInventoryProvider.class, world, x, y, z);
		MalisisInventory.open((EntityPlayerMP) player, te);

		return true;
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List list)
	{

	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata)
	{
		return new VanishingDiamondTileEntity();
	}
}
