package com.github.paweljanicki.engine.assets.environments;

import com.github.paweljanicki.engine.assets.textures.Texture;

public class Environment {
	
	private final Texture environmentMap;
	private final Texture irradianceMap;
	private final Texture prefilterMap;
	
	public Environment(Texture environmentMap, Texture irradianceMap, Texture prefilterMap) {
		this.environmentMap = environmentMap;
		this.irradianceMap = irradianceMap;
		this.prefilterMap = prefilterMap;
	}
	
	public Texture getEnvironmentMap() {
		return environmentMap;
	}
	
	public Texture getIrradianceMap() {
		return irradianceMap;
	}
	
	public Texture getPrefilterMap() {
		return prefilterMap;
	}
	
}
