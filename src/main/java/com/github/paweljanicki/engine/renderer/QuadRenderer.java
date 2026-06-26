package com.github.paweljanicki.engine.renderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

import com.github.paweljanicki.engine.assets.models.Mesh;
import com.github.paweljanicki.engine.assets.models.MeshLoader;

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
	
	private Mesh quad;
	
	public QuadRenderer() {
		quad = MeshLoader.load2D(QUAD_VERTICES, QUAD_UVS, QUAD_INDICES);
	}
	
	public void render() {
		GL30.glBindVertexArray(quad.getVaoId());
		GL11.glDrawElements(GL11.GL_TRIANGLES, quad.getIndexCount(), GL11.GL_UNSIGNED_INT, 0);
		GL30.glBindVertexArray(0);
	}
	
	public void cleanUp() {
		GL30.glDeleteVertexArrays(quad.getVaoId());
		
		for (int vbo : quad.getVbos()) {
			GL15.glDeleteBuffers(vbo);
		}
	}
	
}
