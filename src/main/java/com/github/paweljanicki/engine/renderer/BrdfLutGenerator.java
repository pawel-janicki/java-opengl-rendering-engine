package com.github.paweljanicki.engine.renderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL30;

import com.github.paweljanicki.engine.assets.AssetManager;
import com.github.paweljanicki.engine.assets.shaders.Shader;
import com.github.paweljanicki.engine.assets.textures.Texture;
import com.github.paweljanicki.engine.assets.textures.TextureLoader;
import com.github.paweljanicki.engine.assets.textures.TextureParameters;

public class BrdfLutGenerator {
	
	private static final int SIZE = 512;
	
	private QuadRenderer quadRenderer;
	
	private FrameBuffer fbo;
	private Shader shader;
	
	public BrdfLutGenerator(AssetManager assetManager) {
		quadRenderer = new QuadRenderer();
		
		fbo = new FrameBuffer(SIZE, SIZE);
		
		shader = assetManager.loadShader("/shaders/quadVS.glsl", "/shaders/brdfLutFS.glsl");
	}
	
	public Texture generate() {
		Texture texture = TextureLoader.generate(SIZE, SIZE, new TextureParameters(GL11.GL_FLOAT, GL30.GL_RG, GL30.GL_RG16F, GL11.GL_LINEAR, GL11.GL_LINEAR, GL12.GL_CLAMP_TO_EDGE, GL12.GL_CLAMP_TO_EDGE, false, false));
		
		fbo.bind();
		
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D, texture.getId(), 0);
		GL30.glClear(GL11.GL_COLOR_BUFFER_BIT);
		
		shader.bind();
		
		quadRenderer.render();
		
		shader.unbind();
		
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_2D, 0, 0);
		fbo.unbind();
		
		return texture;
	}
	
	public void cleanUp() {
		quadRenderer.cleanUp();
		fbo.cleanUp();
	}
	
}
