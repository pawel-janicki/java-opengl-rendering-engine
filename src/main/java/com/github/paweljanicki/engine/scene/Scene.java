package com.github.paweljanicki.engine.scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.paweljanicki.engine.assets.environments.Environment;
import com.github.paweljanicki.engine.assets.models.Model;

public class Scene {
	
	private List<Entity> entities = new ArrayList<>();
	private Set<Entity> dirtyEntities = new LinkedHashSet<>();
	private Map<Model, List<Entity>> modelGroups = new HashMap<>();
	private boolean modelGroupsDirty;
	
	private DirectionalLight directionalLight;
	
	private Environment environment;
	
	public void addEntity(Entity entity) {
		entities.add(entity);
		entity.setScene(this);
		
		List<Entity> modelGroupEntities = modelGroups.get(entity.getModel());
		if (modelGroupEntities == null) {
			modelGroupEntities = new ArrayList<>();
			modelGroups.put(entity.getModel(), modelGroupEntities);
		}
		
		modelGroupEntities.add(entity);
		
		modelGroupsDirty = true;
	}
	
	public void removeEntity(Entity entity) {
		entities.remove(entity);
		dirtyEntities.remove(entity);
		entity.setScene(null);
		
		List<Entity> modelGroupEntities = modelGroups.get(entity.getModel());
		modelGroupEntities.remove(entity);
		
		if (modelGroupEntities.size() == 0)
			modelGroups.remove(entity.getModel());
		
		modelGroupsDirty = true;
	}
	
	public List<Entity> getEntities() {
		return entities;
	}
	
	public void addDirtyEntity(Entity entity) {
		dirtyEntities.add(entity);
	}
	
	public void removeDirtyEntity(Entity entity) {
		dirtyEntities.remove(entity);
	}
	
	public Set<Entity> getDirtyEntities() {
		return dirtyEntities;
	}
	
	public Map<Model, List<Entity>> getModelGroups() {
		return modelGroups;
	}
	
	public boolean isModelGroupsDirty() {
		return modelGroupsDirty;
	}
	
	public void setModelGroupsDirty(boolean modelGroupsDirty) {
		this.modelGroupsDirty = modelGroupsDirty;
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
