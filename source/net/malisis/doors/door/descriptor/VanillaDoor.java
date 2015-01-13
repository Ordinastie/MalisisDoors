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

import net.malisis.doors.door.DoorDescriptor;
import net.malisis.doors.door.DoorRegistry;
import net.malisis.doors.door.movement.RotatingDoorMovement;
import net.malisis.doors.door.sound.VanillaDoorSound;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

/**
 * @author Ordinastie
 *
 */
public class VanillaDoor extends DoorDescriptor
{
	public VanillaDoor(Material material)
	{
		boolean wood = material == Material.wood;
		//Block
		setOpeningTime(6);
		setMaterial(material);
		setHardness(wood ? 3.0F : 5.0F);
		setSoundType(wood ? Block.soundTypeWood : Block.soundTypeMetal);
		setName(wood ? "doorWood" : "doorIron");
		setTextureName(wood ? "door_wood" : "door_iron");

		//te
		setRequireRedstone(!wood);
		setMovement(DoorRegistry.getMovement(RotatingDoorMovement.class));
		setSound(DoorRegistry.getSound(VanillaDoorSound.class));

		//item
		setTab(CreativeTabs.tabRedstone);
	}
}
