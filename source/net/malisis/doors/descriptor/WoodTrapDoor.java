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
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.TrapDoorDescriptor;
import net.malisis.doors.movement.TrapDoorMovement;
import net.malisis.doors.sound.VanillaDoorSound;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPlanks.EnumType;
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
		//@formatter:off
//		OAK("", EnumType.OAK),
		ACACIA("trapdoor_acacia", EnumType.ACACIA),
		BIRCH("trapdoor_birch", EnumType.BIRCH),
		DARK_OAK("trapdoor_dark_oak", EnumType.DARK_OAK),
		JUNGLE("trapdoor_jungle", EnumType.JUNGLE),
		SPRUCE("trapdoor_spruce", EnumType.SPRUCE);
		//@formatter:on
		private EnumType type;
		private String name;

		private Type(String name, EnumType type)
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
		setTextureName(MalisisDoors.modid, "blocks/" + type.name);

		//te
		setMovement(DoorRegistry.getMovement(TrapDoorMovement.class));
		setSound(DoorRegistry.getSound(VanillaDoorSound.class));

		//item
		setTab(MalisisDoors.tab);

		//recipe
		setNumCrafted(2);
		setRecipe("AAA", "AAA", 'A', new ItemStack(Blocks.planks, 1, type.type.getMetadata()));
	}
}
