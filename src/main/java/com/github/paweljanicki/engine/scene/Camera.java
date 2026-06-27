package com.github.paweljanicki.engine.scene;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class Camera {
	
	private final Vector3f position;
	private final Vector3f rotation;
	
	private final Matrix4f viewMatrix;
	private boolean viewDirty;
	
	private float fov = 90;
	private float nearPlane = 0.1f;
	private float farPlane = 500;
	
	private int width;
	private int height;
	
	private final Matrix4f projectionMatrix;
	private boolean projectionDirty;
	
	public Camera() {
		this(new Vector3f());
	}
	
	public Camera(Vector3f position) {
		this(position, new Vector3f());
	}
	
	public Camera(Vector3f position, Vector3f rotation) {
		this.position = new Vector3f(position);
		this.rotation = new Vector3f(rotation);
		this.projectionMatrix = new Matrix4f();
		this.projectionDirty = true;
		this.viewMatrix = new Matrix4f();
		this.viewDirty = true;
	}
	
	public void translate(Vector3f vector) {
		translate(vector.x, vector.y, vector.z);
	}
	
	public void translate(float x, float y, float z) {
		position.add(x, y, z);
		viewDirty = true;
	}
	
	public void rotate(Vector3f vector) {
		rotate(vector.x, vector.y, vector.z);
	}
	
	public void rotate(float x, float y, float z) {
		rotation.add(x, y, z);
		viewDirty = true;
	}
	
	public Vector3fc getPosition() {
		return position;
	}
	
	public void setPosition(Vector3f position) {
		this.position.set(position);
		this.viewDirty = true;
	}
	
	public void setPosition(float x, float y, float z) {
		this.position.set(x, y, z);
		this.viewDirty = true;
	}
	
	public Vector3fc getRotation() {
		return rotation;
	}
	
	public void setRotation(Vector3f rotation) {
		this.rotation.set(rotation);
		this.viewDirty = true;
	}
	
	public void setRotation(float x, float y, float z) {
		this.rotation.set(x, y, z);
		this.viewDirty = true;
	}
	
	public float getFov() {
		return fov;
	}
	
	public void setFov(float fov) {
		this.fov = fov;
		this.projectionDirty = true;
	}
	
	public float getNearPlane() {
		return nearPlane;
	}
	
	public void setNearPlane(float nearPlane) {
		this.nearPlane = nearPlane;
		this.projectionDirty = true;
	}
	
	public float getFarPlane() {
		return farPlane;
	}
	
	public void setFarPlane(float farPlane) {
		this.farPlane = farPlane;
		this.projectionDirty = true;
	}
	
	public Matrix4fc getProjectionMatrix(int width, int height) {
		if (projectionDirty || this.width != width || this.height != height) {
			this.width = width;
			this.height = height;
			
			float aspectRatio = (float) width / height;
			projectionMatrix.setPerspective((float) Math.toRadians(fov), aspectRatio, nearPlane, farPlane);
			
			projectionDirty = false;
		}
		
		return projectionMatrix;
	}
	
	public Matrix4fc getViewMatrix() {
		if (viewDirty) {
			viewMatrix.identity();
			viewMatrix.rotateX((float) Math.toRadians(rotation.x));
			viewMatrix.rotateY((float) Math.toRadians(rotation.y));
			viewMatrix.rotateZ((float) Math.toRadians(rotation.z));
			viewMatrix.translate(-position.x, -position.y, -position.z);
			viewDirty = false;
		}
		
		return viewMatrix;
	}
	
}
