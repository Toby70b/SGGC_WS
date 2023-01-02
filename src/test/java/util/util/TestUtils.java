package util.util;

import com.sggc.models.Game;

/**
 * Represents common methods used in multiple tests.
 */
public class TestUtils {
    public static Game createExampleGame(String appid, Boolean multiplayer, String name) {
        Game exampleGame = new Game();
        exampleGame.setAppid(appid);
        exampleGame.setMultiplayer(multiplayer);
        exampleGame.setName(name);
        exampleGame.setId("10");
        return exampleGame;
    }
}
