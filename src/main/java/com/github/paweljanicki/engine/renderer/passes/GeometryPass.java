package com.github.paweljanicki.engine.renderer.passes;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import org.lwjgl.opengl.GL43;

import com.github.paweljanicki.engine.assets.AssetManager;
import com.github.paweljanicki.engine.assets.models.Material;
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
import com.github.paweljanicki.engine.scene.InstancedEntity;
import com.github.paweljanicki.engine.scene.Scene;

public class GeometryPass implements IRenderPass {
	
	private FrameBuffer gBuffer;
	
	private Shader shader;
	private Shader instancedShader;
	
	private Map<InstancedEntity, Integer> ssboIds = new HashMap<>();
	private Set<InstancedEntity> activeEntities = new HashSet<>();
	
	@Override
	public void init(AssetManager assetManager, RenderTargets targets, int width, int height) {
		gBuffer = new FrameBuffer(width, height);
		gBuffer.addDepthAttachment(new TextureParameters(GL11.GL_FLOAT, GL30.GL_DEPTH_COMPONENT, GL30.GL_DEPTH_COMPONENT24, GL11.GL_NEAREST, GL11.GL_NEAREST, GL12.GL_CLAMP_TO_EDGE, GL12.GL_CLAMP_TO_EDGE, false, false)); // Depth
		gBuffer.addColorAttachment(new TextureParameters(GL11.GL_FLOAT, GL11.GL_RGBA, GL30.GL_RGBA16F, GL11.GL_NEAREST, GL11.GL_NEAREST, GL12.GL_CLAMP_TO_EDGE, GL12.GL_CLAMP_TO_EDGE, false, false)); // Normal
		gBuffer.addColorAttachment(new TextureParameters(GL11.GL_UNSIGNED_BYTE, GL11.GL_RGBA, GL11.GL_RGBA8, GL11.GL_NEAREST, GL11.GL_NEAREST, GL12.GL_CLAMP_TO_EDGE, GL12.GL_CLAMP_TO_EDGE, false, false)); // Albedo
		gBuffer.addColorAttachment(new TextureParameters(GL11.GL_UNSIGNED_BYTE, GL11.GL_RGBA, GL11.GL_RGBA8, GL11.GL_NEAREST, GL11.GL_NEAREST, GL12.GL_CLAMP_TO_EDGE, GL12.GL_CLAMP_TO_EDGE, false, false)); // ARM
		gBuffer.addColorAttachment(new TextureParameters(GL11.GL_UNSIGNED_BYTE, GL11.GL_RGBA, GL11.GL_RGBA8, GL11.GL_NEAREST, GL11.GL_NEAREST, GL12.GL_CLAMP_TO_EDGE, GL12.GL_CLAMP_TO_EDGE, false, false)); // Emissive
		gBuffer.checkComplete();
		
		targets.add("gBuffer", gBuffer);
		
		shader = assetManager.loadShader("/shaders/gBufferVS.glsl", "/shaders/gBufferFS.glsl");
		shader.bind();
		shader.setInt("material.albedoMap", 0);
		shader.setInt("material.roughnessMap", 1);
		shader.setInt("material.metallicMap", 2);
		shader.setInt("material.aoMap", 3);
		shader.setInt("material.emissiveMap", 4);
		shader.setInt("material.normalMap", 5);
		shader.unbind();
		
		instancedShader = assetManager.loadShader("/shaders/gBufferInstancedVS.glsl", "/shaders/gBufferFS.glsl");
		instancedShader.bind();
		instancedShader.setInt("material.albedoMap", 0);
		instancedShader.setInt("material.roughnessMap", 1);
		instancedShader.setInt("material.metallicMap", 2);
		instancedShader.setInt("material.aoMap", 3);
		instancedShader.setInt("material.emissiveMap", 4);
		instancedShader.setInt("material.normalMap", 5);
		instancedShader.unbind();
	}
	
	@Override
	public void render(RenderContext context, RenderTargets targets, RenderHelpers helpers) {
		Scene scene = context.getScene();
		Camera camera = context.getCamera();
		
		gBuffer.bind();
		
		GL11.glClearColor(0, 0, 0, 1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		
		shader.bind();
		shader.setMatrix4f("projectionMatrix", camera.getProjectionMatrix(context.getWidth(), context.getHeight()));
		shader.setMatrix4f("viewMatrix", camera.getViewMatrix());
		
		renderEntities(scene.getEntities());
		
		shader.unbind();
		
		instancedShader.bind();
		instancedShader.setMatrix4f("projectionMatrix", camera.getProjectionMatrix(context.getWidth(), context.getHeight()));
		instancedShader.setMatrix4f("viewMatrix", camera.getViewMatrix());
		
		renderInstancedEntities(scene.getInstancedEntities());
		
		instancedShader.unbind();
		gBuffer.unbind();
	}
	
	private void renderEntities(List<Entity> entities) {
		for (Entity entity : entities) {
			shader.setMatrix4f("transformationMatrix", entity.getTransformationMatrix());
			
			for (ModelPart modelPart : entity.getModel().getModelParts()) {
				bindMaterial(shader, modelPart.getMaterial());
				
				GL30.glBindVertexArray(modelPart.getMesh().getVaoId());
				GL11.glDrawElements(GL11.GL_TRIANGLES, modelPart.getMesh().getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
				GL30.glBindVertexArray(0);
			}
		}
	}
	
	private void renderInstancedEntities(List<InstancedEntity> instancedEntities) {
		for (InstancedEntity instancedEntity : instancedEntities) {
			if (instancedEntity.getInstancesAmount() == 0)
				continue;
			
			if (!ssboIds.containsKey(instancedEntity)) {
				int ssboId = GL15.glGenBuffers();
				ssboIds.put(instancedEntity, ssboId);
			}
			
			int ssboId = ssboIds.get(instancedEntity);
			
			if (instancedEntity.isSizeDirty()) {
				FloatBuffer buffer = generateBufferData(instancedEntity);
				
				GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, ssboId);
				GL15.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, buffer, GL15.GL_DYNAMIC_DRAW);
				GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);
				
				instancedEntity.setContentDirty(false);
				instancedEntity.setSizeDirty(false);
			} else if (instancedEntity.isContentDirty()) {
				FloatBuffer buffer = generateBufferData(instancedEntity);
				
				GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, ssboId);
				GL15.glBufferSubData(GL43.GL_SHADER_STORAGE_BUFFER, 0, buffer);
				GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);
				
				instancedEntity.setContentDirty(false);
			}
			
			activeEntities.add(instancedEntity);
			
			for (ModelPart modelPart : instancedEntity.getModel().getModelParts()) {
				bindMaterial(instancedShader, modelPart.getMaterial());
				
				GL30.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 0, ssboId);
				
				GL30.glBindVertexArray(modelPart.getMesh().getVaoId());
				GL33.glDrawElementsInstanced(GL11.GL_TRIANGLES, modelPart.getMesh().getIndexCount(), GL11.GL_UNSIGNED_INT, 0, instancedEntity.getInstancesAmount());
				GL30.glBindVertexArray(0);
			}
		}
		
		Iterator<Entry<InstancedEntity, Integer>> iterator = ssboIds.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<InstancedEntity, Integer> entry = iterator.next();
			
			if (!activeEntities.contains(entry.getKey())) {
				GL15.glDeleteBuffers(entry.getValue());
				iterator.remove();
			}
		}
		
		activeEntities.clear();
	}
	
	private void bindMaterial(Shader shader, Material material) {
		shader.setVector3f("material.albedo", material.getAlbedo());
		shader.setFloat("material.roughness", material.getRoughness());
		shader.setFloat("material.metallic", material.getMetallic());
		
		shader.setBoolean("material.hasAlbedoMap", material.getAlbedoMap() != null);
		shader.setBoolean("material.hasRoughnessMap", material.getRoughnessMap() != null);
		shader.setBoolean("material.hasMetallicMap", material.getMetallicMap() != null);
		shader.setBoolean("material.hasAoMap", material.getAoMap() != null);
		shader.setBoolean("material.hasEmissiveMap", material.getEmissiveMap() != null);
		shader.setBoolean("material.hasNormalMap", material.getNormalMap() != null);
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, material.getAlbedoMap() != null ? material.getAlbedoMap().getId() : 0);
		
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, material.getRoughnessMap() != null ? material.getRoughnessMap().getId() : 0);
		
		GL13.glActiveTexture(GL13.GL_TEXTURE2);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, material.getMetallicMap() != null ? material.getMetallicMap().getId() : 0);
		
		GL13.glActiveTexture(GL13.GL_TEXTURE3);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, material.getAoMap() != null ? material.getAoMap().getId() : 0);
		
		GL13.glActiveTexture(GL13.GL_TEXTURE4);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, material.getEmissiveMap() != null ? material.getEmissiveMap().getId() : 0);
		
		GL13.glActiveTexture(GL13.GL_TEXTURE5);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, material.getNormalMap() != null ? material.getNormalMap().getId() : 0);
	}
	
	private FloatBuffer generateBufferData(InstancedEntity instancedEntity) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(instancedEntity.getTransformationMatrices().size() * 16);
		
		int offset = 0;
		for (Matrix4f matrix : instancedEntity.getTransformationMatrices()) {
			matrix.get(offset, buffer);
			offset += 16;
		}
		
		return buffer;
	}
	
	@Override
	public void cleanUp() {
		for (int ssboId : ssboIds.values()) {
			GL15.glDeleteBuffers(ssboId);
		}
		
		ssboIds.clear();
	}
	
}
