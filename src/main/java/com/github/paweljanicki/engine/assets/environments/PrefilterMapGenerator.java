package com.github.paweljanicki.engine.assets.environments;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL43;

import com.github.paweljanicki.engine.assets.AssetManager;
import com.github.paweljanicki.engine.assets.shaders.Shader;
import com.github.paweljanicki.engine.assets.textures.Texture;
import com.github.paweljanicki.engine.assets.textures.TextureLoader;
import com.github.paweljanicki.engine.assets.textures.TextureParameters;
import com.github.paweljanicki.engine.renderer.CubeRenderer;
import com.github.paweljanicki.engine.renderer.FrameBuffer;

public class PrefilterMapGenerator {
	
	private static final int SIZE = 512;
	private static final int MIP_LEVELS_AMOUNT = 5;
	
	private static final Matrix4f PROJECTION_MATRIX = new Matrix4f().perspective((float) Math.toRadians(90), 1, 0.1f, 10);
	private static final Matrix4f VIEW_MATRICES[] = {
			new Matrix4f().lookAt(new Vector3f(0, 0, 0), new Vector3f(1, 0, 0), new Vector3f(0, -1, 0)),
			new Matrix4f().lookAt(new Vector3f(0, 0, 0), new Vector3f(-1, 0, 0), new Vector3f(0, -1, 0)),
			new Matrix4f().lookAt(new Vector3f(0, 0, 0), new Vector3f(0, 1, 0), new Vector3f(0, 0, 1)),
			new Matrix4f().lookAt(new Vector3f(0, 0, 0), new Vector3f(0, -1, 0), new Vector3f(0, 0, -1)),
			new Matrix4f().lookAt(new Vector3f(0, 0, 0), new Vector3f(0, 0, 1), new Vector3f(0, -1, 0)),
			new Matrix4f().lookAt(new Vector3f(0, 0, 0), new Vector3f(0, 0, -1), new Vector3f(0, -1, 0)),
	};
	
	private FrameBuffer fbo;
	private Shader shader;
	
	public PrefilterMapGenerator(AssetManager assetManager) {
		fbo = new FrameBuffer(SIZE, SIZE);
		
		shader = assetManager.loadShader("/shaders/cubeVS.glsl", "/shaders/prefilterFS.glsl");
		shader.bind();
		shader.setInt("environmentMap", 0);
		shader.setMatrix4f("projectionMatrix", PROJECTION_MATRIX);
		shader.unbind();
	}
	
	public Texture generate(CubeRenderer cubeRenderer, Texture environmentMap) {
		Texture prefilterMap = TextureLoader.generateCubemap(SIZE, new TextureParameters(GL11.GL_FLOAT, GL11.GL_RGB, GL30.GL_RGB16F, GL11.GL_LINEAR_MIPMAP_LINEAR, GL11.GL_LINEAR, GL12.GL_CLAMP_TO_EDGE, GL12.GL_CLAMP_TO_EDGE, true, false));
		
		fbo.bind();
		
		GL11.glEnable(GL43.GL_TEXTURE_CUBE_MAP_SEAMLESS);
		
		shader.bind();
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, environmentMap.getId());
		
		for (int mip = 0; mip < MIP_LEVELS_AMOUNT; mip++) {
			int mipWidth = (int) (SIZE * Math.pow(0.5, mip));
			int mipHeight = (int) (SIZE * Math.pow(0.5, mip));
			
			GL11.glViewport(0, 0, mipWidth, mipHeight);
			
			float roughness = (float) mip / (float) (MIP_LEVELS_AMOUNT - 1);
			shader.setFloat("roughness", roughness);
			
			for (int i = 0; i < 6; i++) {
				shader.setMatrix4f("viewMatrix", VIEW_MATRICES[i]);
				
				GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, prefilterMap.getId(), mip);
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
				
				cubeRenderer.render();
			}
		}
		
		shader.unbind();
		fbo.unbind();
		
		return prefilterMap;
	}
	
	public void cleanUp() {
		fbo.cleanUp();
	}
	
}
