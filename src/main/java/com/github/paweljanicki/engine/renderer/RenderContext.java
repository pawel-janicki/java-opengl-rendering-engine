package com.github.paweljanicki.engine.renderer;

import com.github.paweljanicki.engine.scene.Camera;
import com.github.paweljanicki.engine.scene.Scene;

public class RenderContext {
	
	private int width;
	private int height;
	
	private Scene scene;
	private Camera camera;
	
	public int getWidth() {
		return width;
	}
	
	public void setWidth(int width) {
		this.width = width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public void setHeight(int height) {
		this.height = height;
	}
	
	public Scene getScene() {
		return scene;
	}
	
	public void setScene(Scene scene) {
		this.scene = scene;
	}
	
	public Camera getCamera() {
		return camera;
	}
	
	public void setCamera(Camera camera) {
		this.camera = camera;
	}
	
}
