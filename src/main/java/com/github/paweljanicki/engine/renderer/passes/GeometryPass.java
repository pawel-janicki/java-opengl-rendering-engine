package com.github.paweljanicki.engine.renderer.passes;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map.Entry;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL43;

import com.github.paweljanicki.engine.assets.AssetManager;
import com.github.paweljanicki.engine.assets.models.MaterialManager;
import com.github.paweljanicki.engine.assets.models.Mesh;
import com.github.paweljanicki.engine.assets.models.MeshManager;
import com.github.paweljanicki.engine.assets.models.Model;
import com.github.paweljanicki.engine.assets.models.ModelPart;
import com.github.paweljanicki.engine.assets.shaders.Shader;
import com.github.paweljanicki.engine.assets.textures.TextureParameters;
import com.github.paweljanicki.engine.renderer.FrameBuffer;
import com.github.paweljanicki.engine.renderer.IRenderPass;
import com.github.paweljanicki.engine.renderer.RenderContext;
import com.github.paweljanicki.engine.renderer.RenderHelpers;
import com.github.paweljanicki.engine.renderer.RenderTargets;
import com.github.paweljanicki.engine.scene.Camera;
import com.github.paweljanicki.engine.scene.Entity;
import com.github.paweljanicki.engine.scene.Scene;

public class GeometryPass implements IRenderPass {
	
	private MaterialManager materialManager;
	private MeshManager meshManager;
	
	private FrameBuffer gBuffer;
	
	private Shader shader;
	
	private int commandsBufferId;
	private int objectSsboId;
	private int instanceSsboId;
	
	private ByteBuffer commandsBuffer;
	private ByteBuffer objectBuffer;
	private ByteBuffer instanceBuffer;
	
	private final ByteBuffer matrixUploadBuffer = BufferUtils.createByteBuffer(Matrix4f.BYTES);
	
	private Scene lastScene;
	
	@Override
	public void init(AssetManager assetManager, RenderTargets targets, int width, int height) {
		this.materialManager = assetManager.getMaterialManager();
		this.meshManager = assetManager.getMeshManager();
		
		gBuffer = new FrameBuffer(width, height);
		gBuffer.addDepthAttachment(new TextureParameters(GL11.GL_FLOAT, GL30.GL_DEPTH_COMPONENT, GL30.GL_DEPTH_COMPONENT24, GL11.GL_NEAREST, GL11.GL_NEAREST, GL12.GL_CLAMP_TO_EDGE, GL12.GL_CLAMP_TO_EDGE, false, false)); // Depth
		gBuffer.addColorAttachment(new TextureParameters(GL11.GL_FLOAT, GL30.GL_RG, GL31.GL_RG16_SNORM, GL11.GL_NEAREST, GL11.GL_NEAREST, GL12.GL_CLAMP_TO_EDGE, GL12.GL_CLAMP_TO_EDGE, false, false)); // Normal
		gBuffer.addColorAttachment(new TextureParameters(GL11.GL_UNSIGNED_BYTE, GL11.GL_RGB, GL11.GL_RGB8, GL11.GL_NEAREST, GL11.GL_NEAREST, GL12.GL_CLAMP_TO_EDGE, GL12.GL_CLAMP_TO_EDGE, false, false)); // Albedo
		gBuffer.addColorAttachment(new TextureParameters(GL11.GL_UNSIGNED_BYTE, GL11.GL_RGB, GL11.GL_RGB8, GL11.GL_NEAREST, GL11.GL_NEAREST, GL12.GL_CLAMP_TO_EDGE, GL12.GL_CLAMP_TO_EDGE, false, false)); // ARM
		gBuffer.addColorAttachment(new TextureParameters(GL11.GL_UNSIGNED_BYTE, GL11.GL_RGB, GL11.GL_RGB8, GL11.GL_NEAREST, GL11.GL_NEAREST, GL12.GL_CLAMP_TO_EDGE, GL12.GL_CLAMP_TO_EDGE, false, false)); // Emissive
		gBuffer.checkComplete();
		
		targets.add("gBuffer", gBuffer);
		
		shader = assetManager.loadShader("/shaders/gBufferVS.glsl", "/shaders/gBufferFS.glsl");
		
		commandsBufferId = GL15.glGenBuffers();
		objectSsboId = GL15.glGenBuffers();
		instanceSsboId = GL15.glGenBuffers();
		
		commandsBuffer = BufferUtils.createByteBuffer(1);
		objectBuffer = BufferUtils.createByteBuffer(1);
		instanceBuffer = BufferUtils.createByteBuffer(1);
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
		
		renderEntities(scene);
		
		shader.unbind();
		gBuffer.unbind();
	}
	
	private void renderEntities(Scene scene) {
		int commandsCount = 0;
		int instancesCount = scene.getEntities().size();
		
		for (Entry<Model, List<Entity>> modelGroup : scene.getModelGroups().entrySet()) {
			commandsCount += modelGroup.getKey().getModelParts().size();
		}
		
		if (commandsBuffer.capacity() < commandsCount * 5 * Integer.BYTES) {
			commandsBuffer = BufferUtils.createByteBuffer(commandsCount * 5 * Integer.BYTES);
			objectBuffer = BufferUtils.createByteBuffer(commandsCount * Integer.BYTES);
		}
		
		if (instanceBuffer.capacity() < instancesCount * Matrix4f.BYTES) {
			instanceBuffer = BufferUtils.createByteBuffer(instancesCount * Matrix4f.BYTES);
		}
		
		if (scene != lastScene || scene.isModelGroupsDirty()) {
			int commandOffset = 0;
			int objectOffset = 0;
			int instanceOffset = 0;
			
			int instanceIndex = 0;
			
			for (Entry<Model, List<Entity>> modelGroup : scene.getModelGroups().entrySet()) {
				for (ModelPart modelPart : modelGroup.getKey().getModelParts()) {
					Mesh mesh = modelPart.getMesh();
					
					commandsBuffer.putInt(commandOffset, mesh.getIndexCount());
					commandsBuffer.putInt(commandOffset + 4, modelGroup.getValue().size());
					commandsBuffer.putInt(commandOffset + 8, mesh.getFirstIndex());
					commandsBuffer.putInt(commandOffset + 12, mesh.getBaseVertex());
					commandsBuffer.putInt(commandOffset + 16, instanceIndex);
					commandOffset += 5 * Integer.BYTES;
					
					objectBuffer.putInt(objectOffset, modelPart.getMaterialId());
					objectOffset += Integer.BYTES;
				}
				
				for (int i = 0; i < modelGroup.getValue().size(); i++) {
					modelGroup.getValue().get(i).setGpuIndex(instanceIndex + i);
					
					modelGroup.getValue().get(i).getTransformationMatrix().get(instanceOffset, instanceBuffer);
					instanceOffset += Matrix4f.BYTES;
				}
				
				instanceIndex += modelGroup.getValue().size();
			}
			
			GL15.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, commandsBufferId);
			GL15.glBufferData(GL40.GL_DRAW_INDIRECT_BUFFER, commandsBuffer, GL15.GL_DYNAMIC_DRAW);
			
			GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, objectSsboId);
			GL15.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, objectBuffer, GL15.GL_DYNAMIC_DRAW);
			GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);
			
			GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, instanceSsboId);
			GL15.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, instanceBuffer, GL15.GL_DYNAMIC_DRAW);
			GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);
			
			scene.setModelGroupsDirty(false);
		} else {
			GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, instanceSsboId);
			
			for (Entity entity : scene.getDirtyEntities()) {
				entity.getTransformationMatrix().get(matrixUploadBuffer);
				GL15.glBufferSubData(GL43.GL_SHADER_STORAGE_BUFFER, entity.getGpuIndex() * Matrix4f.BYTES, matrixUploadBuffer);
			}
			
			GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);
		}
		
		scene.getDirtyEntities().clear();
		lastScene = scene;
		
		GL15.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, commandsBufferId);
		
		GL30.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 0, objectSsboId);
		GL30.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 1, instanceSsboId);
		GL30.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 2, materialManager.getMaterialSsboId());
		
		GL30.glBindVertexArray(meshManager.getVaoId());
		
		GL43.glMultiDrawElementsIndirect(GL11.GL_TRIANGLES, GL11.GL_UNSIGNED_INT, 0, commandsCount, 0);
		
		GL15.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, 0);
		
		GL30.glBindVertexArray(0);
	}
	
	@Override
	public void cleanUp() {
		GL15.glDeleteBuffers(commandsBufferId);
		GL15.glDeleteBuffers(objectSsboId);
		GL15.glDeleteBuffers(instanceSsboId);
	}
	
}
