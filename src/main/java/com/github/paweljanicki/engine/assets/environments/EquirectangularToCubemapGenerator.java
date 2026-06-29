package com.github.paweljanicki.engine.assets.environments;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
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

public class EquirectangularToCubemapGenerator {
	
	private static final int SIZE = 1024;
	
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
	
	public EquirectangularToCubemapGenerator(AssetManager assetManager) {
		fbo = new FrameBuffer(SIZE, SIZE);
		
		shader = assetManager.loadShader("/shaders/cubeVS.glsl", "/shaders/equirectangularToCubemapFS.glsl");
		shader.bind();
		shader.setInt("equirectangularMap", 0);
		shader.setMatrix4f("projectionMatrix", PROJECTION_MATRIX);
		shader.unbind();
	}
	
	public Texture generate(CubeRenderer cubeRenderer, Texture texture) {
		Texture cubemap = TextureLoader.generateCubemap(SIZE, TextureParameters.DEFAULT_HDR);
		
		fbo.bind();
		
		GL11.glEnable(GL43.GL_TEXTURE_CUBE_MAP_SEAMLESS);
		
		shader.bind();
		
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getId());
		
		for (int i = 0; i < 6; i++) {
			shader.setMatrix4f("viewMatrix", VIEW_MATRICES[i]);
			
			GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL30.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, cubemap.getId(), 0);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			
			cubeRenderer.render();
		}
		
		shader.unbind();
		fbo.unbind();
		
		return cubemap;
	}
	
	public void cleanUp() {
		fbo.cleanUp();
	}
	
}
