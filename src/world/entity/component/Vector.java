package world.entity.component;

import java.io.Serializable;

public abstract class Vector implements Serializable {
	private static final long serialVersionUID = -3308518601748576201L;
	
	protected float x;
	protected float y;
	
	public Vector(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public float getX() {
		return x;
	}
	
	public float getY() {
		return y;
	}
	
	public void setX(float x) {
		this.x = x;
	}
	
	public void setY(float y) {
		this.y = y;
	}
	
	public void addX(float x) {
		this.x += x;
	}
	
	public void addY(float y) {
		this.y += y;
	}
}
