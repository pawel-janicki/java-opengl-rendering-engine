package com.github.paweljanicki.engine.scene;

import java.util.ArrayList;
import java.util.List;

public class Scene {
	
	private List<Entity> entities = new ArrayList<>();
	private DirectionalLight directionalLight;
	
	public void addEntity(Entity entity) {
		entities.add(entity);
	}
	
	public void removeEntity(Entity entity) {
		entities.remove(entity);
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
	
}
