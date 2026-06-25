package com.github.paweljanicki.engine.renderer;

import com.github.paweljanicki.engine.assets.AssetManager;
import com.github.paweljanicki.engine.platform.Window;
import com.github.paweljanicki.engine.scene.Camera;
import com.github.paweljanicki.engine.scene.Scene;

public class Renderer {
	
	private final Window window;
	
	private final RenderPipeline pipeline;
	private final RenderContext context;
	
	public Renderer(Window window) {
		this.window = window;
		this.pipeline = new RenderPipeline();
		this.context = new RenderContext();
	}
	
	public void init(AssetManager assetManager) {
		pipeline.init(assetManager, window.getWidth(), window.getHeight());
	}
	
	public void render(Scene scene, Camera camera) {
		context.setWidth(window.getWidth());
		context.setHeight(window.getHeight());
		
		context.setScene(scene);
		context.setCamera(camera);
		
		pipeline.render(context);
	}
	
	public void onResize(int width, int height) {
		pipeline.onResize(width, height);
	}
	
	public void cleanUp() {
		pipeline.cleanUp();
	}
	
	public RenderPipeline getPipeline() {
		return pipeline;
	}
	
}
