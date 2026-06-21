package com.github.paweljanicki.engine;

public interface IGame {
	
	public void init(Engine engine);
	public void update(float deltaTime);
	public void render();
	
}
