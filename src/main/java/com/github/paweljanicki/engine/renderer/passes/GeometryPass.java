package com.github.paweljanicki.engine.renderer.passes;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL43;

import com.github.paweljanicki.engine.assets.AssetManager;
import com.github.paweljanicki.engine.assets.models.MaterialManager;
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

public class GeometryPass implements IRenderPass {
	
	private MaterialManager materialManager;
	private MeshManager meshManager;
	
	private FrameBuffer gBuffer;
	
	private Shader shader;
	
	@Override
	public void init(AssetManager assetManager, RenderTargets targets, int width, int height) {
		this.materialManager = assetManager.getMaterialManager();
		this.meshManager = assetManager.getMeshManager();
		
		gBuffer = new FrameBuffer(width, height);
		gBuffer.addDepthAttachment(new TextureParameters(GL11.GL_FLOAT, GL11.GL_DEPTH_COMPONENT, GL14.GL_DEPTH_COMPONENT24, GL11.GL_NEAREST, GL11.GL_NEAREST, GL12.GL_CLAMP_TO_EDGE, GL12.GL_CLAMP_TO_EDGE, false, false)); // Depth
		gBuffer.addColorAttachment(new TextureParameters(GL11.GL_FLOAT, GL30.GL_RG, GL31.GL_RG16_SNORM, GL11.GL_NEAREST, GL11.GL_NEAREST, GL12.GL_CLAMP_TO_EDGE, GL12.GL_CLAMP_TO_EDGE, false, false)); // Normal
		gBuffer.addColorAttachment(new TextureParameters(GL11.GL_UNSIGNED_BYTE, GL11.GL_RGB, GL11.GL_RGB8, GL11.GL_NEAREST, GL11.GL_NEAREST, GL12.GL_CLAMP_TO_EDGE, GL12.GL_CLAMP_TO_EDGE, false, false)); // Albedo
		gBuffer.addColorAttachment(new TextureParameters(GL11.GL_UNSIGNED_BYTE, GL11.GL_RGB, GL11.GL_RGB8, GL11.GL_NEAREST, GL11.GL_NEAREST, GL12.GL_CLAMP_TO_EDGE, GL12.GL_CLAMP_TO_EDGE, false, false)); // ARM
		gBuffer.addColorAttachment(new TextureParameters(GL11.GL_UNSIGNED_BYTE, GL11.GL_RGB, GL11.GL_RGB8, GL11.GL_NEAREST, GL11.GL_NEAREST, GL12.GL_CLAMP_TO_EDGE, GL12.GL_CLAMP_TO_EDGE, false, false)); // Emissive
		gBuffer.checkComplete();
		
		targets.add("gBuffer", gBuffer);
		
		shader = assetManager.loadShader("/shaders/gBufferVS.glsl", "/shaders/gBufferFS.glsl");
	}
	
	@Override
	public void render(RenderContext context, RenderTargets targets, RenderHelpers helpers) {
		Scene scene = context.getScene();
		Camera camera = context.getCamera();
		
		gBuffer.bind();
		
		GL11.glClearColor(0, 0, 0, 1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		
		materialManager.updateMaterialBuffer();
		
		shader.bind();
		shader.setMatrix4f("projectionMatrix", camera.getProjectionMatrix(context.getWidth(), context.getHeight()));
		shader.setMatrix4f("viewMatrix", camera.getViewMatrix());
		
		GL15.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, scene.getGpuScene().getCommandsBufferId());
		
		GL30.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 0, scene.getGpuScene().getObjectSsboId());
		GL30.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 1, scene.getGpuScene().getInstanceSsboId());
		GL30.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 2, materialManager.getMaterialSsboId());
		
		GL30.glBindVertexArray(meshManager.getVaoId());
		
		GL43.glMultiDrawElementsIndirect(GL11.GL_TRIANGLES, GL11.GL_UNSIGNED_INT, 0, scene.getGpuScene().getCommandsCount(), 0);
		
		GL15.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, 0);
		
		GL30.glBindVertexArray(0);
		
		shader.unbind();
		gBuffer.unbind();
	}
	
}
