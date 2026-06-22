package com.github.paweljanicki.engine.assets;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.github.paweljanicki.engine.assets.textures.Texture;
import com.github.paweljanicki.engine.assets.textures.TextureLoader;
import com.github.paweljanicki.engine.assets.textures.TextureParameters;

public class AssetManager {
	
	private Map<String, Texture> textures = new HashMap<>();
	
	public Texture loadTexture(String filePath, TextureParameters textureParameters) {
		if (textures.containsKey(filePath))
			return textures.get(filePath);
		
		Texture texture = TextureLoader.load(filePath, textureParameters);
		textures.put(filePath, texture);
		
		return texture;
	}
	
	public void unloadTexture(String filePath, TextureParameters textureParameters) {
		if (!textures.containsKey(filePath))
			return;
		
		GL11.glDeleteTextures(textures.get(filePath).getId());
		textures.remove(filePath);
	}
	
	public void cleanUp() {
		for (Texture texture : textures.values()) {
			GL11.glDeleteTextures(texture.getId());
		}
		
		textures.clear();
	}
	
}
