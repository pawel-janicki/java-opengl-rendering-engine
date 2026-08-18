package com.github.paweljanicki.engine.scene;

import java.util.ArrayList;
import java.util.List;

import com.github.paweljanicki.engine.assets.environments.Environment;

public class Scene {
	
	private final GPUScene gpuScene;
	
	private List<Entity> entities = new ArrayList<>();
	
	private DirectionalLight directionalLight;
	
	private Environment environment;
	
	public Scene() {
		this.gpuScene = new GPUScene(this);
	}
	
	public void cleanUp() {
		gpuScene.cleanUp();
	}
	
	public GPUScene getGpuScene() {
		return gpuScene;
	}
	
	public void addEntity(Entity entity) {
		entities.add(entity);
		gpuScene.addEntity(entity);
		entity.setScene(this);
	}
	
	public void removeEntity(Entity entity) {
		entities.remove(entity);
		gpuScene.removeEntity(entity);
		entity.setScene(null);
	}
	
	public List<Entity> getEntities() {
		return entities;
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
