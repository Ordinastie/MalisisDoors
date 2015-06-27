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

import net.malisis.doors.MalisisDoors;
import net.malisis.doors.door.DoorDescriptor;
import net.malisis.doors.door.tileentity.SaloonDoorTileEntity;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
public class SaloonDoorBlock extends Door
{
	public SaloonDoorBlock(DoorDescriptor desc)
	{
		super(desc);
	}

	@Override
	public void registerIcons(IIconRegister register)
	{
		blockIcon = register.registerIcon(MalisisDoors.modid + ":saloon_door");
	}

	@Override
	public IIcon getIcon(int side, int metadata)
	{
		return blockIcon;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer p, int par6, float par7, float par8, float par9)
	{
		return true;
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity)
	{
		if (!(entity instanceof EntityPlayer))
			return;

		SaloonDoorTileEntity te = (SaloonDoorTileEntity) getDoor(world, x, y, z);
		if (te == null)
			return;

		if (te.getDescriptor() == null)
			return;

		if (te.isMoving())
			return;

		te.setOpenDirection(entity);

		if (world.isRemote)
			return;

		te.openOrCloseDoor();
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata)
	{
		if ((metadata & FLAG_TOPBLOCK) != 0)
			return null;

		return new SaloonDoorTileEntity();
	}
}
