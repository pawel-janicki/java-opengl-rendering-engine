package com.github.paweljanicki.game;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import com.github.paweljanicki.engine.Engine;
import com.github.paweljanicki.engine.IGame;
import com.github.paweljanicki.engine.assets.models.Model;
import com.github.paweljanicki.engine.scene.Camera;
import com.github.paweljanicki.engine.scene.DirectionalLight;
import com.github.paweljanicki.engine.scene.Entity;
import com.github.paweljanicki.engine.scene.Scene;

public class Game implements IGame {
	
	private Engine engine;
	
	private Scene scene;
	private Camera camera;
	
	private float fpsTimer;
	private int framesCount;
	
	@Override
	public void init(Engine engine) {
		this.engine = engine;
		
		Model damagedHelmet = engine.getAssetManager().loadModel("/models/DamagedHelmet/DamagedHelmet.gltf", "/models/DamagedHelmet/textures");
		System.out.println(damagedHelmet.getModelParts().get(0).getMaterial().getAlbedoMap().getId());
		System.out.println(damagedHelmet.getModelParts().get(0).getMaterial().getRoughnessMap().getId());
		
		scene = new Scene();
		scene.setDirectionalLight(new DirectionalLight(new Vector3f(-0.45f, -1f, 0), new Vector3f(15)));
		scene.addEntity(new Entity(damagedHelmet));
		
		camera = new Camera();
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
		engine.getRenderer().render(scene, camera);
	}
	
}
