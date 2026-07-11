package com.github.paweljanicki.engine.renderer.passes;

import org.joml.Vector2f;
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

public class FXAAPass implements IRenderPass {
	
	private FrameBuffer fxaaFbo;
	private Shader shader;
	
	private Vector2f inverseScreenSize = new Vector2f();
	
	@Override
	public void init(AssetManager assetManager, RenderTargets targets, int width, int height) {
		fxaaFbo = new FrameBuffer(width, height);
		fxaaFbo.addColorAttachment(new TextureParameters(GL11.GL_UNSIGNED_BYTE, GL11.GL_RGBA, GL30.GL_RGBA8, GL11.GL_LINEAR, GL11.GL_LINEAR, GL12.GL_CLAMP_TO_EDGE, GL12.GL_CLAMP_TO_EDGE, false, false));
		fxaaFbo.checkComplete();
		
		targets.add("fxaa", fxaaFbo);
		
		shader = assetManager.loadShader("/shaders/quadVS.glsl", "/shaders/fxaaFS.glsl");
		shader.bind();
		shader.setInt("inputTexture", 0);
		shader.unbind();
	}
	
	@Override
	public void render(RenderContext context, RenderTargets targets, RenderHelpers helpers) {
		FrameBuffer inputFbo = targets.getCurrentRenderTarget();
		
		fxaaFbo.bind();
		
		GL11.glClearColor(0, 0, 0, 1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		
		inverseScreenSize.set(1 / (float) context.getWidth(), 1 / (float) context.getHeight());
		
		shader.bind();
		shader.setVector2f("inverseScreenSize", inverseScreenSize);
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, inputFbo.getColorTexture(0).getId());
		
		helpers.getQuadRenderer().render();
		
		shader.unbind();
		fxaaFbo.unbind();
		
		targets.setCurrentRenderTarget(fxaaFbo);
	}
	
}
