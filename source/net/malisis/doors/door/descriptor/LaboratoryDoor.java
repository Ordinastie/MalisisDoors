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

package net.malisis.doors.door.descriptor;

import net.malisis.doors.MalisisDoors;
import net.malisis.doors.door.DoorDescriptor;
import net.malisis.doors.door.DoorRegistry;
import net.malisis.doors.door.movement.SlidingUpDoorMovement;
import net.malisis.doors.door.sound.PneumaticSound;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Items;

/**
 * @author Ordinastie
 *
 */
public class LaboratoryDoor extends DoorDescriptor
{
	public LaboratoryDoor()
	{
		//Block
		setMaterial(Material.iron);
		setHardness(4.0F);
		setSoundType(Block.soundTypeMetal);
		setName("laboratory_door");
		setTextureName(MalisisDoors.modid + ":laboratory_door");

		//te
		setOpeningTime(12);
		setDoubleDoor(false);
		setMovement(DoorRegistry.getMovement(SlidingUpDoorMovement.class));
		setSound(DoorRegistry.getSound(PneumaticSound.class));

		//Item
		setTab(MalisisDoors.tab);

		//recipe
		setRecipe("AA", "BB", "BB", 'A', Items.gold_ingot, 'B', Items.iron_ingot);
	}
}
