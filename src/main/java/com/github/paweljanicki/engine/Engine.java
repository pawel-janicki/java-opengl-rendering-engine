package com.github.paweljanicki.engine;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

import com.github.paweljanicki.engine.assets.AssetManager;
import com.github.paweljanicki.engine.platform.KeyHandler;
import com.github.paweljanicki.engine.platform.MouseHandler;
import com.github.paweljanicki.engine.platform.Window;

public class Engine {
	
	private IGame game;
	
	private Window window;
	private KeyHandler keyHandler;
	private MouseHandler mouseHandler;
	
	private AssetManager assetManager;
	
	public void createWindow(String title, int width, int height) {
		keyHandler = new KeyHandler();
		mouseHandler = new MouseHandler();
		
		window = new Window(title, width, height);
		window.init();
		window.registerKeyCallback(keyHandler);
		window.registerMouseCallback(mouseHandler);
	}
	
	public void init(IGame game) {
		this.game = game;
		
		GL.createCapabilities();
		
		assetManager = new AssetManager();
		
		game.init(this);
	}
	
	public void run() {
		float lastTime = (float) GLFW.glfwGetTime();
		
		while (!window.shouldClose()) {
			float frameStart = (float) GLFW.glfwGetTime();
			float deltaTime = frameStart - lastTime;
			lastTime = frameStart;
			
			window.pollEvents();
			
			game.update(deltaTime);
			game.render();
			
			keyHandler.endFrame();
			mouseHandler.endFrame();
			
			window.swapBuffers();
		}
		
		cleanUp();
	}
	
	private void cleanUp() {
		window.cleanUp();
		assetManager.cleanUp();
	}
	
	public void close() {
		window.close();
	}
	
	public Window getWindow() {
		return window;
	}
	
	public KeyHandler getKeyHandler() {
		return keyHandler;
	}
	
	public MouseHandler getMouseHandler() {
		return mouseHandler;
	}
	
	public AssetManager getAssetManager() {
		return assetManager;
	}
	
}
