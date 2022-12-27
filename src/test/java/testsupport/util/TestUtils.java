package testsupport.util;

import com.sggc.models.Game;

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
