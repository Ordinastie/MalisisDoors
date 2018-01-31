package net.malisis.doors.movement;

import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.animation.transformation.Rotation;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.doors.DoorState;
import net.malisis.doors.block.Door;
import net.malisis.doors.tileentity.DoorTileEntity;
import net.minecraft.util.math.AxisAlignedBB;

public class Door3x3Movement implements IDoorMovement
{

	@Override
	public AxisAlignedBB getOpenBoundingBox(DoorTileEntity doorTileEntity, boolean left, BoundingBoxType boundingBoxType)
	{
		return null;
	}

	private Rotation getRotation(DoorTileEntity tile, boolean right)
	{
		float fz = 0.5f - Door.DOOR_WIDTH / 2;
		float fx = -1.5f + Door.DOOR_WIDTH / 2;
		float angle = 90;

		if (right)
		{
			fx = 1.5f - Door.DOOR_WIDTH / 2;
			angle = -angle;
		}

		Rotation rotation = new Rotation(angle).aroundAxis(0, 1, 0).offset(fx, 0, fz);
		rotation.reversed(tile.getState() == DoorState.CLOSING || tile.getState() == DoorState.CLOSED);
		rotation.forTicks(tile.getDescriptor().getOpeningTime());

		return rotation;
	}

	@Override
	public Animation<?>[] getAnimations(DoorTileEntity tile, MalisisModel model, RenderParameters renderParameters)
	{
		return new Animation[] {	new Animation<>(model.getShape("left"), getRotation(tile, false)),
									new Animation<>(model.getShape("right"), getRotation(tile, true)) };
	}

	@Override
	public boolean isSpecial()
	{
		return true;
	}
}
