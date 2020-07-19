package ritzow.sandbox.client.ui;

import java.util.ArrayDeque;
import java.util.Deque;
import ritzow.sandbox.client.data.StandardClientOptions;
import ritzow.sandbox.client.graphics.*;
import ritzow.sandbox.util.Utility;

//TODO implement mouse and keyboard events.
//TODO add scissoring in order to display elements "inside" other elements, cropped.
//TODO make world renderer a GuiElement (will take quite a bit of work possibly)
//For mouse events, they will be consumed at the lowest level of recursion first (since those are drawn on top)
//and upper levels will not process them if consumed at the bottom.
//Will need a way to do intersection with basic button/element shapes like rectangles and circles, even when scaled and rotated (and both at same time).
//Input events will be processed entirely separately from drawing, but in a similar fashion using the same "offsets" system.
public class StandardGuiRenderer implements GuiRenderer {
	private final ModelRenderer program;
	private final Deque<GuiLevel> rt;
	private float mouseX, mouseY;
	private float scaleX, scaleY, ratio;
	private long nanos;

	private static class GuiLevel {
		private final RenderTransform transform;
		private final boolean cursorHover;

		public GuiLevel(RenderTransform transform, boolean cursorHover) {
			this.transform = transform;
			this.cursorHover = cursorHover;
		}
	}

	//Apply this Offset's transformation to the parameters (ie a left matrix multiply)
	private static record RenderTransform(float opacity, float x, float y, float scaleX, float scaleY, float rotation) {
		RenderTransform combine(float opacity, float x, float y, float scaleX, float scaleY, float rotation) {
			//https://en.wikipedia.org/wiki/Rotation_matrix
			//TODO deal with weird negatives required for clockwise/proper rotation
			float cos = (float)Math.cos(-this.rotation);
			float sin = (float)Math.sin(-this.rotation);
			return new RenderTransform(
				this.opacity() * opacity,
				(x * cos - y * sin) * this.scaleX + this.x,
				(x * sin + y * cos) * this.scaleY + this.y,
				this.scaleX * scaleX,
				this.scaleY * scaleY,
				this.rotation + rotation
				//this.scissor == null ? null : new Scissor(this.scaleX * this.scissor.width(), this.scaleY * this.scissor.height())
			);
		}

		Position transform(float x, float y) {
			float cos = (float)Math.cos(-rotation);
			float sin = (float)Math.sin(-rotation);
			return Position.of(
				(x * cos - y * sin) * this.scaleX + this.x,
				(x * sin + y * cos) * this.scaleY + this.y
			);
		}

		Position transformInverse(float x, float y) {
			float cos = (float)Math.cos(-rotation);
			float sin = (float)Math.sin(-rotation);
			x = (x - this.x) / this.scaleX;
			y = (y - this.y) / this.scaleY;
			return Position.of(
				(x * cos + y * sin),
				(x * -sin + y * cos)
			);
		}
	}

	public StandardGuiRenderer(ModelRenderer modelProgram) {
		this.program = modelProgram;
		this.rt = new ArrayDeque<>();
		this.rt.addFirst(new GuiLevel(new RenderTransform(1, 0, 0, 1, 1, 0), true));
	}

	@Override
	public void draw(Model model, float opacity, float posX, float posY, float scaleX, float scaleY, float rotation) {
		//TODO glScissor if exists
		//if exists program.flush() before and after, etc.
		RenderTransform transform = rt.peekFirst().transform.combine(opacity, posX, posY, scaleX, scaleY, rotation);
		program.queueRender(
			model,
			transform.opacity,
			transform.x,
			transform.y,
			transform.scaleX,
			transform.scaleY,
			transform.rotation
		);
		//TODO exit glScissor
	}

	@Override
	public void draw(GuiElement element, float posX, float posY) {
		draw(element, 1, posX, posY, 1, 1, 0);
	}

	@Override
	public void draw(GuiElement element, float opacity, float posX, float posY, float scaleX, float scaleY, float rotation) {
		GuiLevel parent = rt.peekFirst();
		RenderTransform transform = parent.transform.combine(opacity, posX, posY, scaleX, scaleY, rotation);
		if(parent.cursorHover && intersect(element, transform)) {
			rt.addFirst(new GuiLevel(transform, true));
			Position pos = transform.transformInverse(mouseX, mouseY);
			element.render(this, nanos, pos.x(), pos.y());
			//TODO implement event consumption (ie prevent consumed events from being propogated further)
			//TODO mouse event processing must happen after render, so that lower elements dont received consumed event, maybe that just means encouraging proper draw
			//and update order?
		} else {
			rt.addFirst(new GuiLevel(transform, false));
			element.render(this, nanos);
		}

		rt.removeFirst();
	}

	private boolean intersect(GuiElement element, RenderTransform transform) {
		//transform represents this gui element centered at the origin
		Shape shape = element.shape();
		if(shape instanceof Rectangle rect) {
			//Transform the rectangle back to origin (basically just the rectangle width and height)
			//and I transform (translate, scale, rotate) the mouse back to origin based on how the rectangle was transformed
			//basically, I perform the inverse of 'transform' to the mouse position then check bounds.
			Position mouseInverse = transform.transformInverse(mouseX, mouseY);
			if(StandardClientOptions.DEBUG) {
				program.queueRender(GameModels.MODEL_DIRT_BLOCK, 1, mouseInverse.x(), mouseInverse.y(), 0.05f,0.05f, 0);
				program.queueRender(GameModels.MODEL_GREEN_FACE, 1, 0, 0, rect.width(), rect.height(), 0);
			}
			return Math.abs(mouseInverse.x()) <= rect.width()/2 && Math.abs(mouseInverse.y()) <= rect.height()/2;
		} else if(shape instanceof InfinitePlane) {
			return true;
		} else if(shape instanceof Position) {
			return false;
		} else {
			throw new UnsupportedOperationException("unsupported Shape type");
		}
	}

	private float viewportRight, viewportTop;

	@Override
	public float viewportLeft() {
		return -viewportRight;
	}

	@Override
	public float viewportRight() {
		return viewportRight;
	}

	@Override
	public float viewportBottom() {
		return -viewportTop;
	}

	@Override
	public float viewportTop() {
		return viewportTop;
	}

	public void render(GuiElement gui, Framebuffer dest, Display display, long nanos, float guiScale) throws OpenGLException {
		dest.clear(161 / 256f, 252 / 256f, 156 / 256f, 1.0f);
		dest.setDraw();
		int windowWidth = display.width();
		int windowHeight = display.height();
		this.scaleX = guiScale/windowWidth;
		this.scaleY = guiScale/windowHeight;
		//convert from pixel coords (from top left) to UI coords
		this.mouseX = Utility.convertRange(0, windowWidth, -1/scaleX, 1/scaleX, display.getCursorX());
		this.mouseY = -Utility.convertRange(0, windowHeight, -1/scaleY, 1/scaleY, display.getCursorY());
		this.nanos = nanos;
		program.loadViewMatrixScale(guiScale, windowWidth, windowHeight);
		program.setCurrent();
		draw(gui, 1, 0, 0, 1, 1, 0);
		//draw(GameModels.MODEL_RED_SQUARE, 1.0f, mouseX, mouseY, 1.0f, 1.0f, 0);
	}
}
