/** @file FitxaGrafica.java
 @brief Una fitxa gràfica per a un tauler de dames.
 */

import javafx.beans.binding.DoubleExpression;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.InputStream;


/**
 * @class FitxaGrafica
 * @brief Una fitxa en forma de cercle de color
 * @details Les coordenades oldX() i oldY() fan referència al centre
 *          del cercle; la fitxa disposa de gestors d'esdeveniments que
 *          permeten moure-la amb el ratolí.
 */
public class Panell extends StackPane{

    private int _pixelsX;               ///< valor de referència (amplada de casella)
    private int _pixelsY;               ///< valor de referència (alçada de casella)
    private Pane _root;
    private Text _tornText;
    private Text _infoText;
    private ImageView _saveButton;
    private ImageView _doButton;
    private ImageView _undoButton;
    private ImageView _openButton;
    private ImageView _panellImg;
    private ImageView _CPUBlackButton;
    private ImageView _CPUWhiteButton;
    private ImageView _solicitarTaulesButton;
    private ImageView _enrocarButton;
    private ImageView _giveUpButton;
    private Robot _robot;
    private EscacsGrafic _escacsGrafic;


    /**
     * @brief Constructor Panell.
     * @pre \p true
     * @post Crea un panell amb botons de control de la partida.
     * @param pixelsX amplada en píxels del panell.
     * @param pixelsY alçada en píxels del panell.
     * @param x columna del tauler.
     * @param y fila del tauler.
     * @param torn torn actual.
     * @param escacsGrafic Referència a l'objecte EscacsGrafic que ha cridat aquest constructor.
     */
    public Panell(int pixelsX, int pixelsY, int x, int y, int torn, EscacsGrafic escacsGrafic) {
        //Guardem referència escacs grafic
        _escacsGrafic = escacsGrafic;

        _pixelsX = pixelsX;
        _pixelsY = pixelsY;
        String tornS = "";

        tornS = (torn==-1)?"BLANQUES":"NEGRES";

        String boto = "";
        DropShadow shadow = new DropShadow();

        try {
            _robot = new Robot();
        } catch (AWTException err) {
            err.printStackTrace();
        }

        //Creem imatge pel panell
        InputStream is_src = getClass().getResourceAsStream("img/pannel.png");
        Image panell = new Image(is_src);
        _panellImg = new ImageView(panell);
        _panellImg.setFitWidth(_pixelsX);
        _panellImg.setFitHeight(_pixelsY);




        //Creem text pel torn
        _tornText = new Text(_pixelsX * 0.10, _pixelsY*0.05, "Torn: " + tornS);
        _tornText.setFont(Font.font("arial", FontWeight.BOLD, FontPosture.REGULAR, _pixelsX*0.08));

        //Creem text per l'enroc
        _infoText = new Text(_pixelsX * 0.10, _tornText.getY() + _pixelsX*0.08*1.5, "");
        _infoText.setFont(Font.font("arial", FontWeight.BOLD, FontPosture.REGULAR, _pixelsX*0.08));

        //Creem text pel boto
        /*_lastPressedButtonText = new Text(_pixelsX * 0.10, _pixelsY*0.09, "Botó: " + boto);
        _lastPressedButtonText.setFont(Font.font("arial", FontWeight.BOLD, FontPosture.REGULAR, _pixelsX*0.08));*/


        //Creem butó de hacer
        is_src = getClass().getResourceAsStream("img/dobutton.png");
        Image dobutton = new Image(is_src);
        _doButton  = new ImageView(dobutton);
        _doButton.setFitWidth(_pixelsX*0.34);
        _doButton.setFitHeight(_pixelsY*0.08);
        _doButton.setX((pixelsX*0.55)-(_doButton.getFitWidth()/2));
        _doButton.setY(_pixelsY * 0.85);
        _doButton.setEffect(shadow);
        _doButton.setOnMouseReleased((javafx.scene.input.MouseEvent e) -> {  // hi afegim un EventHandler anònim (funció lambda)
            _robot.keyPress(KeyEvent.VK_Y);
            _doButton.setEffect(shadow);
        } );
        _doButton.setOnMousePressed((javafx.scene.input.MouseEvent e) -> {  // hi afegim un EventHandler anònim (funció lambda)
            _doButton.setEffect(null);
        } );


        //Creem butó de deshacer
        is_src = getClass().getResourceAsStream("img/undobutton.png");
        Image undobutton = new Image(is_src);
        _undoButton  = new ImageView(undobutton);
        _undoButton.setFitWidth(_pixelsX*0.34);
        _undoButton.setFitHeight(_pixelsY*0.08);
        _undoButton.setX((pixelsX*0.2)-(_undoButton.getFitWidth()/2));
        _undoButton.setY(_pixelsY * 0.85);
        _undoButton.setEffect(shadow);
        _undoButton.setOnMouseReleased((javafx.scene.input.MouseEvent e) -> {  // hi afegim un EventHandler anònim (funció lambda)
            _robot.keyPress(KeyEvent.VK_Z);
            _undoButton.setEffect(shadow);
        } );
        _undoButton.setOnMousePressed((javafx.scene.input.MouseEvent e) -> {  // hi afegim un EventHandler anònim (funció lambda)
            _undoButton.setEffect(null);
        } );

        //Creem butó de guardar
        is_src = getClass().getResourceAsStream("img/savebutton.png");
        Image savebutton = new Image(is_src);
        _saveButton  = new ImageView(savebutton);
        _saveButton.setFitWidth(_pixelsX*0.34);
        _saveButton.setFitHeight(_pixelsY*0.08);
        _saveButton.setX((pixelsX*0.2)-(_saveButton.getFitWidth()/2));
        _saveButton.setY(_undoButton.getY() - _saveButton.getFitHeight());
        _saveButton.setEffect(shadow);
        _saveButton.setOnMouseReleased((javafx.scene.input.MouseEvent e) -> {  // hi afegim un EventHandler anònim (funció lambda)
            _robot.keyPress(KeyEvent.VK_S);
            _saveButton.setEffect(shadow);
        } );
        _saveButton.setOnMousePressed((javafx.scene.input.MouseEvent e) -> {  // hi afegim un EventHandler anònim (funció lambda)
            _saveButton.setEffect(null);
        } );

        //Creem butó de obrir
        is_src = getClass().getResourceAsStream("img/openButton.png");
        Image openButton = new Image(is_src);
        _openButton  = new ImageView(openButton);
        _openButton.setFitWidth(_pixelsX*0.34);
        _openButton.setFitHeight(_pixelsY*0.08);
        _openButton.setX((pixelsX*0.55)-(_openButton.getFitWidth()/2));
        _openButton.setY(_undoButton.getY() - _openButton.getFitHeight());
        _openButton.setEffect(shadow);
        _openButton.setOnMouseReleased((javafx.scene.input.MouseEvent e) -> {  // hi afegim un EventHandler anònim (funció lambda)
            _robot.keyPress(KeyEvent.VK_O);
            _openButton.setEffect(shadow);
        } );
        _openButton.setOnMousePressed((javafx.scene.input.MouseEvent e) -> {  // hi afegim un EventHandler anònim (funció lambda)
            _openButton.setEffect(null);
        } );

        //Creem butó de CPU Negres
        is_src = getClass().getResourceAsStream("img/woodButtonCPUBlack.png");
        Image CPUBlackButton = new Image(is_src);
        _CPUBlackButton  = new ImageView(CPUBlackButton);
        _CPUBlackButton.setFitWidth(_pixelsX*0.34);
        _CPUBlackButton.setFitHeight(_pixelsY*0.08);
        _CPUBlackButton.setX((pixelsX*0.55)-(_CPUBlackButton.getFitWidth()/2));
        _CPUBlackButton.setY(_openButton.getY() - _CPUBlackButton.getFitHeight());
        _CPUBlackButton.setEffect(shadow);
        _CPUBlackButton.setOnMouseReleased((javafx.scene.input.MouseEvent e) -> {  // hi afegim un EventHandler anònim (funció lambda)
            _robot.keyPress(KeyEvent.VK_B);
        } );
        _CPUBlackButton.setOnMousePressed((javafx.scene.input.MouseEvent e) -> {  // hi afegim un EventHandler anònim (funció lambda)
            if(_CPUBlackButton.getEffect() != null)_CPUBlackButton.setEffect(null);
            else _CPUBlackButton.setEffect(shadow);
        } );

        //Creem butó de CPU Blanques
        is_src = getClass().getResourceAsStream("img/woodButtonCPUWhite.png");
        Image CPUWhiteButton = new Image(is_src);
        _CPUWhiteButton  = new ImageView(CPUWhiteButton);
        _CPUWhiteButton.setFitWidth(_pixelsX*0.34);
        _CPUWhiteButton.setFitHeight(_pixelsY*0.08);
        _CPUWhiteButton.setX((pixelsX*0.2)-(_CPUWhiteButton.getFitWidth()/2));
        _CPUWhiteButton.setY(_openButton.getY() - _CPUWhiteButton.getFitHeight());
        _CPUWhiteButton.setEffect(shadow);
        _CPUWhiteButton.setOnMouseReleased((javafx.scene.input.MouseEvent e) -> {  // hi afegim un EventHandler anònim (funció lambda)
            _robot.keyPress(KeyEvent.VK_W);
        } );
        _CPUWhiteButton.setOnMousePressed((javafx.scene.input.MouseEvent e) -> {  // hi afegim un EventHandler anònim (funció lambda)
            if(_CPUWhiteButton.getEffect() != null)_CPUWhiteButton.setEffect(null);
            else _CPUWhiteButton.setEffect(shadow);
        } );



        //Creem butó de solicitar taules
        is_src = getClass().getResourceAsStream("img/taulesButton.png");
        Image solicitarTaulesButton = new Image(is_src);
        _solicitarTaulesButton  = new ImageView(solicitarTaulesButton);
        _solicitarTaulesButton.setFitWidth(_pixelsX*0.4);
        _solicitarTaulesButton.setFitHeight(_pixelsY*0.1);
        _solicitarTaulesButton.setX((pixelsX/2)-(_solicitarTaulesButton.getFitWidth()*0.82));
        _solicitarTaulesButton.setY(_CPUWhiteButton.getY() - _solicitarTaulesButton.getFitHeight());
        _solicitarTaulesButton.setEffect(shadow);
        _solicitarTaulesButton.setOnMouseReleased((javafx.scene.input.MouseEvent e) -> {  // hi afegim un EventHandler anònim (funció lambda)
            _robot.keyPress(KeyEvent.VK_T);
            _solicitarTaulesButton.setEffect(shadow);
        } );
        _solicitarTaulesButton.setOnMousePressed((javafx.scene.input.MouseEvent e) -> {  // hi afegim un EventHandler anònim (funció lambda)
            if(_solicitarTaulesButton.getEffect() != null)_solicitarTaulesButton.setEffect(null);
            else _solicitarTaulesButton.setEffect(shadow);
        } );

        //Creem butó de enrocar
        is_src = getClass().getResourceAsStream("img/enrocarButton.png");
        Image enrocarButton = new Image(is_src);
        _enrocarButton  = new ImageView(enrocarButton);
        _enrocarButton.setFitWidth(_pixelsX*0.4);
        _enrocarButton.setFitHeight(_pixelsY*0.1);
        _enrocarButton.setX((pixelsX/2)-(_enrocarButton.getFitWidth()*0.82));
        _enrocarButton.setY(_solicitarTaulesButton.getY() - _enrocarButton.getFitHeight());
        _enrocarButton.setEffect(shadow);
        _enrocarButton.setOnMouseReleased((javafx.scene.input.MouseEvent e) -> {  // hi afegim un EventHandler anònim (funció lambda)
            if(_escacsGrafic.enrocsActivats()) {
                Boolean desactivar = false;
                if (!_infoText.getText().equals("")) desactivar = true;
                _escacsGrafic.enrocarGrafic(desactivar);
            }
            _enrocarButton.setEffect(shadow);
        } );
        _enrocarButton.setOnMousePressed((javafx.scene.input.MouseEvent e) -> {  // hi afegim un EventHandler anònim (funció lambda)
            if(_enrocarButton.getEffect() != null)_enrocarButton.setEffect(null);
            else _enrocarButton.setEffect(shadow);
        } );

        //Creem butó de abandonar
        is_src = getClass().getResourceAsStream("img/rendirButton.png");
        Image giveUpButton = new Image(is_src);
        _giveUpButton  = new ImageView(giveUpButton);
        _giveUpButton.setFitWidth(_pixelsX*0.4);
        _giveUpButton.setFitHeight(_pixelsY*0.1);
        _giveUpButton.setX((pixelsX/2)-(_giveUpButton.getFitWidth()*0.82));
        _giveUpButton.setY(_enrocarButton.getY() - _giveUpButton.getFitHeight());
        _giveUpButton.setEffect(shadow);
        _giveUpButton.setOnMouseReleased((javafx.scene.input.MouseEvent e) -> {  // hi afegim un EventHandler anònim (funció lambda)
            _escacsGrafic.rendirse();
            _giveUpButton.setEffect(shadow);
        } );
        _giveUpButton.setOnMousePressed((javafx.scene.input.MouseEvent e) -> {  // hi afegim un EventHandler anònim (funció lambda)
            if(_giveUpButton.getEffect() != null)_giveUpButton.setEffect(null);
            else _giveUpButton.setEffect(shadow);
        } );
        _root = new Pane();
        _root.setPrefSize(_pixelsX, _pixelsY);
        _root.getChildren().addAll(_panellImg, _tornText, _infoText, _openButton, _saveButton, _undoButton, _doButton, _CPUBlackButton, _CPUWhiteButton, _solicitarTaulesButton, _enrocarButton, _giveUpButton);

        getChildren().add(_root);

        relocate(x,y); // posicionament del panell a pantalla

    }

    /**
     * @brief Actualitza el torn del text del panell.
     * @pre Torn vàlid.
     * @post El text del panell és actualitzat.
     * @param torn -1 Blanques, 1 Negres.
     */
    public void actualitzaTorn(int torn){

        int i = _root.getChildren().indexOf(_tornText);
        if( torn == -1)  ((Text)_root.getChildren().get(i)).setText("Torn: BLANQUES");
        else if( torn == 1)  ((Text)_root.getChildren().get(i)).setText("Torn: NEGRES");
        else ((Text)_root.getChildren().get(i)).setText("Torn: - - - - - -");
    }

    /** @brief Actualtiza l'amplada del panell depenent dels pixels de la finestra.
     * @pre \p true
     * @post Amplada de la promoció gràfica redimensionada.
     * @param amplada Amplada de la finestra.
     */
    public void modificaAmplada(Double amplada){

        _pixelsX = amplada.intValue();

        _doButton.setFitWidth(_pixelsX*0.34);
        _doButton.setX((_pixelsX*0.55)-(_doButton.getFitWidth()/2));

        _undoButton.setFitWidth(_pixelsX*0.34);
        _undoButton.setX((_pixelsX*0.2)-(_undoButton.getFitWidth()/2));

        _saveButton.setFitWidth(_pixelsX*0.34);
        _saveButton.setX((_pixelsX*0.2)-(_saveButton.getFitWidth()/2));

        _openButton.setFitWidth(_pixelsX*0.34);
        _openButton.setX((_pixelsX*0.55)-(_openButton.getFitWidth()/2));

        _CPUBlackButton.setFitWidth((_pixelsX*0.34));
        _CPUBlackButton.setX((_pixelsX*0.55)-(_CPUBlackButton.getFitWidth()/2));

        _CPUWhiteButton.setFitWidth((_pixelsX*0.34));
        _CPUWhiteButton.setX((_pixelsX*0.2)-(_CPUWhiteButton.getFitWidth()/2));

        _solicitarTaulesButton.setFitWidth((_pixelsX*0.4));
        _solicitarTaulesButton.setX((_pixelsX/2)-(_solicitarTaulesButton.getFitWidth()*0.82));

        _enrocarButton.setFitWidth((_pixelsX*0.4));
        _enrocarButton.setX((_pixelsX/2)-(_enrocarButton.getFitWidth()*0.82));

        _giveUpButton.setFitWidth((_pixelsX*0.4));
        _giveUpButton.setX((_pixelsX/2)-(_giveUpButton.getFitWidth()*0.82));

        _tornText.setX(_pixelsX *0.10);
        _tornText.setFont(Font.font("arial", FontWeight.BOLD, FontPosture.REGULAR, _pixelsX*0.08));

        _infoText.setX( _pixelsX*0.10);
        _infoText.setFont(Font.font("arial", FontWeight.BOLD, FontPosture.REGULAR, _pixelsX*0.08));

        _panellImg.setFitWidth(_pixelsX);

    }

    /**
     * @brief Actualtiza l'alçada del panell depenent dels pixels de la finestra.
     * @pre \p true
     * @post Alçada de la promoció gràfica redimensionada.
     * @param alcada Alçada de la finestra
     */
    public void modificaAlcada(Double alcada){
        _pixelsY = alcada.intValue();

        _doButton.setFitHeight(_pixelsY*0.08);
        _doButton.setY(_pixelsY * 0.85);

        _undoButton.setFitHeight(_pixelsY*0.08);
        _undoButton.setY(_pixelsY * 0.85);

        _saveButton.setFitHeight(_pixelsY*0.08);
        _saveButton.setY(_undoButton.getY() - _saveButton.getFitHeight());

        _openButton.setFitHeight(_pixelsY*0.08);
        _openButton.setY(_undoButton.getY() - _openButton.getFitHeight());

        _CPUBlackButton.setFitHeight(_pixelsY*0.08);
        _CPUBlackButton.setY(_openButton.getY() - _CPUBlackButton.getFitHeight());

        _CPUWhiteButton.setFitHeight(_pixelsY*0.08);
        _CPUWhiteButton.setY(_openButton.getY() - _CPUWhiteButton.getFitHeight());

        _solicitarTaulesButton.setFitHeight(_pixelsY*0.1);
        _solicitarTaulesButton.setY(_CPUWhiteButton.getY() - _solicitarTaulesButton.getFitHeight());

        _enrocarButton.setFitHeight(_pixelsY*0.1);
        _enrocarButton.setY(_solicitarTaulesButton.getY() - _enrocarButton.getFitHeight());

        _giveUpButton.setFitHeight(_pixelsY*0.1);
        _giveUpButton.setY(_enrocarButton.getY() - _giveUpButton.getFitHeight());

        _tornText.setY(_pixelsY *0.05);

        _infoText.setY(_tornText.getY() + _pixelsX*0.08*1.5);

        _panellImg.setFitHeight(_pixelsY);


    }

    /**
     * @brief Actualtiza el text informatiu amb les instruccions per realitzar l'enroc
     * @pre \p true
     * @post Mostra en el panell les instruccions de enrocar.
     * @param estat Estat en el que estroba el moviment de enroc. 1:  Selecciona fitxa 1, 2: Seleccionar fitxa 2.
     */
    public void mostrarEnroc(int estat){
        int i = _root.getChildren().indexOf(_infoText);
        Text aux = (Text)_root.getChildren().get(i);

        if(estat == 1) aux.setText("Selecciona fitxa 1");
        else if(estat == 2) aux.setText("Selecciona fitxa 2");
        else aux.setText("");

    }

    /**
     * @brief Actualtiza el text informatiu indicant el guanyador
     * @pre \p true
     * @post Mostra en el panell el guanyador de la partida.
     * @param guanyador Guanyador de la partida. 1:  Negres, 1: Blanques.
     */
    public void mostrarGuanyador(int guanyador){
        int i = _root.getChildren().indexOf(_infoText);
        Text aux = (Text)_root.getChildren().get(i);
        String color = "- - - - -";
        if(guanyador == -1) color = "blanques";
        else if(guanyador == 1) color = "negres";


        aux.setText("Les guanyadores\nsón les " + color);
    }

    /**
     * @brief Actualtiza el text informatiu el text rei ofegat
     * @pre \p true
     * @post Mostra en el panell el text de Rei ofegat
     */
    public void mostrarTextInfo(String text){
        int i = _root.getChildren().indexOf(_infoText);
        Text aux = (Text)_root.getChildren().get(i);
        aux.setText(text);
    }

    /**
     * @brief Posa el botons de joc CPU desectivats.
     * @pre \p true
     * @post Els botons de joc CPU han estat desectivats visualment.
     */
    public void desectivarCPUs(){
        DropShadow shadow = new DropShadow();
        _CPUBlackButton.setEffect(shadow);
        _CPUWhiteButton.setEffect(shadow);
    }
}
