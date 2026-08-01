package com.github.paweljanicki.engine.assets.models;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL43;

public class MaterialManager {
	
	private static final int MATERIAL_SIZE = 4 * 8 + 6 * 8;
	
	private List<Material> materials = new ArrayList<>();
	
	private int materialSsboId;
	private boolean sizeDirty;
	
	public MaterialManager() {
		materialSsboId = GL15.glGenBuffers();
	}
	
	public void updateMaterialBuffer() {
		GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, materialSsboId);
		
		if (sizeDirty) {
			ByteBuffer buffer = BufferUtils.createByteBuffer(materials.size() * MATERIAL_SIZE);
			
			int offset = 0;
			for (Material material : materials) {
				addMaterialToBuffer(material, buffer, offset);
				offset += MATERIAL_SIZE;
			}
			
			GL15.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, buffer, GL15.GL_DYNAMIC_DRAW);
			
			sizeDirty = false;
		} else {
			for (int i = 0; i < materials.size(); i++) {
				Material material = getMaterial(i);
				
				if (!material.isDirty())
					continue;
				
				ByteBuffer buffer = BufferUtils.createByteBuffer(MATERIAL_SIZE);
				addMaterialToBuffer(material, buffer, 0);
				
				GL15.glBufferSubData(GL43.GL_SHADER_STORAGE_BUFFER, MATERIAL_SIZE * i, buffer);
				
				material.setDirty(false);
			}
		}
		
		GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);
	}
	
	private void addMaterialToBuffer(Material material, ByteBuffer buffer, int offset) {
		buffer.putFloat(offset, material.getAlbedo().x());
		buffer.putFloat(offset + 4, material.getAlbedo().y());
		buffer.putFloat(offset + 8, material.getAlbedo().z());
		buffer.putFloat(offset + 12, 0);
		buffer.putFloat(offset + 16, material.getRoughness());
		buffer.putFloat(offset + 20, material.getMetallic());
		buffer.putFloat(offset + 24, 0);
		buffer.putFloat(offset + 28, 0);
		
		buffer.putLong(offset + 32, material.getAlbedoMap() != null ? material.getAlbedoMap().getBindlessHandle() : 0);
		buffer.putLong(offset + 40, material.getRoughnessMap() != null ? material.getRoughnessMap().getBindlessHandle() : 0);
		buffer.putLong(offset + 48, material.getMetallicMap() != null ? material.getMetallicMap().getBindlessHandle() : 0);
		buffer.putLong(offset + 56, material.getAoMap() != null ? material.getAoMap().getBindlessHandle() : 0);
		buffer.putLong(offset + 64, material.getEmissiveMap() != null ? material.getEmissiveMap().getBindlessHandle() : 0);
		buffer.putLong(offset + 72, material.getNormalMap() != null ? material.getNormalMap().getBindlessHandle() : 0);
	}
	
	public void cleanUp() {
		GL15.glDeleteBuffers(materialSsboId);
	}
	
	public int addMaterial(Material material) {
		materials.add(material);
		sizeDirty = true;
		return materials.size() - 1;
	}
	
	public Material getMaterial(int id) {
		return materials.get(id);
	}
	
	public List<Material> getMaterials() {
		return materials;
	}
	
	public int getMaterialSsboId() {
		return materialSsboId;
	}
	
}
