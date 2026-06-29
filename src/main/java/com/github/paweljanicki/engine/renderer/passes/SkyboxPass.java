package com.github.paweljanicki.engine.renderer.passes;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

import com.github.paweljanicki.engine.assets.AssetManager;
import com.github.paweljanicki.engine.assets.shaders.Shader;
import com.github.paweljanicki.engine.renderer.IRenderPass;
import com.github.paweljanicki.engine.renderer.RenderContext;
import com.github.paweljanicki.engine.renderer.RenderHelpers;
import com.github.paweljanicki.engine.renderer.RenderTargets;
import com.github.paweljanicki.engine.scene.Camera;
import com.github.paweljanicki.engine.scene.Scene;

public class SkyboxPass implements IRenderPass {
	
	private Shader shader;
	
	@Override
	public void init(AssetManager assetManager, RenderTargets targets, int width, int height) {
		shader = assetManager.loadShader("/shaders/skyboxVS.glsl", "/shaders/skyboxFS.glsl");
		shader.bind();
		shader.setInt("cubemap", 0);
		shader.unbind();
	}
	
	@Override
	public void render(RenderContext context, RenderTargets targets, RenderHelpers helpers) {
		Scene scene = context.getScene();
		Camera camera = context.getCamera();
		
		targets.getCurrentRenderTarget().bind();
		
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		
		shader.bind();
		shader.setMatrix4f("projectionMatrix", camera.getProjectionMatrix(context.getWidth(), context.getHeight()));
		shader.setMatrix4f("viewMatrix", camera.getViewMatrix());
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, scene.getEnvironment().getEnvironmentMap().getId());
		
		helpers.getCubeRenderer().render();
		
		shader.unbind();
		targets.getCurrentRenderTarget().unbind();
		
		GL11.glDepthMask(true);
		GL11.glDepthFunc(GL11.GL_LESS);
	}
	
}
