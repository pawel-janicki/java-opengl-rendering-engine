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
	private final IrradianceMapGenerator irradianceMapGenerator;
	private final PrefilterMapGenerator prefilterMapGenerator;
	
	public EnvironmentLoader(AssetManager assetManager) {
		cubeRenderer = new CubeRenderer();
		equirectangularToCubemapGenerator = new EquirectangularToCubemapGenerator(assetManager);
		irradianceMapGenerator = new IrradianceMapGenerator(assetManager);
		prefilterMapGenerator = new PrefilterMapGenerator(assetManager);
	}
	
	public Environment load(String filePath) {
		Texture texture = TextureLoader.load(filePath, TextureParameters.DEFAULT_HDR);
		Texture environmentMap = equirectangularToCubemapGenerator.generate(cubeRenderer, texture);
		Texture irradianceMap = irradianceMapGenerator.generate(cubeRenderer, environmentMap);
		Texture prefilterMap = prefilterMapGenerator.generate(cubeRenderer, environmentMap);
		
		GL11.glDeleteTextures(texture.getId());
		
		return new Environment(environmentMap, irradianceMap, prefilterMap);
	}
	
	public void cleanUp() {
		cubeRenderer.cleanUp();
		equirectangularToCubemapGenerator.cleanUp();
		irradianceMapGenerator.cleanUp();
		prefilterMapGenerator.cleanUp();
	}
	
}
