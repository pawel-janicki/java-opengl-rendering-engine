package com.github.paweljanicki.engine.scene;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import com.github.paweljanicki.engine.assets.models.Model;

public class Entity {
	
	private Model model;
	
	private final Vector3f position;
	private final Vector3f rotation;
	private final Vector3f scale;
	
	private final Matrix4f transformationMatrix;
	private boolean transformationDirty;
	
	public Entity(Model model) {
		this(model, new Vector3f());
	}
	
	public Entity(Model model, Vector3f position) {
		this(model, position, new Vector3f());
	}
	
	public Entity(Model model, Vector3f position, Vector3f rotation) {
		this(model, position, rotation, new Vector3f(1));
	}
	
	public Entity(Model model, Vector3f position, Vector3f rotation, Vector3f scale) {
		this.model = model;
		this.position = new Vector3f(position);
		this.rotation = new Vector3f(rotation);
		this.scale = new Vector3f(scale);
		this.transformationMatrix = new Matrix4f();
		this.transformationDirty = true;
	}
	
	public void translate(Vector3f vector) {
		translate(vector.x, vector.y, vector.z);
	}
	
	public void translate(float x, float y, float z) {
		position.add(x, y, z);
		transformationDirty = true;
	}
	
	public void rotate(Vector3f vector) {
		rotate(vector.x, vector.y, vector.z);
	}
	
	public void rotate(float x, float y, float z) {
		rotation.add(x, y, z);
		transformationDirty = true;
	}
	
	public void scaleBy(Vector3f vector) {
		scaleBy(vector.x, vector.y, vector.z);
	}
	
	public void scaleBy(float x, float y, float z) {
		scale.add(x, y, z);
		transformationDirty = true;
	}
	
	public Model getModel() {
		return model;
	}
	
	public void setModel(Model model) {
		this.model = model;
	}
	
	public Vector3fc getPosition() {
		return position;
	}
	
	public void setPosition(Vector3f position) {
		this.position.set(position);
		this.transformationDirty = true;
	}
	
	public Vector3fc getRotation() {
		return rotation;
	}
	
	public void setRotation(Vector3f rotation) {
		this.rotation.set(rotation);
		this.transformationDirty = true;
	}
	
	public Vector3fc getScale() {
		return scale;
	}
	
	public void setScale(Vector3f scale) {
		this.scale.set(scale);
		this.transformationDirty = true;
	}
	
	public Matrix4fc getTransformationMatrix() {
		if (transformationDirty) {
			transformationMatrix.identity();
			transformationMatrix.translate(position);
			transformationMatrix.rotateX((float) Math.toRadians(rotation.x));
			transformationMatrix.rotateY((float) Math.toRadians(rotation.y));
			transformationMatrix.rotateZ((float) Math.toRadians(rotation.z));
			transformationMatrix.scale(scale);
			
			transformationDirty = false;
		}
		
		return transformationMatrix;
	}
	
}
