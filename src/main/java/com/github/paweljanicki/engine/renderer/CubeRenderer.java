package com.github.paweljanicki.engine.renderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import com.github.paweljanicki.engine.assets.models.Mesh;
import com.github.paweljanicki.engine.assets.models.MeshLoader;

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
	
	private Mesh cube;
	
	public CubeRenderer() {
		cube = MeshLoader.loadCube(VERTICES);
	}
	
	public void render() {
		GL30.glBindVertexArray(cube.getVaoId());
		GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, VERTICES.length / 3);
		GL30.glBindVertexArray(0);
	}
	
	public void cleanUp() {
		GL30.glDeleteVertexArrays(cube.getVaoId());
		
		for (int vbo : cube.getVbos()) {
			GL15.glDeleteBuffers(vbo);
		}
	}
	
}
