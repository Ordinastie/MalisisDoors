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

import javax.vecmath.Matrix4f;

import net.malisis.core.MalisisCore;
import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.block.component.DirectionalComponent;
import net.malisis.core.block.component.ItemTransformComponent;
import net.malisis.core.block.component.PowerComponent;
import net.malisis.core.block.component.PowerComponent.ComponentType;
import net.malisis.core.block.component.PowerComponent.InteractionType;
import net.malisis.core.renderer.component.AnimatedModelComponent;
import net.malisis.core.util.AABBUtils;
import net.malisis.core.util.TransformBuilder;
import net.malisis.core.util.chunkcollision.IChunkCollidable;
import net.malisis.doors.DoorRegistry;
import net.malisis.doors.DoorState;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.sound.IDoorSound;
import net.malisis.doors.sound.PneumaticSound;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 *
 */
public class ModelDoor extends MalisisBlock implements IChunkCollidable
{
	private static PowerComponent OPENED = new PowerComponent(InteractionType.BOTH, ComponentType.RECEIVER);
	private AxisAlignedBB aabb = AABBUtils.identity();
	private AnimatedModelComponent amc = null;

	//Reuse sound
	private static final IDoorSound DOOR_SOUND = DoorRegistry.getSound(PneumaticSound.class);

	public ModelDoor()
	{
		super(Material.IRON);
		setHardness(4.0F);

		setCreativeTab(MalisisDoors.tab);

		//params
		setName(MalisisDoors.modid + ":hitechdoor");
		setTexture(MalisisDoors.modid + ":blocks/hitechdoor");
		aabb = new AxisAlignedBB(-1, 0, 0.375F, 2, 2, 0.625F);

		OPENED.setMetaOffset(2);
		OPENED.onPowerChange((w, p, powered) -> w.playSound(null,
															p,
															DOOR_SOUND.getSound(powered ? DoorState.OPENING : DoorState.CLOSING),
															SoundCategory.BLOCKS,
															1F,
															1F));
		addComponent(OPENED);
		addComponent(new DirectionalComponent());

		if (MalisisCore.isClient())
		{
			amc = new AnimatedModelComponent(MalisisDoors.modid + ":models/hitechdoor.obj");
			amc.onRender((w, p, s, a) -> a.link(p, OPENED.get(s) ? "close" : "open", OPENED.get(s) ? "open" : "close"));
			addComponent(amc);

			addComponent(getTransform());
		}
	}

	@SideOnly(Side.CLIENT)
	private ItemTransformComponent getTransform()
	{
		Matrix4f gui = new TransformBuilder().translate(.0F, -0.15F, 0).rotate(30, 45, 0).scale(.34F).get();
		Matrix4f firstPerson = new TransformBuilder().rotate(0, 135, 0).scale(0.2F).get();
		Matrix4f thirdPerson = new TransformBuilder().translate(0, 0.155F, -0.1F).rotateAfter(75, 100, 0).scale(0.25F).get();
		Matrix4f fixed = new TransformBuilder().translate(0, -.2F, 0).scale(0.4F).get();
		Matrix4f ground = new TransformBuilder().translate(0, 0.3F, 0).scale(0.20F).get();

		return new ItemTransformComponent()	.thirdPerson(thirdPerson, thirdPerson)
											.firstPerson(firstPerson, firstPerson)
											.fixed(fixed)
											.gui(gui)
											.ground(ground);
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockAccess world, BlockPos pos, IBlockState state, BoundingBoxType type)
	{
		if (OPENED.get(state) && type == BoundingBoxType.COLLISION)
			return null;

		return aabb;
	}

	@Override
	public boolean isFullBlock(IBlockState state)
	{
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state)
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}

	@Override
	public int blockRange()
	{
		return 2;
	}

	public void stateCheck(IBlockAccess world, BlockPos pos, IBlockState state, AnimatedModelComponent amc)
	{
		;

	}

}
