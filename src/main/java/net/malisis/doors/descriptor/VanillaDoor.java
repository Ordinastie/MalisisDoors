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

import net.malisis.core.MalisisCore;
import net.malisis.core.renderer.model.EmptyModelLoader;
import net.malisis.doors.DoorDescriptor;
import net.malisis.doors.DoorRegistry;
import net.malisis.doors.movement.RotatingDoorMovement;
import net.malisis.doors.sound.IronDoorSound;
import net.malisis.doors.sound.WoodenDoorSound;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.client.model.ModelLoader;

/**
 * @author Ordinastie
 *
 */
public class VanillaDoor extends DoorDescriptor
{
	public static enum Type
	{
		OAK("wooden_door", "doorOak", "door_wood"),
		SPRUCE("spruce_door", "doorSpruce", "door_spruce"),
		BIRCH("birch_door", "doorBirch", "door_birch"),
		JUNGLE("jungle_door", "doorJungle", "door_jungle"),
		ACACIA("acacia_door", "doorAcacia", "door_acacia"),
		DARK_OAK("dark_oak_door", "doorDarkOak", "door_dark_oak"),
		IRON("iron_door", "doorIron", "door_iron");

		private String registry;
		private String unlocalized;
		private String texture;

		private Type(String registry, String unlocalized, String texture)
		{
			this.registry = registry;
			this.unlocalized = unlocalized;
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
		setRegistryName("minecraft:" + type.registry);
		setUnlocalizedName(type.unlocalized);
		setTextureName("minecraft", type.texture);

		//te
		setRedstoneBehavior(iron ? RedstoneBehavior.REDSTONE_ONLY : RedstoneBehavior.STANDARD);
		setMovement(DoorRegistry.getMovement(RotatingDoorMovement.class));
		setSound(DoorRegistry.getSound(iron ? IronDoorSound.class : WoodenDoorSound.class));

		//item
		setTab(CreativeTabs.REDSTONE);
	}

	@Override
	public DoorDescriptor register()
	{
		super.register();

		if (MalisisCore.isClient())
		{
			ModelResourceLocation mrl = new ModelResourceLocation(item.getRegistryName() + "-malisis", "inventory");
			EmptyModelLoader.register(item, mrl);
			ModelLoader.setCustomModelResourceLocation(item, 0, mrl);
		}

		return this;
	}
}
