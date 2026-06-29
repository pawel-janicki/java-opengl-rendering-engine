package com.github.paweljanicki.engine.assets.textures;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;

public class TextureParameters {
	
	public static final TextureParameters DEFAULT_RGB = new TextureParameters(GL11.GL_UNSIGNED_BYTE, GL11.GL_RGB, GL11.GL_RGB, GL11.GL_LINEAR_MIPMAP_LINEAR, GL11.GL_LINEAR, GL11.GL_REPEAT, GL11.GL_REPEAT, true, true);
	public static final TextureParameters DEFAULT_SRGB = new TextureParameters(GL11.GL_UNSIGNED_BYTE, GL11.GL_RGB, GL30.GL_SRGB8, GL11.GL_LINEAR_MIPMAP_LINEAR, GL11.GL_LINEAR, GL11.GL_REPEAT, GL11.GL_REPEAT, true, true);
	
	public static final TextureParameters DEFAULT_RGBA = new TextureParameters(GL11.GL_UNSIGNED_BYTE, GL11.GL_RGBA, GL11.GL_RGBA, GL11.GL_LINEAR_MIPMAP_LINEAR, GL11.GL_LINEAR, GL11.GL_REPEAT, GL11.GL_REPEAT, true, true);
	public static final TextureParameters DEFAULT_SRGBA = new TextureParameters(GL11.GL_UNSIGNED_BYTE, GL11.GL_RGBA, GL30.GL_SRGB8_ALPHA8, GL11.GL_LINEAR_MIPMAP_LINEAR, GL11.GL_LINEAR, GL11.GL_REPEAT, GL11.GL_REPEAT, true, true);
	
	public static final TextureParameters DEFAULT_HDR = new TextureParameters(GL11.GL_FLOAT, GL11.GL_RGB, GL30.GL_RGB16F, GL11.GL_LINEAR, GL11.GL_LINEAR, GL12.GL_CLAMP_TO_EDGE, GL12.GL_CLAMP_TO_EDGE, false, false);
	
	public final int type;
	public final int format;
	public final int internalFormat;
	
	public final int minFilter;
	public final int magFilter;
	
	public final int wrapS;
	public final int wrapT;
	
	public final boolean generateMipmaps;
	public final boolean enableAnisotropicFiltering;
	
	public TextureParameters(int type, int format, int internalFormat, int minFilter, int magFilter, int wrapS, int wrapT, boolean generateMipmaps, boolean enableAnisotropicFiltering) {
		this.type = type;
		this.format = format;
		this.internalFormat = internalFormat;
		this.minFilter = minFilter;
		this.magFilter = magFilter;
		this.wrapS = wrapS;
		this.wrapT = wrapT;
		this.generateMipmaps = generateMipmaps;
		this.enableAnisotropicFiltering = enableAnisotropicFiltering;
	}
	
}
