package wolfdungeon3d;

public interface EntityController {

	public void onKeyPressed(Character c);

	public void onKeyHeld(Character c);

	public void onKeyReleased(Character c);

	public void update();
}
