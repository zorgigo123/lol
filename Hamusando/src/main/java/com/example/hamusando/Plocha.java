package com.example.hamusando;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Plocha {
    int velkost;
    ArrayList<Integer> zhoraDole;
    ArrayList<Integer> zlavaDoprava;
    ArrayList<ArrayList<Integer>> matica;
    ArrayList<Integer> korektneStlpce;
    ArrayList<Integer> korektneRiadky;

    public Plocha(String levelName) {


        try (FileReader reader = new FileReader(levelName);
             BufferedReader bufferedReader = new BufferedReader(reader)) {

            velkost = Integer.parseInt(bufferedReader.readLine());

            zhoraDole = new ArrayList<>();
            zlavaDoprava = new ArrayList<>();

            String riadok1 = bufferedReader.readLine();

            for(String hodnota : riadok1.split(",")){
                if(hodnota.isEmpty()){
                    zhoraDole.add(-1);
                }else{
                    zhoraDole.add(Integer.parseInt(hodnota));
                }
            }

            String riadok2 = bufferedReader.readLine();
            for(String hodnota : riadok2.split(",")){
                if(hodnota.isEmpty()){
                    zlavaDoprava.add(-1);
                }else{
                    zlavaDoprava.add(Integer.parseInt(hodnota));
                }
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        matica = new ArrayList<>();
        for (int y = 0; y < velkost; y++) {
            ArrayList<Integer> riadok = new ArrayList<>();
            for (int x = 0; x < velkost; x++) {
                riadok.add(0); // všetko prázdne na začiatku
            }
            matica.add(riadok);
        }
    }
    // Zistí či je aktuálna rozohratá pozícia stále riešiteľná
    boolean jeRiesitelna() {
        // uložíme kópiu matice pred backtrackingom
        ArrayList<ArrayList<Integer>> kopia = new ArrayList<>();
        for (ArrayList<Integer> riadok : matica) {
            kopia.add(new ArrayList<>(riadok));
        }

        boolean vysledok = backtrack(0, 0);

        // obnovíme pôvodnú maticu
        matica = kopia;

        return vysledok;
    }

    private boolean backtrack(int x, int y) {
        if (y == velkost) {
            kontrolaPravidiel();
            for (int r : korektneRiadky) {
                if (r == 0) return false;
            }
            for (int s : korektneStlpce) {
                if (s == 0) return false;
            }
            return true;
        }

        int novyX = (x + 1) % velkost;
        int novyY = (novyX == 0) ? y + 1 : y;

        if (matica.get(y).get(x) != 0) {
            return backtrack(novyX, novyY);
        }

        // skúsime dosadiť 1 (kruh)
        matica.get(y).set(x, 1);
        if (!jeZleVRiadku(y) && !jeZleVStlpci(x) && backtrack(novyX, novyY)) {
            return true;
        }

        // skúsime dosadiť 2 (štvorec)
        matica.get(y).set(x, 2);
        if (!jeZleVRiadku(y) && !jeZleVStlpci(x) && backtrack(novyX, novyY)) {
            return true;
        }

        matica.get(y).set(x, 0);
        return false;
    }

    // Skontroluje či riadok ešte môže byť korektný (nie je definitívne pokazený)
    private boolean jeZleVRiadku(int y) {
        ArrayList<Integer> riadok = matica.get(y);

        // spočítame štvorce v riadku
        int pocetStvorcov = 0;
        for (int hodnota : riadok) {
            if (hodnota == 2) pocetStvorcov++;
        }

        // viac ako 2 štvorce = definitívne zlé
        if (pocetStvorcov > 2) return true;

        // ak máme 2 štvorce, skontrolujeme počet kruhov medzi nimi
        if (pocetStvorcov == 2) {
            int pocetKruhov = spocitajKruhyMedziStvorcami(riadok);
            int pozadovany = zlavaDoprava.get(y);
            if (pozadovany != -1 && pocetKruhov != pozadovany) return true;
        }

        return false;
    }

    // Skontroluje či stĺpec ešte môže byť korektný
    private boolean jeZleVStlpci(int x) {
        ArrayList<Integer> stlpec = new ArrayList<>();
        for (int y = 0; y < velkost; y++) {
            stlpec.add(matica.get(y).get(x));
        }

        int pocetStvorcov = 0;
        for (int hodnota : stlpec) {
            if (hodnota == 2) pocetStvorcov++;
        }

        if (pocetStvorcov > 2) return true;

        if (pocetStvorcov == 2) {
            int pocetKruhov = spocitajKruhyMedziStvorcami(stlpec);
            int pozadovany = zhoraDole.get(x);
            if (pozadovany != -1 && pocetKruhov != pozadovany) return true;
        }

        return false;
    }
    void kontrolaPravidiel() {
        korektneRiadky = new ArrayList<>();
        korektneStlpce = new ArrayList<>();

        // --- kontrola RIADKOV ---
        for (int y = 0; y < velkost; y++) {
            ArrayList<Integer> riadok = matica.get(y);

            // spočítame štvorce v riadku
            int pocetStvorcov = 0;
            for (int hodnota : riadok) {
                if (hodnota == 2) pocetStvorcov++;
            }

            // musia byť presne 2 štvorce
            if (pocetStvorcov != 2) {
                korektneRiadky.add(0);
                continue;
            }

            // ak je počet štvorcov správny, skontrolujeme kruhy
            if (zlavaDoprava.get(y) == -1) {
                korektneRiadky.add(1); // neurčené = OK
                continue;
            }

            int pocetKruhov = spocitajKruhyMedziStvorcami(riadok);
            if (pocetKruhov == zlavaDoprava.get(y)) {
                korektneRiadky.add(1);
            } else {
                korektneRiadky.add(0);
            }
        }

        // --- kontrola STĹPCOV ---
        for (int x = 0; x < velkost; x++) {
            ArrayList<Integer> stlpec = new ArrayList<>();
            for (int y = 0; y < velkost; y++) {
                stlpec.add(matica.get(y).get(x));
            }

            // spočítame štvorce v stĺpci
            int pocetStvorcov = 0;
            for (int hodnota : stlpec) {
                if (hodnota == 2) pocetStvorcov++;
            }

            // musia byť presne 2 štvorce
            if (pocetStvorcov != 2) {
                korektneStlpce.add(0);
                continue;
            }

            if (zhoraDole.get(x) == -1) {
                korektneStlpce.add(1); // neurčené = OK
                continue;
            }

            int pocetKruhov = spocitajKruhyMedziStvorcami(stlpec);
            if (pocetKruhov == zhoraDole.get(x)) {
                korektneStlpce.add(1);
            } else {
                korektneStlpce.add(0);
            }
        }
    }

    // Skontroluje či sú všetky riadky a stĺpce korektné = výhra
    boolean skontrolujVsetky() {
        if (korektneRiadky == null || korektneStlpce == null) return false;
        for (int r : korektneRiadky) {
            if (r == 0) return false;
        }
        for (int s : korektneStlpce) {
            if (s == 0) return false;
        }
        return true;
    }

    // Spočíta kruhy (hodnota 1) ktoré sa nachádzajú MEDZI prvým a posledným štvorcom (hodnota 2)
// Ak nie sú práve 2 štvorce, vráti -1 (podmienka nemôže byť splnená)
    int spocitajKruhyMedziStvorcami(ArrayList<Integer> riadok) {
        int prvyStvore = -1;
        int poslednyStvore = -1;

        // nájdeme pozíciu prvého a posledného štvorca
        for (int i = 0; i < riadok.size(); i++) {
            if (riadok.get(i) == 2) {
                if (prvyStvore == -1) {
                    prvyStvore = i;
                }
                poslednyStvore = i;
            }
        }

        // ak nie sú práve 2 štvorce, podmienka nemôže byť splnená
        if (prvyStvore == -1 || prvyStvore == poslednyStvore) return -1;

        // spočítame kruhy medzi nimi
        int pocet = 0;
        for (int i = prvyStvore + 1; i < poslednyStvore; i++) {
            if (riadok.get(i) == 1) {
                pocet++;
            }
        }
        return pocet;
    }
}
