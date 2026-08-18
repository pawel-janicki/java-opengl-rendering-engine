package com.github.paweljanicki.engine.scene;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL43;

import com.github.paweljanicki.engine.assets.models.Mesh;
import com.github.paweljanicki.engine.assets.models.Model;
import com.github.paweljanicki.engine.assets.models.ModelPart;

public class GPUScene {
	
	private static final ByteBuffer MATRIX_UPLOAD_BUFFER = BufferUtils.createByteBuffer(Matrix4f.BYTES);
	
	private final Scene scene;
	
	private final int commandsBufferId;
	private final int objectSsboId;
	private final int instanceSsboId;
	
	private ByteBuffer commandsBuffer;
	private ByteBuffer objectBuffer;
	private ByteBuffer instanceBuffer;
	
	private int commandsCount;
	
	private final Set<Entity> dirtyEntities = new LinkedHashSet<>();
	private final Map<Model, List<Entity>> modelGroups = new HashMap<>();
	private boolean modelGroupsDirty;
	
	public GPUScene(Scene scene) {
		this.scene = scene;
		
		commandsBufferId = GL15.glGenBuffers();
		objectSsboId = GL15.glGenBuffers();
		instanceSsboId = GL15.glGenBuffers();
		
		commandsBuffer = BufferUtils.createByteBuffer(1);
		objectBuffer = BufferUtils.createByteBuffer(1);
		instanceBuffer = BufferUtils.createByteBuffer(1);
	}
	
	public void updateBuffers() {
		if (modelGroupsDirty) {
			commandsCount = 0;
			
			for (Entry<Model, List<Entity>> modelGroup : modelGroups.entrySet()) {
				commandsCount += modelGroup.getKey().getModelParts().size();
			}
			
			int instancesCount = scene.getEntities().size();
			
			if (commandsBuffer.capacity() < commandsCount * 5 * Integer.BYTES) {
				commandsBuffer = BufferUtils.createByteBuffer(commandsCount * 5 * Integer.BYTES);
				objectBuffer = BufferUtils.createByteBuffer(commandsCount * Integer.BYTES);
			}
			
			if (instanceBuffer.capacity() < instancesCount * Matrix4f.BYTES) {
				instanceBuffer = BufferUtils.createByteBuffer(instancesCount * Matrix4f.BYTES);
			}
			
			int commandOffset = 0;
			int objectOffset = 0;
			int instanceOffset = 0;
			
			int instanceIndex = 0;
			
			for (Entry<Model, List<Entity>> modelGroup : modelGroups.entrySet()) {
				for (ModelPart modelPart : modelGroup.getKey().getModelParts()) {
					Mesh mesh = modelPart.getMesh();
					
					commandsBuffer.putInt(commandOffset, mesh.getIndexCount());
					commandsBuffer.putInt(commandOffset + 4, modelGroup.getValue().size());
					commandsBuffer.putInt(commandOffset + 8, mesh.getFirstIndex());
					commandsBuffer.putInt(commandOffset + 12, mesh.getBaseVertex());
					commandsBuffer.putInt(commandOffset + 16, instanceIndex);
					commandOffset += 5 * Integer.BYTES;
					
					objectBuffer.putInt(objectOffset, modelPart.getMaterialId());
					objectOffset += Integer.BYTES;
				}
				
				for (int i = 0; i < modelGroup.getValue().size(); i++) {
					modelGroup.getValue().get(i).setGpuIndex(instanceIndex + i);
					
					modelGroup.getValue().get(i).getTransformationMatrix().get(instanceOffset, instanceBuffer);
					instanceOffset += Matrix4f.BYTES;
				}
				
				instanceIndex += modelGroup.getValue().size();
			}
			
			GL15.glBindBuffer(GL40.GL_DRAW_INDIRECT_BUFFER, commandsBufferId);
			GL15.glBufferData(GL40.GL_DRAW_INDIRECT_BUFFER, commandsBuffer, GL15.GL_DYNAMIC_DRAW);
			
			GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, objectSsboId);
			GL15.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, objectBuffer, GL15.GL_DYNAMIC_DRAW);
			GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);
			
			GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, instanceSsboId);
			GL15.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, instanceBuffer, GL15.GL_DYNAMIC_DRAW);
			GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);
			
			modelGroupsDirty = false;
		} else {
			GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, instanceSsboId);
			
			for (Entity entity : dirtyEntities) {
				entity.getTransformationMatrix().get(MATRIX_UPLOAD_BUFFER);
				GL15.glBufferSubData(GL43.GL_SHADER_STORAGE_BUFFER, entity.getGpuIndex() * Matrix4f.BYTES, MATRIX_UPLOAD_BUFFER);
			}
			
			GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);
		}
		
		dirtyEntities.clear();
	}
	
	public void cleanUp() {
		GL15.glDeleteBuffers(commandsBufferId);
		GL15.glDeleteBuffers(objectSsboId);
		GL15.glDeleteBuffers(instanceSsboId);
	}
	
	public void addEntity(Entity entity) {
		List<Entity> modelGroupEntities = modelGroups.get(entity.getModel());
		if (modelGroupEntities == null) {
			modelGroupEntities = new ArrayList<>();
			modelGroups.put(entity.getModel(), modelGroupEntities);
		}
		
		modelGroupEntities.add(entity);
		
		modelGroupsDirty = true;
	}
	
	public void removeEntity(Entity entity) {
		dirtyEntities.remove(entity);
		
		List<Entity> modelGroupEntities = modelGroups.get(entity.getModel());
		modelGroupEntities.remove(entity);
		
		if (modelGroupEntities.size() == 0)
			modelGroups.remove(entity.getModel());
		
		modelGroupsDirty = true;
	}
	
	public void addDirtyEntity(Entity entity) {
		dirtyEntities.add(entity);
	}
		
	public int getCommandsBufferId() {
		return commandsBufferId;
	}
	
	public int getObjectSsboId() {
		return objectSsboId;
	}
	
	public int getInstanceSsboId() {
		return instanceSsboId;
	}
	
	public int getCommandsCount() {
		return commandsCount;
	}
	
}
