package com.github.paweljanicki.engine.renderer;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class QuadRenderer {
	
	private static final float[] QUAD_VERTICES = new float[] {
			-1, 1,
			-1, -1,
			1, 1,
			1, -1
	};
	
	private static final float[] QUAD_UVS = new float[] {
			0, 1,
			0, 0,
			1, 1,
			1, 0
	};
	
	private static final int[] QUAD_INDICES = new int[] {
			0, 2, 1,
			2, 3, 1
	};
	
	private int vaoId;
	
	private int indicesVboId;
	private int positionsVboId;
	private int textureCoordsVboId;
	
	public QuadRenderer() {
		vaoId = GL30.glGenVertexArrays();
		indicesVboId = GL15.glGenBuffers();
		positionsVboId = GL15.glGenBuffers();
		textureCoordsVboId = GL15.glGenBuffers();
		
		IntBuffer indicesBuffer = BufferUtils.createIntBuffer(QUAD_INDICES.length);
		indicesBuffer.put(QUAD_INDICES);
		indicesBuffer.flip();
		
		FloatBuffer positionsBuffer = BufferUtils.createFloatBuffer(QUAD_VERTICES.length);
		positionsBuffer.put(QUAD_VERTICES);
		positionsBuffer.flip();
		
		FloatBuffer textureCoordsBuffer = BufferUtils.createFloatBuffer(QUAD_UVS.length);
		textureCoordsBuffer.put(QUAD_UVS);
		textureCoordsBuffer.flip();
		
		GL30.glBindVertexArray(vaoId);
		
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesVboId);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, positionsVboId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, positionsBuffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(0, 2, GL11.GL_FLOAT, false, 0, 0);
		GL20.glEnableVertexAttribArray(0);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, textureCoordsVboId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, textureCoordsBuffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 0, 0);
		GL20.glEnableVertexAttribArray(1);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		
		GL30.glBindVertexArray(0);
	}
	
	public void render() {
		GL30.glBindVertexArray(vaoId);
		GL11.glDrawElements(GL11.GL_TRIANGLES, QUAD_INDICES.length, GL11.GL_UNSIGNED_INT, 0);
		GL30.glBindVertexArray(0);
	}
	
	public void cleanUp() {
		GL30.glDeleteVertexArrays(vaoId);
		
		GL15.glDeleteBuffers(indicesVboId);
		GL15.glDeleteBuffers(positionsVboId);
		GL15.glDeleteBuffers(textureCoordsVboId);
	}
	
}
