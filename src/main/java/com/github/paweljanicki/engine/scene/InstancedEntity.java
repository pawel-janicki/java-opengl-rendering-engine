package com.github.paweljanicki.engine.scene;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;

import com.github.paweljanicki.engine.assets.models.Model;

public class InstancedEntity {
	
	private final Model model;
	private final List<Matrix4f> transformationMatrices = new ArrayList<>();
	
	private boolean contentDirty;
	private boolean sizeDirty;
	
	public InstancedEntity(Model model) {
		this.model = model;
	}
	
	public Model getModel() {
		return model;
	}
	
	public List<Matrix4f> getTransformationMatrices() {
		return transformationMatrices;
	}
	
	public Matrix4fc getInstance(int index) {
		return transformationMatrices.get(index);
	}
	
	public void addInstance(Vector3fc position, Vector3fc rotation, Vector3fc scale) {
		Matrix4f transformationMatrix = new Matrix4f();
		transformationMatrix.translate(position);
		transformationMatrix.rotateX((float) Math.toRadians(rotation.x()));
		transformationMatrix.rotateY((float) Math.toRadians(rotation.y()));
		transformationMatrix.rotateZ((float) Math.toRadians(rotation.z()));
		transformationMatrix.scale(scale);
		
		transformationMatrices.add(transformationMatrix);
		
		sizeDirty = true;
	}
	
	public void updateInstance(int index, Vector3fc position, Vector3fc rotation, Vector3fc scale) {
		Matrix4f transformationMatrix = transformationMatrices.get(index);
		transformationMatrix.identity();
		transformationMatrix.translate(position);
		transformationMatrix.rotateX((float) Math.toRadians(rotation.x()));
		transformationMatrix.rotateY((float) Math.toRadians(rotation.y()));
		transformationMatrix.rotateZ((float) Math.toRadians(rotation.z()));
		transformationMatrix.scale(scale);
		
		contentDirty = true;
	}
	
	public void removeInstance(int index) {
		transformationMatrices.remove(index);
		sizeDirty = true;
	}
	
	public int getInstancesAmount() {
		return transformationMatrices.size();
	}
	
	public boolean isContentDirty() {
		return contentDirty;
	}
	
	public void setContentDirty(boolean contentDirty) {
		this.contentDirty = contentDirty;
	}
	
	public boolean isSizeDirty() {
		return sizeDirty;
	}
	
	public void setSizeDirty(boolean sizeDirty) {
		this.sizeDirty = sizeDirty;
	}
	
}
