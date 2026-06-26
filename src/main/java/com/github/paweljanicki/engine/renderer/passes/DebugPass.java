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

public class DebugPass implements IRenderPass {
	
	public enum DebugMode {
		
		DEPTH,
		NORMALS,
		ALBEDO,
		ARM,
		EMISSIVE;
		
	}
	
	private Shader shader;
	
	private boolean enabled;
	private DebugMode mode = DebugMode.DEPTH;
	
	@Override
	public void init(AssetManager assetManager, RenderTargets targets, int width, int height) {
		shader = assetManager.loadShader("/shaders/quadVS.glsl", "/shaders/outputFS.glsl");
		shader.bind();
		shader.setInt("outputTexture", 0);
		shader.unbind();
	}
	
	@Override
	public void render(RenderContext context, RenderTargets targets, RenderHelpers helpers) {
		if (!enabled)
			return;
		
		FrameBuffer gBuffer = targets.get("gBuffer");
		
		int texture = 0;
		switch (mode) {
		case DEPTH: texture = gBuffer.getDepthTexture().getId(); break;
		case NORMALS: texture = gBuffer.getColorTexture(0).getId(); break;
		case ALBEDO: texture = gBuffer.getColorTexture(1).getId(); break;
		case ARM: texture = gBuffer.getColorTexture(2).getId(); break;
		case EMISSIVE: texture = gBuffer.getColorTexture(3).getId(); break;
		}
		
		GL11.glViewport(0, 0, context.getWidth(), context.getHeight());
		
		GL11.glClearColor(0, 0, 0, 1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		shader.bind();
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		
		helpers.getQuadRenderer().render();
		
		shader.unbind();
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public DebugMode getMode() {
		return mode;
	}
	
	public void setMode(DebugMode mode) {
		this.mode = mode;
	}
	
}
