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
import com.github.paweljanicki.engine.scene.Camera;
import com.github.paweljanicki.engine.scene.Scene;

public class LightingPass implements IRenderPass {
	
	private FrameBuffer lightingFbo;
	private Shader shader;
	
	@Override
	public void init(AssetManager assetManager, RenderTargets targets, int width, int height) {
		lightingFbo = new FrameBuffer(width, height);
		lightingFbo.addColorAttachment(new TextureParameters(GL11.GL_FLOAT, GL11.GL_RGBA, GL30.GL_RGBA16F, GL11.GL_NEAREST, GL11.GL_NEAREST, GL12.GL_CLAMP_TO_EDGE, GL12.GL_CLAMP_TO_EDGE, false, false));
		lightingFbo.bind();
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, targets.get("gBuffer").getDepthTexture().getId(), 0);
		lightingFbo.unbind();
		lightingFbo.checkComplete();
		
		targets.add("lighting", lightingFbo);
		
		shader = assetManager.loadShader("/shaders/quadVS.glsl", "/shaders/lightingFS.glsl");
		shader.bind();
		shader.setInt("gDepth", 0);
		shader.setInt("gNormal", 1);
		shader.setInt("gAlbedo", 2);
		shader.setInt("gARM", 3);
		shader.setInt("gEmissive", 4);
		shader.unbind();
	}

	@Override
	public void render(RenderContext context, RenderTargets targets, RenderHelpers helpers) {
		Scene scene = context.getScene();
		Camera camera = context.getCamera();
		FrameBuffer gBuffer = targets.get("gBuffer");
		
		lightingFbo.bind();
		
		GL11.glClearColor(0, 0, 0, 1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		
		shader.bind();
		shader.setVector3f("cameraPosition", camera.getPosition());
		shader.setMatrix4f("inverseProjectionMatrix", camera.getInverseProjectionMatrix(context.getWidth(), context.getHeight()));
		shader.setMatrix4f("inverseViewMatrix", camera.getInverseViewMatrix());
		
		if (scene.getDirectionalLight() != null) {
			shader.setVector3f("lightDirection", scene.getDirectionalLight().getDirection());
			shader.setVector3f("lightColor", scene.getDirectionalLight().getColor());
		}
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, gBuffer.getDepthTexture().getId());
		
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, gBuffer.getColorTexture(0).getId());
		
		GL13.glActiveTexture(GL13.GL_TEXTURE2);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, gBuffer.getColorTexture(1).getId());
		
		GL13.glActiveTexture(GL13.GL_TEXTURE3);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, gBuffer.getColorTexture(2).getId());
		
		GL13.glActiveTexture(GL13.GL_TEXTURE4);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, gBuffer.getColorTexture(3).getId());
		
		helpers.getQuadRenderer().render();
		
		shader.unbind();
		lightingFbo.unbind();
		
		GL11.glDepthMask(true);
		
		targets.setCurrentRenderTarget(lightingFbo);
	}

}
