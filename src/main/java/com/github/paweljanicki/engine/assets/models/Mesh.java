package com.github.paweljanicki.engine.assets.models;

public class Mesh {
	
	private final int vaoId;
	private final int indexCount;
	
	private final int vbos[];
	
	public Mesh(int vaoId, int indexCount, int... vbos) {
		this.vaoId = vaoId;
		this.indexCount = indexCount;
		this.vbos = vbos;
	}
	
	public int getVaoId() {
		return vaoId;
	}
	
	public int getIndexCount() {
		return indexCount;
	}
	
	public int[] getVbos() {
		return vbos;
	}
	
}
