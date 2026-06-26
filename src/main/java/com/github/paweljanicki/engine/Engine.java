package com.github.paweljanicki.engine;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

import com.github.paweljanicki.engine.assets.AssetManager;
import com.github.paweljanicki.engine.platform.KeyHandler;
import com.github.paweljanicki.engine.platform.MouseHandler;
import com.github.paweljanicki.engine.platform.Window;
import com.github.paweljanicki.engine.renderer.Renderer;

public class Engine {
	
	private IGame game;
	
	private Window window;
	private KeyHandler keyHandler;
	private MouseHandler mouseHandler;
	
	private AssetManager assetManager;
	
	private Renderer renderer;
	
	public void createWindow(String title, int width, int height) {
		keyHandler = new KeyHandler();
		mouseHandler = new MouseHandler();
		
		window = new Window(title, width, height);
		window.init();
		window.registerWindowSizeCallback(this::onResize);
		window.registerKeyCallback(keyHandler);
		window.registerMouseCallback(mouseHandler);
	}
	
	public void init(IGame game) {
		this.game = game;
		
		GL.createCapabilities();
		
		assetManager = new AssetManager();
		
		renderer = new Renderer(window);
		
		game.init(this);
		renderer.init(assetManager);
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
	
	private void onResize(int width, int height) {
		renderer.onResize(width, height);
	}
	
	private void cleanUp() {
		window.cleanUp();
		assetManager.cleanUp();
		renderer.cleanUp();
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
	
	public Renderer getRenderer() {
		return renderer;
	}
	
}
