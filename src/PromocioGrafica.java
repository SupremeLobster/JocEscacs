import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.InputStream;
import java.util.ArrayList;

public class PromocioGrafica extends StackPane {
    /** 
     * @file FitxaGrafica.java
     * @brief Una fitxa gràfica per a un tauler de dames.
     */



    private int _pixelsX;               ///< valor de referència (amplada de casella)
    private int _pixelsY;               ///< valor de referència (alçada de casella)
    private Pane _root;
    private ArrayList<FitxaGrafica> _fitxes;
    private ArrayList<Casella> _caselles;
    private ImageView _basePromocio;
    private  EscacsGrafic _escacsGrafic;
    private Tirada _tirada;


    /**
     * @brief Constructor Promoció Grafica.
     * @pre \p true
     * @post Nova finestra promoció gràfica creada.
     * @param pixelsX Amplada en píxels de la finestra de promocio.
     * @param pixelsY Alçada en píxels de la finestra de promocio.
     * @param fitxes Llista de les fitxes a les que es pot promocionar.
     * @param color Ha de ser -1 per Blanques o 1 per Negres. Indica de quin color hauràn de ser les fitxes que es mostrin.
     * @param refEscacsGrafics Referència a l'objecte EscacsGrafic que ha cridat aquest constructor.
     * @param posicio Posició de la fitxa a promocionar.
     * @param tirada Tirada en la qual s'ha assolit un estat que permet fer promoció.
     */
    public PromocioGrafica(int pixelsX, int pixelsY, ArrayList<Fitxa> fitxes, int color, EscacsGrafic refEscacsGrafics, Posicio posicio, Tirada tirada) {
        _fitxes = new ArrayList<>();
        _caselles = new ArrayList<>();

        _pixelsX = ((Double) (pixelsX*0.6)).intValue();
        _pixelsY = ((Double) (pixelsY*0.2)).intValue();

        _escacsGrafic = refEscacsGrafics;

        _escacsGrafic.desactivarTorns();
        _escacsGrafic.desactivarEnrocs();

        _root = new Pane();
        _root.setPrefSize(_pixelsX, _pixelsY);

        _tirada = tirada;

        DropShadow shadow = new DropShadow();
        //Creem imatge pel panell
        InputStream srcImage = getClass().getResourceAsStream("img/basePromocio.png");
        javafx.scene.image.Image panell = new javafx.scene.image.Image(srcImage);
        _basePromocio = new ImageView(panell);
        _basePromocio.setFitWidth(_pixelsX);
        _basePromocio.setFitHeight(_pixelsY);
        _basePromocio.setEffect(shadow);
        _root.getChildren().addAll(_basePromocio);

        for(int i = 0; i < fitxes.size(); i++){
            try {
                if(fitxes.get(i) != null){
                    String casellaSrc = "null.png";
                    Fitxa f = (Fitxa) fitxes.get(i).clone();
                    int widthFG = _pixelsX/(fitxes.size() + 2);   //El +2 es per deixar una merge de una casella per banda i banda
                    int heighFG = widthFG;;
                    f.posarDireccioMoviment(color);
                    if(color == -1) casellaSrc = "/src/img/fonsB.png";
                    else if(color == 1) casellaSrc = "/src/img/fonsN.png";
                    Casella c = new Casella(new Image(casellaSrc), widthFG, heighFG);
                    c.relocate((i+1)*widthFG, _pixelsY/2 - heighFG/2);
                    FitxaGrafica fg = new FitxaGrafica((Fitxa) f.clone(), widthFG, heighFG, (i+1)*widthFG, _pixelsY/2 - heighFG/2 , false, false);
                    fg.setOnMouseEntered((MouseEvent e) -> {
                            fg.setEffect(shadow);
                    });
                    fg.setOnMouseExited((MouseEvent e) -> {
                        fg.setEffect(null);
                    });
                    fg.setOnMousePressed((MouseEvent e) -> {
                        _escacsGrafic.activarTorns();
                        _escacsGrafic.activarEnrocs();
                        _escacsGrafic.ferPromocio(fg.fitxa(), posicio, tirada);
                    });
                    _fitxes.add(fg);
                    _caselles.add(c);
                    _root.getChildren().add(c);
                    _root.getChildren().add(fg);

                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }




        getChildren().add(_root);

        relocate(((Double)((pixelsX/2 - _pixelsX/2) * 0.5)).intValue(),pixelsY/2 - _pixelsY/2); // posicionament del panell a pantalla
    }

    /** @brief Actualtiza l'amplada de la promoció grafica depenent dels pixels de la finestra.
     * @pre \p true
     * @post Amplada de la promoció gràfica redimensionada.
     * @param amplada Amplada de la finestra.
     */
    public void modificaAmplada(Double amplada){
        _pixelsX = ((Double) (amplada*0.6)).intValue();
        _root.setPrefSize(_pixelsX, _pixelsY);
        _basePromocio.setFitWidth(_pixelsX);
        for(int i = 0; i < _fitxes.size(); i++){
            int widthFG = _pixelsX/(_fitxes.size() + 2);   //El +2 es per deixar una merge de una casella per banda i banda
            int heighFG = widthFG;;

            FitxaGrafica aux1 = (FitxaGrafica) _root.getChildren().get(_root.getChildren().indexOf(_fitxes.get(i)));
            aux1.movePantalla(widthFG * (i + 1), _pixelsY/2 - heighFG/2);
            aux1.setHeightProperty(heighFG);

            Casella aux  = (Casella) _root.getChildren().get(_root.getChildren().indexOf(_caselles.get(i)));
            aux.setFitHeight(heighFG);
            aux.relocate((i+1)*widthFG, _pixelsY/2 - heighFG/2);
        }
        relocate(((Double)((amplada/2 - _pixelsX/2) * 0.5)).intValue(),getLayoutY()); // posicionament del panell a pantalla
    }

    /** @brief Actualtiza l'alçada de la promoció grafica depenent dels pixels de la finestra.
     * @pre \p true
     * @post Alçada de la promoció gràfica redimensionada.
     * @param alcada Alçada de la finestra.
     */
    public void modificaAlcada(Double alcada){
        _pixelsY = ((Double) (alcada*0.2)).intValue();
        _root.setPrefSize(_pixelsX, _pixelsY);
        _basePromocio.setFitHeight(_pixelsY);
        for(int i = 0; i < _fitxes.size(); i++){
            int widthFG = _pixelsX/(_fitxes.size() + 2);   //El +2 es per deixar una merge de una casella per banda i banda
            int heighFG = widthFG;;

            FitxaGrafica aux1 = (FitxaGrafica) _root.getChildren().get(_root.getChildren().indexOf(_fitxes.get(i)));
            aux1.movePantalla(widthFG * (i + 1), _pixelsY/2 - heighFG/2);
            aux1.setWidthProperty(widthFG);

            Casella aux  = (Casella) _root.getChildren().get(_root.getChildren().indexOf(_caselles.get(i)));
            aux.setFitWidth(widthFG);
            aux.relocate((i+1)*widthFG, _pixelsY/2 - heighFG/2);
        }
        relocate(getLayoutX(),alcada/2 - _pixelsY/2); // posicionament del panell a pantalla

    }


}
