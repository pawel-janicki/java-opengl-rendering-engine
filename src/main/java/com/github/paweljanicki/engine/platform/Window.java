package com.github.paweljanicki.engine.platform;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

public class Window {
	
	private long window;
	
	private String title;
	
	private int defaultWidth;
	private int defaultHeight;
	
	private int width;
	private int height;
	
	private boolean fullScreen;
	private boolean mouseLocked;
	
	public Window(String title, int width, int height) {
		this.title = title;
		this.defaultWidth = width;
		this.defaultHeight = height;
		this.width = width;
		this.height = height;
	}
	
	public void init() {
		GLFWErrorCallback.createPrint(System.err).set();
		
		if (!GLFW.glfwInit())
			throw new IllegalStateException("Unable to initalize GLFW");
		
		GLFW.glfwDefaultWindowHints();
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GL11.GL_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_TRUE);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GL11.GL_TRUE);
		
		window = GLFW.glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL);
		
		if (window == MemoryUtil.NULL)
			throw new RuntimeException("[ERROR] >> Failed to create GLFW window");
		
		GLFW.glfwSetWindowSizeCallback(window, (window, width, height) -> {
			this.width = width;
			this.height = height;
		});
		
		GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
		GLFW.glfwSetWindowPos(window, (vidMode.width() - width) / 2, (vidMode.height() - height) / 2);
		
		GLFW.glfwMakeContextCurrent(window);
		GLFW.glfwShowWindow(window);
	}
	
	public void registerKeyCallback(KeyHandler keyHandler) {
		keyHandler.init(window);
	}
	
	public void registerMouseCallback(MouseHandler mouseHandler) {
		mouseHandler.init(window);
	}
	
	public void pollEvents() {
		GLFW.glfwPollEvents();
	}
	
	public void swapBuffers() {
		GLFW.glfwSwapBuffers(window);
	}
	
	public void toggleFullScreen() {
		fullScreen = !fullScreen;
		applyFullScreen();
	}
	
	private void applyFullScreen() {
		GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
		
		if (fullScreen) {
			int width = vidMode.width();
			int height = vidMode.height();
			GLFW.glfwSetWindowMonitor(window, GLFW.glfwGetPrimaryMonitor(), 0, 0, width, height, GLFW.GLFW_DONT_CARE);
		} else {
			width = defaultWidth;
			height = defaultHeight;
			GLFW.glfwSetWindowMonitor(window, 0, (vidMode.width() - width) / 2, (vidMode.height() - height) / 2, width, height, GLFW.GLFW_DONT_CARE);
		}
	}
	
	public void lockMouse() {
		mouseLocked = true;
		GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
	}
	
	public void unlockMouse() {
		mouseLocked = false;
		GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
	}
	
	public void toggleMouseLock() {
		mouseLocked = !mouseLocked;
		GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, mouseLocked ? GLFW.GLFW_CURSOR_DISABLED : GLFW.GLFW_CURSOR_NORMAL);
	}
	
	public void close() {
		GLFW.glfwSetWindowShouldClose(window, true);
	}
	
	public void cleanUp() {
		Callbacks.glfwFreeCallbacks(window);
		GLFW.glfwDestroyWindow(window);
		
		GLFW.glfwTerminate();
		GLFW.glfwSetErrorCallback(null).free();
	}
	
	public boolean shouldClose() {
		return GLFW.glfwWindowShouldClose(window);
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
		GLFW.glfwSetWindowTitle(window, title);
	}
	
	public int getWidth() {
		return width;
	}
	
	public void setWidth(int width) {
		this.width = width;
		GLFW.glfwSetWindowSize(window, width, height);
	}
	
	public int getHeight() {
		return height;
	}
	
	public void setHeight(int height) {
		this.height = height;
		GLFW.glfwSetWindowSize(window, width, height);
	}
	
	public boolean isFullScreen() {
		return fullScreen;
	}
	
	public void setFullScreen(boolean fullScreen) {
		this.fullScreen = fullScreen;
		applyFullScreen();
	}
	
	public boolean isMouseLocked() {
		return mouseLocked;
	}
	
	public void setMouseLocked(boolean mouseLocked) {
		this.mouseLocked = mouseLocked;
		GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, mouseLocked ? GLFW.GLFW_CURSOR_DISABLED : GLFW.GLFW_CURSOR_NORMAL);
	}
	
}
