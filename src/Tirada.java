import org.omg.PortableServer._ServantActivatorStub;

import java.util.ArrayList;

import static java.lang.Math.abs;

public class Tirada implements Comparable<Tirada> {

    private Posicio _origen;
    private Posicio _desti;
    private Fitxa _fitxaMoguda;
    private ArrayList<Pair<Posicio,Fitxa>> _mortes; /// Guarda les Fitxes que han mort durant la tirada i les posicions on han mort
    private int _valor;
    private Boolean _shaPromocionat = false;
    private String _fitxaPromocionada;
    private String _fitxaAnteriorPromocionar;
    private boolean _esEnroc = false;
    private TiradaEnroc _tiradaEnroc;

    /**
     * @brief Constructor de la classe Tirada
     * @pre \p origen i \p desti vàlids, \p fitxa vàlida
     * @post Guarda l'informació de la tirada
     * @param origen Posició origen de la tirada
     * @param desti Posició desti de la tirada
     * @param fitxa La fitxa que s'ha mogut en aquesta tirada
     * @param mortes Array de fitxes mortes i la posiciió on han mort
     * @param shaPromocionat Indica si hi sha fet una promocio durant la tirada
     */
    public Tirada(Posicio origen, Posicio desti, Fitxa fitxa, ArrayList<Pair<Posicio,Fitxa>> mortes, Boolean shaPromocionat){
        _fitxaMoguda = fitxa;
        _origen = origen;
        _desti = desti;
        _mortes = mortes;
        _shaPromocionat = shaPromocionat;

        calcularValor();
    }
    /**
     * @brief Setter de _tiradaEnroc
     * @pre \p tiradaEnroc vàlida
     * @post Guarda la -tiradaEnroc
     * @param e TiradaEnroc vàlida
     */
    public Tirada(TiradaEnroc e) {
        _esEnroc = true;
        _tiradaEnroc = e;
    }
    /**
     * @brief Getter de _tiradaEnroc
     */
    public TiradaEnroc tiradaEnroc() {
        return _tiradaEnroc;
    }
    /**
     * @brief Getter de _esEnroc
     */
    public boolean esEnroc() {
        return _esEnroc;
    }
    /**
     * @brief Getter de Posicio origen
     */
    public Posicio origen(){
        return _origen;
    }
    /**
     * @brief Getter de Posicio desti
     */
    public Posicio desti(){
        return _desti;
    }
    /**
     * @brief Getter de Fitxa _fitxaMoguda
     */
    public Fitxa fitxaMoguda() {
        return _fitxaMoguda;
    }
    /**
     * @brief Setter de Fitxa _fitxaMoguda
     */
    public void vinculaFitxa(Fitxa fitxaMoguda) {
        _fitxaMoguda = fitxaMoguda; //Per defecte java ja gestiona la memoria i per tant ja borrarà l'antic objecte
    }
    /**
     * @brief Getter de ArrayList<Pair<Posicio,Fitxa>> _mortes
     */
    public ArrayList<Pair<Posicio,Fitxa>> mortes(){
        return _mortes;
    }
    /**
     * @brief Converteix la Tirada a un string.
     * @pre \p true
     * @post S'ha convertit la Tirada actual a un string.
     * @return Un string construit a partir de la Tirada actual.
     */
    @Override
    public String toString() {
        return _origen.toString() + _desti.toString() + " Valor: " + _valor;// + "moguda: " + _fitxaMoguda.toString() + " promocionada: " + _fitxaPromocionada.toString() + " ant promocio: " + _fitxaAnteriorPromocionar.toString();
    }
    /**
     * @brief Getter de int _valor
     */
    public int valorTirada() {
        return _valor;
    }
    /**
     * @brief Calcula el valor de la tirada acutal (sumant el valor de les peçes que han mort en aquesta tirada.
     * @pre \p true
     * @post Guarda el valor de la tirada a _valor.
     */
    private void calcularValor() {
        _valor = 0;

        if(_mortes != null) {
            for (Pair<Posicio, Fitxa> p : _mortes) {
                if(p.second!=null) _valor += p.second.valor();
            }
        }
    }
    /**
     * @brief Compara el valor d'aquesta tirada amb el d'una altre
     * @pre Tirada vàlida
     * @post Resta el valor absolut d'aquesta tirada - el valor absolut de la tirada \p o
     * @return Int de la resta dels 2 valors en absolut restats.
     */
    @Override
    public int compareTo(Tirada o) {
        return (abs(_valor) - abs(o._valor));
    }
    /**
     * @brief COmprova si 2 Objectes Tirada són iguals
     * @pre Object vàlid
     * @post Retorna true o fals si són iguals o diferents
     * @return  true o fals
     */
    @Override
    public boolean equals(Object o) {
        boolean r = false;

        if (o != null && o instanceof Tirada) {
            Tirada t = (Tirada)o;

            r = (abs(this._valor) == abs(t._valor));
        }

        return r;
    }
    /**
     * @brief Getter de Boolean _shaPromocionat
     */
    public Boolean shaPromocionat(){
        return _shaPromocionat;
    }
    /**
     * @brief Setter de Boolean _shaPromocionat
     */
    public void setPromocio(Boolean shaPromocionat){
        _shaPromocionat = shaPromocionat;
    }
    /**
     * @brief Setter de String _fitxaPromocionada
     */
    public void setFitxaPromocionada(String fitxaPromocionada){
        _fitxaPromocionada = fitxaPromocionada;
    }
    /**
     * @brief Setter de String _fitxaAnteriorPromocionar
     */
    public void set_fitxaAnteriorPromocionar(String fitxaAnteriorPromocionar){
        _fitxaAnteriorPromocionar = fitxaAnteriorPromocionar;
    }
    /**
     * @brief Getter de String _fitxaAnteriorPromocionar
     */
    public String fitxaAnteriorPromocionar() {
        return _fitxaAnteriorPromocionar;
    }
    /**
     * @brief Getter de String _fitxaPromocionada
     */
    public String fitxaPromocionada() {
        return _fitxaPromocionada;
    }
}
