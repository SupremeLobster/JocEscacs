/**  @file Escacs.java
 @brief Un joc d'escacs.
 */

import com.google.gson.Gson;
import com.google.gson.internal.StringMap;

import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.min;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.System.exit;

/**
 * @class Escacs
 * @brief Versió completa d'un joc d'escacs.
 * @details Files vàlides: 0..files()-1; Columnes vàlides:
 * 0..columnes()-1;
 */
public class Escacs {
    private final Map<String, Fitxa> _MapFitxes = new TreeMap<>(); /// Conté els tipus de fitxa llegits del fitxer de configuració inicial.
    private ArrayList<String> _VectorPosInicial; /// Conté el nom de les fitxes en l'ordre en que estàn inicialment segons el fitxer de configuració.
    private final ArrayList<Enroc> _LlistaTipusEnrocs = new ArrayList<>(); /// Conté els tipus d'enrocs que es poden fer en aquesta partida segons el fitxer de configuració.

    private final Fitxa[][] _tauler;

    private int _torn; /// Torn actual -1 "Blanques", 1 "Negres"

    private boolean _blanquesCPU = false; /// Indica si el joc de CPU està activat per les Blanques
    private boolean _negresCPU = false; /// Indica si el joc de CPU està activat per les Negres

    private int _files; /// Alçada del tauler.
    private int _columnes; /// Amplada del tauler


    private final ArrayList<Tirada> _ControlTirades = new ArrayList<>(); /// Historial de les tirades fetes. Serveix per desfer/refer.
    private int _nTirada;

    private int _limitTornsInaccio;
    private int _nTornsInaccio;

    private int _limitEscacsSeguits;
    private int _nEscacsSeguits;

    private Posicio _pos_reiB; /// Per l'enunciat, sabem que sempre hi haurà d'haver un rei Blanc
    private Posicio _pos_reiN; /// Per l'enunciat, sabem que sempre hi haurà d'haver un rei Negre

    private boolean _tornsActivats = true; /// Indica si en un moment donat l'habilitat de fer tirades normals (NO enrocs) està activada o no.
    private boolean _enrocsActivats = true; /// Indica si en un moment donat l'habilitat de fer tirades d'enrocs està activada o no.

    private boolean _estemRefent = false; /// Indica si en un moment donat estem refent una tirada o no. Es fa servir a "aplicaMoviment()", ja que hi ha coses que NO ha de fer si estem refent una tirada, però si s'han de fer en el cas general.

    private int _guanyador = 0;

    private int _maxDepthAI = 4; /// Per defecte el posem a 4, ja que amb l'algorisme alpha-beta de podar l'arbre del minimax, 4 és prou ràpid.

    /**
     * @brief Crea el tauler
     * @pre  El \p fitxerConfig és un fitxer JSON existent i \p depthOptional=NULL or \p depthOptional>0
     * @post S'ha creat el tauler de joc segons les definicions del \p fitxerConfig
     * @param fitxerConfig És el fitxer JSON que conté totes les definicions per aquesta partida.
     * @param detphOptional Es pot deixar NULL i prendrà el valor per defecte. Defineix la profunditat màxima de l'algorisme minimax.
     */
    public Escacs(String fitxerConfig, Integer detphOptional) {
        readConfigFile(fitxerConfig);

        if(detphOptional != null) _maxDepthAI = detphOptional;

        // Inicialitzar torn
        _torn = -1; //Comencen les blanques
        _nTornsInaccio = 0;

        if (_files < 8 || _columnes < 2 || _columnes > 26 || _files > 26)
            throw new RuntimeException("Error en els paràmetres");
        _tauler = new Fitxa[_files][_columnes];

        int aux = 0;
        for (int i = 0; aux<_VectorPosInicial.size(); ++i) { // POSAR NEGRES
            for (int j = 0; j < _columnes && aux<_VectorPosInicial.size(); ++j) {

                try {
                    Fitxa auxF = (Fitxa) _MapFitxes.get(_VectorPosInicial.get(aux));
                    if(auxF != null) {
                        _tauler[i][j] = (Fitxa) _MapFitxes.get(_VectorPosInicial.get(aux)).clone();
                        if (_tauler[i][j].nom().equals("REI")) _pos_reiN = new Posicio(i, j);
                    }
                    else _tauler[i][j] = null;
                }
                catch (CloneNotSupportedException cnse) {
                    System.err.println(cnse);
                }

                if(_tauler[i][j] != null) _tauler[i][j].posarDireccioMoviment(1);
                aux++;
            }
        }

        aux = 0;
        for (int i = _files-1; aux<_VectorPosInicial.size(); i--) { // POSAR BLANQUES
            for (int j = 0; j<_columnes && aux<_VectorPosInicial.size(); j++) {

                try {
                    Fitxa auxF = (Fitxa) _MapFitxes.get(_VectorPosInicial.get(aux));
                    if(auxF != null) {
                        _tauler[i][j] = (Fitxa) _MapFitxes.get(_VectorPosInicial.get(aux)).clone();
                        if (_tauler[i][j].nom().equals("REI")) _pos_reiB = new Posicio(i, j);
                    }
                    else _tauler[i][j] = null;
                }
                catch (CloneNotSupportedException cnse) {
                    System.err.println(cnse);
                }

                if(_tauler[i][j] != null) _tauler[i][j].posarDireccioMoviment(-1);
                aux++;
            }
        }
    }

    /** @brief Diu quantes files té el tauler */
    public int files() {
        return _files;
    }

    /** @brief Diu quantes columnes té el tauler */
    public int columnes() {
        return _columnes;
    }

    /** @brief Retorna la fitxa d'una posició
     @pre 0 <= f < files() i 0 <= c < columnes()
     @return La fitxa de la posició (f,c); null si no hi ha cap fitxa
     en aquesta posició.
     */
    public Fitxa fitxa(int f, int c) {
        return _tauler[f][c];
    }

    /** @brief Diu si un moviment és vàlid, i quines fitxes es maten i les posicions d'aquestes fitxes.
     @pre \p origen i \p desti són posicions vàlides del tauler. \p tornSimular = -1 or \p tornSimular = 1.
     @post Simula el moviment per calcular si és legal i diu quines fitxes es moririen fent aquest moviment.
     @param origen Posició original de la fitxa a moure.
     @param desti Posició final de la fitxa a moure.
     @param tornSimular Ha de ser -1 o 1. Indica per quin jugador es vol simular aquest moviment, blanc(-1) o negre(1).
     @return Parella indicant si el moviment d'una fitxa de \p origen
     a \p desti és possible, i una llista de les fitxes a matar i les posicions d'aquestes.
     */
    public Pair<Boolean,ArrayList<Pair<Posicio,Fitxa>>> moviment(Posicio origen, Posicio desti, int tornSimular) {
        Pair<Boolean, ArrayList<Pair<Posicio,Fitxa>>> r = new Pair<>(false,null);
        ArrayList<Pair<Posicio,Fitxa>> saltadesMatar = null;

        if(!esPosicioValida(origen) || !esPosicioValida(desti)) return r;

        int x0 = origen.fila;
        int y0 = origen.columna;
        int x1 = desti.fila;
        int y1 = desti.columna;
        Fitxa p = _tauler[x0][y0];
        Pair<Boolean, Boolean> resultatMoviment = null;

        Integer A = x1-x0;
        Integer B = y1-y0;

        //Mirem si desti esta a dins del tauler
        if(esPosicioValida(desti)) {
            // MIREM SI ES VOL MATAR
            Integer C = 0;
            if(_tauler[x1][y1] != null) C = 1;

            // MIREM SI ES VOL SALTAR
            Integer D = 0;
            if(A!=0 && B!=0 && abs(A)!=abs(B)) D = 1; // MIREM SI ES MOVIMENT COMBINAT. SI ES COMBINAT SEMPRE ES POT SALTAR
            else { // MIREM SI HI HA INTENCIÓ DE SALTAR
                boolean hiHaSalt = false;
                int i = 1, xDir = 1, yDir = 1;

                if(A < 0) xDir = -1;
                else if(A == 0) xDir = 0;

                if(B < 0) yDir = -1;
                else if(B == 0) yDir = 0;

                saltadesMatar = new ArrayList<>();

                while(i<max(abs(A), abs(B))) {
                    int posX = x0+i*xDir;
                    int posY = y0+i*yDir;
                    if(_tauler[posX][posY] != null && p!=null) { // HI HA UNA FITXA
                        hiHaSalt = true;
                        if(p.esEquipContrari(_tauler[posX][posY]) && !_tauler[posX][posY].esInvulnerable()) { // LA FITXA ES DE L'EQUIP CONTRARI
                            Pair<Posicio, Fitxa> auxPair = new Pair<>(new Posicio(posX, posY), _tauler[posX][posY]);
                            saltadesMatar.add(auxPair);
                        }
                    }

                    i++;
                }

                if(hiHaSalt) {
                    D = 1;
                }
            }

            ArrayList<Integer> mov = new ArrayList<>();
            mov.add(0, A);
            mov.add(1, B);
            mov.add(2, C);
            mov.add(3, D);

            if(p!=null) resultatMoviment = p.esMovimentPermes(mov);

            // Si (a la casella origen hi ha una peça i el moviment és vàlid
            // i és el torn de l'equip de la peça seleccionada i els torns activats i
            // (la casella destí està buida o la casella destí té una peça de l'equip
            // contrari al de la peça seleccionada i la peça a la casella
            // destí NO és invulnerable)); llavors TRUE
            if (p!=null && resultatMoviment.first && tornSimular == p.direccioMoviment() && _tornsActivats && (_tauler[x1][y1] == null || p.esEquipContrari(_tauler[x1][y1]) && !_tauler[x1][y1].esInvulnerable())) {
                if(tornSimular == _torn) {
                    ArrayList<Pair<Posicio, Fitxa>> auxAF = new ArrayList<>();
                    auxAF.add(new Pair<Posicio, Fitxa>(desti, _tauler[desti.fila][desti.columna]));
                    if(saltadesMatar!=null) auxAF.addAll(saltadesMatar);
                    Tirada auxT = new Tirada(origen, desti, p, auxAF, false);

                    aplicaMoviment(auxT, true);

                    if (p.nom().equals("REI")) {
                        r.first = !esDonaEscac(desti, tornSimular * -1);
                    }
                    else {
                        Posicio posRei = null;
                        if(tornSimular == -1) posRei = _pos_reiB;
                        else if(tornSimular == 1) posRei = _pos_reiN;

                        r.first = !esDonaEscac(posRei, tornSimular * -1);
                    }

                    desferTirada(auxT);
                }
                else r.first = true;

                if (C == 1) {
                    r.second = new ArrayList<>();
                    r.second.add(new Pair<Posicio, Fitxa>(desti, _tauler[desti.fila][desti.columna]));
                }

                if (D == 1) { // SI S'HA INTENTAT SALTAR
                    if (resultatMoviment.second && saltadesMatar != null) { // SI ES POT MATAR SALTANT
                        if (r.second == null) r.second = new ArrayList<>();

                        r.second.addAll(saltadesMatar);
                    }
                }
            }
        }

        return r;
    }

    /**
     * @brief Aplica un moviment
     * @pre \p t és una tirada vàlida.
     * @post S'ha aplicat la tirada \p t al tauler i s'ha canviat de torn si s'escau.
     * @param t Tirada a aplicar.
     * @param esSimulacio Indica si aquest moviment s'ha de contar com a moviment
     *                    definitiu o si només s'ha de simular. Si només s'ha de simular no es
     *                    tindrà en compte de cares als comptadors de torns inactius o d'escacs seguits, i tampoc es farà canvi de torn.
     * @return
     * 0 si el moviment s'ha aplicat amb normalitat i no es dona cap cas especial.\n
     * 1 si s'ha superat el límit d'escacs seguits.\n
     * 2 si s'ha superat el límit de torns inactius.\n
     * 3 si es dona rei ofegat.\n
     * 4 si es dona escac i mat.
     */
    public int aplicaMoviment(Tirada t, boolean esSimulacio) {
        int res = 0;

        if(t.esEnroc()) {
            aplicaEnroc(t.tiradaEnroc());
        }
        else {
            if (t.mortes() != null) {
                for (Pair<Posicio, Fitxa> p : t.mortes()) {
                    if (_tauler[p.first.fila][p.first.columna] != null) {
                        if (_tauler[p.first.fila][p.first.columna].nom().equals("REI")) {
                            if (_torn == -1) _pos_reiB = null;
                            else if (_torn == 1) _pos_reiN = null;
                        }
                        _tauler[p.first.fila][p.first.columna] = null;
                    }
                }
            }

            _tauler[t.desti().fila][t.desti().columna] = t.fitxaMoguda();
            _tauler[t.desti().fila][t.desti().columna].sumarMogudes();
            _tauler[t.origen().fila][t.origen().columna] = null;

            if (t.fitxaMoguda().nom().toUpperCase().equals("REI")) {
                switch (t.fitxaMoguda().direccioMoviment()) {
                    case -1: { // Blanques
                        _pos_reiB = t.desti();
                        break;
                    }
                    case 1: { // Negres
                        _pos_reiN = t.desti();
                        break;
                    }
                }
            }
        }

        if(!esSimulacio) { // No estem en simulació
            if (t.mortes() == null) _nTornsInaccio++;
            else _nTornsInaccio = 0;

            boolean _esDonaEscac = esDonaEscac(_pos_reiB, 1) || esDonaEscac(_pos_reiN, -1);
            if(_esDonaEscac) {
                _nEscacsSeguits++;
            }
            else _nEscacsSeguits = 0;

            _torn = _torn * -1; // Per calcular rei ofegat correctament amb la immersió recursiva de "moviment()"

            boolean tornsActivatsPrevi = _tornsActivats;
            _tornsActivats = true;

            boolean reiOfegat = esDonaReiOfegat(_torn);
            _tornsActivats = tornsActivatsPrevi;

            if(_esDonaEscac && reiOfegat) {
                res = 4;
                _guanyador = _torn*-1;
            }
            else if (reiOfegat) {
                res = 3;
            }
            else if (_nEscacsSeguits >= _limitEscacsSeguits) {
                res = 1;
            }
            else if (_nTornsInaccio >= _limitTornsInaccio) {
                res = 2;
            }
            _torn = _torn * -1;  // DEIXEM COM ESTAVA. Per calcular rei ofegat correctament amb la immersió recursiva de "moviment()"

            if(!_estemRefent) {
                eliminarRefer();
                afegirTirada(t);
            }


            _torn = _torn * -1; //Canviem el torn de color
        }


        return res;
    }

    /**
     * @brief Diu si es dona rei ofegat per un jugador en concret.
     * @pre \p tornSimular = -1 or \p tornSimular = 1.
     * @post Diu si es dona rei ofegat o no al jugador \p tornSimular.
     * @param tornSimular Ha de ser -1 o 1. Indica per quin jugador es vol mirar si hi ha rei ofegat, blanc(-1) o negre(1).
     * @return
     * TRUE si aquest jugador no pot moure cap fitxa;\n
     * FALSE en cas contrari.
     */
    private boolean esDonaReiOfegat(int tornSimular) {
        ArrayList<Tirada> aux = obtenirTiradesPossiblesJugador(tornSimular);

        return aux.size() == 0;
    }

    /**
     * @brief Diu si es dona escac a un rei en concret.
     * @pre \p posRei és la posició d'un dels 2 reis i és una posició vàlida. \p tornSimular = -1 or \p tornSimular = 1.
     * @post Diu si es dona escac o no al rei de la posició \p posRei.
     * @param posRei Posició de un dels 2 reis del tauler (Blanc o Negre).
     * @param tornSimular Ha de ser -1 o 1. Indica per quin jugador es vol mirar si hi ha escac, blanc(-1) o negre(1).
     * @return
     * TRUE si el rei està amenaçat en aquella posició;\n
     * FALSE en cas contrari.
     */
    private boolean esDonaEscac(Posicio posRei, int tornSimular) {
        boolean res = false;

        for(int i = 0; i < _files && !res; i++) {
            for(int j = 0; j<_columnes && !res; j++) {
                Posicio pOrigen = new Posicio(i, j);
                if(!pOrigen.equals(posRei) && _tauler[i][j] != null) res = moviment(pOrigen, posRei, tornSimular).first;
            }
        }

        return res;
    }

    /**
     * @brief S'accepten taules.
     * @pre \p true
     * @post S'accepten taules i s'acaba el programa.
     */
    public void taulesAcceptades() {
        exit(0);
    }

    /**
     * @brief S'ajorna la partida.
     * @pre \p true
     * @post S'ajorna la partida i s'acaba el programa.
     */
    public void ajornarPartida() {
        exit(0);
    }

    /**
     * @brief Activa l'habilitat de fer tirades.
     * @pre \p true
     * @post S'ha activat l'habilitat de fer tirades.
     */
    public void activarTorns() {
        _tornsActivats = true;
    }

    /**
     * @brief Desactiva l'habilitat de fer tirades.
     * @pre \p true
     * @post S'ha desactivat l'habilitat de fer tirades.
     */
    public void desactivarTorns() {
        _tornsActivats = false;
    }

    /**
     * @brief Activa l'habilitat de fer enrocs.
     * @pre \p true
     * @post S'ha activat l'habilitat de fer enrocs.
     */
    public void activarEnrocs() {
        _enrocsActivats = true;
    }

    /**
     * @brief Desactiva l'habilitat de fer enrocs.
     * @pre \p true
     * @post S'ha desactivat l'habilitat de fer enrocs.
     */
    public void desactivarEnrocs() {
        _enrocsActivats = false;
    }

    /**
     * @brief Diu si l'habilitat de fer enrocs està activada o no.
     * @pre \p true
     * @return
     * TRUE si els enrocs estan activats;\n
     * FALSE en cas contrari.
     */
    public boolean enrocsActivats() {
        return _enrocsActivats;
    }

    /**
     * @brief Dona el tipus de fitxa amb nom \p nomFitxa.
     * @pre \p true
     * @param nomFitxa Nom del tipus de fitxa que es vol obtenir.
     * @return Una fitxa del tipus \p nomFitxa inicialitzada per defecte.\n
     * NULL si no existeix cap tipus amb aquest nom.
     */
    public Fitxa obtenirTipusDeFitxa(String nomFitxa) {
        nomFitxa = nomFitxa.toUpperCase();
        Fitxa res = null;

        res = _MapFitxes.get(nomFitxa);

        return res;
    }

    /**
     * @brief Es promociona una fitxa a una altra.
     * @pre \p p és una posició vàlida i la fitxa a la posició \p p és promocionable.
     * @post S'ha promocionat la fitxa de la posició \p p, i ara és del tipus de \p fNova.
     * @param p Posició de la fitxa a promocionar.
     * @param fNova Fitxa inicialitzada per defecte del tipus al que es vol promocionar.
     * @return
     * 0 si s'ha promocionat sense errors.\n
     * -2 si s'ha intentat promocionar a REI.\n
     * -1 si la fitxa a la que s'ha intentat promocionar no existeix.\n
     * 1 si s'ha superat el límit d'escacs seguits.\n
     * 2 si s'ha superat el límit de torns inactius.\n
     * 3 si es dona rei ofegat.\n
     * 4 si es dona escac i mat.
     */
    public int promocionarFitxa(Posicio p, Fitxa fNova) {
		int res = 0;
		
        if(fNova != null) {
            String nomFitxaNova = fNova.nom().toUpperCase();

            if (!nomFitxaNova.equals("REI")) { // No es pot promocionar a REI
                _tauler[p.fila][p.columna].promocionar_a_fitxaNova(fNova);

                boolean _esDonaEscac = esDonaEscac(_pos_reiB, 1) || esDonaEscac(_pos_reiN, -1);
                if(_esDonaEscac) {
                    _nEscacsSeguits++;
                }
                else _nEscacsSeguits = 0;

                //_torn = _torn * -1; // Per calcular rei ofegat correctament amb la immersió recursiva de "moviment()"

                boolean tornsActivatsPrevi = _tornsActivats;
                _tornsActivats = true;

                boolean reiOfegat = esDonaReiOfegat(_torn);
                _tornsActivats = tornsActivatsPrevi;

                if(_esDonaEscac && reiOfegat) {
                    res = 4;
                    _guanyador = _torn*-1;
                }
                else if (reiOfegat) {
                    res = 3;
                }
                else if (_nEscacsSeguits >= _limitEscacsSeguits) {
                    res = 1;
                }
                else if (_nTornsInaccio >= _limitTornsInaccio) {
                    res = 2;
                }
                //_torn = _torn * -1;  // DEIXEM COM ESTAVA. Per calcular rei ofegat correctament amb la immersió recursiva de "moviment()"

            }
            else {
				res = -2;
			}
        }
        else {
			res = -1;
		}
		
		return res;
    }

    /**
     * @pre \p true
     * @return Llista amb tots els tipus de fitxa que existeixen en aquesta partida, menys el rei.
     */
    public ArrayList<Fitxa> llistaTipusDeFitxes() {
        ArrayList<Fitxa> res = new ArrayList<>();

        for(Map.Entry<String, Fitxa> e : _MapFitxes.entrySet()) {
            if(!e.getKey().equals("REI")) res.add(e.getValue());
        }

        return res;
    }

    /**
     * @brief Diu si la fitxa de la posició \p p es pot promocionar.
     * @pre \p p és una posició vàlida.
     * @post Diu si la fitxa de la posició \p p es pot promocionar estan en aquesta posició.
     * @param p Posició de la fitxa que es vol mirar si es pot promocionar.
     * @return
     * TRUE si la fitxa pot ser promocionada en aquella posició;\n
     * FALSE en cas contrari.
     */
    public boolean esPotPromocionarFitxa(Posicio p) {
        Fitxa auxF = _tauler[p.fila][p.columna];

        return (estaUltimaFila(auxF, p) && auxF.potPromocionar());
    }

    /**
     * @pre \p p és una posició vàlida i \p f no és NULL.
     * @post Diu si la fitxa \p f de la posició \p p es troba a l'última fila del tauler des del seu punt de vista.
     * @param f És la fitxa de la posició \p p.
     * @param p És la posició de la fitxa \p f.
     * @return
     * TRUE si en aquella posició, la fitxa es troba a l'última fila;\n
     * FALSE en cas contrari.
     */
    private boolean estaUltimaFila(Fitxa f, Posicio p) {
        return ( (p.fila==0 && f.direccioMoviment()==-1) || ((p.fila==(_files-1) && f.direccioMoviment()==1)) );
    }

    /**
     * @brief Diu de qui és el torn actual.
     * @pre \p true
     * @return De qui és el torn actual:\n
     * -1 si és el torn de les Blanques.\n
     * 1 si és el torn de les Negres.
     */
    public int tornActual() {

        return _torn;
    }

    /**
     * @brief Afegir una tirada al control de tirades (per poder desfer/refer).
     * @pre \p t != NULL
     * @post S'ha afegit la tirada \p t al control de tirades
     * @param t La tirada a afegir al control de tirades.
     */
    private void afegirTirada(Tirada t){
        _ControlTirades.add(t);
        _nTirada++;
    }

    /**
     * @brief S'eliminen del control de tirades les que ja no eren necessàries.
     * @pre \p true
     * @post S'han eliminat del control de tirades les que ja no eren necessàries.
     */
    private void eliminarRefer(){
        while(_ControlTirades.size() > _nTirada){
            _ControlTirades.remove(_ControlTirades.size()-1);
        }
    }

    /**
     * @brief Desfer una tirada en concret (aplicar moviment en ordre invers).
     * @pre \p t és una tirada vàlida.
     * @post S'ha desfet la tirada \p t.
     * @param t Tirada a desfer.
     */
    private void desferTirada(Tirada t) {

        if(t.esEnroc()) {
            desferTiradaEnroc(t.tiradaEnroc());
        }
        else {
            //Vinculem amb el nou objecte, Com que carreguem a partir de un fitxer JSON la vinculació que hi havia de les peces amb les tirades es perd, això assegura la seva vinculació.
            t.vinculaFitxa(_tauler[t.desti().fila][t.desti().columna]);
            if (t.shaPromocionat()) {
                _tauler[t.desti().fila][t.desti().columna].promocionar_a_fitxaNova(obtenirTipusDeFitxa(t.fitxaAnteriorPromocionar()));
            }

            _tauler[t.desti().fila][t.desti().columna].restarMogudes();
            _tauler[t.origen().fila][t.origen().columna] = t.fitxaMoguda(); // La peça que s'ha mogut tornar enrere
            _tauler[t.desti().fila][t.desti().columna] = null; // El lloc queda buit

            if (t.mortes() != null) {
                for (Pair<Posicio, Fitxa> p : t.mortes()) {
                    _tauler[p.first.fila][p.first.columna] = p.second;
                }
            }

            if(t.fitxaMoguda().nom().equals("REI")) {
                switch(t.fitxaMoguda().direccioMoviment()) {
                    case -1: {
                        _pos_reiB = t.origen();
                        break;
                    }
                    case 1: {
                        _pos_reiN = t.origen();
                        break;
                    }
                }
            }
        }
    }

    /**
     * @brief Desfà l'última tirada del control de tirades (aplicar moviment en ordre invers).
     * @pre \p true
     * @post S'ha desfet l'última tirada del control de tirades i s'ha canviat de torn.
     * @return La tirada que s'ha desfet.
     */
    public Tirada desfer(){
        Tirada t = null;

        if(_nTirada>0 && _tornsActivats) {
            _nTirada--;
            t = _ControlTirades.get(_nTirada);

            desferTirada(t);

            if(t.mortes() == null) _nTornsInaccio--;
            if(_nEscacsSeguits > 0) _nEscacsSeguits--;
            _torn = _torn * -1;
        }

        return t;
    }

    /**
     * @brief Refà l'última tirada del control de tirades (tornar a aplicar el moviment).
     * @pre \p true
     * @post S'ha refet l'última tirada del control de tirades.
     * @return La tirada que s'ha refet.
     */
    public Tirada refer(){
        Tirada t = null;

        if(_nTirada < _ControlTirades.size() && _tornsActivats){
            _estemRefent = true;
            _nTirada++;
            t = _ControlTirades.get(_nTirada-1);
            if(t.shaPromocionat()){
                _tauler[t.origen().fila][t.origen().columna].promocionar_a_fitxaNova(obtenirTipusDeFitxa(t.fitxaPromocionada()));
            }

            aplicaMoviment(t, false);

            _estemRefent = false;
        }

        return t;
    }

    /**
     * @brief Crea el fitxer on es guarda la partida
     * @pre \p true
     * @post Crea el fitxer si no existeix i en guarda l'inforamció
     * @param file fitxer on guardar l'inforamació.
     */
    public void crearFitxerGuardar(File file) throws IOException {

        File dir = new File("src/saves/");

        //Create the directory si no existeix
        if(dir.mkdir()) {
            System.out.println("S'ha creat el directori!");
        }
        else {
            System.out.println("El directori ja existeix.");
        }


        //Create the file
        if(file.createNewFile()) {
            System.out.println("S'ha creat el fitxer!");
        }
        else {
            System.out.println("El fitxer ja existeix.");
        }

        //Write Content
        String content = convertirPartidaToJson();

        guardarPartidaAFitxer(content, file);
    }
    /**
     * @brief Converteix l'informació de la partida en un String JSON
     * @pre \p true
     * @post retorna un String amb l'informació de la partida en format JSON
     */
    private String convertirPartidaToJson(){

        Gson gson = new Gson();

        return gson.toJson(this);

    }
    /**
     * @brief Guarda la partida actual en un fitxer json
     * @pre \p file existent \p content != ""
     * @post Guarda la partida en un fitxer json
     * @param content Informació de la partida en format json
     * @param file fitxer creat on guardar l'informació
     */
    private void guardarPartidaAFitxer(String content, File file) {
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @brief Converteix el tauler a un string.
     * @pre \p true
     * @post S'ha convertit l'estat actual del tauler a un string.
     * @return Un string construit a partir de l'estat actual del tauler.
     */
    @Override
    public String toString() {
        String s;
        String c = "abcdefghijklmnopqrstuvwxyz";
        String l = "   ";

        for (int j = 0; j < _columnes; ++j) {
            l += "+---";
        }

        l += "+\n";
        s = l;

        for (int i = 0; i < _files; ++i) {
            if (i < 9) s += " ";

            s += (i+1) + " | ";
            for (int j = 0; j < _columnes; ++j) {
                if (_tauler[i][j] == null) s += " ";
                else s += _tauler[i][j];

                s += " | ";
            }

            s += "\n" + l;
        }

        s += "     ";

        for (int j = 0; j < _columnes; ++j) {
            s += c.charAt(j) + "   ";
        }

        return s;
    }

    /**
     * @brief Llegir el fitxer de configuració.
     * @pre \p fitxerConfig és un fitxer vàlid.
     * @post S'ha llegit el fitxer de configuració i s'han guardat les vàriables que toquen segons el que s'ha llegit.
     * @param fitxerConfig El fitxer de configuració a llegir.
     */
    private void readConfigFile(String fitxerConfig){
        try{
            Gson gson = new Gson();
            ArrayList<StringMap<String>> VectorStringMap;
            Reader reader = Files.newBufferedReader(Paths.get(fitxerConfig));
            Map<?, ?> map = gson.fromJson(reader, Map.class);

            _files = ((Double)map.get("nFiles")).intValue();
            _columnes = ((Double)map.get("nCols")).intValue();

            VectorStringMap = (ArrayList<StringMap<String>>)map.get("peces");
            for(StringMap<?> pecaActual : VectorStringMap) {
                String nom = (String) pecaActual.get("nom");

                Fitxa novaPeca = new Fitxa(nom, (String) pecaActual.get("simbol"), (String) pecaActual.get("imatgeBlanca"), (String) pecaActual.get("imatgeNegra"), ((Double) pecaActual.get("valor")).intValue(), (ArrayList<ArrayList<?>>) pecaActual.get("moviments"), (ArrayList<ArrayList<?>>) pecaActual.get("movimentsInicials"), (Boolean) pecaActual.get("promocio"), (Boolean) pecaActual.get("invulnerabilitat"));

                _MapFitxes.put(nom, novaPeca);
            }

            _VectorPosInicial = (ArrayList<String>)map.get("posInicial");
            _limitEscacsSeguits = ((Double)map.get("limitEscacsSeguits")).intValue();
            _limitTornsInaccio = ((Double)map.get("limitTornsInaccio")).intValue();

            ArrayList<StringMap<String>> VectorEnrocs = (ArrayList<StringMap<String>>)map.get("enrocs");

            for(StringMap<?> auxL : VectorEnrocs) {
                Enroc auxE = new Enroc((String)auxL.get("peçaA"), (String)auxL.get("peçaB"), (Boolean)auxL.get("quiets"), (Boolean)auxL.get("buitAlMig"));
                _LlistaTipusEnrocs.add(auxE);
            }

            reader.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * @brief Diu si una posició és vàlida.
     * @pre \p true
     * @post Diu si \p posicio és una posició vàlida del tauler o no.
     * @param posicio Posició de la qual s'en vol comprovar la validesa.
     * @return
     * TRUE si la posició és vàlida;\n
     * FALSE en cas contrari.
     */
    public Boolean esPosicioValida(Posicio posicio){
        boolean res = false;

        if(posicio != null) {
            res = !((posicio.fila >= _files || posicio.fila < 0) || (posicio.columna >= _columnes || posicio.columna < 0));
        }

        return res;
    }

    /**
     * @brief Carrega el fitxer json en una partida
     * @pre \p file no buit
     * @post Fitxer carregat
     * @param file fitxer json d'on obtenir l'informació de la partida a carregar
     */
    public static Escacs carregarEscacs(File file) throws IOException {
        Gson gson = new Gson();
        Escacs object = gson.fromJson(new FileReader(file), Escacs.class); //Crea objectes peces diferents (peces) es tindria que optimitzar la creació, això es soluciuona a la funció de referTirada on es vincula de nou

        return object;
    }

    /**
     * @brief Dona una llista amb totes les fitxes que hi ha al tauler i la posició de cada una d'elles.
     * @pre \p true
     * @return Una llista de parelles de Fitxa i Posicio de totes les fitxes que hi ha al tauler i la posició de cada una d'elles.
     */
    public ArrayList<Pair<Fitxa,Posicio>> obtenirFitxes(){
        ArrayList<Pair<Fitxa,Posicio>> aux = new ArrayList<>();

        for(int i = 0; i < _files; i++) {
            for(int j = 0; j<_columnes; j++) {
                if(_tauler[i][j] != null) aux.add(new Pair(_tauler[i][j], new Posicio(i, j)));
            }
        }

        return aux;
    }

    /**
     * @brief Retorna totes les tirades possibles que pot fer un jugador amb l'estat actual de la partida.
     * @pre \p color = -1 or \p color = 1.
     * @param color El color del jugador per el qual es volen obtenir les possibles tirades.\n
     *              -1 per Blanques\n
     *              1 per Negres
     * @return Llista de totes les tirades possibles que pot fer el jugador entrat.
     * La llista està ordenada per valor de la tirada, de més a menys.
     * Per tant a la primera posició hi haurà la tirada que otorgaría més punts,
     * i a la última posició hi haurà la tirada que otorgaria menys punts.
     */
    private ArrayList<Tirada> obtenirTiradesPossiblesJugador(int color) {
        ArrayList<Tirada> res = new ArrayList<>();

        for(int i=0; i<_files; i++) {
            for(int j=0; j<_columnes; j++) {
                if(_tauler[i][j] != null) {
                    if (_tauler[i][j].direccioMoviment() == color) {
                        res.addAll(obtenirTiradesPossiblesFitxa(new Posicio(i, j), color));
                    }
                }
            }
        }

        //res.sort(Collections.reverseOrder());

        return res;
    }

    /**
     * @brief Retorna totes les tirades possibles que pot fer una fitxa amb l'estat actual de la partida.
     * @pre \p color = -1 or \p color = 1.
     * @param pOrigen La posició de la fitxa de la qual s'en volen calcular totes les tirades possibles.
     * @param color El color del jugador per el qual es volen obtenir les possibles tirades.\n
     *              -1 per Blanques\n
     *              1 per Negres
     * @return Llista de totes les tirades possibles que pot fer la fitxa entrada amb el color entrat.
     */
    public ArrayList<Tirada> obtenirTiradesPossiblesFitxa(Posicio pOrigen, int color) {
        ArrayList<Tirada> res = new ArrayList<>();

        for(int i=0; i<_files; i++) {
            for(int j=0; j<_columnes; j++) {
                Pair<Boolean,ArrayList<Pair<Posicio,Fitxa>>> auxPair;
                Posicio pDesti = new Posicio(i, j);

                auxPair = moviment(pOrigen, pDesti, color);

                if(auxPair.first) { // El moviment és vàlid
                    Tirada t = new Tirada(pOrigen, pDesti, _tauler[pOrigen.fila][pOrigen.columna], auxPair.second,false);

                    res.add(t);
                }
            }
        }

        return  res;
    }

    /**
     * @brief Commuta el joc de CPU per el color entrat.
     * @pre \p color = -1 or \p color = 1.
     * @post S'ha commutat el joc de CPU per el color entrat.
     * @param color El color del jugador per el qual es vol commutar el joc de CPU.\n
     *              -1 per Blanques\n
     *              1 per Negres
     */
    public void toggleCPU(int color) {
        if(color == -1) {
            _blanquesCPU = !_blanquesCPU;
        }
        else if(color == 1) {
            _negresCPU = !_negresCPU;
        }
    }

    /**
     * @brief Diu si el jugador entrat té el joc de CPU activat o no.
     * @pre \p color = -1 or \p color = 1.
     * @param color El color del jugador per el qual es vol consultar l'estat del joc de CPU.\n
     *              -1 per Blanques\n
     *              1 per Negres
     * @return
     * TRUE si el jugador entrat té el joc de CPU activat;\n
     * FALSE en cas contrari.
     */
    public boolean esJugadorCPU(int color) {
        boolean res = false;

        if(color == -1) res = _blanquesCPU;
        else if (color == 1) res = _negresCPU;

        return res;
    }

    /**
     * @brief Busca si s'ha definit algú tipus d'enroc amb les peces que hi ha a les posicions donades.
     * @pre \p posA i \p posB són posicions vàlides.
     * @param posA Posició de la fitxa A.
     * @param posB Posició de la fitxa B.
     * @return El tipus d'enroc definit que coincideix amb els criteris de cerca.
     * NULL si no s'ha trobat.
     */
    private Enroc buscarTipusEnroc(Posicio posA, Posicio posB) {
        Enroc res = null;
        int i = 0;
        boolean trobat = false;
        Fitxa fitxaA = _tauler[posA.fila][posA.columna];
        Fitxa fitxaB = _tauler[posB.fila][posB.columna];

        while(i<_LlistaTipusEnrocs.size() && !trobat) {
            res = _LlistaTipusEnrocs.get(i);
            trobat = fitxaA.nom().equals(res.fitxaA()) && fitxaB.nom().equals(res.fitxaB());
            i++;
        }

        if(!trobat) res = null;

        return res;
    }

    /**
     * @brief Diu si el jugador entrat té el joc de CPU activat o no.
     * @pre \p posFitxaA i \p posFitxaB són posicions vàlides.
     * @param posFitxaA Posició de la fitxa A.
     * @param posFitxaB Posició de la fitxa B.
     * @return Una Pair amb un boolean indicant si l'enroc és vàlid
     * i, si és vàlid, la TiradaEnroc que s'hauria d'aplicar.
     * Si no és vàlid la TiradaEnroc és NULL.
     */
    public Pair<Boolean, TiradaEnroc> esEnrocValid(Posicio posFitxaA, Posicio posFitxaB) {
        Pair<Boolean, TiradaEnroc> res = new Pair<>(true, null); // Hem de intentar demostrar que no és vàlid. Si no podem, assumim que és vàlid.
        Fitxa fitxaA = _tauler[posFitxaA.fila][posFitxaA.columna];
        Fitxa fitxaB = _tauler[posFitxaB.fila][posFitxaB.columna];

        if(fitxaA != null && fitxaB != null && _enrocsActivats) {
            if(esPosicioValida(posFitxaA) && esPosicioValida(posFitxaB)) {
                if (fitxaA.direccioMoviment() == fitxaB.direccioMoviment() && fitxaA.direccioMoviment() == _torn) {
                    if (posFitxaA.fila == posFitxaB.fila) { // Estan a la mateixa fila
                        Enroc eBuscat = buscarTipusEnroc(posFitxaA, posFitxaB);
                        Enroc eBuscatInvertit = buscarTipusEnroc(posFitxaB, posFitxaA);

                        if (eBuscat != null || eBuscatInvertit != null) {
                            Enroc enrocTrobat = null;

                            if (eBuscatInvertit != null && eBuscat == null) {
                                // Fem swap de les posicions.
                                Posicio aux = posFitxaA;
                                posFitxaA = posFitxaB;
                                posFitxaB = aux;
                                enrocTrobat = eBuscatInvertit;
                                Fitxa auxF = fitxaA;
                                fitxaA = fitxaB;
                                fitxaB = auxF;
                            } else enrocTrobat = eBuscat;

                            int distanciaA = posFitxaB.columna - posFitxaA.columna;
                            int direccioA = distanciaA / abs(distanciaA);
                            distanciaA = distanciaA - 1 * direccioA;
                            int columnaFinalA = posFitxaA.columna + (distanciaA / 2);
                            /*if (distanciaA % 2 != 0)*/ columnaFinalA = columnaFinalA + 1 * direccioA;

                            int direccioB = direccioA * -1;
                            int columnaFinalB = columnaFinalA + 1 * direccioB;

                            Posicio posFinalA = new Posicio(posFitxaA.fila, columnaFinalA);
                            Posicio posFinalB = new Posicio(posFitxaB.fila, columnaFinalB);

                            if (_tauler[posFinalA.fila][posFinalA.columna] == null && _tauler[posFinalB.fila][posFinalB.columna] == null) {
                                if (enrocTrobat.quiets()) {
                                    res.first = fitxaA.nVegadesMoguda() == 0 && fitxaB.nVegadesMoguda() == 0;
                                }

                                if (res.first && enrocTrobat.buitAlMig()) { // Si encara no hem demostrat que NO és vàlid
                                    // Mirem que estigui buit al mig
                                    int i = posFitxaA.columna + 1 * direccioA;
                                    boolean estaBuit = true;

                                    while (i != posFitxaB.columna && estaBuit) {
                                        estaBuit = (_tauler[posFitxaA.fila][i] == null);

                                        i = i + 1 * direccioA;
                                    }

                                    res.first = estaBuit;
                                }

                                if (res.first) { // No hem pogut demostrar que NO és vàlid
                                    // Assumim que SÍ és vàlid i construim la TiradaEnroc

                                    res.second = new TiradaEnroc(enrocTrobat, fitxaA, posFitxaA, posFinalA, fitxaB, posFitxaB, posFinalB);
                                }
                            }
                            else res.first = false;
                        }
                        else res.first = false;
                    }
                    else res.first = false;
                }
                else res.first = false;
            }
            else res.first = false;
        }
        else res.first = false;

        return res;
    }

    /**
     * @brief Aplica una tirada d'enroc al tauler.
     * @pre \p te és una TiradaEnroc vàlida.
     * @post S'ha aplicat la tirada d'enroc \p te al tauler.
     * @param te La TiradaEnroc a aplicar.
     */
    private void aplicaEnroc(TiradaEnroc te) {
        _tauler[te.posicioFitxaA_desti().fila][te.posicioFitxaA_desti().columna] = te.fitxaA();
        _tauler[te.posicioFitxaB_desti().fila][te.posicioFitxaB_desti().columna] = te.fitxaB();

        _tauler[te.posicioFitxaA_origen().fila][te.posicioFitxaA_origen().columna] = null;
        _tauler[te.posicioFitxaB_origen().fila][te.posicioFitxaB_origen().columna] = null;

        _tauler[te.posicioFitxaA_desti().fila][te.posicioFitxaA_desti().columna].sumarMogudes();
        _tauler[te.posicioFitxaB_desti().fila][te.posicioFitxaB_desti().columna].sumarMogudes();

        Posicio posRei = null;
        if(te.fitxaA().nom().equals("REI")) posRei = te.posicioFitxaA_desti();
        else if(te.fitxaB().nom().equals("REI")) posRei = te.posicioFitxaB_desti();

        if(posRei != null) {
            switch (_torn) {
                case -1: { // Blanques
                    _pos_reiB = posRei;
                    break;
                }
                case 1: { // Negres
                    _pos_reiN = posRei;
                    break;
                }
            }
        }
    }

    /**
     * @brief Desfer una tirada d'enroc en concret (aplicar moviment en ordre invers).
     * @pre \p te és una tirada d'enroc vàlida.
     * @post S'ha desfet la tirada d'enroc \p te.
     * @param te Tirada d'enroc a desfer.
     */
    private void desferTiradaEnroc(TiradaEnroc te) {
        te.vinculaFitxa_A(_tauler[te.posicioFitxaA_desti().fila][te.posicioFitxaA_desti().columna]);
        te.vinculaFitxa_B(_tauler[te.posicioFitxaB_desti().fila][te.posicioFitxaB_desti().columna]);

        _tauler[te.posicioFitxaA_desti().fila][te.posicioFitxaA_desti().columna] = null;
        _tauler[te.posicioFitxaB_desti().fila][te.posicioFitxaB_desti().columna] = null;

        _tauler[te.posicioFitxaA_origen().fila][te.posicioFitxaA_origen().columna] = te.fitxaA();
        _tauler[te.posicioFitxaB_origen().fila][te.posicioFitxaB_origen().columna] = te.fitxaB();

        _tauler[te.posicioFitxaA_origen().fila][te.posicioFitxaA_origen().columna].restarMogudes();
        _tauler[te.posicioFitxaB_origen().fila][te.posicioFitxaB_origen().columna].restarMogudes();

        Posicio posRei = null;
        if(te.fitxaA().nom().equals("REI")) posRei = te.posicioFitxaA_origen();
        else if(te.fitxaB().nom().equals("REI")) posRei = te.posicioFitxaB_origen();

        if(posRei != null) {
            switch (_torn) {
                case -1: { // Blanques
                    _pos_reiB = posRei;
                    break;
                }
                case 1: { // Negres
                    _pos_reiN = posRei;
                    break;
                }
            }
        }
    }

    /**
     * @brief Diu qui és el guanyador d'aquesta partida.
     * @pre \p true
     * @return
     * 0 si no se sap el guanyador.\n
     * -1 si guanyen les blanques.\n
     * 1 si guanyen les negres.
     */
    public int guanyador() {
        return _guanyador;
    }

    /**
     * @brief El jugador que té el torn actual es rendeix.
     * @pre \p true
     * @post El jugador que té el torn actual s'ha rendit i l'altre jugador ha guanyat.
     * També es desactiven els moviments.
     */
    public void rendirse() {
        _tornsActivats = false;
        _enrocsActivats = false;
        _guanyador = _torn*-1;
    }
    /**
     * @brief Retorna la suma del valor de totes les fitxes del tauler
     * @pre \p true
     * @post Es retorna el valor del tauler
     */
    public Integer valorTauler(){
        Integer valor = 0;
        for(int i = 0; i < _files; i++){
            for(int j = 0; j < _columnes; j++){
                if(_tauler[i][j] != null) valor += _tauler[i][j].valor();
            }
        }
        return valor;
    }
    /**
     * @brief Retorna la millor tirada a depth moviments de distància
     * @pre \p true
     * @post retorna la millor tirada
     */
    public Tirada millorTirada(){
        Tirada millorTirada = null;
        Integer bestScore = 0;
        Integer score = null;
        ArrayList<Tirada> res = new ArrayList<>();
        res = obtenirTiradesPossiblesJugador(_torn);
        Boolean isMaximizing = null;

        if(_torn == 1) {
            isMaximizing = true;
            bestScore = -9999;
        }
        else if (_torn == -1) {
            isMaximizing = false;
            bestScore = 9999;
        }

        for(Tirada tirada : res){
            aplicaMoviment(tirada,true);
            score = minimax( _maxDepthAI-1,-10000,10000, isMaximizing);
            desferTirada(tirada);
            if(isMaximizing){
                if(score > bestScore){
                    bestScore = score;
                    millorTirada = tirada;
                }

            }
            else if(!isMaximizing){
                if(score < bestScore){
                    bestScore = score;
                    millorTirada = tirada;
                }
            }
        }

        return millorTirada;
    }
    /**
     * @brief Retorna el valor de la tirada amb més valor (aplicant la funcio minimax)
     * @pre \p alpha i /p beta amb -infinit i + infinit (-10000 i 10000)
     * @post Valor de la tirada amb més valor retornat
     * @param depth Nombre de tirades a distància que vols que l'algortime miri
     * @param alpha -Infinit
     * @param beta Infinit
     * @param isMaximizing ens indica si el jugador vol aconseguir el valor més alt o el més baix
     */
    public Integer minimax(Integer depth, Integer alpha, Integer beta, Boolean isMaximizing) {
        if(depth == 0) return valorTauler();

        if (isMaximizing) {
            Integer maxEva  = -9999;
            ArrayList<Tirada> res = obtenirTiradesPossiblesJugador(1);

            for (Tirada tirada : res) {
                aplicaMoviment(tirada, true);
                Integer score = minimax(depth - 1, alpha, beta, !isMaximizing);
                desferTirada(tirada);
                maxEva = max(maxEva,score);
                alpha = max(alpha, maxEva);
                if(beta <= alpha) return maxEva;
            }
            return maxEva;
        }
        else {
            Integer minEva = 9999;
            ArrayList<Tirada> res = obtenirTiradesPossiblesJugador(-1);

            for (Tirada tirada : res) {
                aplicaMoviment(tirada, true);
                Integer score2 = minimax(depth - 1, alpha, beta, !isMaximizing);
                desferTirada(tirada);
                minEva = min(minEva,score2);
                beta = min(beta, minEva);
                if(beta <= alpha) return minEva;
            }
            return minEva;
        }
    }

    /**
     * @brief Desactiva les CPUs si ho estan
     * @pre \p true
     * @post Les CPUs han estat desectivades
     */
    public void desectivarCPUs(){
        _blanquesCPU = false;
        _negresCPU = false;
    }
}
