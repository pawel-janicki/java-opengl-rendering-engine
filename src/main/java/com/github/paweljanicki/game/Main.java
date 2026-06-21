package com.github.paweljanicki.game;

import com.github.paweljanicki.engine.Engine;

public class Main {
	
	public static void main(String[] args) {
		Engine engine = new Engine();
		engine.createWindow("Engine", 1280, 720);
		engine.init(new Game());
		engine.run();
	}
	
}
