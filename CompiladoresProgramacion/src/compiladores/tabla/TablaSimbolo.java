package compiladores.tabla;

import compiladores.analizadorlexico.lexer.Entrada;
import compiladores.enums.TokenEnum;
import java.util.HashMap;

/**
 *
 * @author Miguel_Mendoza
 */
public class TablaSimbolo implements TablaSimboloInterface {

    private HashMap<String, Entrada> tabla;

    public TablaSimbolo() {
        tabla = new HashMap<>();
    }

    public HashMap<String, Entrada> getTabla() {
        return tabla;
    }

    public void setTabla(HashMap<String, Entrada> tabla) {
        this.tabla = tabla;
    }

    /**
     * Insertar una entrada en la tabla
     *
     * @param e
     */
    @Override
    public void insertar(Entrada e) {
        tabla.put(e.getLexema(), e);
    }

    /**
     * Busca una entrada en la tabla de símbolos
     * 
     * @param key
     * @return
     */
    @Override
    public Entrada buscar(String key) {
        return tabla.get(key);
    }

    /**
     * Inserta la entrada en la tabla de símbolos.
     * 
     * @param lexema
     * @param compLex
     */
    @Override
    public void insertarTablaSimbolos(String lexema, int compLex) {
        Entrada e = new Entrada();
        e.setComponenteLexico(compLex);
        e.setLexema(lexema);
        insertar(e);
    }

    /**
     * Inicializa la tabla de símbolos
     * 
     */
    @Override
    public void initTablaSimbolos() {
        insertarTablaSimbolos("{", TokenEnum.LLAVE_IZQ.getId());
        insertarTablaSimbolos("}", TokenEnum.LLAVE_DER.getId());
        insertarTablaSimbolos("[", TokenEnum.CORCHETE_IZQ.getId());
        insertarTablaSimbolos("]", TokenEnum.CORCHETE_DER.getId());
        insertarTablaSimbolos(",", TokenEnum.COMA.getId());
        insertarTablaSimbolos(":", TokenEnum.DOS_PUNTOS.getId());
        insertarTablaSimbolos("false", TokenEnum.PR_BOOLEANO_FALSE.getId());
        insertarTablaSimbolos("true", TokenEnum.PR_BOOLEANO_TRUE.getId());
        insertarTablaSimbolos("null", TokenEnum.PR_NULL.getId());
    }
}
