package com.github.paweljanicki.engine.assets.environments;

import com.github.paweljanicki.engine.assets.textures.Texture;

public class Environment {
	
	private final Texture environmentMap;
	
	public Environment(Texture environmentMap) {
		this.environmentMap = environmentMap;
	}
	
	public Texture getEnvironmentMap() {
		return environmentMap;
	}
	
}
