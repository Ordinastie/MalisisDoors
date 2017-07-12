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

import net.malisis.doors.DoorRegistry;
import net.malisis.doors.TrapDoorDescriptor;
import net.malisis.doors.movement.TrapDoorMovement;
import net.malisis.doors.sound.IronTrapDoorSound;
import net.malisis.doors.sound.WoodenTrapDoorSound;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

/**
 * @author Ordinastie
 *
 */
public class VanillaTrapDoor extends TrapDoorDescriptor
{
	public static enum Type
	{
		WOOD,
		IRON
	}

	public VanillaTrapDoor(Type type)
	{
		boolean iron = type == Type.IRON;
		String name = iron ? "iron_trapdoor" : "trapdoor";

		//Block
		setOpeningTime(6);
		setMaterial(iron ? Material.IRON : Material.WOOD);
		setHardness(iron ? 5.0F : 3.0F);
		setSoundType(iron ? SoundType.METAL : SoundType.WOOD);
		setRegistryName("minecraft:" + name);
		setUnlocalizedName(name);
		setTextureName("minecraft", "blocks/" + name);

		//te
		setRedstoneBehavior(iron ? RedstoneBehavior.REDSTONE_ONLY : RedstoneBehavior.STANDARD);
		setMovement(DoorRegistry.getMovement(TrapDoorMovement.class));
		setSound(DoorRegistry.getSound(iron ? IronTrapDoorSound.class : WoodenTrapDoorSound.class));

		//item
		setTab(CreativeTabs.REDSTONE);
	}
}
