package graphics.data;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

import graphics.ShaderProgram;
import java.nio.FloatBuffer;

public class PositionBuffer extends Buffer {
	
	protected FloatBuffer data;
	
	public PositionBuffer(float[] data) {
		this.objectID = glGenBuffers();
		this.data = storeArrayInBuffer(data);
		buffer();
	}

	@Override
	public void buffer() {
		bind();
		glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
		unbind();
	}
	
	public void specifyAttributeFormat() {
		bind();
		glEnableVertexAttribArray(ShaderProgram.ATTRIBUTE_POSITIONS);
		glVertexAttribPointer(ShaderProgram.ATTRIBUTE_POSITIONS, 2, GL_FLOAT, false, 0, 0);
		unbind();
	}

	@Override
	public void bind() {
		glBindBuffer(GL_ARRAY_BUFFER, objectID);
	}

	@Override
	public void unbind() {
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

	@Override
	public void delete() {
		glDeleteBuffers(objectID);
	}
}
