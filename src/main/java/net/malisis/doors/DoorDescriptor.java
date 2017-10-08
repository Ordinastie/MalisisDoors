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

package net.malisis.doors;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import net.malisis.core.MalisisCore;
import net.malisis.core.block.IRegisterable;
import net.malisis.core.registry.MalisisRegistry;
import net.malisis.core.util.modmessage.ModMessage;
import net.malisis.doors.block.Door;
import net.malisis.doors.item.DoorItem;
import net.malisis.doors.movement.IDoorMovement;
import net.malisis.doors.movement.RotatingDoorMovement;
import net.malisis.doors.sound.IDoorSound;
import net.malisis.doors.sound.WoodenDoorSound;
import net.malisis.doors.tileentity.DoorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/**
 * @author Ordinastie
 *
 */
public class DoorDescriptor
{
	public static enum RedstoneBehavior
	{
		STANDARD,
		REDSTONE_ONLY,
		REDSTONE_LOCK,
		HAND_ONLY
	}

	protected Block block;
	protected Item item;

	//block
	protected Material material = Material.WOOD;
	protected float hardness = 3.0F;
	protected SoundType soundType = SoundType.WOOD;
	protected String registryName;
	protected String unlocalizedName;
	protected int autoCloseTime = 0;

	//texture
	protected String modid;
	protected String textureName;

	//te
	protected Class<? extends DoorTileEntity> tileEntityClass = DoorTileEntity.class;
	protected IDoorMovement movement;
	protected IDoorSound sound;
	protected int openingTime = 6;
	protected boolean doubleDoor = true;
	protected boolean proximityDetection = false;
	protected RedstoneBehavior redstoneBehavior = RedstoneBehavior.STANDARD;

	//item
	protected CreativeTabs tab;
	protected int maxStackSize = 64;

	//digicode
	protected String code = null;

	public DoorDescriptor()
	{
		movement = DoorRegistry.getMovement(RotatingDoorMovement.class);
		sound = DoorRegistry.getSound(WoodenDoorSound.class);
	}

	public DoorDescriptor(NBTTagCompound nbt)
	{
		if (nbt != null)
			readNBT(nbt);
		else
		{
			movement = DoorRegistry.getMovement(RotatingDoorMovement.class);
			sound = DoorRegistry.getSound(WoodenDoorSound.class);
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

	public String getRegistryName()
	{
		return registryName;
	}

	public void setName(String name)
	{
		setRegistryName(name);
		setUnlocalizedName(name);
	}

	public void setRegistryName(String name)
	{
		this.registryName = name;
	}

	public String getUnlocalizedName()
	{
		return unlocalizedName;
	}

	public void setUnlocalizedName(String name)
	{
		unlocalizedName = name;
	}

	public String getModId()
	{
		return modid;
	}

	public String getTextureName()
	{
		return textureName;
	}

	public void setTextureName(String modid, String textureName)
	{
		this.modid = modid;
		this.textureName = textureName;
	}

	public Class<? extends DoorTileEntity> getTileEntityClass()
	{
		return tileEntityClass;
	}

	public void setTileEntityClass(Class<? extends DoorTileEntity> clazz)
	{
		this.tileEntityClass = clazz;
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

	public boolean hasProximityDetection()
	{
		return proximityDetection;
	}

	public void setProximityDetection(boolean proximity)
	{
		this.proximityDetection = proximity;
	}

	public RedstoneBehavior getRedstoneBehavior()
	{
		return redstoneBehavior;
	}

	public void setRedstoneBehavior(RedstoneBehavior redstoneBehavior)
	{
		this.redstoneBehavior = redstoneBehavior;
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

	public int getMaxStackSize()
	{
		return maxStackSize;
	}

	public void setMaxStackSize(int maxStackSize)
	{
		this.maxStackSize = maxStackSize;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public String getCode()
	{
		return code;
	}

	public boolean hasCode()
	{
		return !StringUtils.isEmpty(code);
	}

	//#end Getters/Setters

	public void readNBT(NBTTagCompound nbt)
	{
		if (nbt.hasKey("name"))
			registryName = nbt.getString("name");
		if (nbt.hasKey("modid"))
			modid = nbt.getString("modid");
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
		//LEGACY
		if (nbt.hasKey("redstone"))
			setRedstoneBehavior(nbt.getBoolean("redstone") ? RedstoneBehavior.REDSTONE_ONLY : RedstoneBehavior.STANDARD);
		if (nbt.hasKey("redstoneBehavior"))
			setRedstoneBehavior(RedstoneBehavior.values()[nbt.getInteger("redstoneBehavior")]);
		if (nbt.hasKey("doubleDoor"))
			setDoubleDoor(nbt.getBoolean("doubleDoor"));
		if (nbt.hasKey("proximityDetection"))
			setProximityDetection(nbt.getBoolean("proximityDetection"));
		if (nbt.hasKey("autoCloseTime"))
			setAutoCloseTime(nbt.getInteger("autoCloseTime"));
		if (nbt.hasKey("code"))
			setCode(nbt.getString("code"));
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
		nbt.setInteger("redstoneBehavior", getRedstoneBehavior().ordinal());
		nbt.setBoolean("doubleDoor", isDoubleDoor());
		nbt.setBoolean("proximityDetection", hasProximityDetection());
		nbt.setInteger("autoCloseTime", getAutoCloseTime());
		if (hasCode())
			nbt.setString("code", getCode());
		else
			nbt.removeTag("code");
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

	@SuppressWarnings("unchecked")
	public DoorDescriptor register()
	{
		if (block == null || item == null)
			create();

		if (block instanceof IRegisterable)
			MalisisRegistry.register((IRegisterable<Block>) block);
		else
			ForgeRegistries.BLOCKS.register(block);

		if (item instanceof IRegisterable)
			MalisisRegistry.register((IRegisterable<Item>) item);
		else
			ForgeRegistries.ITEMS.register(item);

		return this;
	}

	@ModMessage("createDoor")
	public static Pair<Block, Item> createDoor(HashMap<String, Object> map)
	{
		if (Loader.instance().getLoaderState() != LoaderState.PREINITIALIZATION)
		{
			MalisisCore.log.error(	"Tried to create and register a new door during wrong loader phase (was {}, expected {}).",
									Loader.instance().getLoaderState(),
									LoaderState.PREINITIALIZATION);
			return null;
		}

		DoorDescriptor desc = new DoorDescriptor();
		if (map.containsKey("name"))
			desc.setRegistryName((String) map.get("name"));
		if (map.containsKey("modid") && map.containsKey("textureName"))
			desc.setTextureName((String) map.get("modid"), (String) map.get("textureName"));
		if (map.containsKey("hardness"))
			desc.setRegistryName((String) map.get("name"));
		if (map.containsKey("movement"))
		{
			IDoorMovement mvt = DoorRegistry.getMovement((String) map.get("movement"));
			if (mvt.isSpecial())
			{
				MalisisCore.log.error(	"{} is marked as 'special' and cannot be used for regular doors. Defaulting to 'rotating' movement",
										map.get("movement"));
				mvt = DoorRegistry.getMovement(RotatingDoorMovement.class);
			}
			desc.setMovement(mvt);
		}
		if (map.containsKey("sound"))
			desc.setSound(DoorRegistry.getSound((String) map.get("sound")));
		if (map.containsKey("openingTime"))
			desc.setOpeningTime((int) map.get("openingTime"));
		if (map.containsKey("redstoneBehavior"))
			desc.setRedstoneBehavior(RedstoneBehavior.values()[(int) map.get("redstoneBehavior")]);
		if (map.containsKey("doubleDoor"))
			desc.setDoubleDoor((boolean) map.get("doubleDoor"));
		if (map.containsKey("proximityDetection"))
			desc.setProximityDetection((boolean) map.get("proximityDetection"));
		if (map.containsKey("autoCloseTime"))
			desc.setAutoCloseTime((int) map.get("autoCloseTime"));
		if (map.containsKey("code"))
			desc.setCode((String) map.get("code"));
		if (map.containsKey("tab"))
			desc.setTab((CreativeTabs) map.get("tab"));

		desc.register();

		return Pair.of(desc.getBlock(), desc.getItem());
	}

}
