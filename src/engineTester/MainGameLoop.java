package engineTester;

import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import fontMeshCreator.FontType;
import fontMeshCreator.GUIText;
import fontRendering.TextMaster;
import guis.GuiRenderer;
import guis.GuiTexture;
import models.RawModel;
import models.TexturedModel;
import normalMappingObjConverter.NormalMappedObjLoader;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import terrains.Terrain;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;
import water.WaterFrameBuffers;
import water.WaterRenderer;
import water.WaterShader;
import water.WaterTile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainGameLoop {
    public static void main(String[] args) {
        DisplayManager.createDisplay();
        Loader loader = new Loader();
        TextMaster.init(loader);

		FontType font = new FontType(loader.loadTexture("harrington"), new File("res/harrington.fnt"));
		GUIText text = new GUIText("This is some text!", 3f, font, new Vector2f(0f, 0f), 1f, true);
		text.setColour(1, 0, 0);

        // Terrain

        TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grassy"));
        TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("dirt"));
        TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("pinkFlowers"));
        TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("path"));

        TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);
        TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap"));

        ModelTexture treeTexture = new ModelTexture(loader.loadTexture("bobbleTree"));
        TexturedModel bobble = new TexturedModel(OBJLoader.loadObjModel("bobbleTree", loader), treeTexture);

        ModelTexture fernTextureAtlas = new ModelTexture(loader.loadTexture("fern"));
        fernTextureAtlas.setNumberOfRows(2);
        TexturedModel fern = new TexturedModel(OBJLoader.loadObjModel("fern", loader), fernTextureAtlas);

        ModelTexture grassTexture = new ModelTexture(loader.loadTexture("grassTexture"));
        TexturedModel grassModel = new TexturedModel(OBJLoader.loadObjModel("grassModel", loader), grassTexture);
        grassModel.getTexture().setUseFakeLighting(true);

        ModelTexture lampTexture = new ModelTexture(loader.loadTexture("lamp"));
        TexturedModel lamp = new TexturedModel(OBJLoader.loadObjModel("lamp", loader), lampTexture);

        List<Terrain> terrains = new ArrayList<Terrain>();
        terrains.add(new Terrain(0, -1, loader, texturePack, blendMap, "1944_Hill_heightmap"));
        terrains.add(new Terrain(-1, -1, loader, texturePack, blendMap, "1944_Hill_heightmap"));
        terrains.add(new Terrain(0, 0, loader, texturePack, blendMap, "1944_Hill_heightmap"));
        terrains.add(new Terrain(-1, 0, loader, texturePack, blendMap, "1944_Hill_heightmap"));

        Terrain[][] terrainMap = new Terrain[2][2];
        terrainMap[1][0] = terrains.get(0);
        terrainMap[0][0] = terrains.get(1);
        terrainMap[1][1] = terrains.get(2);
        terrainMap[0][1] = terrains.get(3);

        List<Entity> entities = new ArrayList<Entity>();
        List<Entity> normalMapEntites = new ArrayList<Entity>();

        TexturedModel barrelModel = new TexturedModel(NormalMappedObjLoader.loadOBJ("barrel", loader), new ModelTexture(loader.loadTexture("barrel")));
        barrelModel.getTexture().setNormalMap(loader.loadTexture("barrelNormal"));
        barrelModel.getTexture().setShineDamper(10);
        barrelModel.getTexture().setReflectivity(0.5f);

        normalMapEntites.add(new Entity(barrelModel, new Vector3f(-214, 10, -536 ), 0, 0, 0, 1f));

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
                entities.add(new Entity(grassModel, new Vector3f(x, y, z), 0, random.nextFloat() * 360, 0, random.nextFloat() * 3f + 1f));
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
        lights.add(new Light(new Vector3f(0, 10000, -7000), new Vector3f(0.6f, 0.6f, 0.6f)));
        lights.add(new Light(new Vector3f(185, 10, -293), new Vector3f(2, 0, 0), new Vector3f(1, 0.01f, 0.002f)));
        lights.add(new Light(new Vector3f(370, 17, -300), new Vector3f(0, 2, 2), new Vector3f(1, 0.01f, 0.002f)));
        lights.add(new Light(new Vector3f(293, 7, -305), new Vector3f(2, 2, 10), new Vector3f(1, 0.01f, 0.002f)));

        entities.add(new Entity(lamp, new Vector3f(185, -4.7f, -293), 0, 0, 0, 1));
        entities.add(new Entity(lamp, new Vector3f(370, -4.7f, -300), 0, 0, 0, 1));
        entities.add(new Entity(lamp, new Vector3f(293, -4.7f, -305), 0, 0, 0, 1));

        MasterRenderer renderer = new MasterRenderer(loader);

        RawModel playerModel = OBJLoader.loadObjModel("person", loader);
        TexturedModel person = new TexturedModel(playerModel, new ModelTexture(loader.loadTexture("playerTexture")));
        Player player = new Player(person, new Vector3f(-214, 5, -536), 0, 100, 0, 0.6f);
        Camera camera = new Camera(player);
        entities.add(player);

        WaterFrameBuffers buffers = new WaterFrameBuffers();
        WaterShader waterShader = new WaterShader();
        WaterRenderer waterRenderer = new WaterRenderer(loader, waterShader, renderer.getProjectionMatrix(), buffers);
        List<WaterTile> waters = new ArrayList<WaterTile>();
        WaterTile water = new WaterTile(-224, -536, -12);
        WaterTile water2 = new WaterTile(0, 0, 0);
        waters.add(water);
        waters.add(water2);


        List<GuiTexture> guis = new ArrayList<GuiTexture>();
        GuiTexture gui = new GuiTexture(loader.loadTexture("health"), new Vector2f(-0.75f, 0.95f), new Vector2f(0.25f, 0.25f));
        guis.add(gui);

        GuiRenderer guiRenderer = new GuiRenderer(loader);

        while (!Display.isCloseRequested()) {
            int gridX = (int) (player.getPosition().x / Terrain.SIZE + 1);
            int gridZ = (int) (player.getPosition().z / Terrain.SIZE + 1);
            player.move(terrainMap[gridX][gridZ]);
            System.out.println(player.getPosition());
            camera.move();

            GL11.glEnable(GL30.GL_CLIP_DISTANCE0);

            // render reflection
            buffers.bindReflectionFrameBuffer();
            float distance = 2 * (camera.getPosition().y - water.getHeight());
            camera.getPosition().y -= distance;
            camera.invertPitch();
            renderer.renderScene(entities, normalMapEntites, terrains, lights, camera, new Vector4f(0, 1, 0, -water.getHeight() + 1f));
            camera.getPosition().y += distance;
            camera.invertPitch();

            // render refraction
            buffers.bindRefractionFrameBuffer();
            renderer.renderScene(entities, normalMapEntites, terrains, lights, camera, new Vector4f(0, -1, 0, water.getHeight() + 1f));

            // render scene
            GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
            buffers.unbindCurrentFrameBuffer();
            renderer.renderScene(entities, normalMapEntites, terrains, lights, camera, new Vector4f(0, -1, 0, 1000000));
            waterRenderer.render(waters, camera, lights.get(0));
            guiRenderer.render(guis);
            TextMaster.render();

            DisplayManager.updateDisplay();
        }

        TextMaster.cleanUp();
        buffers.cleanUp();
        waterShader.cleanUp();
        guiRenderer.cleanUp();
        renderer.cleanUp();
        loader.cleanUp();
        DisplayManager.closeDisplay();
    }

}
