package com.github.paweljanicki.engine.assets.models;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class MeshLoader {
	
	public static Mesh load2D(float[] positions, float[] textureCoords, int[] indices) {
		int vaoId = createVAO();
		int ebo = storeIndicesBuffer(indices);
		int positionsVbo = storeDataInAttributeList(0, 2, positions);
		int textureCoordsVbo = storeDataInAttributeList(1, 2, textureCoords);
		
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		
		GL30.glBindVertexArray(0);
		
		return new Mesh(vaoId, indices.length, ebo, positionsVbo, textureCoordsVbo);
	}
	
	public static Mesh load(float[] positions, float[] textureCoords, float[] normals, int[] indices) {
		int vaoId = createVAO();
		int ebo = storeIndicesBuffer(indices);
		int positionsVbo = storeDataInAttributeList(0, 3, positions);
		int textureCoordsVbo = storeDataInAttributeList(1, 2, textureCoords);
		int normalsVbo = storeDataInAttributeList(2, 3, normals);
		
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		
		GL30.glBindVertexArray(0);
		
		return new Mesh(vaoId, indices.length, ebo, positionsVbo, textureCoordsVbo, normalsVbo);
	}
	
	private static int createVAO() {
		int vaoID = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vaoID);
		
		return vaoID;
	}
	
	private static int storeIndicesBuffer(int[] indices) {
		int vboId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboId);
		IntBuffer buffer = storeDataInIntBuffer(indices);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
		
		return vboId;
	}
	
	private static int storeDataInAttributeList(int attributeNumber, int dimensions, float[] data) {
		int vboId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
		FloatBuffer buffer = storeDataInFloatBuffer(data);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(attributeNumber, dimensions, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		
		return vboId;
	}
	
	private static FloatBuffer storeDataInFloatBuffer(float[] data) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		
		return buffer;
	}
	
	private static IntBuffer storeDataInIntBuffer(int[] data) {
		IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		
		return buffer;
	}
	
}
