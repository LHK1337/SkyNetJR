/*
* Controllerklasse für die JavaFX Ansicht
* Beinhaltet Logik der JavaFX Elemente
* */

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
    // @FXML kennzeichnet Objekte aus der JavaFX Ansicht

    // Diagramme und deren Datenreihen
    @FXML private AreaChart<Double, Double> energyDistribution;
    private AreaChart.Series<Double, Double> _energyDistributionCreature;
    private AreaChart.Series<Double, Double> _energyDistributionMap;

    @FXML private BarChart<String, Double> creaturesPerGeneration;
    private BarChart.Series<String, Double> _creaturesPerGenerationSeries;

    // Elemente für den Nutzer zu Interaktion
    @FXML private CheckBox toggleWorldView;
    @FXML private CheckBox toggleFastForward;
    @FXML private CheckBox toggleBrainView;
    @FXML private Button newSim;

    // Timer zum Abrufen der Daten aus der Simulation
    private Timer _mainTimer;

    // Objekte der Simulation
    public VirtualWorld World;
    public SkyNetJR.Creatures.Population Population;
    public View WorldView;
    public View BrainView;
    public SkyNetJR.Threading.SimulationThread SimulationThread;
    private static WindowManager _windowManager;

    @FXML private void initialize()
    {
        // WindowManager vorbereiten, um eigene Fenster erstellen zu können
        _windowManager = new WindowManager();
        _windowManager.init();

        // Fenster für die Simulation erstellen
        WorldView = new View(Settings.ViewSettings.Width, Settings.ViewSettings.Height, "SkyNetJR - Virtuelle Welt", true, _windowManager);
        WorldView.setUseVSync(true);
        WorldView.Start();
        WorldView.setClosable(false);

        // Fenster für das beste neuronale Netz erstellen
        BrainView = new View(1280, 720, "SkyNetJR - Neuronales Netz", true, _windowManager);
        BrainView.setUseVSync(true);
        BrainView.Start();
        BrainView.setClosable(false);
        BrainView.getRenderers().add(new BestBrainRenderer(null));

        // Energieverteilung
        _energyDistributionCreature = new XYChart.Series<>(FXCollections.observableArrayList());
        _energyDistributionCreature.setName("\u2211 Energie der Kreaturen");
        _energyDistributionMap = new XYChart.Series<>(FXCollections.observableArrayList());
        _energyDistributionMap.setName("\u2211 Energie in der Welt");
        energyDistribution.setData(FXCollections.observableArrayList(
                _energyDistributionCreature, _energyDistributionMap)
        );
        energyDistribution.getXAxis().setAutoRanging(false);

        // Kreatur-Generations-Verteilung
        _creaturesPerGenerationSeries = new XYChart.Series<>(FXCollections.observableArrayList());
        creaturesPerGeneration.setData(FXCollections.observableArrayList(_creaturesPerGenerationSeries));

        _mainTimer = new Timer();
        //// Periodische Routinen definieren
        // Aktualisieren der Diagramme
        _mainTimer.scheduleAtFixedRate(new TimerTask() {
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

                    // Ältere Datenpunkte wieder aus Diagramm entfernen
                    Platform.runLater(new Runnable() {  // Definiert Routine im Thread des Timers aber führt diese erst später im GUI-Thread der Anwendung aus.
                        @Override
                        public void run() {
                            for (int i = 0; i < _energyDistributionCreature.getData().size(); i++) {
                                if (_energyDistributionCreature.getData().get(i).getXValue() > time - 600){
                                    if (i > 0) {
                                        _energyDistributionCreature.getData().remove(0, i - 1);
                                        _energyDistributionMap.getData().remove(0, i - 1);
                                    }

                                    break;
                                }
                            }

                            // Neue Datenpunkte in Diagramme laden
                            _energyDistributionCreature.getData().add(new StackedAreaChart.Data(time, (wrapper.getTotalCreatureEnergy())));
                            _energyDistributionMap.getData().add(new StackedAreaChart.Data(time, (wrapper.getTotalMapEnergy() / 10)));

                            _creaturesPerGenerationSeries.getData().clear();
                            for (Long l : creaturePerGenerationCount.keySet()){
                                _creaturesPerGenerationSeries.getData().add(new XYChart.Data<>(l.toString(), creaturePerGenerationCount.get(l)));
                            }

                            // Zeitachse manuell skalieren
                            ((ValueAxis<Double>)energyDistribution.getXAxis()).setLowerBound(Math.round(_energyDistributionCreature.getData().get(0).getXValue()));
                            ((ValueAxis<Double>)energyDistribution.getXAxis()).setUpperBound(Math.round(_energyDistributionCreature.getData().get(_energyDistributionCreature.getData().size() - 1).getXValue()));
                            ((ValueAxis<Double>)energyDistribution.getXAxis()).setMinorTickVisible(false);
                        }
                    });
                }
            }
        }, 1000, 1000);
        // Aktualisieren der Benutzerelemente
        _mainTimer.scheduleAtFixedRate(new TimerTask() {
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

    // Generiert und startet eine neue Simulation
    @FXML private void generateNewSim(){
        // Daten zurücksetzen
        resetData();

        // Ansichten zurücksetzen
        WorldView.getRenderers().clear();
        ((BestBrainRenderer)BrainView.getRenderers().get(0)).setPopulation(null);

        if (SimulationThread != null) SimulationThread.destroy();
        if (Population != null) Population.Destroy();

        // Simulatio vorbereiten
        TileMap map = new TileMap();
        map.setDefaults();
        map.generate();
        World = new VirtualWorld(map);
        World.setRunning(true);
        World.setDraw(true);

        Population = new Population(World);
        Population.FillPopulation();

        // Simulation in seperatem Thread starten
        SimulationThread = new SimulationThread(World, Population, Settings.SimulationSettings.TimePrecision, true, true);
        SimulationThread.start();

        // Neue Renderer für Simulationsansicht erstellen
        WorldView.getRenderers().add(new VirtualWorldRenderer(World));
        WorldView.getRenderers().add(new PopulationRenderer(Population));
        ((BestBrainRenderer)BrainView.getRenderers().get(0)).setPopulation(Population);

    }

    // Daten zurücksetzen
    private void resetData(){
        _energyDistributionMap.getData().clear();
        _energyDistributionCreature.getData().clear();

        _creaturesPerGenerationSeries.getData().clear();
    }

    // Weltansicht anzeigen oder verstecken
    @FXML private void setWorldVisibility(){
        WorldView.setVisible(toggleWorldView.isSelected());
    }

    // Simulation beschleunigen oder in Echtzeit laufen lassen
    @FXML private void setFastForward(){
        if (SimulationThread != null){
            SimulationThread.setRealTime(!toggleFastForward.isSelected());
        }
    }

    // "Bestes Gehirn" anzeigen oder verstecken
    @FXML private void setBrainVisibility(){
        BrainView.setVisible(toggleBrainView.isSelected());
    }

    // Aufräumen - Objekte "zerstören" und Programm kontrolliert schließen
    public void shutdown(){
        _mainTimer.cancel();
        _mainTimer = null;

        BrainView.setVisible(false);
        BrainView.setClosable(true);
        BrainView.Destroy();
        BrainView = null;

        WorldView.setVisible(false);
        WorldView.setClosable(true);
        WorldView.Destroy();
        WorldView = null;

        _windowManager.destroy();

        if (SimulationThread != null){
            SimulationThread.destroy();
            SimulationThread = null;
        }

        if (Population != null){
            Population.Destroy();
            Population = null;
        }

        if (World != null){
            World = null;
        }
    }
}
