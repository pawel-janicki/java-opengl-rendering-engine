package com.github.paweljanicki.game;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import com.github.paweljanicki.engine.Engine;
import com.github.paweljanicki.engine.IGame;
import com.github.paweljanicki.engine.assets.models.Model;
import com.github.paweljanicki.engine.renderer.passes.DebugPass;
import com.github.paweljanicki.engine.renderer.passes.FXAAPass;
import com.github.paweljanicki.engine.renderer.passes.GammaCorrectionPass;
import com.github.paweljanicki.engine.renderer.passes.DebugPass.DebugMode;
import com.github.paweljanicki.engine.renderer.passes.GeometryPass;
import com.github.paweljanicki.engine.renderer.passes.LightingPass;
import com.github.paweljanicki.engine.renderer.passes.OutputPass;
import com.github.paweljanicki.engine.renderer.passes.SkyboxPass;
import com.github.paweljanicki.engine.renderer.passes.TonemapPass;
import com.github.paweljanicki.engine.scene.Camera;
import com.github.paweljanicki.engine.scene.DirectionalLight;
import com.github.paweljanicki.engine.scene.Entity;
import com.github.paweljanicki.engine.scene.Scene;

public class Game implements IGame {
	
	private Engine engine;
	
	private DebugPass debugPass;
	
	private Scene scene;
	
	private Camera camera;
	private CameraController cameraController;
	
	private float fpsTimer;
	private int framesCount;
	
	@Override
	public void init(Engine engine) {
		this.engine = engine;
		
		debugPass = new DebugPass();
		
		engine.getRenderer().getPipeline().addPass(new GeometryPass());
		engine.getRenderer().getPipeline().addPass(new LightingPass());
		engine.getRenderer().getPipeline().addPass(new SkyboxPass());
		
		engine.getRenderer().getPipeline().addPass(new TonemapPass());
		engine.getRenderer().getPipeline().addPass(new GammaCorrectionPass());
		engine.getRenderer().getPipeline().addPass(new FXAAPass());
		
		engine.getRenderer().getPipeline().addPass(new OutputPass());
		engine.getRenderer().getPipeline().addPass(debugPass);
		
		Model damagedHelmet = engine.getAssetManager().loadModel("/models/DamagedHelmet/DamagedHelmet.gltf", "/models/DamagedHelmet/textures");
		Model cube = engine.getAssetManager().loadModel("/models/Cube/cube.gltf", "");
		
		scene = new Scene();
		scene.setDirectionalLight(new DirectionalLight(new Vector3f(-0.45f, -1f, 0), new Vector3f(15)));
		scene.setEnvironment(engine.getAssetManager().loadEnvironment("/hdr/kloppenheim_06_puresky_4k.hdr"));
		
		scene.addEntity(new Entity(damagedHelmet, new Vector3f(0, 5, 0)));
		
		for (int x = 0; x < 300; x++) {
			for (int z = 0; z < 300; z++) {
				scene.addEntity(new Entity(cube, new Vector3f(x * 2.25f, 0, z * 2.25f)));
			}
		}
		
		camera = new Camera(new Vector3f(-2, 7, -2), new Vector3f(25, 90 + 45, 0));
		cameraController = new CameraController(engine.getKeyHandler(), engine.getMouseHandler(), camera);
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
		
		if (engine.getKeyHandler().isKeyPressed(GLFW.GLFW_KEY_1)) {
			debugPass.setEnabled(false);
		} else if (engine.getKeyHandler().isKeyPressed(GLFW.GLFW_KEY_2)) {
			debugPass.setMode(DebugMode.DEPTH);
			debugPass.setEnabled(true);
		} else if (engine.getKeyHandler().isKeyPressed(GLFW.GLFW_KEY_3)) {
			debugPass.setMode(DebugMode.NORMALS);
			debugPass.setEnabled(true);
		} else if (engine.getKeyHandler().isKeyPressed(GLFW.GLFW_KEY_4)) {
			debugPass.setMode(DebugMode.ALBEDO);
			debugPass.setEnabled(true);
		} else if (engine.getKeyHandler().isKeyPressed(GLFW.GLFW_KEY_5)) {
			debugPass.setMode(DebugMode.ARM);
			debugPass.setEnabled(true);
		} else if (engine.getKeyHandler().isKeyPressed(GLFW.GLFW_KEY_6)) {
			debugPass.setMode(DebugMode.EMISSIVE);
			debugPass.setEnabled(true);
		}
		
		scene.getEntities().get(0).rotate(0, 10 * deltaTime, 0);
		
		if (engine.getKeyHandler().isKeyPressed(GLFW.GLFW_KEY_G))
			scene.addEntity(new Entity(scene.getEntities().get(1).getModel(), camera.getPosition()));
		
		if (engine.getWindow().isMouseLocked())
			cameraController.update(deltaTime);
		
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
