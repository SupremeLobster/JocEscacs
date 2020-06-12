import javafx.application.Application;
import javafx.stage.FileChooser;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import java.io.File;

public class JocEscacs {

    /**
     * @pre \p args és fitxerConfiguracio [depth] [-g]
     * @post Executa un joc de dames amb N files i M columnes; amb l'opció -g s'executa en mode gràfic.
     */
    public static void main(String[] args) {
        try {
            if (args.length == 1) {
                EscacsText.juga(new Escacs(args[0], null));
                return;
            }
            else if (args.length == 2) {
                Integer depth = null;

                if(args[1].equals("-g")) {
                    Application.launch(EscacsGrafic.class, args);
                    return;
                }
                else {
                    try {
                        depth = Integer.parseInt(args[1]);
                        if(depth > 0) {
                            EscacsText.juga(new Escacs(args[0], depth));
                            return;
                        }
                    }
                    catch (NumberFormatException nfe) {
                        System.err.println(nfe);
                    }
                }
            }
            else if (args.length == 3 && args[2].equals("-g")) {
                Application.launch(EscacsGrafic.class, args);
                return;
            }
        }
        catch (Exception e) {
            System.out.println("S'ha produït una excepció.");
            e.printStackTrace();
        }
        System.out.println("\nUtilització: java -jar JocEscacs.jar [fitxerConfiguració] [depth] [-g]\nOn 8 <= N (files) <= 26 i 2 <= M (columnes) <= 26\nOPCIONAL: [depth] és un enter >0 que determina l'habilitat del jugador CPU. Com més, gran més bo és, però tradarà més a calcular cada tirada. Si no es fica, l'habilitat per defecte és 4.\nOPCIONAL: [-g] executa l'aplicació en mode gràfic.\n");
    }

}
