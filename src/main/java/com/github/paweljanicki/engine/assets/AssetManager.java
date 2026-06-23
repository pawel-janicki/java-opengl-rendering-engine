package com.github.paweljanicki.engine.assets;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import com.github.paweljanicki.engine.assets.models.Model;
import com.github.paweljanicki.engine.assets.models.ModelLoader;
import com.github.paweljanicki.engine.assets.models.ModelPart;
import com.github.paweljanicki.engine.assets.shaders.Shader;
import com.github.paweljanicki.engine.assets.textures.Texture;
import com.github.paweljanicki.engine.assets.textures.TextureLoader;
import com.github.paweljanicki.engine.assets.textures.TextureParameters;

public class AssetManager {
	
	private Map<String, Texture> textures = new HashMap<>();
	private Map<String, Model> models = new HashMap<>();
	private Map<String, Shader> shaders = new HashMap<>();
	
	public Texture loadTexture(String filePath, TextureParameters textureParameters) {
		Texture texture = textures.get(filePath);
		if (texture != null)
			return texture;
		
		texture = TextureLoader.load(filePath, textureParameters);
		textures.put(filePath, texture);
		
		return texture;
	}
	
	public void unloadTexture(String filePath) {
		Texture texture = textures.get(filePath);
		if (texture == null)
			return;
		
		GL11.glDeleteTextures(texture.getId());
		textures.remove(filePath);
	}
	
	public Model loadModel(String filePath, String texturesDirectory) {
		Model model = models.get(filePath);
		if (model != null)
			return model;
		
		model = ModelLoader.load(this, filePath, texturesDirectory);
		models.put(filePath, model);
		
		return model;
	}
	
	public void unloadModel(String filePath) {
		Model model = models.get(filePath);
		if (model == null)
			return;
		
		cleanUpModel(model);
		models.remove(filePath);
	}
	
	private void cleanUpModel(Model model) {
		for (ModelPart modelPart : model.getModelParts()) {
			GL30.glDeleteVertexArrays(modelPart.getMesh().getVaoId());
			
			for (int vbo : modelPart.getMesh().getVbos()) {
				GL15.glDeleteBuffers(vbo);
			}
		}
	}
	
	public Shader loadShader(String vertexShaderFilePath, String fragmentShaderFilePath) {
		String key = vertexShaderFilePath + "&" + fragmentShaderFilePath;
		Shader shader = shaders.get(key);
		if (shader != null)
			return shader;
		
		shader = new Shader(vertexShaderFilePath, fragmentShaderFilePath);
		shaders.put(key, shader);
		
		return shader;
	}
	
	public void unloadShader(String vertexShaderFilePath, String fragmentShaderFilePath) {
		String key = vertexShaderFilePath + "&" + fragmentShaderFilePath;
		Shader shader = shaders.get(key);
		if (shader == null)
			return;
		
		shader.cleanUp();
		shaders.remove(key);
	}
	
	public void cleanUp() {
		for (Texture texture : textures.values()) {
			GL11.glDeleteTextures(texture.getId());
		}
		
		for (Model model : models.values()) {
			cleanUpModel(model);
		}
		
		for (Shader shader : shaders.values()) {
			shader.cleanUp();
		}
		
		textures.clear();
		models.clear();
		shaders.clear();
	}
	
}
