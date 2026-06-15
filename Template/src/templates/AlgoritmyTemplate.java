package templates;

import java.util.*;

/**
 * TEMPLATE SÚBOR - Najčastejšie algoritmy pre hry s mriežkou
 * ============================================================
 * Obsah:
 * 1. Flood Fill - nájdenie súvislých blokov
 * 2. BFS - najkratšia cesta z bodu A do bodu B
 * 3. DFS - prehľadávanie do hĺbky
 * 4. Backtracking - hľadanie riešenia hrubou silou
 * 5. Pomocné metódy - hranice, susedia, kópia matice
 */
public class AlgoritmyTemplate {

    // =========================================================
    // POMOCNÉ KONŠTANTY A METÓDY
    // =========================================================

    // 4 smery: hore, dole, vľavo, vpravo
    static int[][] SMERY_4 = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

    // 8 smerov: aj uhlopriečky
    static int[][] SMERY_8 = {
            {0, -1}, {0, 1}, {-1, 0}, {1, 0},
            {-1, -1}, {1, -1}, {-1, 1}, {1, 1}
    };

    // Skontroluje či sú súradnice vnútri matice
    static boolean jeVHranici(int x, int y, int[][] matica) {
        if (y < 0 || y >= matica.length) return false;
        if (x < 0 || x >= matica[0].length) return false;
        return true;
    }

    // Skontroluje či sú súradnice vnútri matice (ArrayList verzia)
    static boolean jeVHraniciList(int x, int y, ArrayList<ArrayList<Integer>> matica) {
        if (y < 0 || y >= matica.size()) return false;
        if (x < 0 || x >= matica.get(y).size()) return false;
        return true;
    }

    // Vytvorí hlbokú kópiu matice (int[][])
    static int[][] kopiaMatice(int[][] matica) {
        int[][] kopia = new int[matica.length][matica[0].length];
        for (int y = 0; y < matica.length; y++) {
            kopia[y] = matica[y].clone();
        }
        return kopia;
    }

    // Vytvorí hlbokú kópiu matice (ArrayList verzia)
    static ArrayList<ArrayList<Integer>> kopiaMaticeList(ArrayList<ArrayList<Integer>> matica) {
        ArrayList<ArrayList<Integer>> kopia = new ArrayList<>();
        for (ArrayList<Integer> riadok : matica) {
            kopia.add(new ArrayList<>(riadok));
        }
        return kopia;
    }

    // Vypíše maticu do konzoly
    static void printMatica(int[][] matica) {
        for (int[] riadok : matica) {
            System.out.println(Arrays.toString(riadok));
        }
    }


    // =========================================================
    // 1. FLOOD FILL - nájdenie súvislého bloku
    // =========================================================
    // Použitie: zistenie veľkosti súvislej oblasti rovnakej hodnoty
    // Príklad: v Pakoresan zistiť veľkosť bloku okolo štartovacieho bodu

    static int floodFill(int x, int y, int hladanaHodnota, boolean[][] navstivene, int[][] matica) {
        // zastavíme ak sme mimo hraníc
        if (!jeVHranici(x, y, matica)) return 0;
        // zastavíme ak sme tu už boli
        if (navstivene[y][x]) return 0;
        // zastavíme ak bunka nemá správnu hodnotu
        if (matica[y][x] != hladanaHodnota) return 0;

        // označíme bunku ako navštívenú
        navstivene[y][x] = true;
        int pocet = 1;

        // rekurzívne prejdeme do všetkých 4 smerov
        for (int[] smer : SMERY_4) {
            pocet += floodFill(x + smer[0], y + smer[1], hladanaHodnota, navstivene, matica);
        }

        return pocet;
    }

    // Ako zavolať flood fill od konkrétneho bodu:
    static void prikladFloodFill(int[][] matica) {
        boolean[][] navstivene = new boolean[matica.length][matica[0].length];
        int startX = 2, startY = 2;
        int hladanaHodnota = matica[startY][startX];

        int velkostBloku = floodFill(startX, startY, hladanaHodnota, navstivene, matica);
        System.out.println("Veľkosť bloku: " + velkostBloku);
    }

    // Nájde VŠETKY súvislé bloky v matici a vráti ich veľkosti
    static ArrayList<Integer> najdiVsetkyBloky(int[][] matica, int hladanaHodnota) {
        boolean[][] navstivene = new boolean[matica.length][matica[0].length];
        ArrayList<Integer> bloky = new ArrayList<>();

        for (int y = 0; y < matica.length; y++) {
            for (int x = 0; x < matica[0].length; x++) {
                if (matica[y][x] == hladanaHodnota && !navstivene[y][x]) {
                    int velkost = floodFill(x, y, hladanaHodnota, navstivene, matica);
                    bloky.add(velkost);
                }
            }
        }

        return bloky; // každé číslo = veľkosť jedného bloku
    }


    // =========================================================
    // 2. BFS - najkratšia cesta z bodu A do bodu B
    // =========================================================
    // Použitie: nájsť najkratšiu cestu medzi dvomi bunkami
    // Vráti dĺžku cesty, alebo -1 ak cesta neexistuje

    static int bfsNajkratsiaCesta(int startX, int startY, int cielX, int cielY, int[][] matica) {
        if (!jeVHranici(startX, startY, matica)) return -1;
        if (!jeVHranici(cielX, cielY, matica)) return -1;

        boolean[][] navstivene = new boolean[matica.length][matica[0].length];
        Queue<int[]> fronta = new LinkedList<>();

        // pridáme štart do fronty: [x, y, vzdialenosť]
        fronta.add(new int[]{startX, startY, 0});
        navstivene[startY][startX] = true;

        while (!fronta.isEmpty()) {
            int[] aktualny = fronta.poll();
            int x = aktualny[0];
            int y = aktualny[1];
            int vzdialenost = aktualny[2];

            // našli sme cieľ
            if (x == cielX && y == cielY) {
                return vzdialenost;
            }

            // pridáme všetkých nenavštívených susedov
            for (int[] smer : SMERY_4) {
                int nx = x + smer[0];
                int ny = y + smer[1];

                if (!jeVHranici(nx, ny, matica)) continue;
                if (navstivene[ny][nx]) continue;
                if (matica[ny][nx] != 0) continue; // 0 = priechodná bunka, uprav podľa potreby

                navstivene[ny][nx] = true;
                fronta.add(new int[]{nx, ny, vzdialenost + 1});
            }
        }

        return -1; // cesta neexistuje
    }

    // BFS ktorý vráti aj samotnú cestu (zoznam súradníc)
    static ArrayList<int[]> bfsCesta(int startX, int startY, int cielX, int cielY, int[][] matica) {
        boolean[][] navstivene = new boolean[matica.length][matica[0].length];
        // ukladáme aj odkiaľ sme prišli: predchodca[y][x] = {predX, predY}
        int[][][] predchodca = new int[matica.length][matica[0].length][2];
        for (int[][] riadok : predchodca) {
            for (int[] bunka : riadok) {
                bunka[0] = -1; // -1 znamená žiadny predchodca
                bunka[1] = -1;
            }
        }

        Queue<int[]> fronta = new LinkedList<>();
        fronta.add(new int[]{startX, startY});
        navstivene[startY][startX] = true;

        boolean nasiel = false;
        while (!fronta.isEmpty()) {
            int[] aktualny = fronta.poll();
            int x = aktualny[0];
            int y = aktualny[1];

            if (x == cielX && y == cielY) {
                nasiel = true;
                break;
            }

            for (int[] smer : SMERY_4) {
                int nx = x + smer[0];
                int ny = y + smer[1];

                if (!jeVHranici(nx, ny, matica)) continue;
                if (navstivene[ny][nx]) continue;
                if (matica[ny][nx] != 0) continue;

                navstivene[ny][nx] = true;
                predchodca[ny][nx][0] = x;
                predchodca[ny][nx][1] = y;
                fronta.add(new int[]{nx, ny});
            }
        }

        if (!nasiel) return null; // cesta neexistuje

        // rekonštrukcia cesty od cieľa po štart
        ArrayList<int[]> cesta = new ArrayList<>();
        int x = cielX, y = cielY;
        while (x != -1 && y != -1) {
            cesta.add(new int[]{x, y});
            int predX = predchodca[y][x][0];
            int predY = predchodca[y][x][1];
            x = predX;
            y = predY;
        }

        Collections.reverse(cesta); // otočíme od štartu po cieľ
        return cesta;
    }


    // =========================================================
    // 3. DFS - prehľadávanie do hĺbky
    // =========================================================
    // Použitie: zistenie či existuje cesta, detekcia cyklov

    static boolean dfsExistujeCesta(int x, int y, int cielX, int cielY,
                                    boolean[][] navstivene, int[][] matica) {
        if (!jeVHranici(x, y, matica)) return false;
        if (navstivene[y][x]) return false;
        if (matica[y][x] != 0) return false; // neprechodzná bunka

        if (x == cielX && y == cielY) return true; // našli sme cieľ

        navstivene[y][x] = true;

        for (int[] smer : SMERY_4) {
            if (dfsExistujeCesta(x + smer[0], y + smer[1], cielX, cielY, navstivene, matica)) {
                return true;
            }
        }

        return false;
    }

    // Ako zavolať DFS:
    static void prikladDFS(int[][] matica) {
        boolean[][] navstivene = new boolean[matica.length][matica[0].length];
        boolean existuje = dfsExistujeCesta(0, 0, 4, 4, navstivene, matica);
        System.out.println("Cesta existuje: " + existuje);
    }


    // =========================================================
    // 4. BACKTRACKING - hľadanie riešenia hrubou silou
    // =========================================================
    // Použitie: zistenie či je puzzle riešiteľný, generovanie riešení
    // Kľúč: skúšame všetky možnosti, pri slepej uličke sa vrátime späť

    // Príklad: zaplň maticu hodnotami 1-3 tak aby žiadne dve rovnaké
    // neboli vedľa seba (zjednodušený príklad štruktúry)
    static boolean backtrack(int x, int y, int[][] matica) {
        int rows = matica.length;
        int cols = matica[0].length;

        // ak sme prešli všetky riadky, našli sme riešenie
        if (y == rows) {
            return true;
        }

        // posun na ďalšiu bunku
        int novyX = (x + 1) % cols;
        int novyY = (novyX == 0) ? y + 1 : y;

        // ak bunka nie je prázdna, preskočíme ju
        if (matica[y][x] != 0) {
            return backtrack(novyX, novyY, matica);
        }

        // skúšame všetky možné hodnoty
        for (int hodnota = 1; hodnota <= 3; hodnota++) {
            matica[y][x] = hodnota;

            // PRUNING - skontrolujeme či aktuálny stav ešte môže viesť k riešeniu
            if (jeValidny(x, y, matica)) {
                if (backtrack(novyX, novyY, matica)) {
                    return true;
                }
            }

            // táto hodnota nefunguje, skúsime ďalšiu
            matica[y][x] = 0;
        }

        return false; // žiadna hodnota nefunguje
    }

    // Pomocná funkcia pre backtracking - skontroluje či je stav validný
    // UPRAV podľa pravidiel tvojej hry
    static boolean jeValidny(int x, int y, int[][] matica) {
        int hodnota = matica[y][x];

        // skontrolujeme či susedia nemajú rovnakú hodnotu
        for (int[] smer : SMERY_4) {
            int nx = x + smer[0];
            int ny = y + smer[1];
            if (!jeVHranici(nx, ny, matica)) continue;
            if (matica[ny][nx] == hodnota) return false;
        }

        return true;
    }

    // Backtracking s uložením a obnovením stavu
    // Použitie: keď potrebuješ testovať riešiteľnosť bez zmeny pôvodnej matice
    static boolean jeRiesitelna(int[][] matica) {
        // uložíme kópiu pred backtrackingom
        int[][] kopia = kopiaMatice(matica);

        boolean vysledok = backtrack(0, 0, matica);

        // obnovíme pôvodnú maticu
        for (int y = 0; y < matica.length; y++) {
            matica[y] = kopia[y].clone();
        }

        return vysledok;
    }


    // =========================================================
    // PRÍKLAD POUŽITIA
    // =========================================================
    public static void main(String[] args) {
        int[][] matica = {
                {0, 0, 0, 0, 0},
                {0, 1, 1, 0, 0},
                {0, 1, 0, 0, 0},
                {0, 0, 0, 1, 0},
                {0, 0, 0, 0, 0}
        };

        System.out.println("=== FLOOD FILL ===");
        ArrayList<Integer> bloky = najdiVsetkyBloky(matica, 1);
        System.out.println("Bloky hodnoty 1: " + bloky);

        System.out.println("\n=== BFS NAJKRATSIA CESTA ===");
        int dlzka = bfsNajkratsiaCesta(0, 0, 4, 4, matica);
        System.out.println("Dĺžka cesty: " + dlzka);

        ArrayList<int[]> cesta = bfsCesta(0, 0, 4, 4, matica);
        if (cesta != null) {
            System.out.print("Cesta: ");
            for (int[] bod : cesta) {
                System.out.print("[" + bod[0] + "," + bod[1] + "] ");
            }
            System.out.println();
        }

        System.out.println("\n=== DFS EXISTUJE CESTA ===");
        boolean[][] navstivene = new boolean[matica.length][matica[0].length];
        boolean existuje = dfsExistujeCesta(0, 0, 4, 4, navstivene, matica);
        System.out.println("Cesta existuje: " + existuje);
    }
}