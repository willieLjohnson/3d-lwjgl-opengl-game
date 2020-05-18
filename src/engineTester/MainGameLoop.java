package engineTester;

import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import models.RawModel;
import models.TexturedModel;
import objConverter.ModelData;
import objConverter.OBJFileLoader;
import org.lwjgl.opengl.Display;
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

        ModelData treeData = OBJFileLoader.loadOBJ("lowPolyTree");
        RawModel treeRawModel = loader.loadToVao(treeData.getVertices(), treeData.getTextureCoords(),
                treeData.getNormals(), treeData.getIndices());

        ModelData fernData = OBJFileLoader.loadOBJ("fern");
        RawModel fernRawModel = loader.loadToVao(fernData.getVertices(), fernData.getTextureCoords(),
                fernData.getNormals(), fernData.getIndices());

        ModelData grassData = OBJFileLoader.loadOBJ("grassModel");
        RawModel grassRawModel = loader.loadToVao(grassData.getVertices(), grassData.getTextureCoords(),
                grassData.getNormals(), grassData.getIndices());

        TexturedModel treeTexturedModel = new TexturedModel(treeRawModel, new ModelTexture(loader.loadTexture("lowPolyTree")));
        TexturedModel fernTexturedModel = new TexturedModel(fernRawModel, new ModelTexture(loader.loadTexture("fern")));
        TexturedModel grassTexturedModel = new TexturedModel(grassRawModel, new ModelTexture(loader.loadTexture("grassTexture")));
        grassTexturedModel.getTexture().setUseFakeLighting(true);
        fernTexturedModel.getTexture().setUseFakeLighting(true);

        List<Entity> entities = new ArrayList<Entity>();
        Random random = new Random();
        for (int i = 0; i < 500; i++) {
            entities.add(new Entity(treeTexturedModel, new Vector3f(random.nextFloat() * 800 - 400, 0, random.nextFloat() * -600), 0, 0, 0, 1));
            entities.add(new Entity(fernTexturedModel, new Vector3f(random.nextFloat() * 800 - 400, 0, random.nextFloat() * -600), 0, 0, 0, 1));
            entities.add(new Entity(grassTexturedModel, new Vector3f(random.nextFloat() * 800 - 400, 0, random.nextFloat() * -600), 0, 0, 0, 1));
        }
        Light light = new Light(new Vector3f(3000, 2000, 2000), new Vector3f(1, 1, 1));

        Terrain terrain = new Terrain(0, -1, loader, texturePack, blendMap, "heightMap");
//        Terrain terrain2 = new Terrain(-1, -1, loader, texturePack, blendMap, "heightMap");


        MasterRenderer renderer = new MasterRenderer();

        RawModel playerModel = OBJLoader.loadObjModel("person", loader);
        TexturedModel person = new TexturedModel(playerModel, new ModelTexture(loader.loadTexture("playerTexture")));
        Player player = new Player(person, new Vector3f(100, 0, -50), 0, 0, 0, 1);
        Camera camera = new Camera(player);
        while (!Display.isCloseRequested()) {
            camera.move();
            player.move(terrain);
            renderer.processEntity(player);
            renderer.processTerrain(terrain);
            for (Entity entity : entities) {
                renderer.processEntity(entity);
            }
            renderer.render(light, camera);
            DisplayManager.updateDisplay();
        }

        renderer.cleanUp();
        loader.cleanUp();
        DisplayManager.closeDisplay();
    }

}
