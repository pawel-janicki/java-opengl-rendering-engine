package com.github.paweljanicki.engine.renderer.passes;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import com.github.paweljanicki.engine.assets.AssetManager;
import com.github.paweljanicki.engine.assets.shaders.Shader;
import com.github.paweljanicki.engine.renderer.FrameBuffer;
import com.github.paweljanicki.engine.renderer.IRenderPass;
import com.github.paweljanicki.engine.renderer.RenderContext;
import com.github.paweljanicki.engine.renderer.RenderHelpers;
import com.github.paweljanicki.engine.renderer.RenderTargets;

public class OutputPass implements IRenderPass {
	
	private Shader shader;
	
	@Override
	public void init(AssetManager assetManager, RenderTargets targets, int width, int height) {
		shader = assetManager.loadShader("/shaders/quadVS.glsl", "/shaders/outputFS.glsl");
		shader.bind();
		shader.setInt("outputTexture", 0);
		shader.unbind();
	}
	
	@Override
	public void render(RenderContext context, RenderTargets targets, RenderHelpers helpers) {
		FrameBuffer inputFbo = targets.getCurrentRenderTarget();
		
		GL11.glViewport(0, 0, context.getWidth(), context.getHeight());
		
		GL11.glClearColor(0, 0, 0, 1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		shader.bind();
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, inputFbo.getColorTexture(0).getId());
		
		helpers.getQuadRenderer().render();
		
		shader.unbind();
	}
	
}
