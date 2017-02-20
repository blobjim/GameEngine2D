package ritzow.solomon.engine.graphics;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;

final class PositionBuffer {
	protected final int id;
	
	public PositionBuffer(float[] data) {
		this.id = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, id);
		glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	public void specifyFormat() {
		glBindBuffer(GL_ARRAY_BUFFER, id);
		glEnableVertexAttribArray(ShaderProgram.ATTRIBUTE_POSITIONS);
		glVertexAttribPointer(ShaderProgram.ATTRIBUTE_POSITIONS, 2, GL_FLOAT, false, 0, 0);
	}
	
	public void delete() {
		glDeleteBuffers(id);
	}
}