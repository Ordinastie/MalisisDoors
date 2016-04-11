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
import net.malisis.doors.movement.RotatingDoorMovement;
import net.malisis.doors.sound.IronDoorSound;
import net.malisis.doors.sound.WoodenDoorSound;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

/**
 * @author Ordinastie
 *
 */
public class VanillaDoor extends DoorDescriptor
{
	public static enum Type
	{
		//@formatter:off
		OAK("doorOak", "door_wood"),
		SPRUCE("doorSpruce", "door_spruce"),
		BIRCH("doorBirch", "door_birch"),
		JUNGLE("doorJungle", "door_jungle"),
		ACACIA("doorAcacia", "door_acacia"),
		DARK_OAK("doorDarkOak", "door_dark_oak"),
		IRON("doorIron", "door_iron");
		//@formatter:on

		private String name;
		private String texture;

		private Type(String name, String texture)
		{
			this.name = name;
			this.texture = texture;
		}

	}

	public VanillaDoor(Type type)
	{
		boolean iron = type == Type.IRON;
		//Block
		setOpeningTime(6);
		setMaterial(iron ? Material.IRON : Material.WOOD);
		setHardness(iron ? 5.0F : 3.0F);
		setSoundType(iron ? SoundType.METAL : SoundType.WOOD);
		setName(type.name);
		setTextureName("minecraft", type.texture);
		setNumCrafted(3);

		//te
		setRedstoneBehavior(iron ? RedstoneBehavior.REDSTONE_ONLY : RedstoneBehavior.STANDARD);
		setMovement(DoorRegistry.getMovement(RotatingDoorMovement.class));
		setSound(DoorRegistry.getSound(iron ? IronDoorSound.class : WoodenDoorSound.class));

		//item
		setTab(CreativeTabs.REDSTONE);
	}
}
