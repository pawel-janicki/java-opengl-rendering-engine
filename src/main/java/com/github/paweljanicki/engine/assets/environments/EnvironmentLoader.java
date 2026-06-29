package com.github.paweljanicki.engine.assets.environments;

import org.lwjgl.opengl.GL11;

import com.github.paweljanicki.engine.assets.AssetManager;
import com.github.paweljanicki.engine.assets.textures.Texture;
import com.github.paweljanicki.engine.assets.textures.TextureLoader;
import com.github.paweljanicki.engine.assets.textures.TextureParameters;
import com.github.paweljanicki.engine.renderer.CubeRenderer;

public class EnvironmentLoader {
	
	private final CubeRenderer cubeRenderer;
	
	private final EquirectangularToCubemapGenerator equirectangularToCubemapGenerator;
	
	public EnvironmentLoader(AssetManager assetManager) {
		cubeRenderer = new CubeRenderer();
		equirectangularToCubemapGenerator = new EquirectangularToCubemapGenerator(assetManager);
	}
	
	public Environment load(String filePath) {
		Texture texture = TextureLoader.load(filePath, TextureParameters.DEFAULT_HDR);
		Texture environmentMap = equirectangularToCubemapGenerator.generate(cubeRenderer, texture);
		
		GL11.glDeleteTextures(texture.getId());
		
		return new Environment(environmentMap);
	}
	
	public void cleanUp() {
		cubeRenderer.cleanUp();
		equirectangularToCubemapGenerator.cleanUp();
	}
	
}
