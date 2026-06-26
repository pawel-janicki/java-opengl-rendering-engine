package com.github.paweljanicki.engine.renderer;

import java.util.ArrayList;
import java.util.List;

import com.github.paweljanicki.engine.assets.AssetManager;

public class RenderPipeline {
	
	private final List<IRenderPass> renderPasses = new ArrayList<>();
	private final RenderTargets targets = new RenderTargets();
	
	public void addPass(IRenderPass renderPass) {
		renderPasses.add(renderPass);
	}
	
	public void removePass(IRenderPass renderPass) {
		renderPasses.remove(renderPass);
	}
	
	public void init(AssetManager assetManager, int width, int height) {
		for (IRenderPass pass : renderPasses) {
			pass.init(assetManager, targets, width, height);
		}
	}
	
	public void render(RenderContext context, RenderHelpers helpers) {
		for (IRenderPass pass : renderPasses) {
			pass.render(context, targets, helpers);
		}
	}
	
	public void onResize(int width, int height) {
		targets.onResize(width, height);
	}
	
	public void cleanUp() {
		targets.cleanUp();
	}
	
}
