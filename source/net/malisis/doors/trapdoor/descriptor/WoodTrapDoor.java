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

package net.malisis.doors.trapdoor.descriptor;

import net.malisis.doors.MalisisDoors;
import net.malisis.doors.door.DoorRegistry;
import net.malisis.doors.door.sound.VanillaDoorSound;
import net.malisis.doors.trapdoor.TrapDoorDescriptor;
import net.malisis.doors.trapdoor.movement.TrapDoorMovement;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

/**
 * @author Ordinastie
 *
 */
public class WoodTrapDoor extends TrapDoorDescriptor
{

	public static enum Type
	{
		//{"oak", "spruce", "birch", "jungle", "acacia", "big_oak"};
		//@formatter:off
//		OAK("fenceGate", 0),
		ACACIA("trapdoor_acacia", 4),
		BIRCH("trapdoor_birch", 2),
		DARK_OAK("trapdoor_dark_oak", 5),
		JUNGLE("trapdoor_jungle", 3),
		SPRUCE("trapdoor_spruce", 1);

		//@formatter:on
		private int type;
		private String name;

		private Type(String name, int type)
		{
			this.name = name;
			this.type = type;
		}
	}

	public WoodTrapDoor(Type type)
	{
		//Block
		setOpeningTime(6);
		setMaterial(Material.wood);
		setHardness(4.0F);
		setSoundType(Block.soundTypeWood);
		setName(type.name);
		setTextureName(MalisisDoors.modid + ":" + type.name);

		//te
		setRequireRedstone(false);
		setMovement(DoorRegistry.getMovement(TrapDoorMovement.class));
		setSound(DoorRegistry.getSound(VanillaDoorSound.class));

		//item
		setTab(MalisisDoors.tab);

		//recipe
		setNumCrafted(2);
		setRecipe("AAA", "AAA", 'A', new ItemStack(Blocks.planks, 1, type.type));
	}
}
