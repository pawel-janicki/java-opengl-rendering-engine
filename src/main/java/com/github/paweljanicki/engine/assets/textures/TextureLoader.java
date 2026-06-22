package com.github.paweljanicki.engine.assets.textures;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

public class TextureLoader {
	
	public static Texture load(String filePath, TextureParameters textureParameters) {
		int width;
		int height;
		
		Buffer buffer = null;
		
		int channels = 4;
		if (textureParameters.format == GL11.GL_RGB)
			channels = 3;
		
		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer w = stack.mallocInt(1);
			IntBuffer h = stack.mallocInt(1);
			IntBuffer c = stack.mallocInt(1);
			
			if (textureParameters.type == GL11.GL_UNSIGNED_BYTE) {
				buffer = STBImage.stbi_load("src/main/resources" + filePath, w, h, c, channels);
			} else if (textureParameters.type == GL11.GL_FLOAT) {
				STBImage.stbi_set_flip_vertically_on_load(true);
				buffer = STBImage.stbi_loadf("src/main/resources" + filePath, w, h, c, channels);
				STBImage.stbi_set_flip_vertically_on_load(false);
			}
			
			if (buffer == null)
				throw new RuntimeException("[ERROR] >> Failed to load texture '" + filePath + "'! Failure reason: " + STBImage.stbi_failure_reason());
			
			width = w.get();
			height = h.get();
		}
		
		int alignment = channels == 4 ? 4 : 1;
		
		int id = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, alignment);
		
		if (textureParameters.type == GL11.GL_UNSIGNED_BYTE) {
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, textureParameters.internalFormat, width, height, 0, textureParameters.format, textureParameters.type, (ByteBuffer) buffer);
			STBImage.stbi_image_free((ByteBuffer) buffer);
		} else if (textureParameters.type == GL11.GL_FLOAT) {
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, textureParameters.internalFormat, width, height, 0, textureParameters.format, textureParameters.type, (FloatBuffer) buffer);
			STBImage.stbi_image_free((FloatBuffer) buffer);
		}
		
		if (textureParameters.generateMipmaps)
			GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);
		
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, textureParameters.minFilter);
		GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, textureParameters.magFilter);
		
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, textureParameters.wrapS);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, textureParameters.wrapT);
		
		if (textureParameters.enableAnisotropicFiltering) {
			float amount = Math.min(8, GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT));
			GL11.glTexParameterf(GL11.GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, amount);
		}
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		
		return new Texture(id);
	}
	
}
