import com.google.gson.Gson;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** @class EscacsText
 @brief Juga als escacs en mode text
 */
public abstract class EscacsText {

    private static String s;

    /** @brief Juga als escacs preguntant les coordenades dels moviments. Surt amb 'X'.
     * @pre \p partida d'escacs en joc
     * @post Gestiona el menú
     * @param escacs Partida que s'esta jugant
     */
    public static void juga(Escacs escacs) throws IOException {
        Posicio origen;
        Posicio desti;
        int nF = escacs.files();
        int nC = escacs.columnes();
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);

        System.out.println("\nJOC D'ESCACS\n(X per sortir)\n");

        System.out.println(escacs);
        String tornColor = null;
        if(escacs.tornActual()==-1) tornColor="Blanques";
        else if(escacs.tornActual()==1) tornColor = "Negres";
        System.out.println("Torn: " + tornColor);

        do {
            origen = null;
            desti = null;
            System.out.println("Què vols fer? (TIRAR, DESFER, REFER, AJORNAR, CARREGAR, CPU, ENROCAR, TAULES, RENDIR, ACABAR)");

            if(s!=null) {
                if (!s.toUpperCase().equals("RENDIR")) s = br.readLine();
            }
            else s = br.readLine();

            switch (s.toUpperCase()) {
                case "TIRAR":
                    Tirada t = null;

                    if (escacs.esJugadorCPU(escacs.tornActual())) {
                        // Jugador CPU
                        t = escacs.millorTirada();
                    } else {
                        // Jugador HUMÀ
                        origen = llegirCoordenada("Coordenada origen (ex. a6): ", nF, nC);
                        if (origen != null) {
                            ArrayList<Tirada> tiradesPossibles = escacs.obtenirTiradesPossiblesFitxa(origen, escacs.tornActual());

                            for (Tirada ta : tiradesPossibles) { // Mostrar totes possibles les tirades
                                System.out.println(ta);
                            }

                            desti = llegirCoordenada("Coordenada destí  (ex. a6): ", nF, nC);
                            if (desti != null) {
                                Pair<Boolean, ArrayList<Pair<Posicio, Fitxa>>> r = escacs.moviment(origen, desti, escacs.tornActual());
                                if (r.first) {
                                    Fitxa auxF = (Fitxa) escacs.fitxa(origen.fila, origen.columna);
                                    //auxF es una referencia a la fitxa moguda en el tauler
                                    t = new Tirada(origen, desti, auxF, r.second, false);


                                } else
                                    System.out.println("\nMoviment incorrecte!");
                            }
                        }
                    }

                    if (t != null) { // Si hi ha tirada vàlida
                        //Aplica moviment
                        int resultatAplicar = escacs.aplicaMoviment(t, false);

                        if (escacs.esPotPromocionarFitxa(t.desti())) { // Si es pot promocionar la última fitxa moguda
                            ArrayList<Fitxa> aux = escacs.llistaTipusDeFitxes();

                            System.out.println("PROMOCIÓ!");

                            for (Fitxa f : aux) {
                                System.out.println(f.nom());
                            }

                            int resultatPromocionar = -1; // Inicialitzem a -1 per què entri al bucle almenys 1 vegada.
                            while(resultatPromocionar < 0) { // Mentre hi hagi error intentant promocionar
                                //Hem decidit guardar la fitxa promocionada com a text i la fitxa anteriror a la promocio tambe
                                System.out.println("Entra el nom de la nova fitxa:");
                                String nomFitxaNova = br.readLine();
                                t.setPromocio(true);
                                Fitxa fNova = escacs.obtenirTipusDeFitxa(nomFitxaNova);
                                t.setFitxaPromocionada(fNova.nom());
                                t.set_fitxaAnteriorPromocionar(escacs.fitxa(t.desti().fila, t.desti().columna).nom());

                                resultatPromocionar = escacs.promocionarFitxa(t.desti(), fNova);

                                processaResultatAplicar(resultatPromocionar);
                            }
                        }
                        else processaResultatAplicar(resultatAplicar);

                        System.out.println(escacs);
                        if (escacs.tornActual() == -1) tornColor = "Blanques";
                        else if (escacs.tornActual() == 1) tornColor = "Negres";
                        System.out.println("Torn: " + tornColor);
                    }
                    break;
                case "DESFER":
                    escacs.desfer();
                    System.out.println(escacs);
                    if (escacs.tornActual() == -1) tornColor = "Blanques";
                    else if (escacs.tornActual() == 1) tornColor = "Negres";
                    System.out.println("Torn: " + tornColor);
                    break;
                case "REFER":
                    escacs.refer();
                    System.out.println(escacs);
                    if (escacs.tornActual() == -1) tornColor = "Blanques";
                    else if (escacs.tornActual() == 1) tornColor = "Negres";
                    System.out.println("Torn: " + tornColor);
                    break;
                case "AJORNAR":
                    guardarEscacsText(escacs);
                    return;
                case "CARREGAR":
                    escacs = carregarEscacsText();
                    System.out.println(escacs);
                    if (escacs.tornActual() == -1) tornColor = "Blanques";
                    else if (escacs.tornActual() == 1) tornColor = "Negres";
                    System.out.println("Torn: " + tornColor);
                    break;
                case "CPU":
                    System.out.println("Quin jugador vols commutar? N: Negres | B: Blanques");

                    String jugador = br.readLine();
                    int color = 0;

                    if (jugador.toUpperCase().equals("N")) color = 1;
                    else if (jugador.toUpperCase().equals("B")) color = -1;

                    escacs.toggleCPU(color);
                    break;
                case "ENROCAR":
                    Posicio posA = llegirCoordenada("Coordenada fitxa A (ex. a6): ", nF, nC);

                    if (posA != null) {
                        Posicio posB = llegirCoordenada("Coordenada fitxa B  (ex. a6): ", nF, nC);

                        if (posB != null) {
                            Pair<Boolean, TiradaEnroc> pE = escacs.esEnrocValid(posA, posB);

                            if (pE.first) {
                                int resultatAplicar = escacs.aplicaMoviment(new Tirada(pE.second), false);
                                System.out.println(escacs);
                                if (escacs.tornActual() == -1) tornColor = "Blanques";
                                else if (escacs.tornActual() == 1) tornColor = "Negres";
                                System.out.println("Torn: " + tornColor);
                                processaResultatAplicar(resultatAplicar);
                            } else {
                                System.out.println("\nEnroc incorrecte!");
                            }
                        }
                    }
                    break;
                case "RENDIR":
                    escacs.rendirse();
                    String guanyador = null;

                    if (escacs.guanyador() == -1) guanyador = "Blanques";
                    else if (escacs.guanyador() == 1) guanyador = "Negres";

                    System.out.println("Les guanyadores són les " + guanyador);
                    return;
                case "TAULES":
                    System.out.println("Acceptes Taules? (Y/n)");
                    String resposta = br.readLine();

                    if (resposta.toUpperCase().equals("Y")) {
                        return;
                    } else {
                        System.out.println("Taules rebutjades");
                    }
                    break;
            }
        } while (!s.toUpperCase().equals("ACABAR"));
    }


    /** @brief Llegeix una coordenada
     @pre Cert
     @post Escriu el text  t  i llegeix cadenes del canal escacs'entrada,
     fins trobar "X" o un string de la forma CF, on C és una lletra minúscula de
     l'abecedari que ocupa una posició inferior o igual a nColumnes,
     i F és un enter entre 1 i nFiles; si s'ha trobat "X" es retorna null,
     altrament es retorna la Posicio corresponent a CF.
     */
    private static Posicio llegirCoordenada(String t, int nFiles, int nColumnes) throws IOException {
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);
        String c = "abcdefghijklmnopqrstuvwxyz";
        Posicio p = new Posicio(0,0);
        boolean valid = false;
        do {
            System.out.print(t);
            String s = br.readLine();
            if (s.equals("X")) {
                p = null;
                valid = true;
            }
            else if (s.length() >= 2) {
                p.columna = c.indexOf(s.charAt(0));
                if (p.columna != -1 && p.columna < nColumnes) {
                    try {
                        p.fila = Integer.parseInt(s.substring(1)) - 1;
                        if (p.fila >= 0 && p.fila < nFiles)
                            valid = true;
                        else
                            System.out.println("Fila fora de rang. Torna-hi...");
                    }
                    catch (NumberFormatException e) {
                        System.out.println("Format incorrecte. Torna-hi...");
                    }
                }
                else
                    System.out.println("Columna fora de rang. Torna-hi...");
            }
        } while (!valid);
        return p;
    }

    /**
     * @brief Carrega la partida d'un fitxer json a un Objecte de tipus Escacs
     * @pre \p true
     * @post retorna la partida guardada
     */
    private static Escacs  carregarEscacsText(){
        Escacs aux = null;
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);
        File file;
        String nom = null;
        System.out.println("Nom de la partida a carregar:(sense .json)");
        try (Stream<Path> walk = Files.walk(Paths.get("src/saves/"))) {

            List<String> result = walk.map(x -> x.toString())
                    .filter(f -> f.endsWith(".json")).collect(Collectors.toList());

            result.forEach(System.out::println);

        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            nom = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        file = new File(nom);

        try {
            aux = Escacs.carregarEscacs(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return aux;
    }
    /**
     * @brief Crea el fitxer on es guardarà la partida amb el nom que introduïm
     * @pre \p escacs partida en joc vàlida
     * @post Crea el fitxer on es guardarà la partida
     * @param escacs Partida en joc.
     */
    private static void guardarEscacsText(Escacs escacs){
        InputStreamReader isr = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(isr);
        String nom = null;

        System.out.println("Nom de la partida a guardar:");
        try {
            nom = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            escacs.crearFitxerGuardar(new File("src/saves/" + nom + ".json" ));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processaResultatAplicar(int resultatAplicar) {
        if(resultatAplicar != 0) { // Si s'ha donat algún cas especial en aplicar el moviment
            switch(resultatAplicar) {
                case -2: {
                    System.out.println("No es pot promocionar a REI!");
                    break;
                }
                case -1: {
                    System.out.println("Error");
                    break;
                }
                case 1: {
                    System.out.println("Taules per escac contiunat");
                    return;
                }
                case 2: {
                    System.out.println("Taules per inacció");
                    return;
                }
                case 3: {
                    System.out.println("Rei ofegat, fi de la partida");
                    return;
                }
                case 4: {
                    s = "RENDIR";
                    break;
                }
            }
        }
    }


}