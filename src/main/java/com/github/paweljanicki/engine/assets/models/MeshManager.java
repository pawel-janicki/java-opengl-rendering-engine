package com.github.paweljanicki.engine.assets.models;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;

public class MeshManager {
	
	private int vaoId;
	
	private int indicesVboId;
	private int positionsVboId;
	private int textureCoordsVboId;
	private int normalsVboId;
	private int tangentsVboId;
	
	private int indexOffset;
	private int vertexOffset;
	
	public MeshManager() {
		vaoId = GL30.glGenVertexArrays();
		
		GL30.glBindVertexArray(vaoId);
		
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);
		GL20.glEnableVertexAttribArray(3);
		
		GL30.glBindVertexArray(0);
	}
	
	private void resizeIndicesBuffer(int oldSize, int extraCapacity) {
		indicesVboId = resizeBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesVboId, oldSize, extraCapacity);
		
		GL30.glBindVertexArray(vaoId);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesVboId);
		GL30.glBindVertexArray(0);
	}
	
	private void resizeAttributesBuffers(int oldSize, int extraCapacity) {
		positionsVboId = resizeBuffer(GL15.GL_ARRAY_BUFFER, positionsVboId, oldSize * 3, extraCapacity * 3);
		textureCoordsVboId = resizeBuffer(GL15.GL_ARRAY_BUFFER, textureCoordsVboId, oldSize * 2, extraCapacity * 2);
		normalsVboId = resizeBuffer(GL15.GL_ARRAY_BUFFER, normalsVboId, oldSize * 3, extraCapacity * 3);
		tangentsVboId = resizeBuffer(GL15.GL_ARRAY_BUFFER, tangentsVboId, oldSize * 4, extraCapacity * 4);
		
		GL30.glBindVertexArray(vaoId);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, positionsVboId);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, textureCoordsVboId);
		GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 0, 0);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, normalsVboId);
		GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 0, 0);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, tangentsVboId);
		GL20.glVertexAttribPointer(3, 4, GL11.GL_FLOAT, false, 0, 0);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		
		GL30.glBindVertexArray(0);
	}
	
	private int resizeBuffer(int target, int oldBufferId, int oldSize, int extraCapacity) {
		int newBufferId = GL15.glGenBuffers();
		GL15.glBindBuffer(target, newBufferId);
		GL15.glBufferData(target, oldSize + extraCapacity, GL15.GL_STATIC_DRAW);
		
		GL15.glBindBuffer(GL31.GL_COPY_READ_BUFFER, oldBufferId);
		GL31.glCopyBufferSubData(GL31.GL_COPY_READ_BUFFER, target, 0, 0, oldSize);
		
		GL15.glDeleteBuffers(oldBufferId);
		
		return newBufferId;
	}
	
	public Mesh addMesh(float[] positions, float[] textureCoords, float[] normals, float[] tangents, int[] indices) {
		resizeIndicesBuffer(indexOffset * Integer.BYTES, indices.length * Integer.BYTES);
		
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesVboId);
		GL15.glBufferSubData(GL15.GL_ELEMENT_ARRAY_BUFFER, (long) indexOffset * Integer.BYTES, indices);
		
		int vertexCount = positions.length / 3;
		
		resizeAttributesBuffers(vertexOffset * Float.BYTES, vertexCount * Float.BYTES);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, positionsVboId);
		GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) vertexOffset * 3 * Float.BYTES, positions);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, textureCoordsVboId);
		GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) vertexOffset * 2 * Float.BYTES, textureCoords);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, normalsVboId);
		GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) vertexOffset * 3 * Float.BYTES, normals);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, tangentsVboId);
		GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, (long) vertexOffset * 4 * Float.BYTES, tangents);
		
		Mesh mesh = new Mesh(indices.length, indexOffset, vertexOffset);
		
		indexOffset += indices.length;
		vertexOffset += vertexCount;
		
		return mesh;
	}
	
	public void cleanUp() {
		GL30.glDeleteVertexArrays(vaoId);
		
		GL15.glDeleteBuffers(indicesVboId);
		GL15.glDeleteBuffers(positionsVboId);
		GL15.glDeleteBuffers(textureCoordsVboId);
		GL15.glDeleteBuffers(normalsVboId);
		GL15.glDeleteBuffers(tangentsVboId);
	}
	
	public int getVaoId() {
		return vaoId;
	}
	
}
