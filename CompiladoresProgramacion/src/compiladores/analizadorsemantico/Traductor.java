package compiladores.analizadorsemantico;

import compiladores.analizadorlexico.lexer.Token;
import compiladores.analizadorsintactico.parser.AnalizadorSintactico;
import compiladores.enums.TokenEnum;
import compiladores.tabla.TablaSimbolo;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Miguel_Mendoza
 */
public class Traductor {

    private Token token;
    private AnalizadorSintactico analizadorSintactico;
    private TablaSimbolo tablaSimbolo;
    private List<Token> arrayTokens; //Arreglo de tokens
    private List<String> parents; //Arreglo de tokens
    private boolean error;
    private int posicion;
    private List<Integer> syncToken;
    private Map<String, List<Integer>> siguiente;
    private String index;
    StringBuilder sb;

    public Traductor(TablaSimbolo tablaSimbolo, AnalizadorSintactico analizadorSintactico) {
        this.tablaSimbolo = tablaSimbolo;
        this.analizadorSintactico = analizadorSintactico;
        this.error = true;
    }

    public void init() {
        this.arrayTokens = analizadorSintactico.getArrayTokens();
        this.token = arrayTokens.get(0);
        this.parents = new ArrayList<>();
        this.posicion = 0;
        this.error = false;
        this.index = "";
        sb = new StringBuilder();
        jsonTrad();
        System.out.println(sb.toString());
    }

    public void jsonTrad() {
        sb.append("<dato>");
        elementTrad();
        sb.append("</dato>");
    }

    public void elementTrad() {
        if (token.getComponenteLexico() == TokenEnum.CORCHETE_IZQ.getId()) {
            arrayTrad();
        } else if (token.getComponenteLexico() == TokenEnum.LLAVE_IZQ.getId()) {
            if (estaEnArray()) {
                String nameChildren = getChildName();
                attributeNameTrad(nameChildren, "A");
                objectTrad();
                attributeNameTrad(nameChildren, "C");
            } else{
                objectTrad();
            }
        }
    }

    public void arrayTrad() {
        if (TokenEnum.CORCHETE_IZQ.getId() == token.getComponenteLexico()) {
            addChildName();
            match("[");
            arrayPrimaTrad();
        } else {
            error("Error sintactico en el ARRAY: Se esperaba " + TokenEnum.CORCHETE_IZQ.getNombreToken() + ", vino " 
                    + token.getPunteroEntrada().getLexema());
        }
    }

    public void arrayPrimaTrad() {
        if (TokenEnum.CORCHETE_IZQ.getId() == token.getComponenteLexico() || TokenEnum.LLAVE_IZQ.getId() == token.getComponenteLexico()) {
            elementListTrad();
            removeChildName();
            match("]");
        } else if(TokenEnum.CORCHETE_DER.getId() == token.getComponenteLexico()){
            removeChildName();
            match("]");
        }
    }

    public void elementListTrad() {
        elementTrad();
        elementListPrimaTrad();
    }

    public void elementListPrimaTrad() {
        if (TokenEnum.COMA.getId() == token.getComponenteLexico()) {
            match(",");
            elementTrad();
            elementListPrimaTrad();
        }
    }

    public void objectTrad() {
        if (TokenEnum.LLAVE_IZQ.getId() == token.getComponenteLexico()) {
            getToken();
            objectPrimaTrad();
        }
    }

    public void objectPrimaTrad() {
        if (TokenEnum.STRING.getId() == token.getComponenteLexico()) {
            attributeListTrad();
            getToken();
        } else if (TokenEnum.LLAVE_DER.getId() == token.getComponenteLexico()) {
            getToken();
        }
    }

    public void attributeListTrad() {
        attributeTrad();
        attributeListPrimaTrad();
    }

    public void attributeListPrimaTrad() {
        if (TokenEnum.COMA.getId() == token.getComponenteLexico()) {
            getToken();
            attributeTrad();
            attributeListPrimaTrad();
        }
    }

    public void attributeTrad() {
        String name = token.getPunteroEntrada().getLexema();
        name = name.trim().replace("\"", "");
        if (token.getComponenteLexico() == TokenEnum.STRING.getId()) {
            attributeNameTrad(name, "A");
            getToken();
            getToken();
            attributeValueTrad();
            attributeNameTrad(name, "C");
        }
    }

    public void attributeNameTrad(String name, String tipo) {
        if ("A".equals(tipo)) {
            sb.append("<").append(name).append(">");
        } else if ("C".equals(tipo)) {
            sb.append("</").append(name).append(">");
        }

    }

    public void attributeValueTrad() {
        String name = token.getPunteroEntrada().getLexema();
        name = name.trim();
        if (TokenEnum.PR_BOOLEANO_FALSE.getId() == token.getComponenteLexico() || TokenEnum.PR_BOOLEANO_TRUE.getId() == token.getComponenteLexico()
                || TokenEnum.PR_NULL.getId() == token.getComponenteLexico() || TokenEnum.NUM.getId() == token.getComponenteLexico()
                || TokenEnum.STRING.getId() == token.getComponenteLexico()) {
            sb.append(name);
            getToken();
        } else if (TokenEnum.LLAVE_IZQ.getId() == token.getComponenteLexico() || TokenEnum.CORCHETE_IZQ.getId() == token.getComponenteLexico()) {
            elementTrad();
        }
    }

    private boolean estaEnArray() {
        int pos = posicion - 1;
        int[] compLex = new int[3];
        int i = 0;
        while (pos >= 0 && pos < arrayTokens.size() && i < 3) {
            compLex[i] = arrayTokens.get(pos).getComponenteLexico();
            i++;
            pos--;
        }
        if (compLex.length == 3) {
            if ((compLex[0] == TokenEnum.COMA.getId() && compLex[1] == TokenEnum.LLAVE_DER.getId()) || (compLex[0] == TokenEnum.CORCHETE_IZQ.getId())) {
                return true;
            }
        }
        return false;
    }

    private void match(String expToken) {
        if (expToken.equals(token.getPunteroEntrada().getLexema())) {
            getToken();
        }
    }

    private void addChildName() {
        int pos = posicion - 2;
        if (pos > 0 && pos < arrayTokens.size()) {
            String name = arrayTokens.get(pos).getPunteroEntrada().getLexema().replace("\"", "").trim();
            parents.add(name);
        }
    }

    private void removeChildName() {
        parents.remove(parents.size()-1);
    }

    public String getParentName() {
        return parents.get(parents.size()-1);
    }
    
    private String getChildName() {
        String nameParent = getParentName();
        if (nameParent.endsWith("es")) {
            return nameParent.substring(0, nameParent.length()-2);
        } else if (nameParent.endsWith("s")) {
            return nameParent.substring(0, nameParent.length()-1);
        } else {
            return nameParent + "_hijo";
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
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }
}
