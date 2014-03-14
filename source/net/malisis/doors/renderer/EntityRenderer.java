package net.malisis.doors.renderer;

import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class EntityRenderer extends Render
{
	private Tessellator t = null;

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f, float f1)
	{
		boolean test = false;
		if (test)
		{
		//	test((VanishingTileEntity) te, x, y, z, f);
			return;
		}

		t = Tessellator.instance;
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		RenderHelper.disableStandardItemLighting();
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		bindTexture(TextureMap.locationBlocksTexture);

		t.startDrawingQuads();



		t.draw();
		GL11.glPopMatrix();

	}



	@Override
	protected ResourceLocation getEntityTexture(Entity entity)
	{
		return null;
	}

}
