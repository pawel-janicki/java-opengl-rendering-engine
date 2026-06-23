package com.github.paweljanicki.engine.assets.models;

import java.util.List;

public class Model {
	
	private final List<ModelPart> modelParts;
	
	public Model(List<ModelPart> modelParts) {
		this.modelParts = modelParts;
	}
	
	public List<ModelPart> getModelParts() {
		return modelParts;
	}
	
}
