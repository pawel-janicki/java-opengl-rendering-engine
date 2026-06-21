package com.github.paweljanicki.game;

import org.lwjgl.glfw.GLFW;

import com.github.paweljanicki.engine.Engine;
import com.github.paweljanicki.engine.IGame;

public class Game implements IGame {
	
	private Engine engine;
	
	private float fpsTimer;
	private int framesCount;
	
	@Override
	public void init(Engine engine) {
		this.engine = engine;
	}
	
	@Override
	public void update(float deltaTime) {
		if (engine.getKeyHandler().isKeyPressed(GLFW.GLFW_KEY_ESCAPE))
			engine.close();
		
		if (engine.getKeyHandler().isKeyPressed(GLFW.GLFW_KEY_F11)) {
			engine.getWindow().toggleFullScreen();
		} else if (engine.getKeyHandler().isKeyPressed(GLFW.GLFW_KEY_E)) {
			engine.getWindow().toggleMouseLock();
		}
		
		fpsTimer += deltaTime;
		framesCount++;
		
		if (fpsTimer >= 1) {
			float averageFrameTime = 1000 / (float) framesCount;
			
			System.out.println("FPS: " + framesCount + " | Average Frame Time: " + averageFrameTime + " ms");
			
			fpsTimer -= 1;
			framesCount = 0;
		}
	}
	
	@Override
	public void render() {
		
	}
	
}
