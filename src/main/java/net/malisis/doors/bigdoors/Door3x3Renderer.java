package net.malisis.doors.bigdoors;

import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.animation.AnimationRenderer;
import net.malisis.core.renderer.element.shape.Cube;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.doors.block.Door;
import net.minecraft.util.EnumFacing;

public class Door3x3Renderer extends MalisisRenderer<Door3x3Tile>
{
	private MalisisModel model;
	private RenderParameters rp;
	private AnimationRenderer ar = new AnimationRenderer();

	public Door3x3Renderer()
	{
		registerFor(Door3x3Tile.class);
		ensureBlock(Door3x3.class);
		getBlockDamage = true;
	}

	@Override
	protected void initialize()
	{
		Cube doorLeft = new Cube();
		doorLeft.setSize(0.5f, 3f, Door.DOOR_WIDTH / 3.01f);
		doorLeft.interpolateUV();
		doorLeft.setSize(1.5f, 3f, Door.DOOR_WIDTH);
		doorLeft.scale(1, 1, 0.995f);
		doorLeft.translate(-1f, 0, 1 - Door.DOOR_WIDTH);

		Cube doorRight = new Cube();
		doorRight.setSize(1, 3, Door.DOOR_WIDTH / 3.01f);
		doorRight.limit(0.5f, 0, 0, 1, 3, Door.DOOR_WIDTH);
		doorRight.interpolateUV();
		doorRight.setSize(1.5f, 3f, Door.DOOR_WIDTH);
		doorRight.scale(1, 1, 0.995f);
		doorRight.translate(0, 0, 1 - Door.DOOR_WIDTH);

		model = new MalisisModel();
		model.addShape("left", doorLeft);
		model.addShape("right", doorRight);
		model.storeState();

		rp = new RenderParameters();
		rp.useBlockBounds.set(false);
		rp.interpolateUV.set(false);
	}

	@Override
	public void render()
	{
		if (tileEntity == null)
			return;

		setup();

		if (renderType == RenderType.TILE_ENTITY)
			renderTileEntity();
	}

	private void renderTileEntity()
	{
		ar.setStartTime(tileEntity.getTimer().getStart());

		if (tileEntity.getMovement() != null)
		{
			Animation<?>[] anims = tileEntity.getMovement().getAnimations(tileEntity, model, rp);
			ar.animate(anims);
		}

		model.render(this, rp);
	}

	private void setup()
	{
		model.resetState();
		EnumFacing direction = tileEntity.getDirection();
		if (direction == EnumFacing.NORTH)
			model.rotate(180, 0, 1, 0, 0, 0, 0);
		else if (direction == EnumFacing.WEST)
			model.rotate(-90, 0, 1, 0, 0, 0, 0);
		else if (direction == EnumFacing.EAST)
			model.rotate(90, 0, 1, 0, 0, 0, 0);

		rp.brightness.set(blockState.getPackedLightmapCoords(world, pos));
	}
}
