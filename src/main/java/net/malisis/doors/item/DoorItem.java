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

package net.malisis.doors.item;

import java.util.List;

import net.malisis.core.MalisisCore;
import net.malisis.core.block.IRegisterable;
import net.malisis.core.renderer.MalisisRendered;
import net.malisis.core.renderer.icon.Icon;
import net.malisis.core.renderer.icon.provider.IIconProvider;
import net.malisis.doors.DoorDescriptor;
import net.malisis.doors.renderer.DoorRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@MalisisRendered(item = DoorRenderer.class)
public class DoorItem extends ItemDoor implements IRegisterable, IIconProvider
{
	private DoorDescriptor descriptor;

	public DoorItem(DoorDescriptor desc)
	{
		super(desc.getBlock());

		this.descriptor = desc;
		this.maxStackSize = desc.getMaxStackSize();
		setUnlocalizedName(desc.getName());
		//setTextureName(desc.getTextureName());
		setCreativeTab(desc.getTab());
	}

	public DoorItem()
	{
		super(null);
	}

	public DoorDescriptor getDescriptor(ItemStack itemStack)
	{
		return descriptor;
	}

	@Override
	public String getName()
	{
		return descriptor.getName();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon()
	{
		return Icon.from(descriptor.getModId() + ":items/" + descriptor.getTextureName());
	}

	@Override
	public EnumActionResult onItemUse(ItemStack itemStack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (side != EnumFacing.UP)
			return EnumActionResult.FAIL;

		IBlockState state = world.getBlockState(pos);

		if (!state.getBlock().isReplaceable(world, pos))
			pos = pos.up();

		Block block = getDescriptor(itemStack).getBlock();
		if (block == null)
		{
			MalisisCore.log.error("Can't place Door : block is null for " + itemStack);
			return EnumActionResult.FAIL;
		}

		if (!player.canPlayerEdit(pos, side, itemStack) || !player.canPlayerEdit(pos.up(), side, itemStack))
			return EnumActionResult.FAIL;

		if (!block.canPlaceBlockAt(world, pos))
			return EnumActionResult.FAIL;

		placeDoor(world, pos, EnumFacing.fromAngle(player.rotationYaw), block, false);
		--itemStack.stackSize;
		block.onBlockPlacedBy(world, pos, world.getBlockState(pos), player, itemStack);
		return EnumActionResult.SUCCESS;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced)
	{
		if (stack.getTagCompound() == null)
			return;

		tooltip.add(TextFormatting.WHITE + I18n.translateToLocal("door_movement." + stack.getTagCompound().getString("movement")));
	}
}
