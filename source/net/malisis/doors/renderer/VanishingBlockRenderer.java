package net.malisis.doors.renderer;

import java.util.Random;

import net.malisis.core.renderer.BaseRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.preset.ShapePreset;
import net.malisis.doors.ProxyAccess;
import net.malisis.doors.block.VanishingBlock;
import net.malisis.doors.entity.VanishingTileEntity;
import net.minecraft.client.renderer.RenderBlocks;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

public class VanishingBlockRenderer extends BaseRenderer
{
	public static int renderId;
	public Random rand = new Random();

	@Override
	public void render()
	{
		if (typeRender == TYPE_TESR_WORLD)
			renderVanishingTileEntity();
		else if (typeRender == TYPE_ISBRH_INVENTORY)
		{
			RenderParameters rp = new RenderParameters();
			rp.useBlockBounds.set(false);
			drawShape(ShapePreset.Cube(), rp);
		}
		else if (typeRender == TYPE_WORLD)
		{
			VanishingTileEntity te = (VanishingTileEntity) world.getTileEntity(x, y, z);

			if ((te.getBlockMetadata() & (VanishingBlock.flagPowered | VanishingBlock.flagInTransition)) != 0)
				return;

			if (te.copiedBlock != null)
			{
				tessellatorUnshift();
				renderBlocks.blockAccess = ProxyAccess.get(world);
				renderBlocks.renderAllFaces = true;
				try
				{
					if (te.copiedBlock.canRenderInPass(((VanishingBlock) block).renderPass))
						vertexDrawn = renderBlocks.renderBlockByRenderType(te.copiedBlock, x, y, z);
				}
				catch (Exception e)
				{

					tessellatorShift();
					drawShape(ShapePreset.Cube());
				}

				renderBlocks.renderAllFaces = false;
				renderBlocks.blockAccess = world;
			}
			else if (((VanishingBlock) block).renderPass == 0)
				drawShape(ShapePreset.Cube());

			// if (te.copiedBlock != null)
			// set(te.copiedBlock, te.copiedMetadata);
			// drawShape(ShapePreset.Cube());
		}
	}

	private void renderVanishingTileEntity()
	{
		VanishingTileEntity te = (VanishingTileEntity) this.tileEntity;
		if (!te.inTransition && !te.vibrating)
			return;

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0);

		float fx = 0.0F;
		float fy = 0.0F;
		float fz = 0.0F;
		float scale = (float) (te.getDuration() - te.transitionTimer) / (float) te.getDuration();

		RenderParameters rp = new RenderParameters();
		rp.useBlockBounds.set(false);
		rp.interpolateUV.set(false);

		Shape shape = ShapePreset.Cube();
		// randomize position for vibrations
		if (!te.inTransition && !te.powered)
		{
			rp.alpha.set(200);
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
			rp.alpha.set((int) (255 - scale * 255));
			shape.scale(scale - 0.001F);
		}

		if (te.copiedBlock != null)
		{
			RenderBlocks renderBlocks = new RenderBlocks(ProxyAccess.get(world));
			renderBlocks.renderAllFaces = true;
			try
			{
				// drawShape(shape, rp);
				// next();
				GL11.glTranslated(0.5F, 0.5F, 0.5F);
				GL11.glScalef(scale, scale, scale);
				GL11.glTranslated(-x - 0.5F, -y - 0.5F, -z - 0.5F);
				if (te.copiedBlock.canRenderInPass(0))
				{
					GL11.glBlendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);
					GL14.glBlendColor(0, 0, 0, scale);
					renderBlocks.renderBlockByRenderType(te.copiedBlock, x, y, z);
					next();
					GL14.glBlendColor(0, 0, 0, 1 - scale);
					renderBlocks.overrideBlockTexture = block.getIcon(blockMetadata, 0);
					renderBlocks.renderBlockByRenderType(te.copiedBlock, x, y, z);
				}
				if (te.copiedBlock.canRenderInPass(1))
				{
					renderBlocks.renderBlockByRenderType(te.copiedBlock, x, y, z);
				}
			}
			catch (Exception e)
			{
				drawShape(shape, rp);
			}

		}
		else
			drawShape(shape, rp);
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		return true;
	}
}
