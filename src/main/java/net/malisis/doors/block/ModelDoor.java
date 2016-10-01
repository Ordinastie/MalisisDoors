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
import net.malisis.core.block.component.BooleanComponent;
import net.malisis.core.block.component.DirectionalComponent;
import net.malisis.core.block.component.ItemTransformComponent;
import net.malisis.core.renderer.component.AnimatedModelComponent;
import net.malisis.core.util.AABBUtils;
import net.malisis.core.util.Timer;
import net.malisis.core.util.TransformBuilder;
import net.malisis.core.util.chunkcollision.IChunkCollidable;
import net.malisis.doors.MalisisDoors;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
public class ModelDoor extends MalisisBlock implements IChunkCollidable
{
	private static BooleanComponent OPENED = new BooleanComponent("opened", false, 2);
	private AxisAlignedBB aabb = AABBUtils.identity();
	private AnimatedModelComponent amc = null;
	private ItemTransformComponent transformComp = new ItemTransformComponent();

	public ModelDoor()
	{
		super(Material.IRON);
		setCreativeTab(MalisisDoors.tab);

		//params
		setName(MalisisDoors.modid + ":hitechdoor");
		setTexture(MalisisDoors.modid + ":blocks/hitechdoor");
		aabb = new AxisAlignedBB(-1, 0, 0.375F, 2, 2, 0.625F);

		addComponent(OPENED);
		addComponent(new DirectionalComponent());

		if (MalisisCore.isClient())
		{
			amc = new AnimatedModelComponent(MalisisDoors.modid + ":models/hitechdoor.obj");
			amc.onFirstRender(this::stateCheck);
			addComponent(amc);

			setTransform();
			addComponent(transformComp);
		}
	}

	private void setTransform()
	{
		Matrix4f gui = new TransformBuilder().translate(.0F, -0.15F, 0).rotate(30, 45, 0).scale(.34F).get();
		Matrix4f firstPerson = new TransformBuilder().rotate(0, 135, 0).scale(0.2F).get();
		Matrix4f thirdPerson = new TransformBuilder().translate(0, 0.155F, -0.1F).rotateAfter(75, 100, 0).scale(0.25F).get();
		Matrix4f fixed = new TransformBuilder().translate(0, -.2F, 0).scale(0.4F).get();
		Matrix4f ground = new TransformBuilder().translate(0, 0.3F, 0).scale(0.20F).get();

		transformComp.thirdPerson(thirdPerson, thirdPerson).firstPerson(firstPerson, firstPerson).fixed(fixed).gui(gui).ground(ground);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		boolean opened = OPENED.invert(world, pos);
		if (world.isRemote)
			amc.link(pos, opened ? "close" : "open", opened ? "open" : "close");

		return true;
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
		if (OPENED.get(state) && !amc.isAnimating(pos, "open"))
			amc.start(pos, "open", new Timer(Integer.MIN_VALUE));
	}

}
