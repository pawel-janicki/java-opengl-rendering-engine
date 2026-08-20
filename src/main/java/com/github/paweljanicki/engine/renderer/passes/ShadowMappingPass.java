package com.github.paweljanicki.engine.renderer.passes;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL43;

import com.github.paweljanicki.engine.assets.AssetManager;
import com.github.paweljanicki.engine.assets.models.MeshManager;
import com.github.paweljanicki.engine.assets.shaders.Shader;
import com.github.paweljanicki.engine.assets.textures.TextureParameters;
import com.github.paweljanicki.engine.renderer.FrameBuffer;
import com.github.paweljanicki.engine.renderer.IRenderPass;
import com.github.paweljanicki.engine.renderer.RenderContext;
import com.github.paweljanicki.engine.renderer.RenderHelpers;
import com.github.paweljanicki.engine.renderer.RenderTargets;
import com.github.paweljanicki.engine.scene.Camera;
import com.github.paweljanicki.engine.scene.Scene;

public class ShadowMappingPass implements IRenderPass {
	
	private static final int SHADOW_MAP_SIZE = 4096;
	private static final int SHADOW_DISTANCE = 20;
	
	private static final Vector3f UP = new Vector3f(0, 1, 0);
	
	private MeshManager meshManager;
	
	private FrameBuffer shadowsFbo;
	private Shader shader;
	
	private Matrix4f lightProjectionMatrix = new Matrix4f();
	private Matrix4f lightViewMatrix = new Matrix4f();
	
	@Override
	public void init(AssetManager assetManager, RenderTargets targets, int width, int height) {
		this.meshManager = assetManager.getMeshManager();
		
		shadowsFbo = new FrameBuffer(SHADOW_MAP_SIZE, SHADOW_MAP_SIZE, false);
		shadowsFbo.addDepthAttachment(new TextureParameters(GL11.GL_FLOAT, GL11.GL_DEPTH_COMPONENT, GL14.GL_DEPTH_COMPONENT24, GL11.GL_LINEAR, GL11.GL_LINEAR, GL13.GL_CLAMP_TO_BORDER, GL13.GL_CLAMP_TO_BORDER, false, false));
		shadowsFbo.checkComplete();
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, shadowsFbo.getDepthTexture().getId());
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_COMPARE_MODE, GL14.GL_COMPARE_R_TO_TEXTURE);
		GL11.glTexParameterfv(GL11.GL_TEXTURE_2D, GL13.GL_TEXTURE_BORDER_COLOR, new float[] { 1, 1, 1, 1 });
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		
		targets.add("shadows", shadowsFbo);
		
		shader = assetManager.loadShader("/shaders/shadowMappingVS.glsl", "/shaders/shadowMappingFS.glsl");
	}
	
	@Override
	public void render(RenderContext context, RenderTargets targets, RenderHelpers helpers) {
		Scene scene = context.getScene();
		if (scene.getDirectionalLight() == null)
			return;
		
		Camera camera = context.getCamera();
		
		shadowsFbo.bind();
		
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		
		lightProjectionMatrix.identity().ortho(-SHADOW_DISTANCE, SHADOW_DISTANCE, -SHADOW_DISTANCE, SHADOW_DISTANCE, 0.1f, SHADOW_DISTANCE * 2);
		lightViewMatrix.identity().lookAt(new Vector3f(scene.getDirectionalLight().getDirection()).normalize().mul(-SHADOW_DISTANCE).add(camera.getPosition()), camera.getPosition(), UP);
		context.getLightSpaceMatrix().set(lightProjectionMatrix).mul(lightViewMatrix);
		
		shader.bind();
		shader.setMatrix4f("lightSpaceMatrix", context.getLightSpaceMatrix());
		
		GL15.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, scene.getGpuScene().getCommandsBufferId());
		
		GL30.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 0, scene.getGpuScene().getInstanceSsboId());
		
		GL30.glBindVertexArray(meshManager.getVaoId());
		
		GL43.glMultiDrawElementsIndirect(GL11.GL_TRIANGLES, GL11.GL_UNSIGNED_INT, 0, scene.getGpuScene().getCommandsCount(), 0);
		
		GL15.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, 0);
		
		GL30.glBindVertexArray(0);
		
		shader.unbind();
		shadowsFbo.unbind();
	}
	
}
