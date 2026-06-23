package com.github.paweljanicki.engine.assets.models;

import org.joml.Vector3f;

import com.github.paweljanicki.engine.assets.textures.Texture;

public class Material {
	
	private Vector3f albedo = new Vector3f(1);
	private float roughness;
	private float metallic;
	
	private Texture albedoMap;
	private Texture roughnessMap;
	private Texture metallicMap;
	private Texture aoMap;
	private Texture emissiveMap;
	
	public Vector3f getAlbedo() {
		return albedo;
	}
	
	public void setAlbedo(Vector3f albedo) {
		this.albedo.set(albedo);
	}
	
	public float getRoughness() {
		return roughness;
	}
	
	public void setRoughness(float roughness) {
		this.roughness = roughness;
	}
	
	public float getMetallic() {
		return metallic;
	}
	
	public void setMetallic(float metallic) {
		this.metallic = metallic;
	}
	
	public Texture getAlbedoMap() {
		return albedoMap;
	}
	
	public void setAlbedoMap(Texture albedoMap) {
		this.albedoMap = albedoMap;
	}
	
	public Texture getRoughnessMap() {
		return roughnessMap;
	}
	
	public void setRoughnessMap(Texture roughnessMap) {
		this.roughnessMap = roughnessMap;
	}
	
	public Texture getMetallicMap() {
		return metallicMap;
	}
	
	public void setMetallicMap(Texture metallicMap) {
		this.metallicMap = metallicMap;
	}
	
	public Texture getAoMap() {
		return aoMap;
	}
	
	public void setAoMap(Texture aoMap) {
		this.aoMap = aoMap;
	}
	
	public Texture getEmissiveMap() {
		return emissiveMap;
	}
	
	public void setEmissiveMap(Texture emissiveMap) {
		this.emissiveMap = emissiveMap;
	}
	
}
