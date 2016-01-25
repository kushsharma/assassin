package Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.softnuke.epic.MyGame;
import utils.AssetLord;

/**
 * Created by Kush on 09-12-2015.
 */
public class SplashScreen implements Screen {

    MyGame game;
    public static SplashScreen _splashScreen = null;
    int WIDTH = MyGame.WIDTH, HEIGHT = MyGame.HEIGHT;
    int bWIDTH = MyGame.bWIDTH, bHEIGHT = MyGame.bHEIGHT;
    float PTP = MyGame.PTP;//pixel to point

    OrthographicCamera camera;
    private Image githubImage;
    private Stage stage;
    Texture githubTex;

    AssetLord Assets;

    public SplashScreen(MyGame g, AssetLord ass) {
        game = g;
        _splashScreen = this;
        Assets = ass;

        if (GameScreen.DEBUG)
            Gdx.app.log("Splash Gdx", "W:" + Gdx.graphics.getWidth() + ", H:" + Gdx.graphics.getHeight());
        if (GameScreen.DEBUG) Gdx.app.log("Splash", "W:" + WIDTH + ", H:" + HEIGHT);


        HEIGHT = 768;
        WIDTH = 1280;

        float ASPECT_RATIO = ((float) WIDTH / (float) HEIGHT);

        bWIDTH = 20;
        bHEIGHT = (int) (bWIDTH * 1 / ASPECT_RATIO);

    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, bWIDTH, bHEIGHT);
        camera.position.set(bWIDTH / 2, bHEIGHT / 2, 0);
        camera.update();


        //stage = new Stage();

        stage = new Stage(new StretchViewport(bWIDTH, bHEIGHT));


		/* Load splash image */

        githubTex = new Texture(Gdx.files.internal("logo.png"));
        githubTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        githubImage = new Image(githubTex);

		/* Set the splash image in the center of the screen */

		/* Fade in the image and then swing it down */
        //githubImage.getColor().a = 0f;
        githubImage.setSize(bWIDTH / 3, bWIDTH / 3 * githubImage.getHeight() / githubImage.getWidth());
        githubImage.setPosition((bWIDTH - githubImage.getWidth()) / 2,
                (bHEIGHT));

        githubImage.addAction(Actions.delay(0f, Actions.sequence(Actions.delay(0f,
                Actions.moveBy(0, -(bHEIGHT / 2 + githubImage.getHeight() / 2), 0.1f,
                        Interpolation.swingOut)), Actions.delay(0.15f, Actions.run(new Runnable() {
                    @Override
                    public void run() {


                        //get ready to load
                        Assets.load();

                        //check if everything is loaded
                        //AssetLord.manager.finishLoading();
                        Assets.finishLoading();

                        Texture.setAssetManager(Assets.manager);


                        //ask for user to sign in
                        if (!MyGame.platform.isSignedIn())
                            MyGame.platform.signIn();

                        //game.setScreen(new GameScreen(game, Assets));

				/* Show main menu after swing out */
                        //original
                        game.setScreen(new MainMenuScreen(game, Assets));

                    }
                })))
        ));

        //stage.addActor(codelabsImage);
        stage.addActor(githubImage);


    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act();

        stage.draw();

        //start loading
        //AssetLord.manager.update();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        stage.dispose();
        githubTex.dispose();
    }
}