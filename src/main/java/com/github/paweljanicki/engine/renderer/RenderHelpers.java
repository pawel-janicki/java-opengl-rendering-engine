package com.github.paweljanicki.engine.renderer;

public class RenderHelpers {
	
	private final QuadRenderer quadRenderer = new QuadRenderer();
	private final CubeRenderer cubeRenderer = new CubeRenderer();
	
	public QuadRenderer getQuadRenderer() {
		return quadRenderer;
	}
	
	public CubeRenderer getCubeRenderer() {
		return cubeRenderer;
	}
	
}
