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

package net.malisis.doors.door.renderer;

import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.preset.FacePreset;
import net.malisis.core.renderer.preset.ShapePreset;
import net.malisis.doors.door.Door;
import net.malisis.doors.door.tileentity.CustomDoorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @author Ordinastie
 * 
 */
public class CustomDoorRenderer extends DoorRenderer
{
	private Shape top;
	private Shape bottom;

	private Block frameBlock;
	private Block topMaterialBlock;
	private Block bottomMaterialBlock;

	private int frameMetadata;
	private int topMaterialMetadata;
	private int bottomMaterialMetadata;

	protected CustomDoorTileEntity tileEntity;

	private float width;

	@Override
	protected void initShapes()
	{
		width = 1.0F / 8.0F;
		Shape frameR = ShapePreset.Cube().setSize(width, 1, Door.DOOR_WIDTH);
		Shape frameL = new Shape(frameR).translate(1 - width, 0, 0);
		Shape frame = ShapePreset.Cube().setSize(1 - 2 * width, width, Door.DOOR_WIDTH).translate(width, 0, 0);
		frame.removeFace(frame.getFace("east")).removeFace(frame.getFace("west"));
		frame = Shape.fromShapes(frameR, frameL, frame).scale(1, 1, 0.995F); //scale frame to prevent z-fighting when slided in walls
		frame.interpolateUV();
		Shape mat = new Shape(new Face[] { FacePreset.South(), FacePreset.North(), FacePreset.Top() });
		mat.setSize(1 - 2 * width, 1 - width, Door.DOOR_WIDTH * 0.6F).translate(width, width, Door.DOOR_WIDTH * 0.2F);
		mat.applyMatrix();

		bottom = new Shape();
		bottom.addFaces(frame.getFaces(), "frame");
		bottom.addFaces(mat.getFaces(), "material");
		bottom.storeState();

		top = new Shape(bottom).rotate(180, 0, 0, 1);
		top.storeState();

	}

	@Override
	public void render()
	{
		if (renderType == TYPE_ITEM_INVENTORY)
		{
			if (itemStack.stackTagCompound == null)
				return;
			renderInventory();
			return;
		}
		else
		{
			//initShape();
			super.render();
		}
	}

	@Override
	protected void setTileEntity()
	{
		super.setTileEntity();
		this.tileEntity = (CustomDoorTileEntity) super.tileEntity;
	}

	private void setInfos(CustomDoorTileEntity te)
	{
		frameBlock = te.getFrame();
		topMaterialBlock = te.getTopMaterial();
		bottomMaterialBlock = te.getBottomMaterial();

		frameMetadata = te.getFrameMetadata();
		topMaterialMetadata = te.getTopMaterialMetadata();
		bottomMaterialMetadata = te.getBottomMaterialMetadata();
	}

	private void setInfos(NBTTagCompound nbt)
	{
		frameBlock = Block.getBlockById(nbt.getInteger("frame"));
		topMaterialBlock = Block.getBlockById(nbt.getInteger("topMaterial"));
		bottomMaterialBlock = Block.getBlockById(nbt.getInteger("bottomMaterial"));

		frameMetadata = nbt.getInteger("frameMetadata");
		topMaterialMetadata = nbt.getInteger("topMaterialMetadata");
		bottomMaterialMetadata = nbt.getInteger("bottomMaterialMetadata");
	}

	@Override
	protected void setup(boolean topBlock)
	{
		shape = topBlock ? top : bottom;

		shape.resetState();

		if (renderType == TYPE_TESR_WORLD)
			setInfos(tileEntity);
		else
			setInfos(itemStack.stackTagCompound);

		if (frameBlock == null)
			return;

		rp.icon.set(frameBlock.getIcon(2, frameMetadata));
		rp.colorMultiplier.set(getColor(frameBlock));
		shape.setParameters("frame", rp, true);

		Block block = topBlock ? topMaterialBlock : bottomMaterialBlock;
		int meta = topBlock ? topMaterialMetadata : bottomMaterialMetadata;
		rp.icon.set(block.getIcon(2, meta));
		rp.colorMultiplier.set(getColor(block));
		shape.setParameters("material", rp, true);

		//reset the values to default as rp is used for the whole shape 
		rp.icon.reset();
		rp.colorMultiplier.reset();

		if (renderType == TYPE_TESR_WORLD)
			super.setup(topBlock);
		else
		{
			if (itemRenderType == ItemRenderType.INVENTORY)
				shape.rotate(45, 0, 1, 0).scale(0.9F, 0.8F, 1).translate(0, -1F, 0);
			else if (itemRenderType == ItemRenderType.EQUIPPED_FIRST_PERSON)
				shape.rotate(90, 0, 1, 0);
			else if (itemRenderType == ItemRenderType.ENTITY)
				shape.translate(-0.5F, -0.5F, -0.25F).scale(0.5F);
			else if (itemRenderType == ItemRenderType.EQUIPPED)
				shape.rotate(180, 0, 1, 0);

			if (topBlock)
				shape.translate(0, 1, 0);
		}
	}

	private int getColor(Block block)
	{
		if (block == Blocks.grass)
			return 0xFFFFFF;
		return renderType == TYPE_TESR_WORLD ? block.colorMultiplier(world, x, y, z) : block.getBlockColor();
	}

	private void renderInventory()
	{
		bindTexture(TextureMap.locationBlocksTexture);
		enableBlending();

		setup(false);
		drawShape(shape, rp);

		setup(true);
		drawShape(shape, rp);

	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
	{
		return true;
	}
}
