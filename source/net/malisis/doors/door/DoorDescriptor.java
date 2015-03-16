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

package net.malisis.doors.door;

import net.malisis.doors.door.block.Door;
import net.malisis.doors.door.item.DoorItem;
import net.malisis.doors.door.movement.IDoorMovement;
import net.malisis.doors.door.movement.RotatingDoorMovement;
import net.malisis.doors.door.sound.IDoorSound;
import net.malisis.doors.door.sound.VanillaDoorSound;
import net.minecraft.block.Block;
import net.minecraft.block.Block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.registry.GameRegistry;

/**
 * @author Ordinastie
 *
 */
public class DoorDescriptor
{
	private Block block;
	private Item item;

	//block
	private Material material = Material.wood;
	private float hardness = 3.0F;
	private SoundType soundType = Block.soundTypeWood;
	private String name;
	private String textureName;
	private int autoCloseTime = 0;

	//te
	private IDoorMovement movement;
	private IDoorSound sound;
	private int openingTime = 6;
	private boolean doubleDoor = true;
	private boolean requireRedstone = false;

	//item
	private CreativeTabs tab;
	private int maxStackSize = 1;

	//recipe
	private Object[] recipe;
	private int numCrafted = 1;

	public DoorDescriptor()
	{
		movement = DoorRegistry.getMovement(RotatingDoorMovement.class);
		sound = DoorRegistry.getSound(VanillaDoorSound.class);
	}

	public DoorDescriptor(NBTTagCompound nbt)
	{
		if (nbt != null)
			readNBT(nbt);
		else
		{
			movement = DoorRegistry.getMovement(RotatingDoorMovement.class);
			sound = DoorRegistry.getSound(VanillaDoorSound.class);
		}
	}

	//#region Getters/Setters
	public Block getBlock()
	{
		return block;
	}

	public Item getItem()
	{
		return item;
	}

	public Material getMaterial()
	{
		return material;
	}

	public void setMaterial(Material material)
	{
		this.material = material;
	}

	public float getHardness()
	{
		return hardness;
	}

	public void setHardness(float hardness)
	{
		this.hardness = hardness;
	}

	public SoundType getSoundType()
	{
		return soundType;
	}

	public void setSoundType(SoundType soundType)
	{
		this.soundType = soundType;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getTextureName()
	{
		return textureName != null ? textureName : name;
	}

	public void setTextureName(String textureName)
	{
		this.textureName = textureName;
	}

	public IDoorMovement getMovement()
	{
		return movement;
	}

	public void setMovement(IDoorMovement movement)
	{
		this.movement = movement;
	}

	public IDoorSound getSound()
	{
		return sound;
	}

	public void setSound(IDoorSound sound)
	{
		this.sound = sound;
	}

	public int getOpeningTime()
	{
		return openingTime;
	}

	public void setOpeningTime(int openingTime)
	{
		this.openingTime = openingTime;
	}

	public boolean isDoubleDoor()
	{
		return doubleDoor;
	}

	public void setDoubleDoor(boolean doubleDoor)
	{
		this.doubleDoor = doubleDoor;
	}

	public boolean requireRedstone()
	{
		return requireRedstone;
	}

	public void setRequireRedstone(boolean requireRedstone)
	{
		this.requireRedstone = requireRedstone;
	}

	public int getAutoCloseTime()
	{
		return autoCloseTime;
	}

	public void setAutoCloseTime(int autoCloseTime)
	{
		this.autoCloseTime = autoCloseTime;
	}

	public CreativeTabs getTab()
	{
		return tab;
	}

	public void setTab(CreativeTabs tab)
	{
		this.tab = tab;
	}

	public Object[] getRecipe()
	{
		return recipe;
	}

	public void setRecipe(Object... recipe)
	{
		this.recipe = recipe;
	}

	public int getNumCrafted() {
		return numCrafted;
	}

	public void setNumCrafted(int numCrafted) {
		this.numCrafted = numCrafted;
	}

	public int getMaxStackSize() {
		return maxStackSize;
	}

	public void setMaxStackSize(int maxStackSize) {
		this.maxStackSize = maxStackSize;
	}

	//#end Getters/Setters

	public void readNBT(NBTTagCompound nbt)
	{
		if (nbt.hasKey("name"))
			name = nbt.getString("name");
		if (nbt.hasKey("textureName"))
			textureName = nbt.getString("textureName");
		if (nbt.hasKey("hardness"))
			hardness = nbt.getFloat("hardness");
		if (nbt.hasKey("block"))
			block = Block.getBlockById(nbt.getInteger("block"));
		if (nbt.hasKey("item"))
			item = Item.getItemById(nbt.getInteger("item"));
		if (nbt.hasKey("movement"))
			setMovement(DoorRegistry.getMovement(nbt.getString("movement")));
		if (nbt.hasKey("sound"))
			setSound(DoorRegistry.getSound(nbt.getString("sound")));
		if (nbt.hasKey("openingTime"))
			setOpeningTime(nbt.getInteger("openingTime"));
		if (nbt.hasKey("redstone"))
			setRequireRedstone(nbt.getBoolean("redstone"));
		if (nbt.hasKey("doubleDoor"))
			setDoubleDoor(nbt.getBoolean("doubleDoor"));
		if (nbt.hasKey("autoCloseTime"))
			setAutoCloseTime(nbt.getInteger("autoCloseTime"));
	}

	public void writeNBT(NBTTagCompound nbt)
	{
		nbt.setInteger("block", Block.getIdFromBlock(block));
		nbt.setInteger("item", Item.getIdFromItem(item));
		if (getMovement() != null)
			nbt.setString("movement", DoorRegistry.getId(getMovement()));
		if (getSound() != null)
			nbt.setString("sound", DoorRegistry.getId(getSound()));
		nbt.setInteger("openingTime", getOpeningTime());
		nbt.setBoolean("redstone", requireRedstone());
		nbt.setBoolean("doubleDoor", isDoubleDoor());
		nbt.setInteger("autoCloseTime", getAutoCloseTime());

	}

	public void set(Block block, Item item)
	{
		this.block = block;
		this.item = item;
	}

	public void create()
	{
		block = new Door(this);
		item = new DoorItem(this);
	}

	public DoorDescriptor register()
	{
		if (block == null || item == null)
			create();

		GameRegistry.registerBlock(block, block.getUnlocalizedName().substring(5));
		GameRegistry.registerItem(item, item.getUnlocalizedName());
		if (recipe != null)
			GameRegistry.addRecipe(new ItemStack(item, numCrafted), recipe);

		return this;
	}

}
