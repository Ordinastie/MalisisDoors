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

package net.malisis.doors.door.block;

import java.util.List;

import net.malisis.core.MalisisCore;
import net.malisis.core.renderer.MalisisRendered;
import net.malisis.core.renderer.icon.IIconProvider;
import net.malisis.core.util.EntityUtils;
import net.malisis.doors.door.item.CustomDoorItem;
import net.malisis.doors.door.renderer.CustomDoorRenderer;
import net.malisis.doors.door.tileentity.CustomDoorTileEntity;
import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.ImmutableList;

/**
 * @author Ordinastie
 *
 */
@MalisisRendered(CustomDoorRenderer.class)
public class CustomDoor extends Door
{
	public CustomDoor()
	{
		super(Material.wood);
		setHardness(3.0F);
		setStepSound(soundTypeWood);
		setUnlocalizedName("customDoor");
	}

	@Override
	public String getRegistryName()
	{
		return "customDoor";
	}

	@Override
	public IIconProvider getIconProvider()
	{
		return null;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		if (isTop(state))
			return null;

		return new CustomDoorTileEntity();
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, net.minecraft.util.BlockPos pos)
	{
		DoorTileEntity te = Door.getDoor(world, pos);
		if (!(te instanceof CustomDoorTileEntity))
			return null;

		return CustomDoorItem.fromTileEntity((CustomDoorTileEntity) te);
	}

	@Override
	public boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
	{
		if (!player.capabilities.isCreativeMode)
		{
			DoorTileEntity te = Door.getDoor(world, pos);
			if (!(te instanceof CustomDoorTileEntity))
				return true;
			if (!te.isTopBlock(pos))
				spawnAsEntity(world, pos, CustomDoorItem.fromTileEntity((CustomDoorTileEntity) te));
		}
		return super.removedByPlayer(world, pos, player, willHarvest);
	}

	@Override
	protected ItemStack getDoorItemStack(IBlockAccess world, BlockPos pos)
	{
		DoorTileEntity te = Door.getDoor(world, pos);
		if (!(te instanceof CustomDoorTileEntity))
			return null;
		return CustomDoorItem.fromTileEntity((CustomDoorTileEntity) te);
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
	{
		return ImmutableList.of();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean addHitEffects(World world, MovingObjectPosition target, EffectRenderer effectRenderer)
	{
		DoorTileEntity door = Door.getDoor(world, target.getBlockPos());
		if (!(door instanceof CustomDoorTileEntity))
			return true;

		CustomDoorTileEntity te = (CustomDoorTileEntity) door;
		EntityUtils.addHitEffects(world, target, effectRenderer, te.getFrame(), te.getTop(), te.getBottom());

		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean addDestroyEffects(World world, BlockPos pos, EffectRenderer effectRenderer)
	{
		DoorTileEntity door = Door.getDoor(world, pos);
		if (!(door instanceof CustomDoorTileEntity))
			return true;

		CustomDoorTileEntity te = (CustomDoorTileEntity) door;
		EntityUtils.addDestroyEffects(world, pos, effectRenderer, te.getFrame(), te.getTop(), te.getBottom());

		return true;
	}

	@Override
	public int getLightOpacity(IBlockAccess world, BlockPos pos)
	{
		DoorTileEntity te = Door.getDoor(world, pos);
		return te instanceof CustomDoorTileEntity ? ((CustomDoorTileEntity) te).getLightValue() : 0;
	}

	@Override
	public int getRenderType()
	{
		return MalisisCore.malisisRenderType;
	}
}
