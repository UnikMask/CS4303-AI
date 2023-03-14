package wolfdungeon3d;

import processing.core.PVector;
import processing.core.PImage;

public interface Sprite {
	public PVector getPosition();

	public PVector getSize();

	public PImage getImage();
}
