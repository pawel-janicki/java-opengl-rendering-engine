package com.github.paweljanicki.engine.renderer;

import java.util.HashMap;
import java.util.Map;

public class RenderTargets {
	
	private final Map<String, FrameBuffer> frameBuffers = new HashMap<>();
	
	private FrameBuffer currentRenderTarget;
	
	public void add(String name, FrameBuffer fbo) {
		if (frameBuffers.containsKey(name))
			throw new RuntimeException("[ERROR] >> Failed to add frame buffer to render targets! Frame buffer '" + name + "' already exists.");
		
		frameBuffers.put(name, fbo);
	}
	
	public void remove(String name) {
		FrameBuffer fbo = frameBuffers.get(name);
		if (fbo == null)
			return;
		
		fbo.cleanUp();
		frameBuffers.remove(name);
	}
	
	public FrameBuffer get(String name) {
		FrameBuffer fbo = frameBuffers.get(name);
		if (fbo == null)
			throw new RuntimeException("[ERROR] >> Failed to find frame buffer named " + name + "!");
		
		return fbo;
	}
	
	public void onResize(int width, int height) {
		for (FrameBuffer fbo : frameBuffers.values()) {
			fbo.resize(width, height);
		}
	}
	
	public void cleanUp() {
		for (FrameBuffer fbo : frameBuffers.values()) {
			fbo.cleanUp();
		}
		
		frameBuffers.clear();
	}
	
	public FrameBuffer getCurrentRenderTarget() {
		return currentRenderTarget;
	}
	
	public void setCurrentRenderTarget(FrameBuffer currentRenderTarget) {
		this.currentRenderTarget = currentRenderTarget;
	}
	
}
