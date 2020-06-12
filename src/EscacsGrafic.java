import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.*;
import sun.util.resources.cldr.to.CurrencyNames_to;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import static java.lang.System.exit;


public class EscacsGrafic extends  Application{
    //Variables
    private Escacs _escacs;         /// Joc escacs

    private int _pixelsCasellaX;
    private int _pixelsCasellaY;
    private Group _caselles = new Group();        /// Grup de caselles
    private Group _casellesValides = new Group();        /// Grup de caselles
    private Group _fitxes =  new Group();          /// Grup de fitxes
    private Group _panell = new Group();          /// Grup Elements Panell
    private Group _promocio = new Group();        /// Grup Elements Promoció una fitxa
    private Group _taules = new Group();          /// Grup Elements solicitar taules
    private Scene _scene;
    private Double _panellValorPosicio = 0.20;          /// Ocupa un % de la finestra
    private FileChooser _fileChooser;
    private String _configFile;
    private Semaphore _mutex;
    private int _estatEnroc; /// 0: Enroc no activat, 1: Enroc selecciona peça 1, 2: Enroc selecciona peça 2
    private Pair<Posicio, Posicio> _fitxesEnroc;
    private Boolean _pucJugar; ///Indica a la CPU si pots jugar el seu torn
    /**
     @pre \p args és N M -g on 8 <= N <= 26 i 2 <= M <= 26
     @post S'executa un joc de dames amb N files i M columnes en mode gràfic.
     */
    public static void main(String[] args) {
        launch(args); // Crida init + start
    }

    /** @brief Configura events de teclat i de canvi de mida, així com les propietats de la finestra
     @pre Finestra i events configurats correctament.
     @post La partida s'ha creat y esta preperada per poder-se jugar.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        _scene = new Scene(crearContingut());
        _scene.setFill(Color.BLACK);
        primaryStage.setTitle("JOC D'ESCACAS"); // títol
        primaryStage.setScene(_scene);          // escena

        InputStream is_icon = getClass().getResourceAsStream("img/icon.png");
        primaryStage.getIcons().add(new Image(is_icon));

        primaryStage.show();                   // fem visible

        _scene.setOnKeyPressed((KeyEvent k) -> {
            KeyCode kc = k.getCode();
            if(kc.equals(KeyCode.Z)) {
                desferGrafic();
            }
            else if(kc.equals(KeyCode.Y)) {
                referGrafic();
            }
            else if(kc.equals(KeyCode.S)) {
                guardarEscacsGrafic(primaryStage);
            }else if(kc.equals(KeyCode.O)) {
                carregarEscacsGrafic(primaryStage);
            }else if(kc.equals((KeyCode.B))){
                //Activa CPU per les negres
                _escacs.toggleCPU(1);
                crearThreadCPU();
            }
            else if(kc.equals(KeyCode.W)){
                //Activa CPU per les negres
                _escacs.toggleCPU(-1);
                crearThreadCPU();
            }
            else if(kc.equals(KeyCode.T)){
                if(_taules.getChildren().size() <= 0) {
                    _taules.getChildren().add(new TaulesGrafica(((Double) _scene.getWidth()).intValue(), ((Double) _scene.getHeight()).intValue(), this)); //Com que ja s'ha aplicat el torn el tenim que invertir
                }
            }
        });
        _scene.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (newValue.intValue() > 10) {
                    int pixelsCasellaXAnteriors = _pixelsCasellaX;

                    _pixelsCasellaX = ((Double) (newValue.intValue() * (1 - _panellValorPosicio) / _escacs.columnes())).intValue();

                    //Modifiquem mida caselles
                    for (int i = 0; i < _caselles.getChildren().size(); i++) {

                        Casella aux = (Casella) _caselles.getChildren().get(i);

                        aux.setFitWidth(_pixelsCasellaX);

                        aux.setX((aux.getX() / pixelsCasellaXAnteriors) * _pixelsCasellaX);
                    }
                    //Modifiquem mida fitxes gràfiques
                    for (int i = 0; i < _fitxes.getChildren().size(); i++) {
                        FitxaGrafica aux = (FitxaGrafica) _fitxes.getChildren().get(i);

                        aux.actualitzaX(_pixelsCasellaX);
                    }

                    //Posiciona el panell a la nova posició respecte les X
                    ((Panell) _panell.getChildren().get(0)).modificaAmplada(newValue.doubleValue() * _panellValorPosicio);
                    ((Panell) _panell.getChildren().get(0)).relocate(newValue.doubleValue() * (1 - _panellValorPosicio), 0);

                    //Posicion la promicio a la nova posició respecte les y
                    if (_promocio.getChildren().size() > 0) {
                        ((PromocioGrafica) _promocio.getChildren().get(0)).modificaAmplada(_scene.getWidth());
                    }
                    //Posicion la promicio a la nova posició respecte les y
                    if (_taules.getChildren().size() > 0) {
                        ((TaulesGrafica) _taules.getChildren().get(0)).modificaAmplada(_scene.getWidth());
                    }
                }
            }
        });
        _scene.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if (newValue.intValue() > 10) {
                    int pixelsCasellaYAnteriors = _pixelsCasellaY;
                    _pixelsCasellaY = ((Double) (newValue.doubleValue() / _escacs.files())).intValue();

                    //Modifiquem mida caselles
                    for (int i = 0; i < _caselles.getChildren().size(); i++) {

                        Casella aux = (Casella) _caselles.getChildren().get(i);

                        aux.setFitHeight(_pixelsCasellaY);

                        aux.setY((aux.getY() / pixelsCasellaYAnteriors) * _pixelsCasellaY);
                    }

                    //Modifiquem mida fitxes gràfiques
                    for (int i = 0; i < _fitxes.getChildren().size(); i++) {
                        FitxaGrafica aux = (FitxaGrafica) _fitxes.getChildren().get(i);

                        aux.actualitzaY(_pixelsCasellaY);
                    }

                    //Posiciona el panell a la nova posició respecte les Y
                    ((Panell) _panell.getChildren().get(0)).modificaAlcada(newValue.doubleValue() * 1);

                    //Posicion la promicio a la nova posició respecte les y
                    if (_promocio.getChildren().size() > 0) {
                        ((PromocioGrafica) _promocio.getChildren().get(0)).modificaAlcada(_scene.getHeight());
                    }
                    //Posicion la promicio a la nova posició respecte les y
                    if (_taules.getChildren().size() > 0) {
                        ((TaulesGrafica) _taules.getChildren().get(0)).modificaAlcada(_scene.getHeight());
                    }
                }
            }
        });
    }

    /** @brief Inicialitza elements escacs grafic.
     @pre Cap.
     @post Escacs grafics inicialitzat.
     */
    @Override
    public void init() throws Exception {
        super.init();
        Parameters parameters = getParameters();
        List<String> rawArguments = parameters.getRaw(); // repesquem els paràmetres de la línia de comanda
        _configFile = rawArguments.get(0);
        Integer depth = null;

        if(rawArguments.size()>1) {
            if (!rawArguments.get(1).equals("-g")) {
                try {
                    depth = Integer.parseInt(rawArguments.get(1));
                    if(depth<=0) {
                        System.out.println("\nUtilització: java -jar JocEscacsGràfic.jar [fitxerConfiguració] [depth]\nOn 8 <= N (files) <= 26 i 2 <= M (columnes) <= 26\nOPCIONAL: [depth] és un enter >0 que determina l'habilitat del jugador CPU. Com més gran, més bo és, però tradarà més a calcular cada tirada. Si no s'especifica, l'habilitat per defecte és 4.\nOPCIONAL: [-g] executa l'aplicació en mode gràfic.\n");
                        exit(-1);
                    }
                } catch (NumberFormatException nfe) {
                    System.err.println(nfe);
                    System.out.println("\nUtilització: java -jar JocEscacsGràfic.jar [fitxerConfiguració] [depth]\nOn 8 <= N (files) <= 26 i 2 <= M (columnes) <= 26\nOPCIONAL: [depth] és un enter >0 que determina l'habilitat del jugador CPU. Com més gran, més bo és, però tradarà més a calcular cada tirada. Si no s'especifica, l'habilitat per defecte és 4.\nOPCIONAL: [-g] executa l'aplicació en mode gràfic.\n");
                    exit(-1);
                }
            }
        }

        Rectangle2D r = Screen.getPrimary().getVisualBounds();
        _escacs = new Escacs(_configFile, depth);
        _pixelsCasellaX = Math.min((int)r.getHeight()/_escacs.files(), (int)r.getWidth()/_escacs.columnes()) - 10;
        _pixelsCasellaY = _pixelsCasellaX; //Perque sigui cuadrat al inici;
        _fileChooser = new FileChooser();
        _mutex = new Semaphore(1);
        _estatEnroc = 0; // Enroc parat
        _fitxesEnroc = new Pair<>(null, null);
        _pucJugar = true;
    }

    /** @brief Crea contingut de la partida inicial d'escacs.
     @pre Partida inicial carregada a la variable _escacs.
     @post La partida s'ha creat y esta preperada per poder-se jugar.
     @return Parent.
     */
    private Parent crearContingut(){
        Image imatgeB = null; // imatge que usarem de fons a cada casella blanca del tauler (rajola)
        Image imatgeN = null; // imatge que usarem de fons a cada casella negra del tauler (rajola)

        InputStream is_fonsB = getClass().getResourceAsStream("src/img/fonsB.png");
        InputStream is_fonsN = getClass().getResourceAsStream("src/img/fonsN.png");

        imatgeB = new Image(is_fonsB);
        imatgeN = new Image(is_fonsN);

        Pane root = new Pane();
        Double width = _escacs.columnes() * _pixelsCasellaX + (_escacs.columnes() * _pixelsCasellaX)* _panellValorPosicio;

        //Recalculem la mida de les caselles en tenint en compte el tauler
        _pixelsCasellaX = ((Double)(width*(1-_panellValorPosicio) / _escacs.columnes())).intValue();
        _pixelsCasellaY = _pixelsCasellaX;


        root.setPrefSize(width, _escacs.files() * _pixelsCasellaY);
        root.getChildren().addAll(_caselles, _casellesValides, _panell, _fitxes, _promocio, _taules); // podem afegir els grups encara que estiguin buits
        for (int i = 0; i < _escacs.files(); ++i) {

            for (int j = 0; j < _escacs.columnes(); ++j) {
                Casella casella;
                if( (i+j)%2 == 0 ) casella = new Casella(imatgeB, _pixelsCasellaX, _pixelsCasellaY);
                else casella = new Casella(imatgeN, _pixelsCasellaX, _pixelsCasellaY);

                casella.setX(j * _pixelsCasellaX);
                casella.setY(i * _pixelsCasellaY);
                _caselles.getChildren().add(casella); // els elements es visualitzaran en l'ordre en què s'afegeixin
                Fitxa f = _escacs.fitxa(i,j);
                if (f != null) { // a la rajola hi ha alguna fitxa
                    FitxaGrafica fitxa = creaFitxa(f,j,i);
                    _fitxes.getChildren().add(fitxa);
                }
            }
        }
        _panell.getChildren().add(new Panell(((Double) (width * _panellValorPosicio)).intValue(),_escacs.files() * _pixelsCasellaY, ((Double)(width *  (1-_panellValorPosicio))).intValue(), 0, _escacs.tornActual(), this));

        return root;
    }

    /** @brief Crea una fitxa grafica.
     @pre Fitxa vàlida i fila i columna on situar.
     @post Crea una fitxa grafica
     @param fitxa Tipus de fitxa
     @param x columna del tauler
     @param y fila del tauler
     @return Una FitxaGrafica amb els gestors d'esdeveniments que permeten
     moviments legals fets amb el ratolí i indicar posicions vàlides
     */
    private FitxaGrafica creaFitxa(Fitxa fitxa, int x, int y) {
        FitxaGrafica ret = new FitxaGrafica(fitxa, _pixelsCasellaX, _pixelsCasellaY, x, y, true, true); // fitxa amb event handlers per a "click" i "drag"


        ret.setOnMouseEntered((MouseEvent e) -> {
            Pair<Integer, Integer> aux;
            ArrayList<Tirada> posicionsPossibles = null;

            aux = posTauler(ret.getLayoutX(), ret.getLayoutY());

            posicionsPossibles = _escacs.obtenirTiradesPossiblesFitxa(new Posicio(aux.second, aux.first), _escacs.tornActual());

            Image imatgeB_blue = null; // imatge que usarem de fons a cada casella blanca del tauler (rajola)
            Image imatgeN_blue = null; // imatge que usarem de fons a cada casella negra del tauler (rajola)

            InputStream is_fonsB_blue = getClass().getResourceAsStream("img/fonsB_blue.png");
            InputStream is_fonsN_blue = getClass().getResourceAsStream("img/fonsN_blue.png");

            imatgeB_blue = new Image(is_fonsB_blue);
            imatgeN_blue = new Image(is_fonsN_blue);

            for(Tirada t : posicionsPossibles) {

                Casella casella;
                if( (t.desti().fila+t.desti().columna)%2 == 0 ) casella = new Casella(imatgeB_blue, _pixelsCasellaX, _pixelsCasellaY);
                else casella = new Casella(imatgeN_blue, _pixelsCasellaX, _pixelsCasellaY);

                casella.setX(t.desti().columna * _pixelsCasellaX);
                casella.setY(t.desti().fila * _pixelsCasellaY);
                _casellesValides.getChildren().add(casella); // els elements es visualitzaran en l'ordre en què s'afegeixin
            }



            if(_estatEnroc != 0){
                DropShadow shadow = new DropShadow();
                ret.setEffect(shadow);
            }
        });

        ret.setOnMouseExited((MouseEvent e) -> {
            // Sembla que hi ha un bug del JDK [https://bugs.openjdk.java.net/browse/JDK-8087752]
            // que dona errors si no es desactiva el dirtyopts [-Dprism.dirtyopts=false]
            // quan s'executa el java.
            netejarCasellesValides();

            ret.setEffect(null);
        });

        ret.setOnMouseReleased((MouseEvent e) -> { // Li afegim event handler per a "release"

            // Sembla que hi ha un bug del JDK [https://bugs.openjdk.java.net/browse/JDK-8087752]
            // que dona errors si no es desactiva el dirtyopts [-Dprism.dirtyopts=false]
            // quan s'executa el java.
            netejarCasellesValides();

            //Gestio de seleccionar fitxes per enrocar
            if(_estatEnroc != 0) {

                if (_estatEnroc == 1) {
                    _fitxesEnroc.first = new Posicio(ret.actualY(), ret.actualX());
                }
                else if (_estatEnroc == 2) {
                    _fitxesEnroc.second = new Posicio(ret.actualY(), ret.actualX());
                }
                enrocarGrafic(false);
            }

            ret.setEffect(null);

            Pair<Integer, Integer> aux;

            aux = posTauler(ret.getLayoutX(), ret.getLayoutY());
            int newX = aux.first; // getLayout(X|Y) retorna la translació aplicada al node
            int newY = aux.second; // posTauler transforma la coordenada pantalla a coord. tauler
            Posicio pNew = new Posicio(newY, newX);

            aux = posTauler(ret.oldX(), ret.oldY());
            int oldX = aux.first;
            int oldY = aux.second;
            Posicio pOld = new Posicio(oldY, oldX);
            Pair<Boolean,ArrayList<Pair<Posicio,Fitxa>>> p = _escacs.moviment(pOld, pNew, _escacs.tornActual()); // preguntem als escacs si es pot fer el moviment proposat amb el mouse
            if (p.first) {

                Tirada t = new Tirada(pOld, pNew, ret.fitxa(), p.second,false);
                int resultatAplicar = aplicaMoviment(t, ret, null);

                if(_escacs.esPotPromocionarFitxa(pNew)){
                    // create a label
                    // Si es fa promoció, el promocionar ja processarà el resultat.
                    _promocio.getChildren().add(new PromocioGrafica(((Double)_scene.getWidth()).intValue(), ((Double)_scene.getHeight()).intValue(), _escacs.llistaTipusDeFitxes(), _escacs.tornActual() * -1, this, pNew, t)); //Com que ja s'ha aplicat el torn el tenim que invertir
                }
                else processaResultatAplicar(resultatAplicar);

                //Avisa a la CPU si li toca
                if(_escacs.esJugadorCPU(_escacs.tornActual())){

                    new Thread(() -> {
                        try {
                            TimeUnit.MILLISECONDS.sleep(100);
                            jugarTornCPU();
                        } catch(Exception err) {
                            System.err.println(err);
                        }
                        try {
                            stop();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }).start();
                }
            }
			else ret.abortMove();
        });

        return ret;
    }

    /** @brief Retorna la fila i columna del tauler a partir dels pixels de la pantalla.
     @pre pixelX i pixelY dins de rang .
     @post Retorna dos enters corresponents a la fila i columna del tauler.
     @param pixelX pixel de pantalla en el eix de les X.
     @param pixelY de pantalla en el eix de les X.
     @return Pair<Integer,Integer> correspon a la fila i columna a nivell logic.
     */
    private Pair<Integer, Integer> posTauler(double pixelX, double pixelY) {
        return new Pair<Integer, Integer>((int)(pixelX + _pixelsCasellaX / 2) / _pixelsCasellaX, ((int)(pixelY + _pixelsCasellaY / 2) / _pixelsCasellaY));
    }

    /** @brief Realitza una tirada a nivell grafic i logic.
     @pre Tirada vàlida i fitxa gràfica present en el tauler.
     @post Moviment aplicat correctament.
     @param t tirada a aplicar.
     @param fitxaG1 fitxa 1 a moure.
     @param fitxaG2 fitxa 2 a moure (cas d'enrocs).
     */
    private int aplicaMoviment(Tirada t, FitxaGrafica fitxaG1, FitxaGrafica fitxaG2)  {

        if(t.esEnroc()) {
            fitxaG1.moveCasella(t.tiradaEnroc().posicioFitxaA_desti().columna, t.tiradaEnroc().posicioFitxaA_desti().fila);       // moviment a nivell gràfic
            fitxaG2.moveCasella(t.tiradaEnroc().posicioFitxaB_desti().columna, t.tiradaEnroc().posicioFitxaB_desti().fila);       // moviment a nivell gràfic
        }
        else {
            if (t.mortes() != null) {
                eliminaFitxesGrafiques(t.mortes());
            }
            fitxaG1.moveCasella(t.desti().columna, t.desti().fila);       // moviment a nivell gràfic
        }

        int res = _escacs.aplicaMoviment(t, false); // moviment a nivell lògic


        actualitzaTextTornPanell();

        return res;
    }

    /** @brief Elimina una fitxa gràfica.
     @pre \p p correspon a una posició vàlida del tauler.
     @post La fitxa gràfica corresponent a \p p s'ha eliminat.
     @param mortes Llista de fitxes a eliminar del tauler.
     */
    private void eliminaFitxesGrafiques(ArrayList<Pair<Posicio, Fitxa>> mortes) {

        for(Pair<Posicio, Fitxa> p: mortes) {
            FitxaGrafica f = null;

            for(Node n : _fitxes.getChildren()) {
                f = (FitxaGrafica) n;
                Pair<Integer, Integer> aux = posTauler(f.oldX(), f.oldY());
                if (aux.first == p.first.columna && aux.second == p.first.fila) break;
            }

            _fitxes.getChildren().remove(f);
        }
    }


    /** @brief Actualitza el text del torn al qui toca jugar
    @pre Cap.
    @post Actualitza el text del torn actual, si era blanques ara és negres i viceversa.
    */
    private void actualitzaTextTornPanell(){
            ((Panell) _panell.getChildren().get(0)).actualitzaTorn(_escacs.tornActual());
    }


    /** @brief Desfar una tirada a nivell grafic.
     @pre Tirada anterior a l'actual.
     @post Retorna l'estat de la partida a una anterior.
     */
    private void desferGrafic() {

        Tirada t = _escacs.desfer();

        if(t != null) {
            if(t.esEnroc()) {
                FitxaGrafica fG_moguda1 = buscarFitxaGrafica(t.tiradaEnroc().posicioFitxaA_desti());
                FitxaGrafica fG_moguda2 = buscarFitxaGrafica(t.tiradaEnroc().posicioFitxaB_desti());

                if(fG_moguda1 != null && fG_moguda2 != null) {
                    fG_moguda1.moveCasella(t.tiradaEnroc().posicioFitxaA_origen().columna, t.tiradaEnroc().posicioFitxaA_origen().fila);
                    fG_moguda2.moveCasella(t.tiradaEnroc().posicioFitxaB_origen().columna, t.tiradaEnroc().posicioFitxaB_origen().fila);
                }
                activarEnrocs();
            }
            else {
                if (t.mortes() != null) {
                    for (Pair<Posicio, Fitxa> p : t.mortes()) {
                        FitxaGrafica fG = creaFitxa(p.second, p.first.columna, p.first.fila);
                        _fitxes.getChildren().add(fG);
                    }
                }

                FitxaGrafica fG_moguda = buscarFitxaGrafica(new Posicio(t.desti().fila, t.desti().columna));

                if (fG_moguda != null) {
                    fG_moguda.moveCasella(t.origen().columna, t.origen().fila);
                    if (t.shaPromocionat()) {
                        fG_moguda.actualitzaImatge();
                    }
                }
            }

            actualitzaTextTornPanell();

            //Desectiva joc CPU
            ((Panell)_panell.getChildren().get(0)).desectivarCPUs();
            _escacs.desectivarCPUs();
        }
    }

    /** @brief Refa una tirada a nivell gràfic.
     @pre Tirada posterior a l'actual.
     @post Retorna l'estat de la partida a una posterior.
     */
    private void referGrafic() {
        Tirada t = _escacs.refer();

        if (t != null) {
            if(t.esEnroc()) {
                FitxaGrafica fG1 = buscarFitxaGrafica(t.tiradaEnroc().posicioFitxaA_origen());
                FitxaGrafica fG2 = buscarFitxaGrafica(t.tiradaEnroc().posicioFitxaB_origen());

                if(fG1!=null && fG2!=null) {
                    fG1.moveCasella(t.tiradaEnroc().posicioFitxaA_desti().columna, t.tiradaEnroc().posicioFitxaA_desti().fila);
                    fG2.moveCasella(t.tiradaEnroc().posicioFitxaB_desti().columna, t.tiradaEnroc().posicioFitxaB_desti().fila);

                    actualitzaTextTornPanell();
                }
            }
            else {
                FitxaGrafica aux = buscarFitxaGrafica(new Posicio(t.origen().fila, t.origen().columna));

                if (aux != null) {
                    if (t.mortes() != null) eliminaFitxesGrafiques(t.mortes());

                    aux.moveCasella(t.desti().columna, t.desti().fila);
                    if (t.shaPromocionat()) {
                        aux.actualitzaImatge();
                    }

                    actualitzaTextTornPanell();
                }
            }
        }
    }

    /** @brief Carrega una partida guardada.
     @pre primaryStatge Principal vàlid.
     @post Obre dialag de carregar partida i carrega la partida.
     @param primaryStage Statge on està el tauler.
     */
    private void carregarEscacsGrafic(Stage primaryStage){
        _fileChooser.setTitle("Carregar partida");
        _fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JSON", "*.json"));
        _fileChooser.setInitialDirectory(
                new File("src/saves")
        );
        File file = _fileChooser.showOpenDialog(primaryStage);

        if (file == null) System.err.println("error en obtenir el fitxer de partida");
        else {
            try {
                _escacs = Escacs.carregarEscacs(file);
                netejarFitxes();
                carregarFitxes();
                actualitzaTextTornPanell();
                enrocarGrafic(true);
                activarEnrocs();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** @brief Guarda una partida en curs.
     @pre primaryStatge Principal vàlid.
     @post Obre dialag de guardar partida i guarda la partida.
     @param primaryStage Statge on està el tauler.
     */
    private void guardarEscacsGrafic(Stage primaryStage){
        _fileChooser.setTitle("Guardar partida");
        _fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JSON", "*.json"));
        _fileChooser.setInitialDirectory(
                new File("src/saves")
        );
        File file = _fileChooser.showSaveDialog(primaryStage);
        if (file == null) System.err.println("error en guardar la partida");
        else {
            try {
                _escacs.crearFitxerGuardar(file);
            } catch (IOException e) {
                e.printStackTrace();
            }

            _escacs.ajornarPartida();
        }

    }

    /** @brief Elimina totes les fitxes presents.
     @pre Cap.
     @post Totes les fitxes del tauler han estat eliminades
     */
    private void netejarFitxes(){
        _fitxes.getChildren().clear();
    }

    /** @brief Elimina les caselles vàlides mostrades.
     @pre Cap.
     @post Totes les caselles vàlides del tauler han estat eliminades
     */
    private void netejarCasellesValides(){
        _casellesValides.getChildren().clear();
    }

    /** @brief Carrega les fitxes carregades en la variable _escacs
     @pre Cap.
     @post carrega les fitxes de la partida
     */
    private  void carregarFitxes(){
        ArrayList<Pair<Fitxa,Posicio>> fitxesPos = _escacs.obtenirFitxes();
        for(Pair<Fitxa, Posicio> p : fitxesPos) {
            FitxaGrafica fitxa = creaFitxa(p.first, p.second.columna, p.second.fila);
            _fitxes.getChildren().add(fitxa);
        }
    }

    /** @brief La CPU simula una tirada i l'aplica graficament
     @pre Cap.
     @post Tirada aplicada a nivell logic i gràfic
     */
    private void jugarTornCPU(){
        //IA
        try {
            _mutex.acquire();
            // Avoid throwing IllegalStateException by running from a non-JavaFX thread.
            Platform.runLater(
                    () -> {
                        Tirada t = _escacs.millorTirada();

                        if(t != null) {
                            FitxaGrafica fG_aMoure = buscarFitxaGrafica(t.origen());

                            if (fG_aMoure != null) {
                                int resultatAplicar = aplicaMoviment(t, fG_aMoure, null);

                                processaResultatAplicar(resultatAplicar);
                            }
                        }

                        if(_escacs.esJugadorCPU(_escacs.tornActual())){
                            new Thread(() -> {
                                try {
                                    TimeUnit.MILLISECONDS.sleep(100);
                                    jugarTornCPU();
                                } catch(Exception err) {
                                    System.err.println(err);
                                }
                                try {
                                    stop();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }).start();
                        }
                    }
            );
            _mutex.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /** @brief Promiciona una fitxa
     @pre novaFitxa vàlida, posicio on es troba la ftixa a promocionar, tirada vàlida.
     @post Les propietats de la fitxa promocionada han cambiat
     */
    public void ferPromocio(Fitxa novaFitxa, Posicio posicio, Tirada tirada){
        _promocio.getChildren().clear();
        FitxaGrafica fg = buscarFitxaGrafica(posicio);
        tirada.set_fitxaAnteriorPromocionar(fg.fitxa().nom());
        int resultatPromocio = _escacs.promocionarFitxa(tirada.desti(), novaFitxa);
        tirada.setFitxaPromocionada(novaFitxa.nom());
        tirada.setPromocio(true);
        fg.actualitzaImatge();

        processaResultatAplicar(resultatPromocio);
    }

    /** @brief Busca una fitxa grafica en el tauler a partir de la sea posició.
     @pre Posició valida, te que ser una posició del tauler.
     @post Retorna la referència a la fitxa gràfica trobada o un valor null si no.
     @return FitxaGrafica si Trobada altrement null.
     */
    public FitxaGrafica buscarFitxaGrafica(Posicio posicio){
        Boolean trobada = false;
        FitxaGrafica ret = null;

        int i = 0;
        while (i < _fitxes.getChildren().size() && !trobada) {
            ret = (FitxaGrafica) _fitxes.getChildren().get(i);
            if (ret.actualX() == posicio.columna && ret.actualY() == posicio.fila) trobada = true;
            i++;
        }
        return ret;
    }

    /** @brief Tenca la finestra de taules si està oberta
     @pre \p true
     @post Totes les components de _taules són eliminades.
     */
    public void tencarFinestraTaules(){
        _taules.getChildren().clear();
    }

    public void acceptarTaules(){
        _escacs.taulesAcceptades();
    }

    public void activarTorns() {
        _escacs.activarTorns();
    }

    public void desactivarTorns() {
        _escacs.desactivarTorns();
    }

    public void activarEnrocs() {
        _escacs.activarEnrocs();
    }

    public void desactivarEnrocs() {
        _escacs.desactivarEnrocs();
    }

    public boolean enrocsActivats() {
        return _escacs.enrocsActivats();
    }

    /** @brief Busca una fitxa grafica en el tauler a partir de la sea posició. Pot realitzar un enroc, fer els preperatius per fer un enroc o abortar els prperatius de fer un enroc.
     @pre \p true
     @post Gestiona el enroc en funció del seu estat anterior.
     @param  desactivar Indica si es vols desectuivar l'estat d'enroc
     */
    public void enrocarGrafic(boolean desactivar){

        //Tenim que aplicar enroc si _estatEnroc == 2
        if(_escacs.guanyador()==0) {
            if (_estatEnroc == 2 && !desactivar) {
                Pair<Boolean, TiradaEnroc> pE = _escacs.esEnrocValid(_fitxesEnroc.first, _fitxesEnroc.second);

                if (pE.first) {
                    FitxaGrafica fG1 = buscarFitxaGrafica(pE.second.posicioFitxaA_origen());
                    FitxaGrafica fG2 = buscarFitxaGrafica(pE.second.posicioFitxaB_origen());

                    Tirada t = new Tirada(pE.second);

                    int resultatAplicar = aplicaMoviment(t, fG1, fG2);

                    processaResultatAplicar(resultatAplicar);

                    //Si li toca la CPU tira
                    crearThreadCPU();
                }
            }

            if (_estatEnroc == 0) _estatEnroc = 1;
            else if (_estatEnroc == 1) _estatEnroc = 2;
            else if (_estatEnroc == 2) _estatEnroc = 0;


            if (desactivar) _estatEnroc = 0;

            if (desactivar || _estatEnroc == 0) {
                _fitxesEnroc.first = null;
                _fitxesEnroc.second = null;
                activarTorns();
                activarEnrocs();
            }
            else if(_estatEnroc == 1) desactivarTorns();

            ((Panell) (_panell.getChildren().get(0))).mostrarEnroc(_estatEnroc);
        }
    }

    private void processaResultatAplicar(int resultatAplicar) {
        if(resultatAplicar != 0) { // Si s'ha donat algún cas especial en aplicar el moviment
            _escacs.desactivarTorns();  //Desactivem jugabilitat
            switch(resultatAplicar) {
                case -1: {
                    ((Panell)_panell.getChildren().get(0)).mostrarTextInfo("Error");
                    break;
                }
                case 1: {
                    ((Panell)_panell.getChildren().get(0)).mostrarTextInfo("Taules per escac\n continuat");
                    break;
                }
                case 2: {
                    ((Panell)_panell.getChildren().get(0)).mostrarTextInfo("Taules per\n inacció");
                    break;
                }
                case 3: {
                    ((Panell)_panell.getChildren().get(0)).mostrarTextInfo("Rei ofegat,\n fi de la partida");
                    break;
                }
                case 4: {
                    ((Panell)_panell.getChildren().get(0)).mostrarGuanyador(_escacs.guanyador());
                    break;
                }
            }
        }
    }

    /** @brief Finalitza una partida. El jugador que solicita la rendició és el que perd
     @pre \p true
     @post Partida finalitzada, el jugador que solicita rendir-se perd la partida.
     */
    public void rendirse(){
        _escacs.rendirse();
        ((Panell)_panell.getChildren().get(0)).mostrarGuanyador(_escacs.guanyador());
    }

    /** @brief Si li toca jugar a la cpu crea un Thread y juga el seu torn.
     @pre \p true
     @post La cpu ha jugat el seu torn si li tocava.
     */
    private void crearThreadCPU(){
        //Avisa a la CPU si li toca
        if(_escacs.esJugadorCPU(_escacs.tornActual())) {
            new Thread(() -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                    jugarTornCPU();
                } catch (Exception err) {
                    System.err.println(err);
                }
                try {
                    stop();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        }
    }
}
