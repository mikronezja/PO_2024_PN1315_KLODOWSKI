package agh.ics.oop.presenter;

import agh.ics.oop.Simulation;
import agh.ics.oop.SimulationEngine;
import agh.ics.oop.model.*;
import agh.ics.oop.model.util.DailyDataCollector;
import agh.ics.oop.model.util.WorldElementVisualizer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.w3c.dom.Text;

import java.util.List;
import java.util.Optional;

import static java.lang.Integer.parseInt;
import static java.lang.Math.min;

public class SimulationWindowPresenter implements MapChangeListener {

    private ProjectWorldMap worldMap;
    private Simulation simulation;
    private Stage stage;

    private WorldElementVisualizer worldElementVisualizer = new WorldElementVisualizer();

    private Image tile = new Image("tile.png");
    private Image equator = new Image("equtor.png");



    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private GridPane mapGrid;
    @FXML
    private javafx.scene.text.Text numberOfDays;
    @FXML
    private javafx.scene.text.Text numberOfAnimals;
    @FXML
    private javafx.scene.text.Text numberOfPlants;
    @FXML
    private javafx.scene.text.Text numberOfFreeTiles;
    @FXML
    private javafx.scene.text.Text mostPopularGenotype;
    @FXML
    private javafx.scene.text.Text averageEnergyLevel;
    @FXML
    private javafx.scene.text.Text averageDaysAlive;
    @FXML
    private javafx.scene.text.Text averageNumberOfKids;


    public void setupSimulation(ProjectWorldMap worldMap, int howManyAnimalsToStartWith, int howManyEnergyAnimalsStartWith,
                                int energyNeededToReproduce, int energyGettingPassedToDescendant, int minMutationInNewborn, int maxMutationInNewborn,
                                int genomeLength, boolean ifAnimalsMoveSlowerWhenOlder, boolean writeIntoACSVFile,
                                Stage simulationStage) {
        this.worldMap = worldMap;
        stage = simulationStage;
        mainBorderPane.setMargin(mapGrid, new Insets(12,12,12,12));
        this.simulation = new Simulation(worldMap, howManyAnimalsToStartWith, howManyEnergyAnimalsStartWith,
                energyNeededToReproduce, energyGettingPassedToDescendant,minMutationInNewborn, maxMutationInNewborn,
                genomeLength, ifAnimalsMoveSlowerWhenOlder, writeIntoACSVFile);
        List<Simulation> simulationsList = List.of(simulation);
        SimulationEngine simulationEngine = new SimulationEngine(simulationsList);
        simulationEngine.runAsync();
    }

    public void drawMap(ProjectWorldMap map) {
        clearGrid();
        Boundary boundary = map.getCurrentBounds();
        Label label = new Label();
        GridPane.setHalignment(label, HPos.CENTER);
        int widthtOfMap = boundary.upperRightCorner().getX();
        int heightOfMap = boundary.upperRightCorner().getY();

        double windowWidthToMapWidthRatio = stage.getWidth() / widthtOfMap;
        double windowHeightToMapWidthRatio = stage.getHeight() / heightOfMap;

        int cellWidth = min((int)windowHeightToMapWidthRatio,(int)windowWidthToMapWidthRatio) - 20;
        int cellHight = min((int)windowHeightToMapWidthRatio,(int)windowWidthToMapWidthRatio) - 20;


        for (int i = 0; i <= widthtOfMap;i++){
            for (int j = 0; j <= heightOfMap;j++){

                ImageView tileView;
                if ( map.isPositionMoreDesirableForPlants(new Vector2d(i, heightOfMap - j)))
                 tileView = new ImageView(equator);
                else
                    tileView = new ImageView(tile);
                tileView.setFitHeight(cellHight);
                tileView.setFitWidth(cellWidth);
                mapGrid.add(tileView, i, j);
            }
        }
        for (int i = 0; i < widthtOfMap+1;i++){
            mapGrid.getColumnConstraints().add(new ColumnConstraints(cellWidth));
        }
        for (int i = 0; i < heightOfMap+1;i++){
            mapGrid.getRowConstraints().add(new RowConstraints(cellHight));
        }

        List<WorldElement> elements = map.getElements();
        for (WorldElement element : elements)
        {
            Vector2d positionOfElement = element.getPosition();
            ImageView animal = worldElementVisualizer.getImageView(element);
            animal.setFitHeight(cellHight);
            animal.setFitWidth(cellWidth);
            mapGrid.add(animal, positionOfElement.getX() , heightOfMap - positionOfElement.getY());
            GridPane.setHalignment(animal, HPos.CENTER);
        }
    }

    private void clearGrid() {
        mapGrid.getChildren().retainAll(mapGrid.getChildren().get(0));
        mapGrid.getColumnConstraints().clear();
        mapGrid.getRowConstraints().clear();
    }

    public void drawCurrentDayInfo(ProjectWorldMap worldMap)
    {
        DailyDataCollector collectData = new DailyDataCollector(worldMap, simulation.getDeadAnimals(),simulation.getSimulationDays());

        numberOfDays.setText(String.valueOf(collectData.getCurrentSimulationDay()));
        numberOfAnimals.setText(String.valueOf(collectData.numberOfAliveAnimals()));
        numberOfPlants.setText(String.valueOf(collectData.numberOfPlants()));
        numberOfFreeTiles.setText(String.valueOf(collectData.numberOfFreeTiles()));
//        mostPopularGenotype.setText(String.valueOf(collectData.mostPopularGenotype()));
        averageEnergyLevel.setText(String.valueOf(collectData.averageEnergyLevel()));

        Optional<Integer> returnedAverageLifeSpan = collectData.averageLifeSpan();
        String displayText = returnedAverageLifeSpan
                .map(String::valueOf)
                .orElse("No data available");
        averageDaysAlive.setText(displayText);

        averageNumberOfKids.setText(String.valueOf(collectData.averageKidsNumber()));
    }

    @Override
    public void mapChanged(ProjectWorldMap worldMap, String message) {
        Platform.runLater(() -> {
            drawMap(worldMap);
            drawCurrentDayInfo(worldMap);
        });
    }
}
