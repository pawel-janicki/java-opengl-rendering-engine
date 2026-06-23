package com.github.paweljanicki.engine.assets.shaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector4f;
import org.joml.Vector4i;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class Shader {
	
	private int programId;
	
	private int vertexShaderId;
	private int fragmentShaderId;
	
	private Map<String, Integer> uniforms = new HashMap<>();
	
	private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
	
	public Shader(String vertexShaderFilePath, String fragmentShaderFilePath) {
		programId = GL20.glCreateProgram();
		
		vertexShaderId = setShader(vertexShaderFilePath, GL20.GL_VERTEX_SHADER);
		fragmentShaderId = setShader(fragmentShaderFilePath, GL20.GL_FRAGMENT_SHADER);
		
		GL20.glAttachShader(programId, vertexShaderId);
		GL20.glAttachShader(programId, fragmentShaderId);
		
		GL20.glLinkProgram(programId);
		GL20.glValidateProgram(programId);
		
		if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
			System.err.println(GL20.glGetProgramInfoLog(programId));
			throw new RuntimeException("[ERROR] >> Could not link shader program!");
		}
	}
	
	private int setShader(String filePath, int type) {
		StringBuilder shaderSource = new StringBuilder();
		InputStream in = getClass().getResourceAsStream(filePath);
		if (in == null)
			throw new RuntimeException("[ERROR] >> Shader file '" + filePath + "' not found!");
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line;
		
		try {
			while ((line = reader.readLine()) != null) {
				shaderSource.append(line).append("\n");
			}
			
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		int shaderId = GL20.glCreateShader(type);
		GL20.glShaderSource(shaderId, shaderSource);
		GL20.glCompileShader(shaderId);
		
		if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
			System.err.println(GL20.glGetShaderInfoLog(shaderId));
			throw new RuntimeException("[ERROR] >> Could not compile shader '" + filePath + "'!");
		}
		
		return shaderId;
	}
	
	public void bind() {
		GL20.glUseProgram(programId);
	}
	
	public void unbind() {
		GL20.glUseProgram(0);
	}
	
	public void setBoolean(String name, boolean value) {
		GL20.glUniform1i(getUniformLocation(name), value ? 1 : 0);
	}
	
	public void setInt(String name, int value) {
		GL20.glUniform1i(getUniformLocation(name), value);
	}
	
	public void setFloat(String name, float value) {
		GL20.glUniform1f(getUniformLocation(name), value);
	}
	
	public void setVector2i(String name, Vector2i vector) {
		GL20.glUniform2i(getUniformLocation(name), vector.x, vector.y);
	}
	
	public void setVector3i(String name, Vector3i vector) {
		GL20.glUniform3i(getUniformLocation(name), vector.x, vector.y, vector.z);
	}
	
	public void setVector4i(String name, Vector4i vector) {
		GL20.glUniform4i(getUniformLocation(name), vector.x, vector.y, vector.z, vector.w);
	}
	
	public void setVector2f(String name, Vector2f vector) {
		GL20.glUniform2f(getUniformLocation(name), vector.x, vector.y);
	}
	
	public void setVector3f(String name, Vector3f vector) {
		GL20.glUniform3f(getUniformLocation(name), vector.x, vector.y, vector.z);
	}
	
	public void setVector4f(String name, Vector4f vector) {
		GL20.glUniform4f(getUniformLocation(name), vector.x, vector.y, vector.z, vector.w);
	}
	
	public void setMatrix4f(String name, Matrix4fc matrix) {
		matrix.get(matrixBuffer);
		GL20.glUniformMatrix4fv(getUniformLocation(name), false, matrixBuffer);
	}
	
	private int getUniformLocation(String name) {
		if (uniforms.containsKey(name))
			return uniforms.get(name);
		
		int location = GL20.glGetUniformLocation(programId, name);
		
		if (location != -1)
			uniforms.put(name, location);
		
		return location;
	}
	
	public void cleanUp() {
		unbind();
		
		GL20.glDetachShader(programId, vertexShaderId);
		GL20.glDetachShader(programId, fragmentShaderId);
		
		GL20.glDeleteShader(vertexShaderId);
		GL20.glDeleteShader(fragmentShaderId);
		
		GL20.glDeleteProgram(programId);
	}
	
}
