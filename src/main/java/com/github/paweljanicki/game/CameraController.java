package com.github.paweljanicki.game;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import com.github.paweljanicki.engine.platform.KeyHandler;
import com.github.paweljanicki.engine.platform.MouseHandler;
import com.github.paweljanicki.engine.scene.Camera;

public class CameraController {
	
	private final float sensitivity = 0.125f;
	private final float speed = 10;
	
	private final Vector3f movement = new Vector3f();
	private final Vector3f forward = new Vector3f();
	private final Vector3f right = new Vector3f();
	
	private KeyHandler keyHandler;
	private MouseHandler mouseHandler;
	
	private Camera camera;
	
	public CameraController(KeyHandler keyHandler, MouseHandler mouseHandler, Camera camera) {
		this.keyHandler = keyHandler;
		this.mouseHandler = mouseHandler;
		this.camera = camera;
	}
	
	public void update(float deltaTime) {
		camera.rotate(mouseHandler.getDeltaY() * sensitivity, mouseHandler.getDeltaX() * sensitivity, 0);
		
		if (camera.getRotation().x() > 90) camera.setRotation(90, camera.getRotation().y(), camera.getRotation().z());
		if (camera.getRotation().x() < -90) camera.setRotation(-90, camera.getRotation().y(), camera.getRotation().z());
		
		movement.zero();
		
		if (keyHandler.isKeyDown(GLFW.GLFW_KEY_W)) movement.z -= 1;
		if (keyHandler.isKeyDown(GLFW.GLFW_KEY_S)) movement.z += 1;
		if (keyHandler.isKeyDown(GLFW.GLFW_KEY_A)) movement.x -= 1;
		if (keyHandler.isKeyDown(GLFW.GLFW_KEY_D)) movement.x += 1;
		if (keyHandler.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) movement.y -= 1;
		if (keyHandler.isKeyDown(GLFW.GLFW_KEY_SPACE)) movement.y += 1;
		
		if (movement.x != 0 || movement.y != 0 || movement.z != 0) {
			movement.normalize();
			movement.mul(speed * deltaTime);
			
			float yaw = (float) Math.toRadians(camera.getRotation().y());
			
			forward.set((float) -Math.sin(yaw), 0, (float) Math.cos(yaw));
			right.set((float) Math.cos(yaw), 0, (float) Math.sin(yaw));
			
			camera.translate(forward.x * movement.z + right.x * movement.x, movement.y, forward.z * movement.z + right.z * movement.x);
		}
	}
	
}
