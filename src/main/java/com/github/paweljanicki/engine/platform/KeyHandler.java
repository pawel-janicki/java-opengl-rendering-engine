package com.github.paweljanicki.engine.platform;

import java.util.Arrays;

import org.lwjgl.glfw.GLFW;

public class KeyHandler {
	
	private final boolean[] keysDown = new boolean[GLFW.GLFW_KEY_LAST + 1];
	private final boolean[] keysPressed = new boolean[GLFW.GLFW_KEY_LAST + 1];
	private final boolean[] keysReleased = new boolean[GLFW.GLFW_KEY_LAST + 1];
	
	public void init(long window) {
		GLFW.glfwSetKeyCallback(window, this::keyCallback);
	}
	
	private void keyCallback(long window, int key, int scancode, int action, int mods) {
		if (key < 0 || key >= keysDown.length)
			return;
		
		if (action == GLFW.GLFW_PRESS) {
			keysDown[key] = true;
			keysPressed[key] = true;
		} else if (action == GLFW.GLFW_RELEASE) {
			keysDown[key] = false;
			keysReleased[key] = true;
		}
	}
	
	public void endFrame() {
		Arrays.fill(keysPressed, false);
		Arrays.fill(keysReleased, false);
	}
	
	public boolean isKeyDown(int key) {
		return keysDown[key];
	}
	
	public boolean isKeyPressed(int key) {
		return keysPressed[key];
	}
	
	public boolean isKeyReleased(int key) {
		return keysReleased[key];
	}
	
}
