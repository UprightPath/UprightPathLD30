package com.uprightpath.ld.thirty;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.uprightpath.ld.thirty.logic.WorldGroup;
import com.uprightpath.ld.thirty.screens.GameScreen;
import com.uprightpath.ld.thirty.screens.GameplayScreenScreen;
import com.uprightpath.ld.thirty.screens.LoadingScreen;
import com.uprightpath.ld.thirty.screens.WorldSelectScreen;

public class Main extends Game {
    public static AssetManager manager = new AssetManager();
    public static SoundManager soundManager = new SoundManager();
    public static MusicManager musicManager = new MusicManager();
    public static Preferences preferences;
    public SpriteBatch batch;
    private Skin skin;
    private TextureAtlas gameAtlas;
    private GameScreen currentScreen;
    private GameScreen loadingScreen;
    private GameplayScreenScreen worldScreen;
    private WorldSelectScreen worldSelectScreen;
    private WorldGroup worldGroup;

    @Override
    public void create() {
        batch = new SpriteBatch();
        preferences = Gdx.app.getPreferences("com.upright.ld.thirty");
        manager.setLoader(WorldGroup.class, new WorldGroupLoader(new InternalFileHandleResolver()));

        // Set up the various assets to be loaded by the manager.
        manager.load("ui/ui.json", Skin.class);
        manager.load("ui/ui.atlas", TextureAtlas.class);
        manager.finishLoading();
        skin = manager.get("ui/ui.json", Skin.class);
        gameAtlas = manager.get("ui/ui.atlas", TextureAtlas.class);
        manager.load("sound/jump.wav", Sound.class);
        manager.load("sound/land.wav", Sound.class);
        manager.load("sound/pickup.wav", Sound.class);
        for(int i =0; i < 6; i++) {
            manager.load("worlds/" + i + ".wg", WorldGroup.class);
        }
        loadingScreen = new LoadingScreen(this);
        worldSelectScreen = new WorldSelectScreen(this);
        worldScreen = new GameplayScreenScreen(this);
        currentScreen = worldSelectScreen;
        this.setScreen(loadingScreen);
    }


    /**
     * Used to swap screens or do something while waiting for loading to complete.
     */
    public void loadingAssets() {
        currentScreen = ((GameScreen) this.getScreen());
        this.setScreen(loadingScreen);
    }

    /**
     * Called when the AssetManager has finished loading assets.
     * <p/>
     * Should be used to set up the various screens that require assets.
     */
    public void doneLoadingAssets() {
        this.soundManager.addSound("land", manager.get("sound/land.wav", Sound.class));
        this.soundManager.addSound("jump", manager.get("sound/jump.wav", Sound.class));
        this.soundManager.addSound("pickup", manager.get("sound/pickup.wav", Sound.class));
        currentScreen.update();
        this.setScreen(currentScreen);
    }

    public int getUnits() {
        return 16;
    }

    public Skin getSkin() {
        return skin;
    }

    public void completedWorldGroup() {
        this.preferences.putBoolean("w" + worldGroup.getId(), true);
        this.preferences.flush();
        this.manager.unload("worlds/" + worldGroup.getId() + ".wg");
        this.manager.load("worlds/" + worldGroup.getId() + ".wg", WorldGroup.class);
        this.worldGroup.destoryDisplay();
        this.worldGroup = null;
        this.manager.finishLoading();
        this.worldSelectScreen.update();
        this.setScreen(worldSelectScreen);
    }

    public void playWorldGroup(WorldGroup worldGroup) {
        this.worldGroup = worldGroup;
        this.worldGroup.setMain(this);
        this.worldGroup.createDisplay();
        worldScreen.setWorldGroup(worldGroup);
        this.setScreen(worldScreen);
    }

    public void restart() {
        this.manager.unload("worlds/" + worldGroup.getId() + ".wg");
        this.manager.load("worlds/" + worldGroup.getId() + ".wg", WorldGroup.class);
        this.worldGroup.destoryDisplay();
        this.manager.finishLoading();
        this.worldGroup = this.manager.get("worlds/" + worldGroup.getId() + ".wg", WorldGroup.class);
        this.worldGroup.setMain(this);
        this.worldGroup.createDisplay();
        this.setScreen(worldScreen);
    }

    public void close() {
        if (this.getScreen() == this.worldSelectScreen) {
            Gdx.app.exit();
        } else {
            this.manager.unload("worlds/" + worldGroup.getId() + ".wg");
            this.manager.load("worlds/" + worldGroup.getId() + ".wg", WorldGroup.class);
            this.worldGroup.destoryDisplay();
            this.manager.finishLoading();
            this.worldGroup = this.manager.get("worlds/" + worldGroup.getId() + ".wg", WorldGroup.class);
            this.worldSelectScreen.update();
            this.setScreen(worldSelectScreen);
        }
    }
}
