package wolfdungeon3d;

import processing.core.PVector;

public interface EntityController {

	public Entity getEntity();

	public void onKeyPressed(Character c);

	public void onKeyHeld(Character c);

	public void onKeyReleased(Character c);

	public void onMouseMove(PVector mouseVelocity);

	public void onMouseClick(PVector mousePosition);

	public CombatCommand getCombatTurn(Combat combat);

	public void update();
}
