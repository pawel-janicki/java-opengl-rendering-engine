package com.github.paweljanicki.engine.assets.models;

public class Mesh {
	
	private final int indexCount;
	private final int firstIndex;
	private final int baseVertex;
	
	public Mesh(int indexCount, int firstIndex, int baseVertex) {
		this.indexCount = indexCount;
		this.firstIndex = firstIndex;
		this.baseVertex = baseVertex;
	}
	
	public int getIndexCount() {
		return indexCount;
	}
	
	public int getFirstIndex() {
		return firstIndex;
	}
	
	public int getBaseVertex() {
		return baseVertex;
	}
	
}
