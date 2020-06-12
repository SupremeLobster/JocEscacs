import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.io.InputStream;

public class TaulesGrafica extends StackPane{
    private int _pixelsX;               ///< valor de referència (amplada de casella)
    private int _pixelsY;               ///< valor de referència (alçada de casella)
    private Pane _root;
    private ImageView _baseTaules;
    private ImageView _rebutjarTaules;
    private ImageView _acceptarTaules;
    private  EscacsGrafic _escacsGrafic;
    private Text _textTaules;



    /**
     * @brief Constructor Taules Gràfiques.
     * @pre \p true
     * @post Nova finestra taules gràfiques creada
     * @param pixelsX Amplada en píxels de la finestra de taules.
     * @param pixelsY Alçada en píxels de la finestra de taules.
     * @param refEscacsGrafics Referència a l'objecte EscacsGrafic que ha cridat aquest constructor.
     */
    public TaulesGrafica(int pixelsX, int pixelsY, EscacsGrafic refEscacsGrafics) {
        _pixelsX = ((Double) (pixelsX*0.6)).intValue();
        _pixelsY = ((Double) (pixelsY*0.2)).intValue();

        _escacsGrafic = refEscacsGrafics;

        _escacsGrafic.desactivarTorns();
        _escacsGrafic.desactivarEnrocs();

        _root = new Pane();
        _root.setPrefSize(_pixelsX, _pixelsY);

        DropShadow shadow = new DropShadow();
        //Creem imatge pel panell
        InputStream srcImage = getClass().getResourceAsStream("img/basePromocio.png");
        javafx.scene.image.Image panell = new javafx.scene.image.Image(srcImage);
        _baseTaules = new ImageView(panell);
        _baseTaules.setFitWidth(_pixelsX);
        _baseTaules.setFitHeight(_pixelsY);
        _baseTaules.setEffect(shadow);
        _root.getChildren().addAll(_baseTaules);


        //Creem butó rebutjar taules
        srcImage = getClass().getResourceAsStream("img/noButton.png");
        Image dobutton = new Image(srcImage);
        _rebutjarTaules  = new ImageView(dobutton);
        _rebutjarTaules.setFitWidth(_pixelsX*0.12);
        _rebutjarTaules.setFitHeight(_pixelsX*0.12);
        _rebutjarTaules.setX((pixelsX/2 - _pixelsX/2) * 0.5);
        _rebutjarTaules.setY(_pixelsY/2 - _pixelsY*0.3);
        _rebutjarTaules.setEffect(shadow);
        _rebutjarTaules.setOnMouseReleased((javafx.scene.input.MouseEvent e) -> {  // hi afegim un EventHandler anònim (funció lambda)
            _rebutjarTaules.setEffect(shadow);
            _escacsGrafic.activarTorns();
            _escacsGrafic.tencarFinestraTaules();
        } );
        _rebutjarTaules.setOnMousePressed((javafx.scene.input.MouseEvent e) -> {  // hi afegim un EventHandler anònim (funció lambda)
            _escacsGrafic.activarEnrocs();
            _rebutjarTaules.setEffect(null);
        } );


        //Creem butó de acceptar taules
        srcImage = getClass().getResourceAsStream("img/siButton.png");
        Image SolicitarTaulesButton = new Image(srcImage);
        _acceptarTaules  = new ImageView(SolicitarTaulesButton);
        _acceptarTaules.setFitWidth(_pixelsX*0.12);
        _acceptarTaules.setFitHeight(_pixelsX*0.12);
        _acceptarTaules.setX((pixelsX/2 - _pixelsX/2) *2);
        _acceptarTaules.setY(_pixelsY/2 - _pixelsY*0.3);
        _acceptarTaules.setEffect(shadow);
        _acceptarTaules.setOnMouseReleased((javafx.scene.input.MouseEvent e) -> {  // hi afegim un EventHandler anònim (funció lambda)
            _acceptarTaules.setEffect(shadow);
            _escacsGrafic.acceptarTaules();
            _escacsGrafic.tencarFinestraTaules();

        } );
        _acceptarTaules.setOnMousePressed((javafx.scene.input.MouseEvent e) -> {  // hi afegim un EventHandler anònim (funció lambda)
            _acceptarTaules.setEffect(null);
        } );


        _textTaules = new Text((pixelsX/2 - _pixelsX/2) *0.75, _pixelsY *0.22, "ACCEPTES LES TAULES?");
        _textTaules.setFont(Font.font("arial", FontWeight.BOLD, FontPosture.REGULAR, _pixelsX*0.04));


        _root.getChildren().addAll(_acceptarTaules, _rebutjarTaules, _textTaules);
        getChildren().add(_root);

        relocate(((Double)((pixelsX/2 - _pixelsX/2) * 0.5)).intValue(),pixelsY/2 - _pixelsY/2); // posicionament del panell a pantalla
    }

    /** @brief Actualtiza l'amplada de les taules gràfiques depenent dels pixels de la finestra.
     * @pre \p true
     * @post Amplada de les taules gràfiques redimensionada.
     * @param amplada Amplada de la finestra
     */
    public void modificaAmplada(Double amplada){
        //Actualitza el pixels X
        _pixelsX = ((Double) (amplada*0.6)).intValue();

        //Redimensiona tota la finestra
        _root.setPrefSize(_pixelsX, _pixelsY);
        _baseTaules.setFitWidth(_pixelsX);

        _rebutjarTaules.setFitWidth(_pixelsX*0.12);
        _rebutjarTaules.setX((amplada/2 - _pixelsX/2) * 0.5);

        _acceptarTaules.setFitWidth(_pixelsX*0.12);
        _acceptarTaules.setX((amplada/2 - _pixelsX/2) *2);

        _textTaules.setX((amplada/2 - _pixelsX/2) *0.75);
        _textTaules.setFont(Font.font("arial", FontWeight.BOLD, FontPosture.REGULAR, _pixelsX*0.04));

        relocate(((Double)((amplada/2 - _pixelsX/2) * 0.5)).intValue(),getLayoutY()); // posicionament del panell a pantalla

    }

    /** @brief Actualtiza l'alçada de les taules grafiques depenent dels pixels de la finestra.
     * @pre \p true
     * @post Alçada de les taules gràfiques redimensionada.
     * @param alcada Alçada de la finestra.
     */
    public void modificaAlcada(Double alcada){
        //Actualitza el pixels X
        _pixelsY = ((Double) (alcada*0.2)).intValue();

        //Redimensiona tota la finestra
        _root.setPrefSize(_pixelsX, _pixelsY);
        _baseTaules.setFitHeight(_pixelsY);

        _rebutjarTaules.setFitHeight(_pixelsX*0.12);
        _rebutjarTaules.setY(_pixelsY/2 - _pixelsY*0.3);

        _acceptarTaules.setFitHeight(_pixelsX*0.12);
        _acceptarTaules.setY(_pixelsY/2 - _pixelsY*0.3);

        _textTaules.setY(_pixelsY *0.22);

        relocate(getLayoutX(), alcada/2 - _pixelsY/2); // posicionament del panell a pantalla
    }
}
