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

import net.malisis.core.MalisisCore;
import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.block.IBlockDirectional;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.renderer.DefaultRenderer;
import net.malisis.core.renderer.MalisisRendered;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.malisis.core.renderer.icon.provider.IBlockIconProvider;
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
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 *
 */
@MalisisRendered(block = BigDoorRenderer.class, item = DefaultRenderer.Item.class)
public class BigDoor extends MalisisBlock implements ITileEntityProvider, IChunkCollidable, IBlockListener, IBlockDirectional
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
	private Type type;

	public BigDoor(Type type)
	{
		super(Material.wood);
		this.type = type;
		setHardness(5.0F);
		setResistance(10.0F);
		setStepSound(soundTypeStone);
		setName(type.name);
		setCreativeTab(MalisisDoors.tab);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void createIconProvider(Object object)
	{
		iconProvider = new BigDoorIconProvider(type);
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
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ)
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
	public AxisAlignedBB[] getBoundingBoxes(IBlockAccess world, BlockPos pos, BoundingBoxType type)
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
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean isFullCube()
	{
		return false;
	}

	@Override
	public int getRenderType()
	{
		return MalisisCore.malisisRenderType;
	}

	@Override
	public boolean canRenderInLayer(EnumWorldBlockLayer layer)
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

	public static class BigDoorIconProvider implements IBlockIconProvider
	{
		MalisisIcon itemIcon;
		MalisisIcon doorIcon;

		public BigDoorIconProvider(Type type)
		{
			itemIcon = new MalisisIcon(MalisisDoors.modid + ":items/" + type.name + "_item");
			doorIcon = new MalisisIcon(MalisisDoors.modid + ":blocks/" + type.name);
		}

		@Override
		public void registerIcons(net.minecraft.client.renderer.texture.TextureMap map)
		{
			itemIcon = itemIcon.register(map);
			doorIcon = doorIcon.register(map);
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

	}

}
