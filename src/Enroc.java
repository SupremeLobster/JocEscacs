/**
 * @class Enroc
 * @brief Defineix un tipus d'enroc.
 */
public class Enroc {
    private String _nomFitxaA;
    private String _nomFitxaB;
    private boolean _quiets;
    private boolean _buitAlMig;

    /**
     * @brief Constructor Enroc.
     * @pre \p true
     * @post S'ha creat un tipus enroc entre dues fitxes.
     * @param nomFitxaA El nom del tipus de fitxa A que pot fer aquest enroc.
     * @param nomFitxaB El nom del tipus de fitxa B que pot fer aquest enroc.
     * @param quiets Indica si les fitxes es poden haver mogut o no per poder fer aquest enroc.
     * @param buitAlMig Indica si les caselles entre les dos fitxes han d'estar buides o no.
     */
    public Enroc(String nomFitxaA, String nomFitxaB, boolean quiets, boolean buitAlMig) {
        _nomFitxaA = nomFitxaA;
        _nomFitxaB = nomFitxaB;
        _quiets = quiets;
        _buitAlMig = buitAlMig;
    }

    /**
     * @pre \p true
     * @return El nom del tipus de la fitxa A
     */
    public String fitxaA() {
        return _nomFitxaA;
    }

    /**
     * @pre \p true
     * @return El nom del tipus de la fitxa B
     */
    public String fitxaB() {
        return _nomFitxaB;
    }

    /**
     * @pre \p true
     * @return
     * TRUE si les fitxes NO es poden haver mogut per fer aquest tipus d'enroc;\n
     * FALSE en cas contrari.
     */
    public boolean quiets() {
        return _quiets;
    }

    /**
     * @pre \p true
     * @return
     * TRUE si les caselles entre les dos fitxes han d'estar buides per fer aquest tipus d'enroc;\n
     * FALSE en cas contrari.
     */
    public boolean buitAlMig() {
        return _buitAlMig;
    }
}
