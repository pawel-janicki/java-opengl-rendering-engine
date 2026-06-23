package com.github.paweljanicki.engine.assets.models;

public class ModelPart {
	
	private final Mesh mesh;
	private final Material material;
	
	public ModelPart(Mesh mesh, Material material) {
		this.mesh = mesh;
		this.material = material;
	}
	
	public Mesh getMesh() {
		return mesh;
	}
	
	public Material getMaterial() {
		return material;
	}
	
}
