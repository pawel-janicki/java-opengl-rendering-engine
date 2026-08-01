package com.github.paweljanicki.engine.renderer;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class CubeRenderer {
	
	private static final float[] VERTICES = {
			-1, 1, -1,
			-1, -1, -1,
			1, -1, -1,
			1, -1, -1,
			1, 1, -1,
			-1, 1, -1,
			
			-1, -1, 1,
			-1, -1, -1,
			-1, 1, -1,
			-1, 1, -1,
			-1, 1, 1,
			-1, -1, 1,
			
			1, -1, -1,
			1, -1, 1,
			1, 1, 1,
			1, 1, 1,
			1, 1, -1,
			1, -1, -1,
			
			-1, -1, 1,
			-1, 1, 1,
			1, 1, 1,
			1, 1, 1,
			1, -1, 1,
			-1, -1, 1,
			
			-1, 1, -1,
			1, 1, -1,
			1, 1, 1,
			1, 1, 1,
			-1, 1, 1,
			-1, 1, -1,
			
			-1, -1, -1,
			-1, -1, 1,
			1, -1, -1,
			1, -1, -1,
			-1, -1, 1,
			1, -1, 1
	};
	
	private int vaoId;
	
	private int positionsVboId;
	
	public CubeRenderer() {
		vaoId = GL30.glGenVertexArrays();
		positionsVboId = GL15.glGenBuffers();
		
		FloatBuffer positionsBuffer = BufferUtils.createFloatBuffer(VERTICES.length);
		positionsBuffer.put(VERTICES);
		positionsBuffer.flip();
		
		GL30.glBindVertexArray(vaoId);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, positionsVboId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, positionsBuffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
		GL20.glEnableVertexAttribArray(0);
		
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		
		GL30.glBindVertexArray(0);
	}
	
	public void render() {
		GL30.glBindVertexArray(vaoId);
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, VERTICES.length / 3);
		GL30.glBindVertexArray(0);
	}
	
	public void cleanUp() {
		GL30.glDeleteVertexArrays(vaoId);
		
		GL15.glDeleteBuffers(positionsVboId);
	}
	
}
