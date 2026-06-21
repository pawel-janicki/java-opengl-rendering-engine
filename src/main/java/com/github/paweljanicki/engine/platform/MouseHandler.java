package com.github.paweljanicki.engine.platform;

import java.util.Arrays;

import org.lwjgl.glfw.GLFW;

public class MouseHandler {
	
	private int mouseX;
	private int mouseY;
	
	private int lastMouseX;
	private int lastMouseY;
	
	private int deltaX;
	private int deltaY;
	
	private final boolean[] buttonsDown = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST + 1];
	private final boolean[] buttonPressed = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST + 1];
	private final boolean[] buttonReleased = new boolean[GLFW.GLFW_MOUSE_BUTTON_LAST + 1];
	
	public void init(long window) {
		GLFW.glfwSetCursorPosCallback(window, this::mousePosCallback);
		GLFW.glfwSetMouseButtonCallback(window, this::mouseButtonCallback);
	}
	
	private void mousePosCallback(long window, double xPosition, double yPosition) {
		mouseX = (int) xPosition;
		mouseY = (int) yPosition;
	}
	
	private void mouseButtonCallback(long window, int button, int action, int mods) {
		if (button < 0 || button >= buttonsDown.length)
			return;
		
		if (action == GLFW.GLFW_PRESS) {
			buttonsDown[button] = true;
			buttonPressed[button] = true;
		} else if (action == GLFW.GLFW_RELEASE) {
			buttonsDown[button] = false;
			buttonReleased[button] = true;
		}
	}
	
	public void endFrame() {
		Arrays.fill(buttonPressed, false);
		Arrays.fill(buttonReleased, false);
		
		deltaX = mouseX - lastMouseX;
		deltaY = mouseY - lastMouseY;
		
		lastMouseX = mouseX;
		lastMouseY = mouseY;
	}
	
	public int getMouseX() {
		return mouseX;
	}
	
	public int getMouseY() {
		return mouseY;
	}
	
	public int getDeltaX() {
		return deltaX;
	}
	
	public int getDeltaY() {
		return deltaY;
	}
	
	public boolean isButtonDown(int button) {
		return buttonsDown[button];
	}
	
	public boolean isButtonPressed(int button) {
		return buttonPressed[button];
	}
	
	public boolean isButtonReleased(int button) {
		return buttonReleased[button];
	}
	
}
