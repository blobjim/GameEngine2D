package ritzow.sandbox.client.world;

import static org.lwjgl.opengl.GL46C.*;

import ritzow.sandbox.client.graphics.Camera;
import ritzow.sandbox.client.graphics.Framebuffer;
import ritzow.sandbox.client.graphics.GraphicsUtility;
import ritzow.sandbox.client.graphics.ModelRenderProgram;
import ritzow.sandbox.client.graphics.ModelRenderProgram.RenderInstance;
import ritzow.sandbox.client.graphics.RenderConstants;
import ritzow.sandbox.client.graphics.Renderable;
import ritzow.sandbox.client.graphics.Renderer;
import ritzow.sandbox.client.util.ClientUtility;
import ritzow.sandbox.client.world.block.ClientBlockProperties;
import ritzow.sandbox.util.Utility;
import ritzow.sandbox.world.BlockGrid;
import ritzow.sandbox.world.World;
import ritzow.sandbox.world.entity.Entity;

public final class ClientWorldRenderer implements Renderer {
	private final ModelRenderProgram modelProgram;
	private final World world;
	private final Camera camera;

	public ClientWorldRenderer(ModelRenderProgram modelProgram, Camera camera, World world) {
		this.modelProgram = modelProgram;
		this.camera = camera;
		this.world = world;
		GraphicsUtility.checkErrors();
	}

	@Override
	public void render(Framebuffer framebuffer, final int width, final int height) {
		//ensure that model program, camera, world are cached on stack
		ModelRenderProgram program = this.modelProgram;
		Camera camera = this.camera;
		World world = this.world;
		
		//set the blending mode to allow transparency
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		
		//set the current shader program
		program.setCurrent();
		framebuffer.setDraw();
		
		program.loadViewMatrixStandard(width, height);
		
		//render before loading view matrix
		float scale = 2f * (width > height ? width/(float)height : height/(float)width);
		program.render(RenderConstants.MODEL_SKY, 1.0f, 0, 0, scale, scale, 0);

		//load the view transformation
		program.loadViewMatrix(camera, width, height);

		//get visible world coordinates
		float worldLeft = 	ClientUtility.getViewLeftBound(camera, width, height);
		float worldRight = 	ClientUtility.getViewRightBound(camera, width, height);
		float worldTop = 	ClientUtility.getViewTopBound(camera, width, height);
		float worldBottom =	ClientUtility.getViewBottomBound(camera, width, height);

		//cache foreground and background of world
		BlockGrid foreground = world.getForeground();
		BlockGrid background = world.getBackground();

		//get visible block grid bounds
		int leftBound = 	Utility.clampLowerBound(0, worldLeft);
		int rightBound = 	Utility.clampUpperBound(foreground.getWidth()-1, worldRight);
		int topBound = 		Utility.clampUpperBound(foreground.getHeight()-1, worldTop);
		int bottomBound =	Utility.clampLowerBound(0, worldBottom);

		//render the blocks visible in the viewport
		for(int row = bottomBound; row <= topBound; row++) {
			for(int column = leftBound; column <= rightBound; column++) {
				var back = (ClientBlockProperties)background.get(column, row);
				var front = (ClientBlockProperties)foreground.get(column, row);

				if(back != null && (front == null || front.isTransparent())) {
					program.queueRender(back.getModelIndex(), 0.5f, column, row, 1.0f, 1.0f, 0.0f);
				}

				if(front != null) {
					program.queueRender(front.getModelIndex(), 1.0f, column, row, 1.0f, 1.0f, 0.0f);
				}
			}
		}

		//render the entities
		for(Entity e : world) {
			//pre-compute variables
			float posX = e.getPositionX();
			float posY = e.getPositionY();
			float halfWidth = e.getWidth()/2;
			float halfHeight = e.getHeight()/2;

			//check if the entity is visible inside the viewport and render it
			if(posX < worldRight + halfWidth && posX > worldLeft - halfWidth && posY < worldTop + halfHeight && posY > worldBottom - halfHeight) {
				((Renderable)e).render(program);
			}
		}
		
		program.render();
	}
}