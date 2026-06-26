package com.github.paweljanicki.engine.renderer.passes;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

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
import com.github.paweljanicki.engine.scene.Entity;

public class GeometryPass implements IRenderPass {
	
	private FrameBuffer gBuffer;
	private Shader shader;
	
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
		shader.unbind();
	}
	
	@Override
	public void render(RenderContext context, RenderTargets targets, RenderHelpers helpers) {
		gBuffer.bind();
		
		GL11.glClearColor(0, 0, 0, 1);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		
		shader.bind();
		shader.setMatrix4f("projectionMatrix", context.getCamera().getProjectionMatrix(context.getWidth(), context.getHeight()));
		shader.setMatrix4f("viewMatrix", context.getCamera().getViewMatrix());
		
		for (Entity entity : context.getScene().getEntities()) {
			shader.setMatrix4f("transformationMatrix", entity.getTransformationMatrix());
			
			for (ModelPart modelPart : entity.getModel().getModelParts()) {
				Material material = modelPart.getMaterial();
				
				shader.setVector3f("material.albedo", material.getAlbedo());
				shader.setFloat("material.roughness", material.getRoughness());
				shader.setFloat("material.metallic", material.getMetallic());
				
				shader.setBoolean("material.hasAlbedoMap", material.getAlbedoMap() != null);
				shader.setBoolean("material.hasRoughnessMap", material.getRoughnessMap() != null);
				shader.setBoolean("material.hasMetallicMap", material.getMetallicMap() != null);
				shader.setBoolean("material.hasAoMap", material.getAoMap() != null);
				shader.setBoolean("material.hasEmissiveMap", material.getEmissiveMap() != null);
				
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
				
				GL30.glBindVertexArray(modelPart.getMesh().getVaoId());
				GL11.glDrawElements(GL11.GL_TRIANGLES, modelPart.getMesh().getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
				GL30.glBindVertexArray(0);
			}
		}
		
		shader.unbind();
		gBuffer.unbind();
	}
	
}
