package net.malisis.doors.bigdoors;

import java.util.ArrayList;
import java.util.List;

import net.malisis.core.MalisisCore;
import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.block.component.DirectionalComponent;
import net.malisis.core.renderer.DefaultRenderer;
import net.malisis.core.renderer.MalisisRendered;
import net.malisis.core.renderer.icon.Icon;
import net.malisis.core.renderer.icon.provider.IBlockIconProvider;
import net.malisis.core.util.AABBUtils;
import net.malisis.core.util.MBlockState;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.core.util.chunkcollision.ChunkCollision;
import net.malisis.core.util.chunkcollision.IChunkCollidable;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.block.Door;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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

@MalisisRendered(block = Door3x3Renderer.class, item = DefaultRenderer.Item.class)
public class Door3x3 extends MalisisBlock implements ITileEntityProvider, IChunkCollidable
{
	public enum Type
	{
		OAK("big_door_oak_3x3", false),
		SPRUCE("big_door_spruce_3x3", false),
		BIRCH("big_door_birch_3x3", false),
		JUNGLE("big_door_jungle_3x3", false),
		ACACIA("big_door_acacia_3x3", false),
		DARK_OAK("big_door_dark_oak_3x3", false),
		IRON("big_door_iron_3x3", true),
		RUSTY("big_door_rusty_3x3", true);

		public String name;
		public boolean metal;

		Type(String name, boolean metal)
		{
			this.name = name;
			this.metal = metal;
		}
	}

	private Type type;
	private AxisAlignedBB defaultBoundingBox = new AxisAlignedBB(-1, 0, 1 - Door.DOOR_WIDTH, 2, 3, 1);

	public Door3x3(Type type)
	{
		super(type.metal ? Material.IRON : Material.WOOD);
		this.type = type;
		setName(type.name);

		setResistance(type.metal ? 200 : 10.0f);
		setSoundType(type.metal ? SoundType.METAL : SoundType.WOOD);
		setCreativeTab(MalisisDoors.tab);
		addComponent(new DirectionalComponent());

		if (MalisisCore.isClient())
			addComponent(Door3x3IconProvider.get(type));
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack itemStack)
	{
		super.onBlockPlacedBy(world, pos, state, player, itemStack);

		ChunkCollision.get().replaceBlocks(world, new MBlockState(world, pos));

		Door3x3Tile te = TileEntityUtils.getTileEntity(Door3x3Tile.class, world, pos);
		if (te != null)
			te.setFrameState(MBlockState.fromNBT(itemStack.getTagCompound()));
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (world.isRemote)
			return true;

		Door3x3Tile te = TileEntityUtils.getTileEntity(Door3x3Tile.class, world, pos);
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

		Door3x3Tile te = TileEntityUtils.getTileEntity(Door3x3Tile.class, world, pos);
		if (te == null)
			return AABBUtils.identities();

		AxisAlignedBB[] aabbs = new AxisAlignedBB[] { defaultBoundingBox };
		if ((type == BoundingBoxType.COLLISION || type == BoundingBoxType.RAYTRACE || type == BoundingBoxType.RENDER)
				&& (te.isOpened() || te.isMoving()))
			aabbs = new AxisAlignedBB[] {	new AxisAlignedBB(-1, 0, -.5f, Door.DOOR_WIDTH - 1, 3, 1),
											new AxisAlignedBB(2 - Door.DOOR_WIDTH, 0, -.5f, 2, 3, 1) };

		return aabbs;
	}

	@Override
	public int blockRange()
	{
		return 3;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata)
	{
		return new Door3x3Tile(type.metal);
	}

	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
	{
		if (!player.capabilities.isCreativeMode)
		{
			Door3x3Tile te = TileEntityUtils.getTileEntity(Door3x3Tile.class, world, pos);
			if (te != null)
				spawnAsEntity(world, pos, te.getDroppedItemStack());
		}

		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
	{
		return new ArrayList<>();
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
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer)
	{
		return true;
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighbor, BlockPos fromPos)
	{
		Door3x3Tile te = TileEntityUtils.getTileEntity(Door3x3Tile.class, world, pos);
		if (te == null)
			return;

		boolean powered = world.isBlockPowered(pos);
		if (powered || neighbor.getDefaultState().canProvidePower())
			te.setPowered(powered);
	}

	public static class Door3x3IconProvider implements IBlockIconProvider
	{
		Icon itemIcon;
		Icon doorIcon;

		public Door3x3IconProvider(Type type)
		{
			itemIcon = Icon.from(MalisisDoors.modid + ":items/" + type.name);
			doorIcon = Icon.from(MalisisDoors.modid + ":blocks/" + type.name);
		}

		@Override
		public Icon getIcon(IBlockState state, EnumFacing side)
		{
			return doorIcon;
		}

		@Override
		public Icon getIcon(ItemStack itemStack, EnumFacing side)
		{
			return doorIcon;
		}

		public static Door3x3IconProvider get(Type type)
		{
			return new Door3x3IconProvider(type);
		}
	}
}
