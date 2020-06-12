/**  @file Fitxa.java
 @brief Enumeració dels diferents tipus de fitxa.
 */

import java.util.ArrayList;

/**
 * @class Fitxa
 * @brief Tipus de fitxa
 */
public class Fitxa implements Cloneable {

	private String _nom;
	private String _simbol;
	private String _imatgeBlanca;
	private String _imatgeNegra;
	private Integer _valor;
	private ArrayList<ArrayList<?>> _moviments;
	private ArrayList<ArrayList<?>> _movimentsInicials;
	private Boolean _promocio;
	private Boolean _invulnerabilitat;
	private int _nVegadesMoguda;

	private int _direccioMoviment; /// Atribut que indica si aquestes fitxes es mouen amunt (-1 BLANQUES) o avall (1 NEGRES)

	/**
	 * @brief Constructor Fitxa.
	 * @pre \p true
	 * @post S'ha creat una fitxa nova.
	 * @param nom El nom de la fitxa.
	 * @param simbol El símbol (caràcter) que es farà servir per representar aquesta fitxa en mode text.
	 * @param imatgeBlanca El nom del fitxer que conté la imatge que es farà servir per representar la versió blanca d'aquesta fitxa en mode gràfic.
	 * @param imatgeNegra El nom del fitxer que conté la imatge que es farà servir per representar la versió negre d'aquesta fitxa en mode gràfic.
	 * @param valor El valor que té aquesta fitxa (la puntuació que s'otorga si es mata).
	 * @param moviments Llista de tots els moviments que pot fer aquesta fitxa.
	 * @param movimentsInicials Llista dels moviments INICIALS que pot fer aquesta fitxa (només els pot fer si no s'ha mogut mai).
	 * @param promocio Indica si aquesta fitxa té la capacitat de ser promocionada o no.
	 * @param invulnerabilitat Indica si aquesta fitxa és invulnerable o no. Si ho és, ningú la pot matar en cap cas.
	 */
	Fitxa(String nom, String simbol, String imatgeBlanca, String imatgeNegra, Integer valor, ArrayList<ArrayList<?>> moviments, ArrayList<ArrayList<?>> movimentsInicials, Boolean promocio, Boolean invulnerabilitat)  {
		_nom  = nom;
		_simbol = simbol;
		_imatgeBlanca = imatgeBlanca;
		_imatgeNegra = imatgeNegra;
		_valor = valor;
		_moviments = moviments;
		_movimentsInicials = movimentsInicials;
		_promocio = promocio;
		_invulnerabilitat = invulnerabilitat;
		_direccioMoviment = 0; // Es té que assignar direcció, per defecte 0 que indica que no ha estat assignada encara.
		_nVegadesMoguda = 0;
	}

	/**
	 * @pre \p true
	 * @return La direcció en que es pot moure aquesta fitxa.\n
	 * -1 són Blanques;\n
	 * 1 són Negres.
	 */
	public int direccioMoviment() {
		return _direccioMoviment;
	}

	/**
	 * @brief Assigna una direcció de moviment (defineix el color de la fitxa).
	 * @pre \p dir=-1 o \p dir=1
	 * @post S'ha assignat la direccó en que es pot moure aquesta fitxa (el color de la fitxa).
	 * @param dir La direcció (color) que es vol assignar a aquesta fitxa.\n
	 * -1 la definirà de color Blanc;\n
	 * 1 la definirà de color Negre.
	 */
	public void posarDireccioMoviment(int dir) {
		_direccioMoviment = dir;
	}

	/**
	 * @brief Diu si una fitxa és de l'equip contrari.
	 * @pre \p f != NULL
	 * @param f La fitxa que es vol mirar si és de l'equip contrari.
	 * @return
	 * TRUE si \p f és de l'equip contrari;\n
	 * FALSE en cas contrari.
	 */
	public boolean esEquipContrari(Fitxa f) {

		return _direccioMoviment != f._direccioMoviment;
	}

	/**
	 * @brief Diu el valor de la fitxa.
	 * @pre \p true
	 * @return El valor d'aquesta fitxa relatiu al seu color (equip):\n
	 * valor<0 si és de l'equip Blanc.\n
	 * valor>0 si és de l'equip Negre.
	 */
	public int valor() {
		return _valor*_direccioMoviment;
	}

	/**
	 * @brief Augmenta les vegades que s'ha mogut aquesta fitxa.
	 * @pre \p true
	 * @post S'ha incrementat en 1 el nombre de vegades que s'ha mogut aquesta fitxa.
	 */
	public void sumarMogudes() {
		_nVegadesMoguda++;
	}

	/**
	 * @brief Disminueix les vegades que s'ha mogut aquesta fitxa.
	 * @pre \p true
	 * @post S'ha decrementat en 1 el nombre de vegades que s'ha mogut aquesta fitxa. Mai passarà per sota de 0.
	 */
	public void restarMogudes() {
		if(_nVegadesMoguda>0) _nVegadesMoguda--;
	}

	/**
	 * @brief Diu si la fitxa és invulnerable.
	 * @pre \p true
	 * @return
	 * TRUE si la fitxa és invulnerable;\n
	 * FALSE en cas contrari.
	 */
	public Boolean esInvulnerable() {
		return _invulnerabilitat;
	}

	/**
	 * @brief Diu si la fitxa té la capacitat de ser promocionada.
	 * @pre \p true
	 * @return
	 * TRUE si la fitxa té la capacitat de ser promocionada;\n
	 * FALSE en cas contrari.
	 */
	public Boolean potPromocionar() {
		return _promocio;
	}

	/**
	 * @brief Es promociona el tipus d'aquesta fitxa al d'una altra.
	 * @pre \p fNova != NULL
	 * @post S'ha promocionat el tipus d'aquesta fitxa al de la fitxa passada per paràmetre.
	 * @param fNova El tipus de fitxa al qual es vol promocionar.
	 */
	public void promocionar_a_fitxaNova(Fitxa fNova) {
		_nom = fNova._nom;
		_simbol = fNova._simbol;
		_imatgeBlanca = fNova._imatgeBlanca;
		_imatgeNegra = fNova._imatgeNegra;
		_valor = fNova._valor;
		_moviments = fNova._moviments;
		_movimentsInicials = fNova._movimentsInicials;
		_invulnerabilitat = fNova._invulnerabilitat;
		_promocio = !_promocio;
	}

	/**
	 * @brief Es fa una còpia d'aquesta fitxa.
	 * @pre \p true
	 * @return Un Object que és una còpia d'aquesta fitxa. S'ha de fer cast a Fitxa.
	 */
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * @brief Converteix aquesta fitxa a un string.
	 * @pre \p true
	 * @post S'ha convertit aquesta fitxa a un string.
	 * @return Un string construit a partir del símbol d'aquesta fitxa en:\n
	 * MINÚSCULES si és Blanca.\n
	 * MAJÚSCULES si és Negra.\n
	 */
	@Override
	public String toString() {
		String res = "";

		if(_direccioMoviment == 1) res = _simbol;
		else if(_direccioMoviment == -1) res = _simbol.toLowerCase();
		else res = "-";

		return res;
	}

	/**
	 * @brief Diu si un moviment està permès.
	 * @pre \p true
	 * @post S'ha assignat la direccó en que es pot moure aquesta fitxa (el color de la fitxa).
	 * @param mov Una llista del tipus [A, B, C, D].\n
	 * A i B són valors enters i formen un vector espaial NO UNITARI, v=(A,B), que indica direcció, sentit i deplaçament.\n
	 * C indica si s'està intentant matar amb aquest moviment. C=0 vol dir que NO. C=1 vol dir que sí.
	 * D indica si s'està intentant saltar amb aquest moviment. D=0 vol dir que NO. D=1 vol dir que sí.
	 * @return Una Pair de 2 booleans on:\n
	 * FIRST indica si el moviment entrat, \p mov, coincideix amb algún dels moviments possibles definits per aquesta fitxa.\n
	 * SECOND indica si, essent el cas que el moviment és vàlid, el moviment definit coincident permet saltar matant les fitxes que hi hagi al mig.
	 */
	public Pair<Boolean, Boolean> esMovimentPermes(ArrayList<Integer> mov) {
		Pair<Boolean, Boolean> r = new Pair<>(false, false);
		Boolean permes = false;
		Boolean esPotMatarSaltant = false;
		int i = 0;

		if(mov == null) return r;
		if(mov.size() != 4) return r;
		if(mov.get(0)==null || mov.get(1)==null || mov.get(2)==null || mov.get(3)==null) return r;
		if(mov.get(2)>1 || mov.get(2)<0) return r;
		if(mov.get(3)>1 || mov.get(3)<0) return r;

		// Si NO s'ha mogut mai també tindrem en compte els moviments inicials.
		if(_nVegadesMoguda == 0) _moviments.addAll(_movimentsInicials);

		while(!permes && i<_moviments.size()) {
			boolean segueix = true;
			int j = 0;

			while(j<2 && segueix) { // Comprovem la A i la B
				if(String.valueOf(_moviments.get(i).get(j)).equals("a") || String.valueOf(_moviments.get(i).get(j)).equals("m")) { // A or B és un String
					if(String.valueOf(_moviments.get(i).get(j)).equals("m")) {
						int A_intentat = mov.get(j)*_direccioMoviment;

						if(A_intentat < 0) segueix = false;
						else if(String.valueOf(_moviments.get(i).get(j+1)).equals("m")){
							int B_intentat = mov.get(j+1)*_direccioMoviment;

							if(B_intentat < 0) segueix = false;
							else if(A_intentat != B_intentat) segueix = false;
							else j++; // FORÇAR SORTIDA DE BUCLE
						}
						else if(String.valueOf(_moviments.get(i).get(j+1)).equals("n")){
							int B_intentat = mov.get(j+1)*_direccioMoviment;

							if(B_intentat < 0) segueix = false;
							else j++; // FORÇAR SORTIDA DE BUCLE
						}
						else if(_moviments.get(i).get(j+1).equals("b")) {
							j++; // FORÇAR SORTIDA DE BUCLE
						}
					}
					else if(_moviments.get(i).get(j+1).equals("a")) {
						int A_intentat = mov.get(j)*_direccioMoviment;
						int B_intentat = mov.get(j+1)*_direccioMoviment;

						if(A_intentat != B_intentat) segueix = false;
						else j++; // FORÇAR SORTIDA DE BUCLE
					}
					else if(_moviments.get(i).get(j+1).equals("-a")) {
						int A_intentat = mov.get(j)*_direccioMoviment;
						int B_intentat = mov.get(j+1)*_direccioMoviment;

						if(A_intentat != B_intentat*-1) segueix = false;
						else j++; // FORÇAR SORTIDA DE BUCLE
					}
					else if(_moviments.get(i).get(j+1).equals("b")) {
						j++; // FORÇAR SORTIDA DE BUCLE
					}
					else if(_moviments.get(i).get(j+1).equals("n")) {
						if(mov.get(j+1)*_direccioMoviment > 0) j++; // FORÇAR SORTIDA DE BUCLE
					}
				}
				else { // A or B és un número
					int A_or_B_definit = ((Double)_moviments.get(i).get(j)).intValue();
					int A_or_B_intentat = mov.get(j)*_direccioMoviment;

					if(A_or_B_definit != A_or_B_intentat) {
						segueix = false;
					}
				}

				j++;
			}

			permes = segueix;

			if(permes) { // Comprovem la C
				Integer C_definit = ((Double)_moviments.get(i).get(2)).intValue();
				Integer C_intentat = mov.get(2);

				if(C_intentat > C_definit) permes = false;
				else if(C_definit==2 && C_intentat!=1) permes = false;
			}

			if(permes) { // Comprovem la D
				Integer D_definit = ((Double)_moviments.get(i).get(3)).intValue();
				Integer D_intentat = mov.get(3);

				if(D_intentat > D_definit) permes = false;
				if(D_definit == 2) esPotMatarSaltant = true;
			}

			i++;
		}

		// Si NO s'ha mogut mai tornem a deixa la llista de moviments com estava, sense els moviments inicials.
		if(_nVegadesMoguda == 0) _moviments.removeAll(_movimentsInicials);

		r.first = permes;
		r.second = esPotMatarSaltant;

		return r;
	}

	/**
	 * @pre \p true
	 * @return Nom del fitxer que conté la imatge del tipus i color d'aquesta fitxa.
	 */
	public String imatge(){
		if(_direccioMoviment == -1) return  _imatgeBlanca;
		else if(_direccioMoviment == 1) return  _imatgeNegra;
		else return "noTexture.png";
	}

	/**
	 * @pre \p true
	 * @return Número de vegades que s'ha mogut aquesta fitxa.
	 */
	public int nVegadesMoguda(){
		return _nVegadesMoguda;
	}

	/**
	 * @pre \p true
	 * @return El nom d'aquesta fitxa.
	 */
	public String nom(){
		return _nom;
	}
}