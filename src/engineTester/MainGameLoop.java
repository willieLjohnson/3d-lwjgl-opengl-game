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

        List<Terrain> terrains = new ArrayList<Terrain>();
        terrains.add(new Terrain(0, -1, loader, texturePack, blendMap, "heightMap"));
        terrains.add(new Terrain(-1, -1, loader, texturePack, blendMap, "heightMap"));
        terrains.add(new Terrain(0, 0, loader, texturePack, blendMap, "heightMap"));
        terrains.add(new Terrain(-1, 0, loader, texturePack, blendMap, "heightMap"));

        Terrain[][] terrainMap = new Terrain[2][2];
        terrainMap[1][0] = terrains.get(0);
        terrainMap[0][0] = terrains.get(1);
        terrainMap[1][1] = terrains.get(2);
        terrainMap[0][1] = terrains.get(3);

        List<Entity> entities = new ArrayList<Entity>();
        Random random = new Random(676452);
        Terrain currentTerrain;
        for (int i = 0; i < 400; i++) {
            if (i % 2 == 0) {
                float x = random.nextFloat() * 800 - 400;
                float z = random.nextFloat() * -600;
                int gridX = (int) (x / Terrain.SIZE + 1);
                int gridZ = (int) (z / Terrain.SIZE + 1);
                currentTerrain = terrainMap[gridX][gridZ];
                float y = currentTerrain.getHeightOfTerrain(x, z);
                entities.add(new Entity(fern, random.nextInt(4), new Vector3f(x, y, z), 0, random.nextFloat() * 360, 0, 0.9f));

                x = random.nextFloat() * 800 - 400;
                z = random.nextFloat() * -600;
                gridX = (int) (x / Terrain.SIZE + 1);
                gridZ = (int) (z / Terrain.SIZE + 1);
                currentTerrain = terrainMap[gridX][gridZ];
                y = currentTerrain.getHeightOfTerrain(x, z);
                entities.add(new Entity(grassTexturedModel, new Vector3f(x, y, z), 0, random.nextFloat() * 360, 0, 0.9f));
            }
            if (i % 5 == 0) {
                float x = random.nextFloat() * 800 - 400;
                float z = random.nextFloat() * -600;
                int gridX = (int) (x / Terrain.SIZE + 1);
                int gridZ = (int) (z / Terrain.SIZE + 1);
                currentTerrain = terrainMap[gridX][gridZ];
                float y = currentTerrain.getHeightOfTerrain(x, z);
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
        Player player = new Player(person, new Vector3f(0, 5, 0), 0, 100, 0, 0.6f);
        Camera camera = new Camera(player);
        entities.add(player);

        List<GuiTexture> guis = new ArrayList<GuiTexture>();
        GuiTexture gui = new GuiTexture(loader.loadTexture("health"), new Vector2f(-0.75f, 0.95f), new Vector2f(0.25f, 0.25f));
        guis.add(gui);

        GuiRenderer guiRenderer = new GuiRenderer(loader);

        while (!Display.isCloseRequested()) {
            int gridX = (int) (player.getPosition().x / Terrain.SIZE + 1);
            int gridZ = (int) (player.getPosition().z / Terrain.SIZE + 1);
            player.move(terrainMap[gridX][gridZ]);
            camera.move();

            renderer.renderScene(entities, terrains, lights, camera);

            guiRenderer.render(guis);
            DisplayManager.updateDisplay();
        }

        guiRenderer.cleanUp();
        renderer.cleanUp();
        loader.cleanUp();
        DisplayManager.closeDisplay();
    }

}
