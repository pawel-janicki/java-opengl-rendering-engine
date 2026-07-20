package com.github.paweljanicki.engine.assets.models;

import org.joml.Vector3f;
import org.joml.Vector3fc;

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
	private Texture normalMap;
	
	private boolean dirty;
	
	public Vector3fc getAlbedo() {
		return albedo;
	}
	
	public void setAlbedo(Vector3f albedo) {
		this.albedo.set(albedo);
		this.dirty = true;
	}
	
	public float getRoughness() {
		return roughness;
	}
	
	public void setRoughness(float roughness) {
		this.roughness = roughness;
		this.dirty = true;
	}
	
	public float getMetallic() {
		return metallic;
	}
	
	public void setMetallic(float metallic) {
		this.metallic = metallic;
		this.dirty = true;
	}
	
	public Texture getAlbedoMap() {
		return albedoMap;
	}
	
	public void setAlbedoMap(Texture albedoMap) {
		this.albedoMap = albedoMap;
		this.dirty = true;
	}
	
	public Texture getRoughnessMap() {
		return roughnessMap;
	}
	
	public void setRoughnessMap(Texture roughnessMap) {
		this.roughnessMap = roughnessMap;
		this.dirty = true;
	}
	
	public Texture getMetallicMap() {
		return metallicMap;
	}
	
	public void setMetallicMap(Texture metallicMap) {
		this.metallicMap = metallicMap;
		this.dirty = true;
	}
	
	public Texture getAoMap() {
		return aoMap;
	}
	
	public void setAoMap(Texture aoMap) {
		this.aoMap = aoMap;
		this.dirty = true;
	}
	
	public Texture getEmissiveMap() {
		return emissiveMap;
	}
	
	public void setEmissiveMap(Texture emissiveMap) {
		this.emissiveMap = emissiveMap;
		this.dirty = true;
	}
	
	public Texture getNormalMap() {
		return normalMap;
	}
	
	public void setNormalMap(Texture normalMap) {
		this.normalMap = normalMap;
		this.dirty = true;
	}
	
	public boolean isDirty() {
		return dirty;
	}
	
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	
}
