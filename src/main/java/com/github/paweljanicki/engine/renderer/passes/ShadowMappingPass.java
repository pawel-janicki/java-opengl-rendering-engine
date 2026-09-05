package com.github.paweljanicki.engine.renderer.passes;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL32;
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
import com.github.paweljanicki.engine.scene.DirectionalLight;
import com.github.paweljanicki.engine.scene.Scene;

public class ShadowMappingPass implements IRenderPass {
	
	public static final int SHADOW_CASCADES = 4;
	public static final float[] SHADOW_DISTANCES = new float[] { 10, 40, 90, 200 };
	public static final float SHADOW_BLEND_SIZE = 2;
	
	private static final int SHADOW_MAP_SIZE = 4096;
	
	private static final Vector3f WORLD_SPACE_UP = new Vector3f(0, 1, 0);
	private static final Vector3f WORLD_SPACE_RIGHT = new Vector3f(1, 0, 0);
	
	private MeshManager meshManager;
	
	private FrameBuffer shadowsFbo;
	private Shader shader;
	
	private Matrix4f[] lightSpaceMatrices = new Matrix4f[SHADOW_CASCADES];
	
	@Override
	public void init(AssetManager assetManager, RenderTargets targets, int width, int height) {
		this.meshManager = assetManager.getMeshManager();
		
		shadowsFbo = new FrameBuffer(SHADOW_MAP_SIZE, SHADOW_MAP_SIZE, false);
		shadowsFbo.addDepthArrayAttachment(SHADOW_CASCADES, new TextureParameters(GL11.GL_FLOAT, GL11.GL_DEPTH_COMPONENT, GL14.GL_DEPTH_COMPONENT24, GL11.GL_LINEAR, GL11.GL_LINEAR, GL13.GL_CLAMP_TO_BORDER, GL13.GL_CLAMP_TO_BORDER, false, false));
		shadowsFbo.checkComplete();
		
		GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, shadowsFbo.getDepthTexture().getId());
		GL11.glTexParameteri(GL30.GL_TEXTURE_2D_ARRAY, GL14.GL_TEXTURE_COMPARE_MODE, GL14.GL_COMPARE_R_TO_TEXTURE);
		GL11.glTexParameterfv(GL30.GL_TEXTURE_2D_ARRAY, GL13.GL_TEXTURE_BORDER_COLOR, new float[] { 1, 1, 1, 1 });
		GL11.glBindTexture(GL30.GL_TEXTURE_2D_ARRAY, 0);
		
		targets.add("shadows", shadowsFbo);
		
		shader = assetManager.loadShader("/shaders/shadowMappingVS.glsl", "/shaders/shadowMappingFS.glsl");
		
		for (int i = 0; i < SHADOW_CASCADES; i++) {
			lightSpaceMatrices[i] = new Matrix4f();
		}
	}
	
	@Override
	public void render(RenderContext context, RenderTargets targets, RenderHelpers helpers) {
		Scene scene = context.getScene();
		if (scene.getDirectionalLight() == null)
			return;
		
		Camera camera = context.getCamera();
		
		shadowsFbo.bind();
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL32.GL_DEPTH_CLAMP);
		
		shader.bind();
		
		for (int i = 0; i < SHADOW_CASCADES; i++) {
			GL30.glFramebufferTextureLayer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, shadowsFbo.getDepthTexture().getId(), 0, i);
			GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
			
			float nearPlane = i == 0 ? camera.getNearPlane() : SHADOW_DISTANCES[i - 1] - SHADOW_BLEND_SIZE;
			float farPlane = i == SHADOW_CASCADES - 1 ? SHADOW_DISTANCES[i] : SHADOW_DISTANCES[i] + SHADOW_BLEND_SIZE;
			lightSpaceMatrices[i].set(getLightSpaceMatrix(camera, context.getWidth(), context.getHeight(), scene.getDirectionalLight(), nearPlane, farPlane));
			
			shader.setMatrix4f("lightSpaceMatrix", lightSpaceMatrices[i]);
			
			GL15.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, scene.getGpuScene().getCommandsBufferId());
			
			GL30.glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 0, scene.getGpuScene().getInstanceSsboId());
			
			GL30.glBindVertexArray(meshManager.getVaoId());
			
			GL43.glMultiDrawElementsIndirect(GL11.GL_TRIANGLES, GL11.GL_UNSIGNED_INT, 0, scene.getGpuScene().getCommandsCount(), 0);
			
			GL15.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, 0);
			
			GL30.glBindVertexArray(0);
		}
		
		shader.unbind();
		
		GL11.glDisable(GL32.GL_DEPTH_CLAMP);
		
		shadowsFbo.unbind();
		
		context.setLightSpaceMatrices(lightSpaceMatrices);
	}
	
	private Matrix4f getLightSpaceMatrix(Camera camera, int width, int height, DirectionalLight light, float nearPlane, float farPlane) {
		Matrix4f projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(camera.getFov()), width / (float) height, nearPlane, farPlane);
		Vector4f[] frustumCorners = getFrustumCorners(projectionMatrix, camera.getViewMatrix());
		
		Vector3f center = new Vector3f();
		for (Vector4f corner : frustumCorners) {
			center.add(corner.x, corner.y, corner.z);
		}
		center.div(frustumCorners.length);
		
		float radius = 0.0f;
		for (Vector4f corner : frustumCorners) {
			float distance = new Vector3f(corner.x, corner.y, corner.z).distance(center);
			if (distance > radius)
				radius = distance;
		}
		
		Vector3f lightDirection = new Vector3f(light.getDirection()).normalize();
		
		Vector3f lightSpaceUp = Math.abs(lightDirection.y) == 1 ? WORLD_SPACE_RIGHT : WORLD_SPACE_UP;
		Vector3f lightSpaceRight = new Vector3f(lightDirection).cross(lightSpaceUp).normalize();
		lightSpaceUp = new Vector3f(lightSpaceRight).cross(lightDirection).normalize();
		
		float centerLightSpaceX = center.dot(lightSpaceRight);
		float centerLightSpaceY = center.dot(lightSpaceUp);
		float centerLightSpaceZ = center.dot(lightDirection);
		
		float texelSize = (radius * 2) / SHADOW_MAP_SIZE;
		
		float snappedCenterX = Math.round(centerLightSpaceX / texelSize) * texelSize;
		float snappedCenterY = Math.round(centerLightSpaceY / texelSize) * texelSize;
		
		Vector3f snappedCenter = new Vector3f(lightSpaceRight).mul(snappedCenterX).add(new Vector3f(lightSpaceUp).mul(snappedCenterY)).add(new Vector3f(lightDirection).mul(centerLightSpaceZ));
		
		Matrix4f lightViewMatrix = new Matrix4f().lookAt(new Vector3f(snappedCenter).sub(lightDirection), snappedCenter, lightSpaceUp);
		
		Vector3f min = new Vector3f(Float.MAX_VALUE);
		Vector3f max = new Vector3f(-Float.MAX_VALUE);
		
		for (Vector4f corner : frustumCorners) {
			Vector4f v = new Vector4f(corner).mul(lightViewMatrix);
			
			min.min(new Vector3f(v.x, v.y, v.z));
			max.max(new Vector3f(v.x, v.y, v.z));
		}
		
		Matrix4f lightProjectionMatrix = new Matrix4f().ortho(-radius, radius, -radius, radius, -max.z, -min.z);
		
		return new Matrix4f(lightProjectionMatrix).mul(lightViewMatrix);
	}
	
	private Vector4f[] getFrustumCorners(Matrix4fc projectionMatrix, Matrix4fc viewMatrix) {
		Matrix4f inverseProjectionViewMatrix = new Matrix4f(projectionMatrix).mul(viewMatrix).invert();
		
		Vector4f[] frustumCorners = new Vector4f[8];
		int i = 0;
		
		for (int x = 0; x < 2; x++) {
			for (int y = 0; y < 2; y++) {
				for (int z = 0; z < 2; z++) {
					Vector4f corner = new Vector4f(x * 2 - 1, y * 2 - 1, z * 2 - 1, 1).mul(inverseProjectionViewMatrix);
					frustumCorners[i] = corner.div(corner.w);
					i++;
				}
			}
		}
		
		return frustumCorners;
	}
	
}
