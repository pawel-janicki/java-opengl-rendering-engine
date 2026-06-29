package com.github.paweljanicki.engine.renderer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import com.github.paweljanicki.engine.assets.textures.Texture;
import com.github.paweljanicki.engine.assets.textures.TextureParameters;

public class FrameBuffer {
	
	private int id;
	
	private int width;
	private int height;
	
	private List<TextureParameters> colorAttachmentsParameters = new ArrayList<>();
	private List<Texture> colorTextures = new ArrayList<>();
	
	private TextureParameters depthAttachmentParameters;
	private Texture depthTexture;
	
	public FrameBuffer(int width, int height) {
		this.width = width;
		this.height = height;
		this.id = GL30.glGenFramebuffers();
	}
	
	public void addColorAttachment(TextureParameters attachmentParameters) {
		colorAttachmentsParameters.add(attachmentParameters);
		
		Texture texture = generateAttachmentTexture(attachmentParameters);
		
		bind();
		int attachmentIndex = GL30.GL_COLOR_ATTACHMENT0 + colorTextures.size();
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, attachmentIndex, GL11.GL_TEXTURE_2D, texture.getId(), 0);
		colorTextures.add(texture);
		updateDrawBuffers();
		unbind();
	}
	
	public void addDepthAttachment(TextureParameters attachmentParameters) {
		depthAttachmentParameters = attachmentParameters;
		
		Texture texture = generateAttachmentTexture(attachmentParameters);
		depthTexture = texture;
		
		bind();
		int attachmentIndex = GL30.GL_DEPTH_ATTACHMENT;
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, attachmentIndex, GL11.GL_TEXTURE_2D, texture.getId(), 0);
		unbind();
	}
	
	private Texture generateAttachmentTexture(TextureParameters attachmentParameters) {
		int id = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, attachmentParameters.internalFormat, width, height, 0, attachmentParameters.format, attachmentParameters.type, (ByteBuffer) null);
		
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, attachmentParameters.minFilter);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, attachmentParameters.magFilter);
		
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, attachmentParameters.wrapS);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, attachmentParameters.wrapT);
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		
		return new Texture(id);
	}
	
	private void updateDrawBuffers() {
		int[] buffers = new int[colorTextures.size()];
		for (int i = 0; i < buffers.length; i++) {
			buffers[i] = GL30.GL_COLOR_ATTACHMENT0 + i;
		}
		
		GL30.glDrawBuffers(buffers);
	}
	
	public void checkComplete() {
		bind();
		
		if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE)
			throw new RuntimeException("Framebuffer incomplete!");
		
		unbind();
	}
	
	public void bind() {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, id);
		GL11.glViewport(0, 0, width, height);
	}
	
	public void unbind() {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
	}
	
	public void resize(int width, int height) {
		this.width = width;
		this.height = height;
		
		for (int i = 0; i < colorTextures.size(); i++) {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, colorTextures.get(i).getId());
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, colorAttachmentsParameters.get(i).internalFormat, width, height, 0, colorAttachmentsParameters.get(i).format, colorAttachmentsParameters.get(i).type, (ByteBuffer) null);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		}
		
		if (depthTexture != null) {
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, depthTexture.getId());
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, depthAttachmentParameters.internalFormat, width, height, 0, depthAttachmentParameters.format, depthAttachmentParameters.type, (ByteBuffer) null);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		}
	}
	
	public void cleanUp() {
		for (Texture texture : colorTextures) {
			GL11.glDeleteTextures(texture.getId());
		}
		
		if (depthTexture != null)
			GL11.glDeleteTextures(depthTexture.getId());
		
		GL30.glDeleteFramebuffers(id);
	}
	
	public int getId() {
		return id;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public Texture getColorTexture(int index) {
		return colorTextures.get(index);
	}
	
	public Texture getDepthTexture() {
		return depthTexture;
	}
	
}
