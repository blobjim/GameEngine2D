package world.block;

import graphics.Model;
import java.io.Serializable;
import world.World;
import world.entity.GenericEntity;

public abstract class Block implements Serializable {
	private static final long serialVersionUID = -5852957760473837301L;
	
	protected int integrity;
	
	public abstract Model getModel();
	public abstract int getHardness();
	public abstract float getFriction();
	public abstract Block createNew();
	
	public int getIntegrity() {
		return integrity;
	}
	
	public void onBreak(World world, float x, float y) {
		GenericEntity particle = new GenericEntity(getModel());
		particle.setPositionX(x);
		particle.setPositionY(y);
		particle.getHitbox().setPriority(-1);
		particle.getGraphics().rotation().setVelocity((float)Math.random() - 0.5f);
		particle.setVelocityX(-0.2f + ((float)Math.random() * (0.4f))); //min + ((float)Math.random() * (max - min))
		particle.setVelocityY((float)Math.random() * (0.35f));
		world.getEntities().add(particle);
	}
	
	public void onPlace(World world, float x, float y) {
		for(int i = 0; i < 5; i++) {
			GenericEntity particle = new GenericEntity(getModel());
			particle.getGraphics().scale().setX(0.2f);
			particle.getGraphics().scale().setY(0.2f);
			particle.setPositionX(x);
			particle.setPositionY(y);
			particle.getHitbox().setPriority(-1);
			particle.getGraphics().rotation().setVelocity((float)Math.random() - 0.5f);
			particle.setVelocityX(-0.2f + ((float)Math.random() * (0.4f))); //min + ((float)Math.random() * (max - min))
			particle.setVelocityY((float)Math.random() * (0.35f));
			world.getEntities().add(particle);
		}
	}
}
