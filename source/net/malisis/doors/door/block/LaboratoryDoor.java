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

import java.util.Random;

import net.malisis.doors.MalisisDoors;
import net.malisis.doors.door.DoorRegistry;
import net.malisis.doors.door.movement.SlidingUpDoor;
import net.malisis.doors.door.sound.SpaceDoorSound;
import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 * 
 */
public class LaboratoryDoor extends Door
{
	public LaboratoryDoor()
	{
		super(Material.iron);
		setHardness(3.0F);
		setStepSound(soundTypeMetal);
		setBlockName("laboratory_door");
		setBlockTextureName(MalisisDoors.modid + ":laboratory_door");
	}

	@Override
	public Item getItemDropped(int metadata, Random par2Random, int par3)
	{
		if ((metadata & 8) != 0)
			return null;

		return MalisisDoors.Items.laboratoryDoorItem;
	}

	@Override
	public Item getItem(World world, int x, int y, int z)
	{
		return MalisisDoors.Items.laboratoryDoorItem;
	}

	@Override
	public void setTileEntityInformations(DoorTileEntity te)
	{
		te.setOpeningTime(12);
		te.setDoubleDoor(false);
		te.setMovement(DoorRegistry.getMouvement(SlidingUpDoor.class));
		te.setDoorSound(DoorRegistry.getSound(SpaceDoorSound.class));
	}

}
