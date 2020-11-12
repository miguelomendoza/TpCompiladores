package compiladores.analizadorsintactico.parser;

import compiladores.analizadorlexico.lexer.AnalizadorLexico;
import compiladores.analizadorlexico.lexer.Token;
import compiladores.enums.TokenEnum;
import compiladores.tabla.TablaSimbolo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Miguel_Mendoza
 */
public class AnalizadorSintactico {

    private Token token;
    private AnalizadorLexico analizadorLexico;
    private TablaSimbolo tablaSimbolo;
    private ArrayList<Token> arrayTokens; //Arreglo de tokens
    private boolean error;
    private int posicion;
    private List<Integer> syncToken;
    private Map<String, List<Integer>> siguiente;
    private String index;

    public AnalizadorSintactico(TablaSimbolo tablaSimbolo, AnalizadorLexico analizadorLexico) {
        this.tablaSimbolo = tablaSimbolo;
        this.analizadorLexico = analizadorLexico;
        this.error = true;
    }

    public void init() {
        this.arrayTokens = analizadorLexico.getArrayTokens();
        this.token = arrayTokens.get(0);
        this.posicion = 0;
        this.error = false;
        this.index = "";
        conjuntoSiguiente();
        json();
    }

    public void json() {
        element();
    }

    public void element() {
        if (token.getComponenteLexico() == TokenEnum.CORCHETE_IZQ.getId()) {
            array();
        } else if (token.getComponenteLexico() == TokenEnum.LLAVE_IZQ.getId()) {
            object();
        } else {
            error("Error sintactico en el ELEMENT: Se esperaba " + TokenEnum.CORCHETE_IZQ.getNombreToken() + " o " 
                    + TokenEnum.LLAVE_IZQ.getNombreToken() + ", vino " + token.getPunteroEntrada().getLexema());
        }
    }

    public void array() {
        if (TokenEnum.CORCHETE_IZQ.getId() == token.getComponenteLexico()) {
            match("[");
            arrayPrima();
        } else {
            error("Error sintactico en el ARRAY: Se esperaba " + TokenEnum.CORCHETE_IZQ.getNombreToken() + ", vino " 
                    + token.getPunteroEntrada().getLexema());
        }
    }

    public void arrayPrima() {
        this.index = "ARRAY_PRIMA";
        if (TokenEnum.CORCHETE_IZQ.getId() == token.getComponenteLexico() || TokenEnum.LLAVE_IZQ.getId() == token.getComponenteLexico()) {
            elementList();
            match("]");
        } else if(TokenEnum.CORCHETE_DER.getId() == token.getComponenteLexico()){
            match("]");
        } else {
            error("Error sintactico en el ARRAY_PRIMA: Se esperaba " + TokenEnum.CORCHETE_IZQ.getNombreToken() + " o " 
                    + TokenEnum.LLAVE_IZQ.getNombreToken() + ", vino " + token.getPunteroEntrada().getLexema());
        }
    }

    public void elementList() {
        element();
        elementListPrima();
    }

    public void elementListPrima() {
        if (TokenEnum.COMA.getId() == token.getComponenteLexico()) {
            match(",");
            element();
            elementListPrima();
        }
    }

    public void object() {
        this.index = "OBJECT";
        if (TokenEnum.LLAVE_IZQ.getId() == token.getComponenteLexico()) {
            match("{");
            objectPrima();
        } else {
            error("Error sintactico en el OBJECT: Se esperaba " + TokenEnum.LLAVE_IZQ.getNombreToken() + ", vino " 
                    + token.getPunteroEntrada().getLexema());
        }
    }

    public void objectPrima() {
        this.index = "OBJECT_PRIMA";
        if (TokenEnum.STRING.getId() == token.getComponenteLexico()) {
            attributeList();
            match("}");
        } else if(TokenEnum.LLAVE_DER.getId() == token.getComponenteLexico()){
            match("}");
        } else {
            error("Error sintactico en el ARRAY_PRIMA: Se esperaba " + TokenEnum.CORCHETE_IZQ.getNombreToken() + " o " 
                    + TokenEnum.LLAVE_IZQ.getNombreToken() + ", vino " + token.getPunteroEntrada().getLexema());
        }
    }

    public void attributeList() {
        attribute();
        attributeListPrima();
    }

    public void attributeListPrima() {
        if (TokenEnum.COMA.getId() == token.getComponenteLexico()) {
            match(",");
            attribute();
            attributeListPrima();
        }
    }

    public void attribute() {
        this.index = "ATTRIBUTE";
        if (token.getComponenteLexico() == TokenEnum.STRING.getId()) {
            attributeName();
            match(":");
            attributeValue();
        } else {
            error("Error sintactico en el ATTRIBUTE: Se esperaba " 
                    + TokenEnum.STRING.getNombreToken() + ", vino '" + token.getPunteroEntrada().getLexema() +"'.");
        }
    }

    public void attributeName() {
        this.index = "ATTRIBUTENAME";
        if (token.getComponenteLexico() == TokenEnum.STRING.getId() && token.getComponenteLexico() != TokenEnum.COMA.getId()) {
            match(token.getPunteroEntrada().getLexema());
        } else {
            error("Error sintactico en el ATTRIBUTE_NAME: Se esperaba " 
                    + TokenEnum.STRING.getNombreToken() + ", vino '" + token.getPunteroEntrada().getLexema() +"'.");
        }
    }

    public void attributeValue() {
        this.index = "ATTRIBUTEVALUE";
        if (TokenEnum.PR_BOOLEANO_FALSE.getId() == token.getComponenteLexico()) {
            match(token.getPunteroEntrada().getLexema());
        } else if (TokenEnum.PR_BOOLEANO_TRUE.getId() == token.getComponenteLexico()) {
            match(token.getPunteroEntrada().getLexema());
        } else if (TokenEnum.PR_NULL.getId() == token.getComponenteLexico()) {
            match(token.getPunteroEntrada().getLexema());
        } else if (TokenEnum.NUM.getId() == token.getComponenteLexico()) {
            match(token.getPunteroEntrada().getLexema());
        } else if (TokenEnum.STRING.getId() == token.getComponenteLexico()) {
            match(token.getPunteroEntrada().getLexema());
        } else if (TokenEnum.LLAVE_IZQ.getId() == token.getComponenteLexico() || TokenEnum.CORCHETE_IZQ.getId() == token.getComponenteLexico()) {
            element();
        } else {
            error("Error sintactico en el ATTRIBUTE_VALUE: Se esperaba " 
                    + TokenEnum.PR_BOOLEANO_FALSE.getNombreToken() + ", " + TokenEnum.PR_BOOLEANO_TRUE.getNombreToken() + ", "
                    + TokenEnum.PR_NULL.getNombreToken() + ", " + TokenEnum.NUM.getNombreToken() + ", " + TokenEnum.STRING.getNombreToken() + " o ELEMENT, vino '"
                    + token.getPunteroEntrada().getLexema() + "'");
        }
    }

    public void match(String expToken) {
        if (expToken.equals(token.getPunteroEntrada().getLexema())) {
            getToken();
        } else {
            error("Error sintactico en el matching: Se esperaba '" 
                    + expToken + "', vino '" + token.getPunteroEntrada().getLexema() +"'.");
        }
    }

    public void getToken() {
        posicion++;
        if (posicion < arrayTokens.size()) {
            token = arrayTokens.get(posicion);
        }
    }

    public void error(String mensajeError) {
        this.error = true;
        System.err.println(mensajeError);
        scan();
    }

    private void scan() {
        getToken();
        synchronize();
    }

    private void synchronize(){
        boolean sync = false;
        syncToken = siguiente.get(index);
        do {
            for (Integer syncT : syncToken) {
                if (syncT.equals(token.getComponenteLexico())) {
                    sync = true;
                    break;
                }
            }
            if (!sync) {
                getToken();
            }
        } while (token.getComponenteLexico() != TokenEnum.EOF.getId() && !sync);
        verificarSiEsEof();
    }
    
    private void verificarSiEsEof() {
        if (token.getComponenteLexico() == TokenEnum.EOF.getId()) {
            System.err.println("Error inesperado. Se llegó al final del archivo.");
            System.exit(0);
        }
    }
    
    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    private void conjuntoSiguiente(){
        siguiente = new HashMap<>();
        siguiente.put("JSON", new ArrayList<>());
        siguiente.put("ELEMENT", Arrays.asList(TokenEnum.LLAVE_DER.getId(), TokenEnum.CORCHETE_DER.getId(), TokenEnum.COMA.getId()));
        siguiente.put("ARRAY", Arrays.asList(TokenEnum.LLAVE_DER.getId(), TokenEnum.CORCHETE_DER.getId(), TokenEnum.COMA.getId()));
        siguiente.put("ARRAY_PRIMA", Arrays.asList(TokenEnum.LLAVE_DER.getId(), TokenEnum.COMA.getId()));
        siguiente.put("OBJECT", Arrays.asList(TokenEnum.LLAVE_DER.getId(), TokenEnum.CORCHETE_DER.getId(), TokenEnum.COMA.getId()));
        siguiente.put("OBJECT_PRIMA", Arrays.asList(TokenEnum.CORCHETE_DER.getId(), TokenEnum.LLAVE_DER.getId(), TokenEnum.COMA.getId()));
        siguiente.put("ELEMENTLIST", Arrays.asList(TokenEnum.CORCHETE_DER.getId()));
        siguiente.put("ELEMENTLIST_PRIMA", Arrays.asList(TokenEnum.CORCHETE_DER.getId()));
        siguiente.put("ATTRIBUTELIST", Arrays.asList(TokenEnum.LLAVE_DER.getId()));
        siguiente.put("ATTRIBUTELIST_PRIMA", Arrays.asList(TokenEnum.LLAVE_DER.getId()));
        siguiente.put("ATTRIBUTE", Arrays.asList(TokenEnum.LLAVE_DER.getId(), TokenEnum.COMA.getId()));
        siguiente.put("ATTRIBUTENAME", Arrays.asList(TokenEnum.DOS_PUNTOS.getId()));
        siguiente.put("ATTRIBUTEVALUE", Arrays.asList(TokenEnum.LLAVE_DER.getId(), TokenEnum.COMA.getId()));
    }

    public ArrayList<Token> getArrayTokens() {
        return arrayTokens;
    }

    public void setArrayTokens(ArrayList<Token> arrayTokens) {
        this.arrayTokens = arrayTokens;
    }
}
