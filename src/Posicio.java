/** @file Posicio.java
 @brief Una posició d'un tauler.
 */

/** @class Posicio
 @brief Parell fila, columna
 */
public class Posicio {
    public int fila;
    public int columna;

    public Posicio(int f, int c) {
        fila = f;
        columna = c;
    }

    @Override
    public String toString() {
        String c = "abcdefghijklmnopqrstuvwxyz";
        return "(" + c.charAt(columna) + (fila+1) + ")";
    }

    @Override
    public boolean equals(Object o) {
        boolean r = false;

        if (o != null && o instanceof Posicio) {
            Posicio p = (Posicio)o;

            r = (this.fila == p.fila && this.columna==p.columna);
        }

        return r;
    }
}
