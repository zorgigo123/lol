package com.example.hamusando;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;

public class Main extends Application {
    String levelName = "hamusando1.txt";
    Plocha plocha;
    int TILE_SIZE;
    ArrayList<ArrayList<ArrayList<Integer>>> historia = new ArrayList<>();


    @Override
    public void start(Stage stage) throws Exception {
        plocha = new Plocha(levelName);

        int cols = plocha.matica.get(0).size();
        int rows = plocha.matica.size();

        // vypočítame TILE_SIZE podľa veľkosti okna
        double sirkaPlochy = 600;
        double vyskaPlochy = 600;
        TILE_SIZE = (int) Math.min(sirkaPlochy / (cols + 2), vyskaPlochy / (rows + 2));

        // --- Herná plocha (Canvas) ---
        Canvas canvas = new Canvas((cols + 2) * TILE_SIZE, (rows + 2) * TILE_SIZE);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawPlocha(gc);

        // prepočítanie TILE_SIZE pri zmene šírky okna
        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            TILE_SIZE = (int) Math.min(newVal.doubleValue() / (cols + 2), stage.getHeight() / (rows + 2));
            canvas.setWidth((cols + 2) * TILE_SIZE);
            canvas.setHeight((rows + 2) * TILE_SIZE);
            drawPlocha(gc);
        });

        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            TILE_SIZE = (int) Math.min(stage.getWidth() / (cols + 2), newVal.doubleValue() / (rows + 2));
            canvas.setWidth((cols + 2) * TILE_SIZE);
            canvas.setHeight((rows + 2) * TILE_SIZE);
            drawPlocha(gc);
        });
        canvas.setOnMouseClicked(e -> {
            int surX = (int) (e.getX() / TILE_SIZE) - 1;
            int surY = (int) (e.getY() / TILE_SIZE) - 1;

            if (surX < 0 || surX >= plocha.velkost) return;
            if (surY < 0 || surY >= plocha.velkost) return;
            ulozDoHistorie();

            int hodnotaMatice = plocha.matica.get(surY).get(surX);
            if (hodnotaMatice == 0) {
                plocha.matica.get(surY).set(surX, 1);
            } else if (hodnotaMatice == 1) {
                plocha.matica.get(surY).set(surX, 2);
            } else {
                plocha.matica.get(surY).set(surX, 0);
            }

            plocha.kontrolaPravidiel();
            drawPlocha(gc);

            if (plocha.skontrolujVsetky()) {
                System.out.println("GRATULUJEME, VYHRAL SI!");
                drawGratulacia(gc);
                return;
            }

            if (plocha.jeRiesitelna()) {
                System.out.println("Pozícia je stále riešiteľná");
            } else {
                System.out.println("Pozícia už nie je riešiteľná!");
            }
        });
        Button btnSave = new Button("Save");
        Button btnLoad = new Button("Load");
        Button btnUndo = new Button("Undo");

        btnSave.setOnAction(e -> save());
        btnLoad.setOnAction(e -> load(gc));
        btnUndo.setOnAction(e -> undo(gc));

        javafx.scene.layout.HBox bottomPanel = new javafx.scene.layout.HBox(10, btnSave, btnLoad, btnUndo);
        bottomPanel.setAlignment(javafx.geometry.Pos.CENTER);

        VBox root = new VBox(canvas,bottomPanel);
        root.setAlignment(javafx.geometry.Pos.CENTER);
        root.setStyle("-fx-background-color: white;");

        Scene scene = new Scene(root, 600, 600);
        stage.setTitle("Hamusando");
        stage.setScene(scene);
        stage.show();
    }
    void drawGratulacia(GraphicsContext gc) {
        // polopriesvitné prekrytie
        gc.setFill(Color.color(0, 0, 0, 0.5));
        gc.fillRect(0, 0, (plocha.velkost + 2) * TILE_SIZE, (plocha.velkost + 2) * TILE_SIZE);

        // text gratulace
        gc.setFill(Color.YELLOW);
        gc.setFont(javafx.scene.text.Font.font(40));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.setTextBaseline(javafx.geometry.VPos.CENTER);
        gc.fillText(
                "VYHRAL SI!",
                (plocha.velkost + 2) * TILE_SIZE / 2.0,
                (plocha.velkost + 2) * TILE_SIZE / 2.0
        );
    }
    void ulozDoHistorie() {
        ArrayList<ArrayList<Integer>> kopia = new ArrayList<>();
        for (ArrayList<Integer> riadok : plocha.matica) {
            kopia.add(new ArrayList<>(riadok));
        }
        historia.add(kopia);
    }

    void undo(GraphicsContext gc) {
        if (historia.isEmpty()) {
            System.out.println("Žiadna história");
            return;
        }
        plocha.matica = historia.get(historia.size() - 1);
        historia.remove(historia.size() - 1);
        plocha.kontrolaPravidiel();
        drawPlocha(gc);
    }

    void save() {
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(
                new java.io.FileOutputStream("save.dat"))) {
            oos.writeObject(plocha.matica);
            System.out.println("Uložené úspešne");
        } catch (java.io.IOException e) {
            System.out.println("Chyba pri ukladaní: " + e.getMessage());
        }
    }

    void load(GraphicsContext gc) {
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.FileInputStream("save.dat"))) {
            plocha.matica = (ArrayList<ArrayList<Integer>>) ois.readObject();
            historia.clear();
            plocha.kontrolaPravidiel();
            drawPlocha(gc);
            System.out.println("Načítané úspešne");
        } catch (java.io.IOException | ClassNotFoundException e) {
            System.out.println("Chyba pri načítaní: " + e.getMessage());
        }
    }
    void drawPlocha(GraphicsContext gc) {
        gc.clearRect(0, 0, (plocha.matica.getFirst().size() + 2) * TILE_SIZE, (plocha.matica.size() + 2) * TILE_SIZE);

        // --- kreslenie mriežky ---
        for (int y = 0; y < plocha.matica.size(); y++) {
            for (int x = 0; x < plocha.matica.getFirst().size(); x++) {
                int hodnota = plocha.matica.get(y).get(x);
                if (hodnota == 0) {
                    gc.setFill(Color.BLACK);
                    gc.strokeRect(TILE_SIZE + x * TILE_SIZE, TILE_SIZE + y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                } else if (hodnota == 1) {
                    gc.setFill(Color.RED);
                    gc.fillOval(TILE_SIZE + x * TILE_SIZE, TILE_SIZE + y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                } else {
                    gc.setFill(Color.BROWN);
                    gc.fillRect(TILE_SIZE + x * TILE_SIZE, TILE_SIZE + y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }

        // --- čísla zhoraDole (horný riadok) ---
        for (int x = 0; x < plocha.zhoraDole.size(); x++) {
            int hodnota = plocha.zhoraDole.get(x);

            if (plocha.korektneStlpce != null && plocha.korektneStlpce.get(x) == 1) {
                gc.setFill(Color.GREEN);
            } else {
                gc.setFill(Color.WHITE);
            }

            String text;
            if (hodnota == -1) {
                text = "?";
            } else {
                text = String.valueOf(hodnota);
            }

            gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
            gc.setTextBaseline(javafx.geometry.VPos.CENTER);
            gc.fillText(text, TILE_SIZE + x * TILE_SIZE + TILE_SIZE / 2.0, TILE_SIZE / 2.0);
        }

        // --- čísla zlavaDoprava (ľavý stĺpec) ---
        for (int y = 0; y < plocha.zlavaDoprava.size(); y++) {
            int hodnota = plocha.zlavaDoprava.get(y);

            if (plocha.korektneRiadky != null && plocha.korektneRiadky.get(y) == 1) {
                gc.setFill(Color.GREEN);
            } else {
                gc.setFill(Color.WHITE);
            }

            String text;
            if (hodnota == -1) {
                text = "?";
            } else {
                text = String.valueOf(hodnota);
            }

            gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
            gc.setTextBaseline(javafx.geometry.VPos.CENTER);
            gc.fillText(text, TILE_SIZE / 2.0, TILE_SIZE + y * TILE_SIZE + TILE_SIZE / 2.0);
        }
    }
}