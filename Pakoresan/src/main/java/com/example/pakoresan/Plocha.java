package com.example.pakoresan;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Plocha {
    ArrayList<ArrayList<Integer>> matica;
    ArrayList<ArrayList<Integer>> zaciatocneBody;

    // Načíta hraciu plochu zo súboru a inicializuje začiatočné body
    public Plocha(String levelName) {
        matica = new ArrayList<>();

        try (FileReader reader = new FileReader(levelName);
             BufferedReader bufferedReader = new BufferedReader(reader)) {

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                ArrayList<Integer> riadok = new ArrayList<>();

                for (String cislo : line.trim().split("\\s+")) {
                    // bodka znamená prázdna bunka = 0
                    if (cislo.equals(".")) {
                        riadok.add(0);
                    } else {
                        riadok.add(Integer.parseInt(cislo));
                    }
                }
                matica.add(riadok);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        initZaciatocneBody();
    }

    // Prejde maticu a uloží všetky pevné kamene (hodnoty iné ako 0, 1, -1)
    // Každý začiatočný bod je uložený ako [hodnota, x, y]
    public void initZaciatocneBody() {
        zaciatocneBody = new ArrayList<>();

        for (int y = 0; y < matica.size(); y++) {
            for (int x = 0; x < matica.get(y).size(); x++) {
                int hodnota = matica.get(y).get(x);

                // preskočíme prázdne bunky a hráčom označené bunky
                if (hodnota == 0 || hodnota == 1 || hodnota == -1) continue;

                ArrayList<Integer> bod = new ArrayList<>();
                bod.add(hodnota); // index 0: hodnota (napr. 3 alebo -2)
                bod.add(x);       // index 1: stĺpec
                bod.add(y);       // index 2: riadok
                zaciatocneBody.add(bod);
            }
        }
    }

    // Skontroluje podmienku pre jeden pevný kameň na pozícii (zacX, zacY)
    // Podmienka: súvislý blok (pevný kameň + okolité bunky rovnakej farby)
    // musí mať dokopy presne toľko buniek, aká je absolútna hodnota pevného kameňa
    // Kladná hodnota = červená farba (1), záporná = modrá farba (-1)
    public boolean skontrolujPodmienku(int zacX, int zacY) {
        int pevnaHodnota = matica.get(zacY).get(zacX);

        // určíme farbu bloku podľa znamienka
        int farba;
        if (pevnaHodnota > 0) {
            farba = 1;   // červená
        } else {
            farba = -1;  // modrá
        }

        int potrebnaVelkost = Math.abs(pevnaHodnota);

        // pole navštívených buniek, aby sme každú bunku počítali len raz
        boolean[][] navstivene = new boolean[matica.size()][matica.get(0).size()];

        // štartovací bod sám patrí do bloku, označíme ho hneď
        navstivene[zacY][zacX] = true;
        int pocet = 1;

        // spustíme flood fill do všetkých 4 smerov od štartovacieho bodu
        int[][] smery = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        for (int[] smer : smery) {
            int nx = zacX + smer[0];
            int ny = zacY + smer[1];
            pocet += floodFill(nx, ny, farba, navstivene);
        }

        // podmienka je splnená ak sa počet zhoduje s hodnotou kameňa
        return pocet == potrebnaVelkost;
    }

    // Rekurzívne počíta veľkosť súvislého bloku buniek danej farby
    // Zastaví sa keď: sme mimo plochy, bunka už bola navštívená,
    // alebo bunka nemá správnu farbu
    private int floodFill(int x, int y, int farba, boolean[][] navstivene) {
        if (!jeVHranici(x, y)) return 0;
        if (navstivene[y][x]) return 0;
        if (matica.get(y).get(x) != farba) return 0;

        // označíme bunky ako navštívenú a počítame ju
        navstivene[y][x] = true;
        int pocet = 1;

        // rekurzívne pokračujeme do všetkých 4 smerov
        int[][] smery = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        for (int[] smer : smery) {
            pocet += floodFill(x + smer[0], y + smer[1], farba, navstivene);
        }

        return pocet;
    }

    // Skontroluje či sú súradnice vnútri hracej plochy
    private boolean jeVHranici(int x, int y) {
        if (y < 0 || y >= matica.size()) return false;
        if (x < 0 || x >= matica.get(y).size()) return false;
        return true;
    }

    // Skontroluje podmienku pre VŠETKY pevné kamene
    // Vráti true iba ak každý pevný kameň má správne veľký súvislý blok = výhra
    public boolean skontrolujVsetky() {
        for (ArrayList<Integer> bod : zaciatocneBody) {
            int x = bod.get(1);
            int y = bod.get(2);
            if (!skontrolujPodmienku(x, y)) {
                return false;
            }
        }
        return true;
    }

    // Vypíše maticu do konzoly (užitočné pri ladení)
    public void printMatica() {
        for (ArrayList<Integer> riadok : matica) {
            System.out.println(riadok);
        }
    }
}