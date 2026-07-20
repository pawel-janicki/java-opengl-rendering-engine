package com.github.paweljanicki.engine.assets.textures;

import org.lwjgl.opengl.ARBBindlessTexture;

public class Texture {
	
	private final int id;
	private long bindlessHandle;
	
	public Texture(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public long getBindlessHandle() {
		if (bindlessHandle == 0) {
			bindlessHandle = ARBBindlessTexture.glGetTextureHandleARB(id);
			ARBBindlessTexture.glMakeTextureHandleResidentARB(bindlessHandle);
		}
		
		return bindlessHandle;
	}
	
}
