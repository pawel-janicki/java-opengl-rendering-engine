package com.github.paweljanicki.engine.assets.models;

public class ModelPart {
	
	private final Mesh mesh;
	private final int materialId;
	
	public ModelPart(Mesh mesh, int materialId) {
		this.mesh = mesh;
		this.materialId = materialId;
	}
	
	public Mesh getMesh() {
		return mesh;
	}
	
	public int getMaterialId() {
		return materialId;
	}
	
}
