package com.github.paweljanicki.engine.scene;

import org.joml.Vector3f;

public class DirectionalLight {
	
	private Vector3f direction = new Vector3f();
	private Vector3f color = new Vector3f();
	
	public DirectionalLight(Vector3f direction, Vector3f color) {
		this.direction.set(direction);
		this.color.set(color);
	}
	
	public Vector3f getDirection() {
		return direction;
	}
	
	public void setDirection(Vector3f direction) {
		this.direction.set(direction);
	}
	
	public Vector3f getColor() {
		return color;
	}
	
	public void setColor(Vector3f color) {
		this.color.set(color);
	}
	
}
