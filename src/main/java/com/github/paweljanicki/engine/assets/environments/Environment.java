package com.github.paweljanicki.engine.assets.environments;

import com.github.paweljanicki.engine.assets.textures.Texture;

public class Environment {
	
	private final Texture environmentMap;
	private final Texture irradianceMap;
	
	public Environment(Texture environmentMap, Texture irradianceMap) {
		this.environmentMap = environmentMap;
		this.irradianceMap = irradianceMap;
	}
	
	public Texture getEnvironmentMap() {
		return environmentMap;
	}
	
	public Texture getIrradianceMap() {
		return irradianceMap;
	}
	
}
