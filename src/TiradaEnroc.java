/**
 * @class Enroc
 * @brief Defineix un tipus d'enroc.
 */
public class TiradaEnroc {
    private final Enroc _tipusEnroc; // Associa aquesta tirada a un tipus d'enroc en concret

    private Fitxa _fitxaA;
    private final Posicio _posFitxaA_origen;
    private final Posicio _posFitxaA_desti;

    private Fitxa _fitxaB;
    private final Posicio _posFitxaB_origen;
    private final Posicio _posFitxaB_desti;

    /**
     * @brief Constructor TiradaEnroc.
     * @pre \p Moure aquestes fitxes és vàlid segons la definició del \p tipusEnroc.
     * @post Crea una tirada d'enroc entre dues fitxes.
     * @param tipusEnroc El tipus d'enroc al que correspon aquesta tirada.
     * @param fitxaA La fitxa A que fa aquest enroc.
     * @param posFitxaA_origen La posició original de la fitxa A.
     * @param posFitxaA_desti La posició final de la fitxa A.
     * @param fitxaB La fitxa B que fa aquest enroc.
     * @param posFitxaB_origen La posició original de la fitxa B.
     * @param posFitxaB_desti La posició final de la fitxa B.
     */
    public TiradaEnroc(Enroc tipusEnroc, Fitxa fitxaA, Posicio posFitxaA_origen, Posicio posFitxaA_desti, Fitxa fitxaB, Posicio posFitxaB_origen, Posicio posFitxaB_desti) {
        _tipusEnroc = tipusEnroc;

        _fitxaA = fitxaA;
        _posFitxaA_origen = posFitxaA_origen;
        _posFitxaA_desti = posFitxaA_desti;

        _fitxaB = fitxaB;
        _posFitxaB_origen = posFitxaB_origen;
        _posFitxaB_desti = posFitxaB_desti;
    }

    /**
     * @brief Dona el tipus d'enroc d'aquesta tirada.
     * @pre \p true
     * @return El tipus d'enroc al qual està associat aquesta tirada d'enroc.
     */
    public Enroc tipusEnroc() {
        return _tipusEnroc;
    }

    /**
     * @brief Dona la fitxa A d'aquesta tirada d'enroc.
     * @pre \p true
     * @return La fitxa A d'aquesta tirada d'enroc.
     */
    public Fitxa fitxaA() {

        return _fitxaA;
    }

    /**
     * @brief Dona la posició original de la fitxa A en aquesta tirada d'enroc.
     * @pre \p true
     * @return La posició original de la fitxa A en aquesta tirada d'enroc.
     */
    public Posicio posicioFitxaA_origen() {

        return _posFitxaA_origen;
    }

    /**
     * @brief Dona la posició final de la fitxa A en aquesta tirada d'enroc.
     * @pre \p true
     * @return La posició final de la fitxa A en aquesta tirada d'enroc.
     */
    public Posicio posicioFitxaA_desti() {

        return _posFitxaA_desti;
    }

    /**
     * @brief Dona la fitxa B d'aquesta tirada d'enroc.
     * @pre \p true
     * @return La fitxa B d'aquesta tirada d'enroc.
     */
    public Fitxa fitxaB() {

        return _fitxaB;
    }

    /**
     * @brief Dona la posició original de la fitxa B en aquesta tirada d'enroc.
     * @pre \p true
     * @return La posició original de la fitxa B en aquesta tirada d'enroc.
     */
    public Posicio posicioFitxaB_origen() {

        return _posFitxaB_origen;
    }

    /**
     * @brief Dona la posició final de la fitxa B en aquesta tirada d'enroc.
     * @pre \p true
     * @return La posició final de la fitxa B en aquesta tirada d'enroc.
     */
    public Posicio posicioFitxaB_desti() {

        return _posFitxaB_desti;
    }

    /**
     * @pre \p true
     * @param fitxaA La fitxa a la que es vol associar.
     * @post S'ha associat la fitxa A d'aquesta tirada d'enroc a la fitxa passada per paràmetre.
     */
    public void vinculaFitxa_A(Fitxa fitxaA) {

        _fitxaA = fitxaA; //Per defecte java ja gestiona la memòria i per tant ja borrarà l'antic objecte
    }

    /**
     * @pre \p true
     * @param fitxaB La fitxa a la que es vol associar.
     * @post S'ha associat la fitxa B d'aquesta tirada d'enroc a la fitxa passada per paràmetre.
     */
    public void vinculaFitxa_B(Fitxa fitxaB) {

        _fitxaB = fitxaB; //Per defecte java ja gestiona la memòria i per tant ja borrarà l'antic objecte
    }
}
