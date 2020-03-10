package SkyNetJR.Main.MainFx.fxml.DashboardView;

import SkyNetJR.Analytics.AnalyticsWrapper;
import SkyNetJR.Creatures.Population;
import SkyNetJR.Graphics.GLFWWindowManager.WindowManager;
import SkyNetJR.Graphics.Rendering.Renderers.BestBrainRenderer;
import SkyNetJR.Graphics.Rendering.Renderers.PopulationRenderer;
import SkyNetJR.Graphics.Rendering.Renderers.VirtualWorldRenderer;
import SkyNetJR.Graphics.Rendering.View;
import SkyNetJR.Settings;
import SkyNetJR.Threading.SimulationThread;
import SkyNetJR.VirtualWorld.TileMap;
import SkyNetJR.VirtualWorld.VirtualWorld;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class DashboardView {
    @FXML private PieChart geneDistribution;

    @FXML private AreaChart<Double, Double> energyDistribution;
    private AreaChart.Series<Double, Double> energyDistributionCreature;
    private AreaChart.Series<Double, Double> energyDistributionMap;

    @FXML private BarChart<String, Double> creaturesPerGeneration;
    private BarChart.Series<String, Double> creaturesPerGenerationSeries;

    @FXML private CheckBox toggleWorldView;
    @FXML private CheckBox toggleFastForward;
    @FXML private CheckBox toggleBrainView;
    @FXML private Button newSim;

    private Timer mainTimer;

    public VirtualWorld World;
    public SkyNetJR.Creatures.Population Population;
    public View WorldView;
    public View BrainView;
    public SkyNetJR.Threading.SimulationThread SimulationThread;
    private static WindowManager _windowManager;
    public AnalyticsWrapper Info;

    @FXML private void initialize()
    {
        // WindowManager vorbereiten, um eigene Fenster erstellen zu können
        _windowManager = new WindowManager();
        _windowManager.Init();

        // Fenster für die Simulation erstellen
        WorldView = new View(Settings.ViewSettings.Width, Settings.ViewSettings.Height, "SkyNetJR - Virtuelle Welt", true, _windowManager);
        WorldView.setUseVSync(true);
        WorldView.Start();
        WorldView.setClosable(false);
        //WorldView.setVisible(true);

        // Fenster für das beste neuronale Netz erstellen
        BrainView = new View(1280, 720, "SkyNetJR - Neuronales Netz", true, _windowManager);
        BrainView.setUseVSync(true);
        BrainView.Start();
        BrainView.setClosable(false);
        BrainView.getRenderers().add(new BestBrainRenderer(null));
        //BrainView.setVisible(true);

        geneDistribution.setData(FXCollections.observableArrayList());

        // Energieverteilung
        energyDistributionCreature = new XYChart.Series<>(FXCollections.observableArrayList());
        energyDistributionCreature.setName("\u2211 Energie der Kreaturen");
        energyDistributionMap = new XYChart.Series<>(FXCollections.observableArrayList());
        energyDistributionMap.setName("\u2211 Energie in der Welt");
        energyDistribution.setData(FXCollections.observableArrayList(
                energyDistributionCreature, energyDistributionMap)
        );
        energyDistribution.getXAxis().setAutoRanging(false);

        // Kreatur-Generations-Verteilung
        creaturesPerGenerationSeries = new XYChart.Series<>(FXCollections.observableArrayList());
        creaturesPerGeneration.setData(FXCollections.observableArrayList(creaturesPerGenerationSeries));

        mainTimer = new Timer();
        mainTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (SimulationThread != null && SimulationThread.isStarted()) {
                    // Informationen sammeln

                    AnalyticsWrapper wrapper = new AnalyticsWrapper(World, Population);
                    double time = World.getWorldTime() / 1000d;

                    Map<Long, Double> creaturePerGenerationCount = new HashMap<>();
                    for (int i = 0; i < Population.getCreatures().size(); i++) {
                        long g = Population.getCreatures().get(i).getGeneration();
                        if (creaturePerGenerationCount.containsKey(g))
                            creaturePerGenerationCount.replace(g, creaturePerGenerationCount.get(g) + 1);
                        else creaturePerGenerationCount.put(g, 1d);
                    }

                    // Informationen in Diagramme laden
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < energyDistributionCreature.getData().size(); i++) {
                                if (energyDistributionCreature.getData().get(i).getXValue() > time - 600){
                                    if (i > 0) {
                                        energyDistributionCreature.getData().remove(0, i - 1);
                                        energyDistributionMap.getData().remove(0, i - 1);
                                    }

                                    break;
                                }
                            }

                            energyDistributionCreature.getData().add(new StackedAreaChart.Data(time, (wrapper.getTotalCreatureEnergy())));
                            energyDistributionMap.getData().add(new StackedAreaChart.Data(time, (wrapper.getTotalMapEnergy() / 10)));
                            ((ValueAxis<Double>)energyDistribution.getXAxis()).setLowerBound(Math.round(energyDistributionCreature.getData().get(0).getXValue()));
                            ((ValueAxis<Double>)energyDistribution.getXAxis()).setUpperBound(Math.round(energyDistributionCreature.getData().get(energyDistributionCreature.getData().size() - 1).getXValue()));
                            ((ValueAxis<Double>)energyDistribution.getXAxis()).setMinorTickVisible(false);

                            creaturesPerGenerationSeries.getData().clear();
                            for (Long l : creaturePerGenerationCount.keySet()){
                                creaturesPerGenerationSeries.getData().add(new XYChart.Data<>(l.toString(), creaturePerGenerationCount.get(l)));
                            }
                        }
                    });
                }
            }
        }, 1000, 1000);
        mainTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (SimulationThread == null || !SimulationThread.isStarted()) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            resetData();
                        }
                    });

                    toggleWorldView.setSelected(false);
                    toggleFastForward.setSelected(false);
                    toggleBrainView.setSelected(false);

                    toggleWorldView.setDisable(true);
                    toggleFastForward.setDisable(true);
                    toggleBrainView.setDisable(true);
                }else {
                    toggleWorldView.setDisable(false);
                    toggleFastForward.setDisable(false);
                    toggleBrainView.setDisable(false);

                    toggleWorldView.setSelected(WorldView.isVisible());
                    toggleFastForward.setSelected(!SimulationThread.isRealTime());
                    toggleBrainView.setSelected(BrainView.isVisible());
                }
            }
        }, 200, 200);
    }

    @FXML private void generateNewSim(){
        resetData();

        WorldView.getRenderers().clear();
        ((BestBrainRenderer)BrainView.getRenderers().get(0)).setPopulation(null);

        if (SimulationThread != null) SimulationThread.Destroy();
        if (Population != null) Population.Destroy();
        if (World != null) World.Destroy();

        TileMap map = new TileMap();
        map.SetDefaults();
        map.Generate();
        World = new VirtualWorld(map);
        World.setRunning(true);
        World.setDraw(true);

        Population = new Population(World);
        Population.FillPopulation();

        SimulationThread = new SimulationThread(World, Population, 60, true, true);
        SimulationThread.start();

        WorldView.getRenderers().add(new VirtualWorldRenderer(World));
        WorldView.getRenderers().add(new PopulationRenderer(Population));
        ((BestBrainRenderer)BrainView.getRenderers().get(0)).setPopulation(Population);

    }

    private void resetData(){
        geneDistribution.getData().clear();

        energyDistributionMap.getData().clear();
        energyDistributionCreature.getData().clear();

        creaturesPerGenerationSeries.getData().clear();
    }

    @FXML private void setWorldVisibility(){
        WorldView.setVisible(toggleWorldView.isSelected());
    }

    @FXML private void setFastForward(){
        if (SimulationThread != null){
            SimulationThread.setRealTime(!toggleFastForward.isSelected());
        }
    }

    @FXML private void setBrainVisibility(){
        BrainView.setVisible(toggleBrainView.isSelected());
    }

    public void Shutdown(){
        // Aufräumen - Objekte "zerstören" und Programm kontrolliert schließen
        mainTimer.cancel();
        mainTimer = null;

        BrainView.setVisible(false);
        BrainView.setClosable(true);
        BrainView.Destroy();
        BrainView = null;

        WorldView.setVisible(false);
        WorldView.setClosable(true);
        WorldView.Destroy();
        WorldView = null;

        _windowManager.Destroy();

        if (SimulationThread != null){
            SimulationThread.Destroy();
            SimulationThread = null;
        }

        if (Population != null){
            Population.Destroy();
            Population = null;
        }

        if (World != null){
            World.Destroy();
            World = null;
        }
    }
}
