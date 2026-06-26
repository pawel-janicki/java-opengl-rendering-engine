package com.github.paweljanicki.engine.renderer;

import com.github.paweljanicki.engine.assets.AssetManager;

public interface IRenderPass {
	
	public void init(AssetManager assetManager, RenderTargets targets, int width, int height);
	public void render(RenderContext context, RenderTargets targets, RenderHelpers helpers);
	
}
