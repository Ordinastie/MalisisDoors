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

import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.element.Shape;
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
	private Shape baseFrameBottom;
	private Shape baseFrameTop;
	private Shape baseTopMaterial;
	private Shape baseBottomMaterial;

	private Shape frameBottom;
	private Shape frameTop;
	private Shape topMaterial;
	private Shape bottomMaterial;

	private Shape bottom;
	private Shape top;

	private Block frameBlock;
	private Block topMaterialBlock;
	private Block bottomMaterialBlock;

	private int frameMetadata;
	private int topMaterialMetadata;
	private int bottomMaterialMetadata;

	protected CustomDoorTileEntity tileEntity;

	private float width;

	@Override
	protected void initShape()
	{
		super.initShape();
		width = 1.0F / 8.0F;
		Shape frameBR = ShapePreset.Cube().setSize(width, 1, Door.DOOR_WIDTH);
		Shape frameBL = new Shape(frameBR).translate(1 - width, 0, 0);
		Shape frameB = ShapePreset.Cube().setSize(1 - 2 * width, width, Door.DOOR_WIDTH).translate(width, 0, 0);
		baseFrameBottom = Shape.fromShapes(frameBR, frameBL, frameB).scale(1, 1, 0.995F);;

		Shape frameTR = new Shape(frameBR);
		Shape frameTL = new Shape(frameBL);
		Shape frameT = new Shape(frameB).translate(0, 1 - width, 0);
		baseFrameTop = Shape.fromShapes(frameTR, frameTL, frameT).scale(1, 1, 0.995F);

		baseBottomMaterial = ShapePreset.Cube().setSize(1 - 2 * width, 1 - width, Door.DOOR_WIDTH * 0.6F)
				.translate(width, width, Door.DOOR_WIDTH * 0.2F);
		baseTopMaterial = new Shape(baseBottomMaterial).translate(0, -width, 0);
	}

	@Override
	protected void initRenderParameters()
	{
		rp = new RenderParameters();
		rp.renderAllFaces.set(true);
		rp.calculateAOColor.set(false);
		rp.useBlockBounds.set(false);
		rp.useBlockBrightness.set(false);
		rp.usePerVertexColor.set(true);
		rp.interpolateUV.set(true);
	}

	@Override
	public void render()
	{
		rp.applyTexture.set(false);
		if (renderType == TYPE_ITEM_INVENTORY)
		{
			if (itemStack.stackTagCompound == null)
				return;

			setup();
			renderInventory();

			return;
		}
		else
			super.render();
		//reset to true for destroy progress
		rp.applyTexture.set(true);
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
	protected void setup()
	{
		super.setup();
		if (renderType == TYPE_TESR_WORLD)
			setInfos(tileEntity);
		else
			setInfos(itemStack.stackTagCompound);

		if (frameBlock == null)
			return;

		rp.interpolateUV.set(true);
		rp.icon.set(frameBlock.getIcon(2, frameMetadata));

		frameBottom = new Shape(baseFrameBottom);
		applyTexture(frameBottom, rp);
		frameBottom.setColor(getColor(frameBlock));

		frameTop = new Shape(baseFrameTop);
		applyTexture(frameTop, rp);
		frameTop.setColor(getColor(frameBlock));

		rp.interpolateUV.set(false);
		rp.icon.set(topMaterialBlock.getIcon(2, topMaterialMetadata));

		topMaterial = new Shape(baseTopMaterial);
		applyTexture(topMaterial, rp);
		topMaterial.setColor(getColor(topMaterialBlock));

		rp.icon.set(bottomMaterialBlock.getIcon(2, bottomMaterialMetadata));

		bottomMaterial = new Shape(baseBottomMaterial);
		applyTexture(bottomMaterial, rp);
		bottomMaterial.setColor(getColor(bottomMaterialBlock));

		bottom = Shape.fromShapes(frameBottom, bottomMaterial);
		top = Shape.fromShapes(frameTop, topMaterial).translate(0, 1, 0);

		if (renderType == TYPE_TESR_WORLD)
		{
			if (direction == Door.DIR_SOUTH)
			{
				bottom.rotate(180, 0, 1, 0);
				top.rotate(180, 0, 1, 0);
			}
			if (direction == Door.DIR_EAST)
			{
				bottom.rotate(-90, 0, 1, 0);
				top.rotate(-90, 0, 1, 0);
			}
			if (direction == Door.DIR_WEST)
			{
				bottom.rotate(90, 0, 1, 0);
				top.rotate(90, 0, 1, 0);
			}
		}
		else
		{
			if (itemRenderType == ItemRenderType.INVENTORY)
			{
				bottom.rotate(45, 0, 1, 0).scale(0.9F, 0.8F, 1).translate(0, -1F, 0);
				top.rotate(45, 0, 1, 0).scale(0.9F, 0.8F, 1).translate(0, -1.2F, 0);
			}
			else if (itemRenderType == ItemRenderType.EQUIPPED_FIRST_PERSON)
			{
				bottom.rotate(90, 0, 1, 0);
				top.rotate(90, 0, 1, 0);
			}
			else if (itemRenderType == ItemRenderType.ENTITY)
			{
				bottom.translate(-0.5F, -0.5F, -0.25F).scale(0.5F);
				top.translate(-0.5F, -1F, -0.25F).scale(0.5F);
			}
			else if (itemRenderType == ItemRenderType.EQUIPPED)
			{
				bottom.rotate(180, 0, 1, 0);
				top.rotate(180, 0, 1, 0);
			}
		}
	}

	private int getColor(Block block)
	{
		if (block == Blocks.grass)
			return 0xFFFFFF;
		return renderType == TYPE_TESR_WORLD ? block.colorMultiplier(world, x, y, z) : block.getBlockColor();
	}

	@Override
	protected void renderTileEntity()
	{
		ar.setStartTime(tileEntity.getStartTime());

		enableBlending();
		if (tileEntity.getMovement() != null)
		{
			ar.animate(bottom, tileEntity.getMovement().getBottomTransformation(tileEntity));
			ar.animate(s, tileEntity.getMovement().getBottomTransformation(tileEntity));
		}
		drawShape(bottom, rp);

		if (tileEntity.getMovement() != null)
			ar.animate(top, tileEntity.getMovement().getTopTransformation(tileEntity));
		drawShape(top, rp);
	}

	private void renderInventory()
	{
		bindTexture(TextureMap.locationBlocksTexture);

		enableBlending();
		drawShape(new Shape(top), rp);
		drawShape(new Shape(bottom), rp);

	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
	{
		return true;
	}
}
