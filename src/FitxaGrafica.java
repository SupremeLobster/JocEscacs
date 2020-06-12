/** @file FitxaGrafica.java
 @brief Una fitxa gràfica per a un tauler de dames.
 */

import javafx.animation.PathTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;

import java.io.InputStream;


/** 
 * @class FitxaGrafica
 * @brief Una fitxa amb la seva imatge corresponent
 * @details Les coordenades oldX() i oldY() fan referència al centre
 *          del cercle; la fitxa disposa de gestors d'esdeveniments que 
 *          permeten moure-la amb el ratolí.
 */
public class FitxaGrafica extends StackPane {

    private int _pixelsX;              ///< valor de referència (amplada de casella)
    private int _pixelsY;
    private Fitxa _fitxa;             ///< blanca o vermella, tipus de peça
    private double _mouseX, _mouseY;  ///< coordenades click mouse en píxels
    private double _oldX, _oldY;      ///< coordenades antigues en píxels (abans de moure)
    private ImageView _scaledImage;

    /** 
     * @brief Tipus de fitxa.
     * @pre \p true
     * @post Retorna la fitxa corresponent a la fitxa grafica.
     * @return Fitxa.
     */
    public Fitxa fitxa() {
        return _fitxa;
    }

    /** 
     * @brief Coordenada x en píxels (abans de moure).
     * @pre \p true
     * @post Retorna la coordenada x en píxels.
     * @return double.
     */
    public double oldX() {
        return _oldX;
    }

    /** 
     * @brief Coordenada y en píxels (abans de moure).
     * @pre \p true
     * @post Retorna la coordenada y en píxels.
     * @return double.
     */
    public double oldY() {
        return _oldY;
    }


    /** 
     * @brief Constructor FitxaGrafica.
     * @param fitxa Fitxa blanca o negra.
     * @param pixelsX Amplada en píxels de la casella contenidora.
     * @param pixelsY Alçada en píxels de la casella contenidora.
     * @param x Columna del tauler.
     * @param y Fila del tauler.
     * @param moviment Indica si aquesta FitxaGrafica tindrà la capacitat de ser moguda o no.
     * @param moveCasella Indica si el posicionament inicial d'aquesta FitxaGrafica ha de ser relatiu al tauler o a la pantalla.
     */
    public FitxaGrafica(Fitxa fitxa, int pixelsX, int pixelsY, int x, int y, boolean moviment, boolean moveCasella) {
        _fitxa = fitxa;
        _pixelsX = pixelsX;
        _pixelsY = pixelsY;

        if(moveCasella)moveCasella(x,y); // posicionament de la peça a la pantalla amb coordenades del tauler
        else movePantalla(x,y);          // posicionament de la peça a la pantalla amb coordeades de pixels

        //Primerament només mostrarem les fitxes amb diferents colors, postariorment canviaré la imatge per l'adecuada
        String srcImage = fitxa.imatge();

        InputStream is_image = getClass().getResourceAsStream("img/" + srcImage);

        Image image = new Image(is_image);

        _scaledImage = new ImageView(image);
        _scaledImage.setFitWidth(_pixelsX);
        _scaledImage.setFitHeight(_pixelsY);
        getChildren().add(_scaledImage);
        if(moviment){
            setOnMousePressed((MouseEvent e) -> {  // hi afegim un EventHandler anònim (funció lambda)
                _mouseX = e.getSceneX();
                _mouseY = e.getSceneY();
                toFront(); //Posiciona la fitxa a la capa superior
            } );
            setOnMouseDragged((MouseEvent e) -> {  // EventHandler per moure la peça quan l'arrosseguem
                relocate(_oldX + e.getSceneX() - _mouseX, _oldY + e.getSceneY() - _mouseY);
            } );
        }

    }


    /** 
     * @brief Mou la peça al centre de la casella (x,y).
     * @pre Coordenades vàlides en el tauler.
     * @post Mou la fitxa a la posició x y del tauler.
     * @param x Fila.
     * @param y Columna.
     */
    public void moveCasella(int x, int y) {
        _oldX = x * _pixelsX;
        _oldY = y * _pixelsY;
        relocate(_oldX, _oldY); // operació de Node (JavaFX)
    }

    /** 
     * @brief Mou la peça a les coordenades amb pixels (x,y).
     * @pre \p true
     * @post Mou la fitxa a les coordenades x y de la pantalla.
     * @param x Eix de les X.
     * @param y Eix de les Y.
     */
    public void movePantalla(int x, int y){
        relocate(x, y);
    }

    /** 
     * @brief Retorna la peça al seu lloc.
     * @pre Fitxa en moviment.
     * @post Aborta el moviment y recoloca la peça en la seva posició anterior.
     */
    public void abortMove() {
        relocate(_oldX, _oldY);
    }

    /** 
     * @brief Retorna la posició actual X en el tauler.
     * @pre \p true
     * @post Obtens la posició actual X en el tauler.
     * @return  int
     */
    public int actualX(){
        return ((Double)( _oldX/_pixelsX)).intValue();
    }

    /** 
     * @brief Retorna la posició actual Y en el tauler.
     * @pre \p true
     * @post Obtens la posició actual Y en el tauler.
     * @return  int
     */
    public int actualY(){
        return ((Double)( _oldY/_pixelsY)).intValue();
    }

    /** 
     * @brief Actualtiza la seva posició en el eix de les X amb referència a la seva amplada
     * @pre \p true
     * @post Fitxa redimensionada i posicionada en el eix de les X.
     * @param pixelsX Nova amplada amb pixels de la fitxa.
     * @return  None.
     */
    public void  actualitzaX(int pixelsX){
        double oldPixel = _pixelsX;

        _pixelsX = pixelsX;

        _scaledImage.setFitWidth(_pixelsX);

        relocate(_pixelsX * _oldX/oldPixel, _oldY); // operació de Node (JavaFX)

        _oldX = _pixelsX * _oldX/oldPixel;
    }


    /** 
     * @brief Actualtiza la seva posició en el eix de les X amb referència a la seva amplada
     * @pre \p true
     * @post Fitxa redimensionada i posicionada en el eix de les X.
     * @param pixelsY Nova alçada amb pixels de la fitxa.
     * @return  None.
     */
    public  void  actualitzaY(int pixelsY){
        double oldPixel = _pixelsY;

        _pixelsY = pixelsY;

        _scaledImage.setFitHeight(_pixelsY);

        relocate(_oldX, _pixelsY * _oldY/oldPixel); // operació de Node (JavaFX)

        _oldY = _pixelsY * _oldY/oldPixel;
    }

    /** 
     * @brief Recarrega la imatge propia de la fitxa
     * @pre \p true
     * @post Imatge de la fitxa recarregada
     * @return  None.
     */
    public void actualitzaImatge(){
        //Elimina anterior imatge
        getChildren().remove(_scaledImage);

        //Genera la nova imatge
        Image image = new Image("src/img/" + _fitxa.imatge());
        _scaledImage = new ImageView(image);
        _scaledImage.setFitWidth(_pixelsX);
        _scaledImage.setFitHeight(_pixelsY);

        //Afegeix la nova imatge
        getChildren().add(_scaledImage);
    }

    /** 
     * @brief Actualtiza l'amplada de la fitxa amb pixels.
     * @pre \p true
     * @post Fitxa redimensionada l'amplada de la fitxa.
     * @param value Nova amplada amb pixels de la fitxa.
     * @return  None.
     */
    public void setWidthProperty(double value){
        _scaledImage.setFitWidth(value);
    }

    /** 
     * @brief Actualtiza l'alçada de la fitxa amb pixels.
     * @pre \p true
     * @post Fitxa redimensionada l'alçada de la fitxa.
     * @param value Nova alçada amb pixels de la fitxa.
     * @return  None.
     */
    public void setHeightProperty(double value){
        _scaledImage.setFitHeight(value);
    }
}
