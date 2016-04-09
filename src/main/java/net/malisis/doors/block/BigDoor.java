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

import java.util.List;

import net.malisis.core.MalisisCore;
import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.block.component.DirectionalComponent;
import net.malisis.core.renderer.DefaultRenderer;
import net.malisis.core.renderer.MalisisRendered;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.malisis.core.renderer.icon.provider.IIconProvider;
import net.malisis.core.util.AABBUtils;
import net.malisis.core.util.MBlockState;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.core.util.chunkcollision.ChunkCollision;
import net.malisis.core.util.chunkcollision.IChunkCollidable;
import net.malisis.core.util.chunklistener.IBlockListener;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.renderer.BigDoorRenderer;
import net.malisis.doors.tileentity.BigDoorTileEntity;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.google.common.collect.Lists;

/**
 * @author Ordinastie
 *
 */
@MalisisRendered(block = BigDoorRenderer.class, item = DefaultRenderer.Item.class)
public class BigDoor extends MalisisBlock implements ITileEntityProvider, IChunkCollidable, IBlockListener
{
	public enum Type
	{
		CARRIAGE("carriage_door", Items.oak_door), MEDIEVAL("medieval_door", Items.spruce_door);

		public String name;
		public Item door;

		private Type(String name, Item door)
		{
			this.name = name;
			this.door = door;
		}
	}

	private AxisAlignedBB defaultBoundingBox = new AxisAlignedBB(0, 0, 1 - Door.DOOR_WIDTH, 4, 5, 1);

	public BigDoor(Type type)
	{
		super(Material.wood);
		setHardness(5.0F);
		setResistance(10.0F);
		setSoundType(SoundType.STONE);
		setName(type.name);
		setCreativeTab(MalisisDoors.tab);

		addComponent(new DirectionalComponent());

		if (MalisisCore.isClient())
			addComponent(BigDoorIconProvider.get(type));
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		super.onBlockPlacedBy(world, pos, state, placer, stack);

		ChunkCollision.get().replaceBlocks(world, new MBlockState(world, pos));

		BigDoorTileEntity te = TileEntityUtils.getTileEntity(BigDoorTileEntity.class, world, pos);
		if (te != null)
			te.setFrameState(MBlockState.fromNBT(stack.getTagCompound()));
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (world.isRemote)
			return true;

		BigDoorTileEntity te = TileEntityUtils.getTileEntity(BigDoorTileEntity.class, world, pos);
		if (te == null)
			return true;

		te.openOrCloseDoor();
		return true;
	}

	@Override
	public AxisAlignedBB[] getBoundingBoxes(IBlockAccess world, BlockPos pos, IBlockState state, BoundingBoxType type)
	{
		if (type == BoundingBoxType.PLACEDBOUNDINGBOX)
			return new AxisAlignedBB[] { defaultBoundingBox };
		BigDoorTileEntity te = TileEntityUtils.getTileEntity(BigDoorTileEntity.class, world, pos);
		if (te == null)
			return AABBUtils.identities();

		AxisAlignedBB[] aabbs = new AxisAlignedBB[] { defaultBoundingBox };
		if ((type == BoundingBoxType.COLLISION || type == BoundingBoxType.RAYTRACE) && (te.isOpened() || te.isMoving()))
		{
			aabbs = new AxisAlignedBB[] { new AxisAlignedBB(0, 0, -0.5F, 0.5F, 4, 1), new AxisAlignedBB(3.5F, 0, -0.5F, 4, 4, 1),
					new AxisAlignedBB(0, 4, 1 - Door.DOOR_WIDTH, 4, 5, 1) };
		}

		return aabbs;
	}

	@Override
	public int blockRange()
	{
		return 5;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata)
	{
		return new BigDoorTileEntity();
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
	{
		if (!player.capabilities.isCreativeMode)
		{
			BigDoorTileEntity te = TileEntityUtils.getTileEntity(BigDoorTileEntity.class, world, pos);
			if (te != null)
				spawnAsEntity(world, pos, te.getDroppedItemStack());
		}
		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
	{
		return Lists.newArrayList();
	}

	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state)
	{
		return false;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public boolean canRenderInLayer(BlockRenderLayer layer)
	{
		return true;
	}

	@Override
	public boolean onBlockSet(World world, BlockPos pos, MBlockState blockSet)
	{
		if (!blockSet.getBlock().isReplaceable(world, blockSet.getPos()))
			return true;

		for (AxisAlignedBB aabb : AABBUtils.getCollisionBoundingBoxes(world, new MBlockState(pos, this), true))
		{
			if (aabb != null && aabb.intersectsWith(AABBUtils.identity(blockSet.getPos())))
				return false;
		}

		return true;
	}

	@Override
	public boolean onBlockRemoved(World world, BlockPos pos, BlockPos blockPos)
	{
		return true;
	}

	public static class BigDoorIconProvider implements IIconProvider
	{
		MalisisIcon itemIcon;
		MalisisIcon doorIcon;

		public BigDoorIconProvider(Type type)
		{
			itemIcon = MalisisIcon.from(MalisisDoors.modid + ":items/" + type.name + "_item");
			doorIcon = MalisisIcon.from(MalisisDoors.modid + ":blocks/" + type.name);
		}

		@Override
		public MalisisIcon getIcon()
		{
			return itemIcon;
		}

		public MalisisIcon getDoorIcon()
		{
			return doorIcon;
		}

		public static BigDoorIconProvider get(Type type)
		{
			return new BigDoorIconProvider(type);
		}

	}

}
