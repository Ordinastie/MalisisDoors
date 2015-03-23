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

package net.malisis.doors.block;

import net.malisis.core.inventory.IInventoryProvider;
import net.malisis.core.inventory.MalisisInventory;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.entity.BlockMixerTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockMixer extends Block implements ITileEntityProvider
{
	private IIcon frontIcon;

	public BlockMixer()
	{
		super(Material.iron);
		setCreativeTab(MalisisDoors.tab);
		setHardness(3.0F);
		setUnlocalizedName("block_mixer");
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister iconRegister)
	{
		this.blockIcon = iconRegister.registerIcon(MalisisDoors.modid + ":" + (this.getUnlocalizedName().substring(5)) + "_side");
		this.frontIcon = iconRegister.registerIcon(MalisisDoors.modid + ":" + (this.getUnlocalizedName().substring(5)));
	}

	@Override
	public IIcon getIcon(int side, int metadata)
	{
		if ((metadata != 0 && side == metadata) || (metadata == 0 && side == 3))
			return frontIcon;
		return blockIcon;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack)
	{
		int side = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		int metadata = 0;
		if (side == 0)
			metadata = 2;
		if (side == 1)
			metadata = 5;
		if (side == 2)
			metadata = 3;
		if (side == 3)
			metadata = 4;
		world.setBlockMetadataWithNotify(x, y, z, metadata, 2);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int metadata, float hitX, float hitY, float hitZ)
	{
		if (world.isRemote)
			return true;

		if (player.isSneaking())
			return false;

		IInventoryProvider te = TileEntityUtils.getTileEntity(IInventoryProvider.class, world, x, y, z);
		MalisisInventory.open((EntityPlayerMP) player, te);
		return true;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int metadata)
	{
		IInventoryProvider provider = TileEntityUtils.getTileEntity(IInventoryProvider.class, world, x, y, z);
		for (MalisisInventory inventory : provider.getInventories())
			inventory.breakInventory(world, x, y, z);
		super.breakBlock(world, x, y, z, block, metadata);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metdata)
	{
		return new BlockMixerTileEntity();
	}
}
