package engineTester;

import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import guis.GuiRenderer;
import guis.GuiTexture;
import models.RawModel;
import models.TexturedModel;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import terrains.Terrain;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;
import toolbox.MousePicker;

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

        ModelTexture lampTexture = new ModelTexture(loader.loadTexture("lamp"));
        TexturedModel lamp = new TexturedModel(OBJLoader.loadObjModel("lamp", loader), lampTexture);

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
        List<Light> lights = new ArrayList<Light>();
        lights.add(new Light(new Vector3f(0, 10000, -7000), new Vector3f(0.2f, 0.2f, 0.2f)));
        lights.add(new Light(new Vector3f(185, 10, -293), new Vector3f(2, 0, 0), new Vector3f(1, 0.01f, 0.002f)));
        lights.add(new Light(new Vector3f(370, 17, -300), new Vector3f(0, 2, 2), new Vector3f(1, 0.01f, 0.002f)));
        lights.add(new Light(new Vector3f(293, 7, -305), new Vector3f(2, 2, 10), new Vector3f(1, 0.01f, 0.002f)));

        entities.add(new Entity(lamp, new Vector3f(185, -4.7f, -293), 0, 0, 0, 1));
        entities.add(new Entity(lamp, new Vector3f(370, -4.7f, -300), 0, 0, 0, 1));
        entities.add(new Entity(lamp, new Vector3f(293, -4.7f, -305), 0, 0, 0, 1));

        MasterRenderer renderer = new MasterRenderer(loader);

        RawModel playerModel = OBJLoader.loadObjModel("person", loader);
        TexturedModel person = new TexturedModel(playerModel, new ModelTexture(loader.loadTexture("playerTexture")));
        Player player = new Player(person, new Vector3f(153, 5, -274), 0, 100, 0, 0.6f);
        Camera camera = new Camera(player);

        List<GuiTexture> guis = new ArrayList<GuiTexture>();
        GuiTexture gui = new GuiTexture(loader.loadTexture("health"), new Vector2f(-0.75f, 0.95f), new Vector2f(0.25f, 0.25f));
        guis.add(gui);

        GuiRenderer guiRenderer = new GuiRenderer(loader);

        MousePicker picker = new MousePicker(camera, renderer.getProjectionMatrix(), terrain);

        Entity lampEntity = (new Entity(lamp, new Vector3f(293, -6.8f, -305), 0, 0, 0, 1));
        entities.add(lampEntity);

        Light light = (new Light(new Vector3f(293, 7, -305), new Vector3f(0, 2, 2), new Vector3f(1, 0.01f, 0.002f)));
        lights.add(light);

        while (!Display.isCloseRequested()) {
            player.move(terrain);
            camera.move();

            picker.update();
            Vector3f terrainPoint = picker.getCurrentTerrainPoint();
            if (terrainPoint != null) {
                lampEntity.setPosition(terrainPoint);
                light.setPosition(new Vector3f(terrainPoint.x, terrainPoint.y + 15, terrainPoint.z));
            }

            renderer.processEntity(player);
            renderer.processTerrain(terrain);
            for (Entity entity : entities) {
                renderer.processEntity(entity);
            }
            renderer.render(lights, camera);
            guiRenderer.render(guis);
            DisplayManager.updateDisplay();
        }

        guiRenderer.cleanUp();
        renderer.cleanUp();
        loader.cleanUp();
        DisplayManager.closeDisplay();
    }

}
