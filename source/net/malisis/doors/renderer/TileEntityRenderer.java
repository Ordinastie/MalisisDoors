package net.malisis.doors.renderer;

import java.util.Random;

import net.malisis.core.renderer.BaseRenderer;
import net.malisis.core.renderer.element.RenderParameters;
import net.malisis.core.renderer.element.Shape;
import net.malisis.doors.block.Door;
import net.malisis.doors.block.SlidingDoor;
import net.malisis.doors.entity.DoorTileEntity;
import net.malisis.doors.entity.VanishingTileEntity;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

public class TileEntityRenderer extends TileEntitySpecialRenderer
{
	private Tessellator t = null;
	private Random rand = new Random();

	private void init(double x, double y, double z)
	{
		RenderHelper.disableStandardItemLighting();
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		bindTexture(TextureMap.locationBlocksTexture);

		t = Tessellator.instance;
		t.startDrawingQuads();
	}

	private void next()
	{
		t.draw();
		t.startDrawingQuads();
	}
	private void end()
	{
		t.draw();
		GL11.glPopMatrix();
	}

	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float f)
	{
		boolean test = false;
		if (test)
		{
			test((VanishingTileEntity) te, x, y, z, f);
			return;
		}

		init(x, y, z);

		if(te instanceof VanishingTileEntity)
			renderVanishingTileEntityAt((VanishingTileEntity) te, x, y, z, f);
		else if(te instanceof DoorTileEntity)
			renderDoorTileEntity((DoorTileEntity) te, x, y, z, f);


		end();
	}

	public void renderDoorTileEntity(DoorTileEntity te, double x, double y, double z, float f)
	{
		if(!te.moving)
			return;

		
		
		if(te.getBlockType() instanceof SlidingDoor)
		{
			te.setSlidingDoorPosition(f);
			GL11.glTranslatef(te.hingeOffsetX, 0, te.hingeOffsetZ);
		}
		else
		{
			te.setRegularDoorPosition(f);
			GL11.glTranslatef(te.hingeOffsetX, 0, te.hingeOffsetZ);
			GL11.glRotatef(te.angle, 0, 1, 0);
			GL11.glTranslatef(-te.hingeOffsetX, 0, -te.hingeOffsetZ);
		}


		BaseRenderer mrenderer = new BaseRenderer().set(te.worldObj, te.getBlockType(), te.xCoord, te.yCoord, te.zCoord, te.getBlockMetadata());

		RenderParameters rp = new RenderParameters();
		rp.renderAllFaces = true;
		rp.useBlockBounds = false;
		rp.renderBounds = te.blockType.calculateBlockBoundsD(te.getBlockMetadata(), false);
		rp.useBlockBrightness = false;
		rp.brightness = te.worldObj.getLightBrightnessForSkyBlocks(te.xCoord, te.yCoord, te.zCoord, 0);
		rp.calculateAOColor = false;
	//	rp.colorFactor = te.brightnessFactor();
		mrenderer.drawShape(Shape.Cube, rp);

		next();

		GL11.glTranslated(0, 1, 0);
		mrenderer.set(te.xCoord, te.yCoord + 1, te.zCoord);
		mrenderer.set(te.getBlockMetadata() | Door.flagTopBlock);
		mrenderer.drawShape(Shape.Cube, rp);


	}

	public void renderVanishingTileEntityAt(VanishingTileEntity te, double x, double y, double z, float f)
	{
		if (!te.inTransition && !te.vibrating)
			return;

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		BaseRenderer mrenderer = new BaseRenderer().set(te.worldObj, te.blockType, te.xCoord, te.yCoord, te.zCoord, te.blockMetadata);
		RenderParameters params = RenderParameters.Default();
		float fx = 0.0F;
		float fy = 0.0F;
		float fz = 0.0F;
		float scale = (float) (VanishingTileEntity.maxTransitionTime - te.transitionTimer) / (float) VanishingTileEntity.maxTransitionTime;

		params.useBlockBounds = false;

		// randomize position for vibrations
		if (!te.inTransition && !te.powered)
		{
			params.alpha = 200;
			fx = rand.nextFloat() * 0.05F;
			fy = rand.nextFloat() * 0.05F;
			fz = rand.nextFloat() * 0.05F;
			if (rand.nextBoolean())
				GL11.glTranslated(fx, fy, fz);
			else
				GL11.glRotatef(rand.nextInt(5), 1, 1, 1);
		}
		else
		{
			params.alpha = (int) (scale * 255);
			params.scale = scale;
		}

		mrenderer.drawShape(Shape.Cube, params);

		if(te.copiedBlock != null)
		{
			mrenderer.set(te.copiedBlock, te.copiedMetadata);
			mrenderer.drawShape(Shape.Cube, params);
		}

	}

	public void test(VanishingTileEntity te, double x, double y, double z, float f)
	{
	}

}
