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

package net.malisis.doors.descriptor;

import net.malisis.doors.DoorDescriptor;
import net.malisis.doors.DoorRegistry;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.movement.SlidingDoorMovement;
import net.malisis.doors.sound.GlassDoorSound;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;

/**
 * @author Ordinastie
 *
 */
public class GlassDoor extends DoorDescriptor
{
	public GlassDoor(Material material)
	{
		boolean wood = material == Material.WOOD;
		//Block
		setMaterial(material);
		setHardness(wood ? 2.0F : 3.0F);
		setSoundType(wood ? SoundType.WOOD : SoundType.METAL);
		setName(wood ? "wood_sliding_door" : "iron_sliding_door");
		setTextureName(MalisisDoors.modid, wood ? "sliding_door_wood" : "sliding_door_iron");

		//te
		setRedstoneBehavior(wood ? RedstoneBehavior.STANDARD : RedstoneBehavior.REDSTONE_ONLY);
		setMovement(DoorRegistry.getMovement(SlidingDoorMovement.class));
		setSound(DoorRegistry.getSound(GlassDoorSound.class));

		//Item
		setTab(MalisisDoors.tab);
	}
}
