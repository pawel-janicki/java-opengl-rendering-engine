package com.github.paweljanicki.engine.scene;

import java.util.ArrayList;
import java.util.List;

import com.github.paweljanicki.engine.assets.environments.Environment;

public class Scene {
	
	private List<Entity> entities = new ArrayList<>();
	private List<InstancedEntity> instancedEntities = new ArrayList<>();
	
	private DirectionalLight directionalLight;
	
	private Environment environment;
	
	public void addEntity(Entity entity) {
		entities.add(entity);
	}
	
	public void removeEntity(Entity entity) {
		entities.remove(entity);
	}
	
	public List<Entity> getEntities() {
		return entities;
	}
	
	public void addInstancedEntity(InstancedEntity entity) {
		instancedEntities.add(entity);
	}
	
	public void removeInstancedEntity(InstancedEntity entity) {
		instancedEntities.remove(entity);
	}
	
	public List<InstancedEntity> getInstancedEntities() {
		return instancedEntities;
	}
	
	public DirectionalLight getDirectionalLight() {
		return directionalLight;
	}
	
	public void setDirectionalLight(DirectionalLight directionalLight) {
		this.directionalLight = directionalLight;
	}
	
	public Environment getEnvironment() {
		return environment;
	}
	
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}
	
}
