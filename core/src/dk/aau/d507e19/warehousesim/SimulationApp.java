package dk.aau.d507e19.warehousesim;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.google.gson.Gson;
import dk.aau.d507e19.warehousesim.controller.pathAlgorithms.PathFinderEnum;
import dk.aau.d507e19.warehousesim.controller.server.taskAllocator.TaskAllocatorEnum;
import dk.aau.d507e19.warehousesim.input.CameraMover;
import dk.aau.d507e19.warehousesim.statistics.StatisticsAutomator;
import dk.aau.d507e19.warehousesim.ui.SideMenu;

import java.io.*;

public class SimulationApp extends ApplicationAdapter {

    public static final long DEFAULT_SEED = 1234L;

	public static final String CURRENT_RUN_CONFIG = "defaultSpecs.json";
    //public static final String CURRENT_RUN_CONFIG = "manyRobots.json";

	public static final int MENU_WIDTH_IN_PIXELS = 300;

	// Size of a single square/tile in the grid
	private static final int DEFAULT_PIXELS_PER_TILE = 64;
	private static final int MAX_UPDATES_PER_FRAME = 30;

	private OrthographicCamera menuCamera = new OrthographicCamera();
	private OrthographicCamera simulationCamera = new OrthographicCamera();
	private OrthographicCamera simFontCamera = new OrthographicCamera();

	private ScreenViewport menuViewport;
	private ScreenViewport simulationViewport;

	// Variables for simulation loop logic
	public static final int TICKS_PER_SECOND = 30;
	public static final long MILLIS_PER_TICK = 1000 / TICKS_PER_SECOND;
	public static final int FAST_FORWARD_MULTIPLIER = 8;
	public UpdateMode updateMode = UpdateMode.MANUAL;
	private long millisSinceUpdate = 0L;
	private long lastUpdateTime = 0L;

	private static final Color simBGColor = Color.GRAY;

	private Simulation simulation;

	private SideMenu sideMenu;

	private CameraMover cameraMover;
    private InputMultiplexer inputMultiplexer;

    // Currently using the following pathFinder and TaskAllocators.
	public static PathFinderEnum pathFinderSelected = PathFinderEnum.MANHATTAN_PATH_FINDER;
	private static TaskAllocatorEnum taskAllocatorSelected = TaskAllocatorEnum.SEQUENTIAL_TASK_ALLOCATOR;

	@Override
	public void create () {
        GraphicsManager.loadAssets();

        inputMultiplexer = new InputMultiplexer();
        simulationViewport = new ScreenViewport(simulationCamera);
        simulationViewport.setUnitsPerPixel(1f / (float) DEFAULT_PIXELS_PER_TILE);
		simulationViewport.setUnitsPerPixel(1f / (float) DEFAULT_PIXELS_PER_TILE);

		menuViewport = new ScreenViewport(menuCamera);

		centerCamera(simulationCamera);
		centerCamera(menuCamera);

		// Quick way to generate new json files
		// createJsonFileFromSpecs("newSpecName.json");

		simulation = new Simulation(DEFAULT_SEED, CURRENT_RUN_CONFIG, this, CURRENT_RUN_CONFIG);
		sideMenu = new SideMenu(menuViewport, this);

		Gdx.input.setInputProcessor(inputMultiplexer);
		cameraMover = new CameraMover(this, simulationCamera, simulationViewport);

		inputMultiplexer.addProcessor(cameraMover);
		inputMultiplexer.addProcessor(simulation.getInputProcessor());
        lastUpdateTime = System.currentTimeMillis();
	}

	private void createJsonFileFromSpecs(String newSpecName){
		File newSpecFile = new File(StatisticsAutomator.PATH_TO_RUN_CONFIGS + File.separator + newSpecName);
		Gson gson = new Gson();
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(newSpecFile.getPath()))){
			String jsonString = gson.toJson(new WarehouseSpecs());
			writer.write(jsonString);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void centerCamera(OrthographicCamera camera) {
		camera.position.x = camera.viewportWidth / 2f;
		camera.position.y = camera.viewportHeight / 2f;
	}

	@Override
	// Called when the simulation window is resized; adjusts the screen height to fit the new aspect ratio
	public void resize(int width, int height) {
        simulationViewport.update(width - MENU_WIDTH_IN_PIXELS, height);
        menuViewport.update(MENU_WIDTH_IN_PIXELS, height);
	    menuViewport.setScreenX(width - MENU_WIDTH_IN_PIXELS);

		simFontCamera.viewportWidth = simulationViewport.getScreenWidth();
		simFontCamera.viewportHeight = simulationViewport.getScreenHeight();

		//updateSimulationScreenSize(width, height);
		//updateMenuScreenSize(width, height);
		centerCamera(menuCamera);
		centerCamera(simulationCamera);
		centerCamera(simFontCamera);
		simFontCamera.update();

		sideMenu.resize();
		refreshCamera();
	}

	@Override
	// Called repeatedly by the libgdx framework
	public void render () {
		if(updateMode == UpdateMode.NO_GRAPHICS) return;

		cameraMover.update();
		int updatesSinceLastRender = 0;
		while(shouldUpdateSimulation() && updatesSinceLastRender < MAX_UPDATES_PER_FRAME){
			simulation.update();
			updatesSinceLastRender++;
		}

		updateMenu();
		clearScreen();
		renderMenu();
		renderSimulation();
	}

	// Determines whether it is time to update to simulation
	// by comparing the time that has passed
	private boolean shouldUpdateSimulation(){
		// Always update when in fast mode
		if(updateMode == UpdateMode.FASTEST_FORWARD || updateMode == UpdateMode.NO_GRAPHICS)
			return true;

		// Fast forward
		if(updateMode == UpdateMode.FAST_FORWARD){
			long currentTime = System.currentTimeMillis();
			millisSinceUpdate += currentTime - lastUpdateTime;
			lastUpdateTime = currentTime;

			if(millisSinceUpdate >= MILLIS_PER_TICK / FAST_FORWARD_MULTIPLIER){
				millisSinceUpdate -= MILLIS_PER_TICK / FAST_FORWARD_MULTIPLIER;
				return true;
			}
		}

		// don't automatically update when in manual mode
		if(updateMode == UpdateMode.MANUAL)
			return false;


		if(updateMode == UpdateMode.REAL_TIME){
			long currentTime = System.currentTimeMillis();
			millisSinceUpdate += currentTime - lastUpdateTime;
			lastUpdateTime = currentTime;

			// It is only time to update if enough time has passed since last time we rendered/updated
			if(millisSinceUpdate >= MILLIS_PER_TICK) {
				millisSinceUpdate -= MILLIS_PER_TICK;
				return true;
			}
		}

		return false;
	}

	private void clearScreen() {
		Gdx.gl.glClearColor(simBGColor.r, simBGColor.g, simBGColor.b, simBGColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	}

	private void updateMenu() {
		sideMenu.update();
	}

	private void updateSimulation(){
		simulation.update();
	}

	private void renderMenu(){
		//cameraMover.update();
		menuCamera.update();
		menuViewport.apply();
		sideMenu.render(menuCamera);
	}

	private void renderSimulation(){
		Gdx.gl.glEnable(GL30.GL_BLEND);
		Gdx.gl.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
		simulationCamera.update();
		simFontCamera.update();
		simulationViewport.apply();
		simulation.render(simulationCamera, simFontCamera);
		Gdx.gl.glDisable(GL30.GL_BLEND);

	}

	private void switchUpdateMode(UpdateMode newMode){
		updateMode = newMode;
		millisSinceUpdate = 0L;
		lastUpdateTime = System.currentTimeMillis();
	}

	public void globalStepForward(){
		if(updateMode != UpdateMode.MANUAL)
			throw new IllegalStateException("Update mode must be manual to step forward manually; But current " +
					"update mode is : " + updateMode.name());
		simulation.update();
	}

	public void pause(){
		switchUpdateMode(UpdateMode.MANUAL);
		sideMenu.updatePlayPauseButtons();
	}

	public void play(){
		switchUpdateMode(UpdateMode.REAL_TIME);
		sideMenu.updatePlayPauseButtons();
	}

	public void fastForward() {
		switchUpdateMode(UpdateMode.FAST_FORWARD);
	}

	public void fastestForward(){
		switchUpdateMode(UpdateMode.FASTEST_FORWARD);
	}

	@Override
	public void dispose() {
		simulation.dispose();
		GraphicsManager.disposeAssetManager();
	}

	public InputMultiplexer getInputMultiplexer() {
		return inputMultiplexer;
	}

	public Simulation getSimulation() {
		return simulation;
	}

	public void resetSimulation() {
		inputMultiplexer.removeProcessor(simulation.getInputProcessor());
		simulation.dispose();
		pause();
		simulation = new Simulation(DEFAULT_SEED, CURRENT_RUN_CONFIG, this, CURRENT_RUN_CONFIG);
		inputMultiplexer.addProcessor(simulation.getInputProcessor());

		sideMenu.resetSideMenu();
	}

	public OrthographicCamera getWorldCamera() {
		return simulationCamera;
	}

	public OrthographicCamera getFontCamera() {
		return simFontCamera;
	}

	public ScreenViewport getWorldViewport() {
		return simulationViewport;
	}

	public SideMenu getSideMenu() {
		return sideMenu;
	}

	public UpdateMode getUpdateMode() {
		return updateMode;
	}

	public PathFinderEnum getPathFinderSelected() {
		return pathFinderSelected;
	}

	public void setPathFinderSelected(PathFinderEnum pathFinderSelected) {
		this.pathFinderSelected = pathFinderSelected;
	}

	public void setTaskAllocatorSelected(TaskAllocatorEnum taskAllocatorSelected) {
		this.taskAllocatorSelected = taskAllocatorSelected;
	}

	public TaskAllocatorEnum getTaskAllocatorSelected() {
		return taskAllocatorSelected;
	}

	public static String getCurrentRunConfig() {
		return CURRENT_RUN_CONFIG;
	}

	public void refreshCamera() {
		simulation.updateRenderedBounds();
	}
}
