package uk.co.quphoria.advancedrocketryadditions.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fluids.FluidStack;

public class FluidRenderer {
	protected static Minecraft mc = Minecraft.getMinecraft();

	public static void renderTiledFluid(int x, int y, int width, int height, float depth, FluidStack fluidStack) {
		TextureAtlasSprite fluidSprite = mc.getTextureMapBlocks()
				.getAtlasSprite(fluidStack.getFluid().getStill(fluidStack).toString());
		setColorRGBA(fluidStack.getFluid().getColor(fluidStack));
		renderTiledTextureAtlas(x, y, width, height, depth, fluidSprite, fluidStack.getFluid().isGaseous(fluidStack));
	}

	public static void setColorRGBA(int color) {
		float a = alpha(color) / 255.0F;
		float r = red(color) / 255.0F;
		float g = green(color) / 255.0F;
		float b = blue(color) / 255.0F;

		GlStateManager.color(r, g, b, a);
	}
	
	public static int alpha(int c) {
	    return (c >> 24) & 0xFF;
	  }

	  public static int red(int c) {
	    return (c >> 16) & 0xFF;
	  }

	  public static int green(int c) {
	    return (c >> 8) & 0xFF;
	  }

	  public static int blue(int c) {
	    return (c) & 0xFF;
	  }

	public static void renderTiledTextureAtlas(int x, int y, int width, int height, float depth,
			TextureAtlasSprite sprite, boolean upsideDown) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldrenderer = tessellator.getBuffer();
		worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		putTiledTextureQuads(worldrenderer, x, y, width, height, depth, sprite, upsideDown);

		tessellator.draw();
	}

	public static void putTiledTextureQuads(BufferBuilder renderer, int x, int y, int width, int height, float depth,
			TextureAtlasSprite sprite, boolean upsideDown) {
		float u1 = sprite.getMinU();
		float v1 = sprite.getMinV();
		do {
			int renderHeight = Math.min(sprite.getIconHeight(), height);
			height -= renderHeight;
			float v2 = sprite.getInterpolatedV((16f * renderHeight) / (float) sprite.getIconHeight());
			int x2 = x;
			int width2 = width;
			do {
				int renderWidth = Math.min(sprite.getIconWidth(), width2);
				width2 -= renderWidth;

				float u2 = sprite.getInterpolatedU((16f * renderWidth) / (float) sprite.getIconWidth());

				if (upsideDown) {
					renderer.pos(x2, y, depth).tex(u2, v1).endVertex();
					renderer.pos(x2, y + renderHeight, depth).tex(u2, v2).endVertex();
					renderer.pos(x2 + renderWidth, y + renderHeight, depth).tex(u1, v2).endVertex();
					renderer.pos(x2 + renderWidth, y, depth).tex(u1, v1).endVertex();
				} else {
					renderer.pos(x2, y, depth).tex(u1, v1).endVertex();
					renderer.pos(x2, y + renderHeight, depth).tex(u1, v2).endVertex();
					renderer.pos(x2 + renderWidth, y + renderHeight, depth).tex(u2, v2).endVertex();
					renderer.pos(x2 + renderWidth, y, depth).tex(u2, v1).endVertex();
				}
				x2 += renderWidth;
			} while (width2 > 0);
			y += renderHeight;
		} while (height > 0);
	}
}
