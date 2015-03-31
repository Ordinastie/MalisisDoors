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

package net.malisis.doors.door.item;

import net.malisis.core.MalisisCore;
import net.malisis.doors.door.DoorDescriptor;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class DoorItem extends ItemDoor
{
	private DoorDescriptor descriptor;

	public DoorItem(DoorDescriptor desc)
	{
		super(desc.getMaterial());

		this.descriptor = desc;
		this.maxStackSize = desc.getMaxStackSize();
		setUnlocalizedName(desc.getName());
		setTextureName(desc.getTextureName());
		setCreativeTab(desc.getTab());
	}

	public DoorItem()
	{
		super(Material.wood);
	}

	public DoorDescriptor getDescriptor(ItemStack itemStack)
	{
		return descriptor;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister)
	{
		this.itemIcon = iconRegister.registerIcon(descriptor.getTextureName());
	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float par8, float par9, float par10)
	{
		if (side != 1)
			return false;

		y++;
		Block block = getDescriptor(itemStack).getBlock();
		if (block == null)
		{
			MalisisCore.log.error("Can't place Door : block is null for " + itemStack);
			return false;
		}

		if (!player.canPlayerEdit(x, y, z, side, itemStack) || !player.canPlayerEdit(x, y + 1, z, side, itemStack))
			return false;

		if (!block.canPlaceBlockAt(world, x, y, z))
			return false;

		int i1 = MathHelper.floor_double((player.rotationYaw + 180.0F) * 4.0F / 360.0F - 0.5D) & 3;
		placeDoorBlock(world, x, y, z, i1, block);
		itemStack.stackSize--;

		block.onBlockPlacedBy(world, x, y, z, player, itemStack);
		return true;
	}
}
