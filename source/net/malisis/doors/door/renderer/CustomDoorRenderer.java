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

import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.face.BottomFace;
import net.malisis.core.renderer.element.face.NorthFace;
import net.malisis.core.renderer.element.face.SouthFace;
import net.malisis.core.renderer.element.face.TopFace;
import net.malisis.core.renderer.element.shape.Cube;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.doors.door.block.Door;
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
	private Block frameBlock;
	private Block topMaterialBlock;
	private Block bottomMaterialBlock;

	private int frameMetadata;
	private int topMaterialMetadata;
	private int bottomMaterialMetadata;

	protected CustomDoorTileEntity tileEntity;

	private float width;

	@Override
	protected void initialize()
	{
		width = 1.0F / 8.0F;
		/**
		 * BOTTOM
		 */
		//frame right
		Shape frameR = new Cube().setSize(width, 1, Door.DOOR_WIDTH);
		//frame left
		Shape frameL = new Shape(frameR);
		frameL.translate(1 - width, 0, 0);
		//frame horizontal
		Shape frameH = new Shape(new NorthFace(), new SouthFace(), new TopFace(), new BottomFace());
		frameH.setSize(1 - 2 * width, width, Door.DOOR_WIDTH);
		frameH.translate(width, 0, 0);

		//full bottom frame
		Shape frame = Shape.fromShapes(frameR, frameL, frameH);
		frame.scale(1, 1, 0.995F); //scale frame to prevent z-fighting when slid in walls
		frame.applyMatrix();

		//bottom material
		Shape mat = new Shape(new SouthFace(), new NorthFace(), new TopFace());
		mat.setSize(1 - 2 * width, 1 - width, Door.DOOR_WIDTH * 0.6F).translate(width, width, Door.DOOR_WIDTH * 0.2F);
		mat.applyMatrix();

		Shape bottom = new Shape();
		bottom.addFaces(frame.getFaces(), "frame");
		bottom.addFaces(mat.getFaces(), "material");
		bottom.interpolateUV();
		bottom.storeState();

		/**
		 * TOP
		 */
		frameR = new Shape(frameR);
		frameL = new Shape(frameL);
		frameH = new Shape(frameH);
		frameH.translate(0, 1 - width, 0);

		//full top frame
		frame = Shape.fromShapes(frameR, frameL, frameH);
		frame.scale(1, 1, 0.995F); //scale frame to prevent z-fighting when slid in walls
		frame.applyMatrix();

		//top material
		mat = new Shape(mat);
		mat.translate(0, -width, 0);
		mat.applyMatrix();

		Shape top = new Shape();
		top.addFaces(frame.getFaces(), "frame");
		top.addFaces(mat.getFaces(), "material");
		top.translate(0, 1, 0);
		top.interpolateUV();
		top.storeState();

		//store top and bottom inside a model
		model = new MalisisModel();
		model.addShape("bottom", bottom);
		model.addShape("top", top);
		model.storeState();

		initParams();
	}

	@Override
	public void render()
	{
		initialize();
		if (renderType == RenderType.ITEM_INVENTORY)
		{
			if (itemStack.stackTagCompound == null)
				return;
			renderInventory();
			return;
		}

		super.render();
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
		model.resetState();

		if (renderType == RenderType.TESR_WORLD)
			setInfos(tileEntity);
		else
			setInfos(itemStack.stackTagCompound);

		if (frameBlock == null)
			return;

		setupParams(true);
		setupParams(false);

		if (renderType == RenderType.TESR_WORLD)
			super.setup();
		else
		{
			if (itemRenderType == ItemRenderType.INVENTORY)
			{
				model.rotate(45, 0, 1, 0, 0, 0, 0);
				model.scale(0.9F, 0.8F, 1, 0, 0, 0);
				model.translate(0, -1F, 0);
			}
			else if (itemRenderType == ItemRenderType.EQUIPPED_FIRST_PERSON)
				model.rotate(90, 0, 1, 0, 0, 0, 0);
			else if (itemRenderType == ItemRenderType.ENTITY)
			{
				model.translate(-0.5F, -0.5F, -0.25F);
				model.scale(0.5F, 0.5F, 0.5F, 0, 0, 0);
			}
			else if (itemRenderType == ItemRenderType.EQUIPPED)
				model.rotate(180, 0, 1, 0, 0, 0, 0);
		}
	}

	private void setupParams(boolean topBlock)
	{
		//reset alpha before so it doesn't bleed to the shapes
		rp.alpha.reset();

		Shape s = model.getShape(topBlock ? "top" : "bottom");
		rp.icon.set(frameBlock.getIcon(2, frameMetadata));
		rp.colorMultiplier.set(getColor(frameBlock));
		s.setParameters("frame", rp, true);

		Block block = topBlock ? topMaterialBlock : bottomMaterialBlock;
		int meta = topBlock ? topMaterialMetadata : bottomMaterialMetadata;
		rp.icon.set(block.getIcon(2, meta));
		rp.colorMultiplier.set(getColor(block));
		s.setParameters("material", rp, true);

		//reset the values to default as rp is used for the whole shape
		rp.icon.reset();
		rp.colorMultiplier.reset();
	}

	private int getColor(Block block)
	{
		if (block == Blocks.grass)
			return 0xFFFFFF;
		return renderType == RenderType.TESR_WORLD ? block.colorMultiplier(world, x, y, z) : block.getBlockColor();
	}

	private void renderInventory()
	{
		bindTexture(TextureMap.locationBlocksTexture);
		enableBlending();

		setup();

		model.render(this, rp);
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
	{
		return true;
	}
}
