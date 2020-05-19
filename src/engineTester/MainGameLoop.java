package engineTester;

import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import guis.GuiRenderer;
import guis.GuiTexture;
import models.RawModel;
import models.TexturedModel;
import objConverter.ModelData;
import objConverter.OBJFileLoader;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import renderEngine.*;
import terrains.Terrain;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainGameLoop {
    public static void main(String[] args) {
        DisplayManager.createDisplay();
        Loader loader = new Loader();

        TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grassy"));
        TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("dirt"));
        TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("pinkFlowers"));
        TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("path"));

        TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);
        TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap"));

        ModelTexture treeTexture = new ModelTexture(loader.loadTexture("pine"));
        TexturedModel bobble = new TexturedModel(OBJLoader.loadObjModel("pine", loader), treeTexture);

        ModelTexture fernTextureAtlas = new ModelTexture(loader.loadTexture("fern"));
        fernTextureAtlas.setNumberOfRows(2);
        TexturedModel fern = new TexturedModel(OBJLoader.loadObjModel("fern", loader), fernTextureAtlas);

        ModelTexture grassTexture = new ModelTexture(loader.loadTexture("grassTexture"));
        TexturedModel grassTexturedModel = new TexturedModel(OBJLoader.loadObjModel("grassModel", loader), grassTexture);
        grassTexturedModel.getTexture().setUseFakeLighting(true);

        Terrain terrain = new Terrain(0, -1, loader, texturePack, blendMap, "heightMap");

        List<Entity> entities = new ArrayList<Entity>();
        Random random = new Random(676452);
        for (int i = 0; i < 400; i++) {
            if (i % 2 == 0) {
                float x = random.nextFloat() * 800 - 400;
                float z = random.nextFloat() * -600;
                float y = terrain.getHeightOfTerrain(x, z);
                entities.add(new Entity(fern, random.nextInt(4), new Vector3f(x, y, z), 0, random.nextFloat() * 360, 0, 0.9f));

                x = random.nextFloat() * 800 - 400;
                z = random.nextFloat() * -600;
                y = terrain.getHeightOfTerrain(x, z);
                entities.add(new Entity(grassTexturedModel, new Vector3f(x, y, z), 0, random.nextFloat() * 360, 0, 2f));
            }
            if (i % 5 == 0) {
                float x = random.nextFloat() * 800 - 400;
                float z = random.nextFloat() * -600;
                float y = terrain.getHeightOfTerrain(x, z);
                entities.add(new Entity(bobble, new Vector3f(x, y, z), 0, random.nextFloat() * 360, 0, random.nextFloat() * 0.1f + 1f));
            }
        }
        Light light = new Light(new Vector3f(3000, 2000, 2000), new Vector3f(1, 1, 1));


//        Terrain terrain2 = new Terrain(-1, -1, loader, texturePack, blendMap, "heightMap");


        MasterRenderer renderer = new MasterRenderer();

        RawModel playerModel = OBJLoader.loadObjModel("person", loader);
        TexturedModel person = new TexturedModel(playerModel, new ModelTexture(loader.loadTexture("playerTexture")));
        Player player = new Player(person, new Vector3f(100, 0, -50), 0, 0, 0, 1);
        Camera camera = new Camera(player);

        List<GuiTexture> guis = new ArrayList<GuiTexture>();
        GuiTexture gui = new GuiTexture(loader.loadTexture("socuwan"), new Vector2f(0.5f, 0.5f), new Vector2f(0.25f, 0.25f));
        guis.add(gui);

        GuiTexture gui2 = new GuiTexture(loader.loadTexture("thinmatrix"), new Vector2f(0.30f, 0.75f), new Vector2f(0.4f, 0.4f));
        guis.add(gui2);

        GuiRenderer guiRenderer = new GuiRenderer(loader);

        while (!Display.isCloseRequested()) {
            camera.move();
            player.move(terrain);
            renderer.processEntity(player);
            renderer.processTerrain(terrain);
            for (Entity entity : entities) {
                renderer.processEntity(entity);
            }
            renderer.render(light, camera);
            guiRenderer.render(guis);
            DisplayManager.updateDisplay();
        }

        guiRenderer.cleanUp();
        renderer.cleanUp();
        loader.cleanUp();
        DisplayManager.closeDisplay();
    }

}
