import javafx.scene.image.ImageView;
import javafx.scene.image.Image;

public class Casella extends ImageView {
    /**
     * @brief Constructor casella.
     * @pre \p true
     * @post Crea una casella que correspon a la imatge passada per el constructor.
     * @param img Imatge de la casella.
     * @param x Amplada en pixels.
     * @param y Alçada en pixels.
     */
    public Casella(Image img, int x, int y){
        setImage(img);
        setFitWidth(x);       //Redimensionament, establim amplada.
        setFitHeight(y);
        //setPreserveRatio(true);     //No volem distoricons
        setSmooth(true);            //Volem qualitat a la representació de la imatge.
        setCache(true);             //Per millorar l'eficiència
    }
}
