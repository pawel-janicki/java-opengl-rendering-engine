package com.github.paweljanicki.engine.renderer.passes;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

import com.github.paweljanicki.engine.assets.AssetManager;
import com.github.paweljanicki.engine.assets.shaders.Shader;
import com.github.paweljanicki.engine.assets.textures.TextureParameters;
import com.github.paweljanicki.engine.renderer.FrameBuffer;
import com.github.paweljanicki.engine.renderer.IRenderPass;
import com.github.paweljanicki.engine.renderer.RenderContext;
import com.github.paweljanicki.engine.renderer.RenderHelpers;
import com.github.paweljanicki.engine.renderer.RenderTargets;

public class TonemapPass implements IRenderPass {
	
	private FrameBuffer tonemapFbo;
	private Shader shader;
	
	private float exposure = 0.4f;
	
	@Override
	public void init(AssetManager assetManager, RenderTargets targets, int width, int height) {
		tonemapFbo = new FrameBuffer(width, height);
		tonemapFbo.addColorAttachment(new TextureParameters(GL11.GL_FLOAT, GL11.GL_RGBA, GL30.GL_RGBA16F, GL11.GL_NEAREST, GL11.GL_NEAREST, GL12.GL_CLAMP_TO_EDGE, GL12.GL_CLAMP_TO_EDGE, false, false));
		tonemapFbo.checkComplete();
		
		targets.add("tonemap", tonemapFbo);
		
		shader = assetManager.loadShader("/shaders/quadVS.glsl", "/shaders/tonemapFS.glsl");
		shader.bind();
		shader.setInt("inputTexture", 0);
		shader.unbind();
	}
	
	@Override
	public void render(RenderContext context, RenderTargets targets, RenderHelpers helpers) {
		FrameBuffer inputFbo = targets.getCurrentRenderTarget();
		
		tonemapFbo.bind();
		
		GL11.glClearColor(0, 0, 0, 1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		
		shader.bind();
		shader.setFloat("exposure", exposure);
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, inputFbo.getColorTexture(0).getId());
		
		helpers.getQuadRenderer().render();
		
		shader.unbind();
		tonemapFbo.unbind();
		
		targets.setCurrentRenderTarget(tonemapFbo);
	}
	
	public float getExposure() {
		return exposure;
	}
	
	public void setExposure(float exposure) {
		this.exposure = exposure;
	}
	
}
