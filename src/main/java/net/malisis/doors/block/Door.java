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

import static com.google.common.base.Preconditions.*;

import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.Lists;

import net.malisis.core.MalisisCore;
import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.block.IBoundingBox;
import net.malisis.core.block.IComponent;
import net.malisis.core.block.IComponentProvider;
import net.malisis.core.block.IRegisterable;
import net.malisis.core.renderer.MalisisRendered;
import net.malisis.core.renderer.icon.provider.IIconProvider;
import net.malisis.core.util.AABBUtils;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.core.util.raytrace.RaytraceBlock;
import net.malisis.doors.DoorDescriptor;
import net.malisis.doors.DoorDescriptor.RedstoneBehavior;
import net.malisis.doors.DoorState;
import net.malisis.doors.gui.DigicodeGui;
import net.malisis.doors.iconprovider.DoorIconProvider;
import net.malisis.doors.renderer.DoorRenderer;
import net.malisis.doors.tileentity.DoorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 *
 */
@MalisisRendered(item = DoorRenderer.class)
public class Door extends BlockDoor implements IBoundingBox, IComponentProvider, IRegisterable
{
	public static Block[] centerBlocks = new Block[] {	Blocks.IRON_BARS,
														Blocks.COBBLESTONE_WALL,
														Blocks.SPRUCE_FENCE,
														Blocks.BIRCH_FENCE,
														Blocks.JUNGLE_FENCE,
														Blocks.DARK_OAK_FENCE,
														Blocks.ACACIA_FENCE };

	public static final float DOOR_WIDTH = 0.1875F;

	protected DoorDescriptor descriptor;
	protected final List<IComponent> components = Lists.newArrayList();

	public Door(DoorDescriptor desc)
	{
		super(desc.getMaterial());

		this.descriptor = checkNotNull(desc);

		setHardness(desc.getHardness());
		setSoundType(desc.getSoundType());
		setUnlocalizedName(desc.getName());

		if (MalisisCore.isClient())
			addComponent(getIconProvider());

	}

	public Door(Material material)
	{
		super(material);
	}

	public DoorDescriptor getDescriptor()
	{
		return descriptor;
	}

	@Override
	public Item getItem(Block block)
	{
		return null;
	}

	@Override
	public String getName()
	{
		return descriptor.getName();
	}

	@Override
	public void addComponent(IComponent component)
	{
		components.add(component);
	}

	@Override
	public List<IComponent> getComponents()
	{
		return components;
	}

	@SideOnly(Side.CLIENT)
	protected IIconProvider getIconProvider()
	{
		return new DoorIconProvider(descriptor);
	}

	// #region Events

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack itemStack)
	{
		DoorTileEntity te = Door.getDoor(world, pos);
		if (te == null)
			return;
		te.onBlockPlaced(this, itemStack);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		DoorTileEntity te = Door.getDoor(world, pos);
		if (te == null)
			return true;

		if (te.getDescriptor() == null)
			return true;

		if (te.getDescriptor().hasCode() && !te.isOpened())
		{
			if (world.isRemote)
				new DigicodeGui(te).display();
			return true;
		}

		if (world.isRemote)
			return true;

		if (te.getDescriptor().getRedstoneBehavior() == RedstoneBehavior.REDSTONE_ONLY)
			return true;

		if (te.getDescriptor().getRedstoneBehavior() == RedstoneBehavior.REDSTONE_LOCK)
		{
			if (te.isPowered())
				return true;
			DoorTileEntity door = te.getDoubleDoor();
			if (door != null && door.isPowered())
				return true;
		}

		if (te.getDescriptor().getAutoCloseTime() > 0 && !te.isOpened())
			world.scheduleBlockUpdate(pos, this, te.getDescriptor().getAutoCloseTime() + te.getDescriptor().getOpeningTime(), 0);

		te.openOrCloseDoor();

		return true;
	}

	/**
	 * Called from villagers AI opening doors
	 */

	@Override
	public void toggleDoor(World world, BlockPos pos, boolean opening)
	{
		DoorTileEntity te = Door.getDoor(world, pos);
		if (te == null)
			return;

		if (te.getDescriptor().hasCode())
			return;

		if (te.getDescriptor().getRedstoneBehavior() == RedstoneBehavior.REDSTONE_ONLY)
			return;

		if (te.getDescriptor().getRedstoneBehavior() == RedstoneBehavior.REDSTONE_LOCK && te.isPowered())
			return;

		if (opening == te.isOpened())
			return;

		te.openOrCloseDoor();
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos)
	{
		if (!Door.isTop(state))
		{
			//current block is bottom
			ItemStack itemStack = null;
			IBlockState upState = world.getBlockState(pos.up());
			//top is not door anymore
			if (upState.getBlock() != this)
			{
				//remove this block
				itemStack = getDoorItemStack(world, pos);
				world.setBlockToAir(pos);
			}

			//check if still on ground
			if (!world.getBlockState(pos.down()).isSideSolid(world, pos.down(), EnumFacing.UP))
			{
				itemStack = getDoorItemStack(world, pos);
				world.setBlockToAir(pos);
				//remove top block too
				if (upState.getBlock() == this)
					world.setBlockToAir(pos.up());
			}

			if (itemStack != null)
			{
				if (!world.isRemote)
					spawnAsEntity(world, pos, itemStack);
			}
			else
			{
				//handle redstone interactions
				DoorTileEntity te = getDoor(world, pos);
				if (te == null || te.getDescriptor() == null)
					return;

				if (te.getDescriptor().getRedstoneBehavior() == RedstoneBehavior.HAND_ONLY
						|| te.getDescriptor().getRedstoneBehavior() == RedstoneBehavior.REDSTONE_LOCK)
					return;

				//digicode doors can only be opened by hand
				if (te.getDescriptor().hasCode())
					return;

				boolean powered = te.isPowered();
				if ((powered || block.getDefaultState().canProvidePower()) && block != this)
					te.setPowered(powered);

				//center check
				boolean centered = te.shouldCenter();
				DoorTileEntity dd = te.getDoubleDoor();
				if (dd != null)
				{
					centered |= dd.shouldCenter();
					dd.setCentered(centered);
				}

				te.setCentered(centered);
			}
		}
		else
		{
			//current block is top
			IBlockState downState = world.getBlockState(pos.down());
			if (downState.getBlock() != this) //bottom is not door anymore
				world.setBlockToAir(pos);
			else if (block != this) //pass the neighbor block change to bottom
				neighborChanged(downState, world, pos.down(), block, fromPos);
		}
	}

	// #end Events

	protected ItemStack getDoorItemStack(IBlockAccess world, BlockPos pos)
	{
		DoorTileEntity te = getDoor(world, pos);
		return te != null ? te.getItemStack() : null;
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
	{
		return getDoorItemStack(world, pos);
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
	{
		//		if (!player.capabilities.isCreativeMode)
		//		{
		//			DoorTileEntity te = Door.getDoor(world, pos);
		//			if (te != null)
		//				spawnAsEntity(world, pos, getDoorItemStack(world, pos));
		//		}
		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
	{
		return Lists.newArrayList();
	}

	//#region BoundingBox

	@Override
	public AxisAlignedBB getBoundingBox(IBlockAccess world, BlockPos pos, IBlockState state, BoundingBoxType type)
	{
		DoorTileEntity te = Door.getDoor(world, pos);
		if (te == null || te.isMoving() || te.getMovement() == null)
			return null;

		AxisAlignedBB aabb = te.isOpened()	? te.getMovement().getOpenBoundingBox(te, te.isTopBlock(pos), type)
											: te.getMovement().getClosedBoundingBox(te, te.isTopBlock(pos), type);

		if (aabb != null && te.isCentered())
			aabb = aabb.offset(0, 0, 0.5F - Door.DOOR_WIDTH / 2);

		aabb = AABBUtils.rotate(aabb, te.getDirection());

		return aabb;
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity entityIn, boolean useActualState)
	{
		AxisAlignedBB[] aabbs = getBoundingBoxes(world, pos, state, BoundingBoxType.COLLISION);
		for (AxisAlignedBB aabb : AABBUtils.offset(pos, aabbs))
		{
			if (aabb != null && mask.intersects(aabb))
				list.add(aabb);
		}
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos)
	{
		AxisAlignedBB[] aabbs = getBoundingBoxes(world, pos, state, BoundingBoxType.SELECTION);
		if (ArrayUtils.isEmpty(aabbs) || aabbs[0] == null)
			return AABBUtils.empty(pos);

		return AABBUtils.offset(pos, aabbs)[0];
	}

	@Override
	public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d src, Vec3d dest)
	{
		return new RaytraceBlock(world, src, dest, pos).trace();
	}

	//#end BoudingBox

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
	{
		DoorTileEntity te = Door.getDoor(world, pos);
		if (te == null)
			return;

		if (te.getDescriptor().getAutoCloseTime() <= 0)
			return;

		if (te.getState() == DoorState.CLOSED || te.getState() == DoorState.CLOSING)
			return;

		te.openOrCloseDoor();
	}

	@Override
	public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		//allows fences and iron bars to connect to the doors
		Block b = world.getBlockState(pos.offset(side)).getBlock();
		return ArrayUtils.contains(centerBlocks, b);
		//if(side == ForgeDirection.UP|| side == ForgeDirection.DOWN) super.isSideSolid(world, x, y, z, side);
	}

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return !isTop(state);
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		if (isTop(state))
			return null;

		DoorTileEntity te;
		try
		{
			te = descriptor.getTileEntityClass().newInstance();
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			te = new DoorTileEntity();
		}
		te.setDescriptor(descriptor);
		return te;
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	public static DoorTileEntity getDoor(IBlockAccess world, BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);
		if (isTop(state))
			pos = pos.down();

		DoorTileEntity te = TileEntityUtils.getTileEntity(DoorTileEntity.class, world, pos);
		if (te != null && te.getWorld() == null && world instanceof World)
		{
			MalisisCore.log.error("[MalisisDoors] DoorTileEntity found without a world!");
			te.setPos(pos);
			te.setWorld((World) world);
		}
		return te;
	}

	public static boolean isTop(IBlockState state)
	{
		return state.getBlock() instanceof Door && state.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER;
	}
}
