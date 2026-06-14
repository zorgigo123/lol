package com.example.pakoresan;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.*;
import java.util.ArrayList;

public class Main extends Application {
    ArrayList<ArrayList<ArrayList<Integer>>> historia = new ArrayList<>();
    String levelName = "Pakoreshon1.txt";
    Plocha plocha;
    int TILE_SIZE = 80;
    int aktualnyLevel = 1;
    int vyskaHornehoPanelu = 60;
    int sekundy = 0;
    javafx.animation.Timeline casovac;
    javafx.scene.control.Label lblCas = new javafx.scene.control.Label("Čas: 0");

    public void start(Stage stage) throws Exception {
        plocha = new Plocha(levelName);

        int cols = plocha.matica.get(0).size();
        int rows = plocha.matica.size();

        // --- Horný panel ---
        javafx.scene.layout.HBox topPanel = new javafx.scene.layout.HBox();
        topPanel.setPrefHeight(60);
        topPanel.setStyle("-fx-background-color: #333;");

        // lblCas je field triedy, len ho nastylujeme a pridáme do panelu
        lblCas.setStyle("-fx-text-fill: white; -fx-font-size: 18;");
        topPanel.setAlignment(Pos.CENTER);
        topPanel.getChildren().add(lblCas);

        // každú sekundu zvýši počítadlo a aktualizuje label
        casovac = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> {
                    sekundy++;
                    lblCas.setText("Čas: " + sekundy);
                })
        );
        casovac.setCycleCount(javafx.animation.Animation.INDEFINITE);
        casovac.play();

        // --- Herná plocha (Canvas) ---
        Canvas canvas = new Canvas(cols * TILE_SIZE, rows * TILE_SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawPlocha(gc);

        // --- Dolný panel ---
        Button btnSave = new Button("Save");
        Button btnLoad = new Button("Load");
        Button btnUndo = new Button("Undo");
        Button btnQuit = new Button("Quit");
        Button btnDalsiLevel = new Button("Ďalší level");
        javafx.scene.layout.HBox bottomPanel = new javafx.scene.layout.HBox(10, btnSave, btnLoad, btnUndo, btnDalsiLevel, btnQuit);
        bottomPanel.setPrefHeight(vyskaHornehoPanelu);
        bottomPanel.setAlignment(Pos.CENTER);
        bottomPanel.setStyle("-fx-background-color: #333;");

        // --- VBox spája všetko ---
        VBox root = new VBox(topPanel, canvas, bottomPanel);

        Scene scene = new Scene(root);

        canvas.setOnMouseClicked(e -> {
            int xSuradnica = (int) e.getX() / TILE_SIZE;
            int ySuradnica = (int) e.getY() / TILE_SIZE;

            if (!zistiCiZaciatok(xSuradnica, ySuradnica)) {
                // uložíme stav PRED zmenou
                ulozDoHistorie();

                int hodnota = plocha.matica.get(ySuradnica).get(xSuradnica);
                if (hodnota == 0) {
                    hodnota = -1;
                } else if (hodnota == -1) {
                    hodnota = 1;
                } else {
                    hodnota = 0;
                }
                plocha.matica.get(ySuradnica).set(xSuradnica, hodnota);
            }

            drawPlocha(gc);
            if (plocha.skontrolujVsetky()) {
                System.out.println("VYHRAL SI!");
            }
        });

        btnSave.setOnAction(e -> save());
        btnLoad.setOnAction(e -> load(gc));
        btnQuit.setOnAction(e -> stage.close());
        btnUndo.setOnAction(e -> undo(gc));
        btnDalsiLevel.setOnAction(e -> dalsiLevel(gc));

        stage.setTitle("Pakoresan");
        stage.setScene(scene);
        stage.show();
    }

    // Načíta ďalší level, resetuje históriu a čas
    public void dalsiLevel(GraphicsContext gc) {
        int pocetLevelov = zistiPocetLevelov();

        if (aktualnyLevel >= pocetLevelov) {
            System.out.println("Toto bol posledný level!");
            return;
        }

        aktualnyLevel++;
        levelName = "Pakoreshon" + aktualnyLevel + ".txt";
        plocha = new Plocha(levelName);
        historia.clear();
        sekundy = 0;
        lblCas.setText("Čas: " + sekundy);
        drawPlocha(gc);
        System.out.println("Načítaný level " + aktualnyLevel);
    }

    // Prejde súbory v roote a zistí koľko levelov existuje
    public int zistiPocetLevelov() {
        int pocet = 0;
        while (new java.io.File("Pakoreshon" + (pocet + 1) + ".txt").exists()) {
            pocet++;
        }
        return pocet;
    }

    // Uloží kópiu aktuálnej matice do histórie
    public void ulozDoHistorie() {
        ArrayList<ArrayList<Integer>> kopia = new ArrayList<>();
        for (ArrayList<Integer> riadok : plocha.matica) {
            kopia.add(new ArrayList<>(riadok));
        }
        historia.add(kopia);
    }

    // Vráti maticu na predchádzajúci stav
    public void undo(GraphicsContext gc) {
        if (historia.isEmpty()) {
            System.out.println("Žiadna história");
            return;
        }
        plocha.matica = historia.get(historia.size() - 1);
        historia.remove(historia.size() - 1);
        drawPlocha(gc);
        System.out.println("Krok späť");
    }

    // Uloží maticu, čas a aktuálny level do súboru save.dat
    public void save() {
        System.out.println("SAVING CONFIG");
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream("save.dat"))) {
            oos.writeObject(plocha.matica);
            oos.writeInt(sekundy);
            oos.writeInt(aktualnyLevel);
            System.out.println("Uložené úspešne");
        } catch (IOException e) {
            System.out.println("Chyba pri ukladaní: " + e.getMessage());
        }
    }

    // Načíta maticu, čas a aktuálny level zo súboru save.dat
    public void load(GraphicsContext gc) {
        System.out.println("LOADING CONFIG");
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream("save.dat"))) {
            plocha.matica = (ArrayList<ArrayList<Integer>>) ois.readObject();
            sekundy = ois.readInt();
            aktualnyLevel = ois.readInt();
            lblCas.setText("Čas: " + sekundy);
            drawPlocha(gc);
            System.out.println("Načítané úspešne");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Chyba pri načítaní: " + e.getMessage());
        }
    }

    // Vráti true ak sa na daných súradniciach nachádza pevný kameň
    private boolean zistiCiZaciatok(int x, int y) {
        for (ArrayList<Integer> body : plocha.zaciatocneBody) {
            int xbod = body.get(1);
            int ybod = body.get(2);
            if (xbod == x && ybod == y) {
                return true;
            }
        }
        return false;
    }

    // Vykreslí celú hraciu plochu na canvas
    private void drawPlocha(GraphicsContext gc) {
        gc.clearRect(0, 0, plocha.matica.getFirst().size() * TILE_SIZE, plocha.matica.size() * TILE_SIZE);

        for (int y = 0; y < plocha.matica.size(); y++) {
            for (int x = 0; x < plocha.matica.get(y).size(); x++) {
                int hodnota = plocha.matica.get(y).get(x);

                // farba bunky podľa hodnoty
                if (hodnota == 0) {
                    gc.setFill(Color.LIGHTGRAY);
                } else if (hodnota == -1) {
                    gc.setFill(Color.STEELBLUE);
                } else if (hodnota == 1) {
                    gc.setFill(Color.RED);
                } else {
                    // pevný kameň — skontrolujeme či má splnenú podmienku
                    boolean splnena = plocha.skontrolujPodmienku(x, y);
                    if (hodnota > 0) {
                        if (splnena) {
                            gc.setFill(Color.DARKRED);
                        } else {
                            gc.setFill(Color.RED);
                        }
                    } else {
                        if (splnena) {
                            gc.setFill(Color.DARKBLUE);
                        } else {
                            gc.setFill(Color.STEELBLUE);
                        }
                    }
                }

                gc.fillOval(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);

                // číslo do pevného kameňa
                for (ArrayList<Integer> body : plocha.zaciatocneBody) {
                    int xbody = body.get(1);
                    int ybody = body.get(2);
                    if (x == xbody && y == ybody) {
                        gc.setFill(Color.WHITE);
                        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
                        gc.setTextBaseline(javafx.geometry.VPos.CENTER);
                        gc.fillText(
                                String.valueOf(Math.abs(hodnota)),
                                x * TILE_SIZE + TILE_SIZE / 2.0,
                                y * TILE_SIZE + TILE_SIZE / 2.0
                        );
                    }
                }
            }
        }
    }
}