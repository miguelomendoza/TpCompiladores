package compiladores.analizadorlexico.lexer;

/**
 *
 * @author Miguel_Mendoza
 */
public class Entrada {

    private int componenteLexico;
    private String lexema;

    public Entrada() {
    
    }

    public int getComponenteLexico() {
        return componenteLexico;
    }

    public void setComponenteLexico(int componenteLexico) {
        this.componenteLexico = componenteLexico;
    }

    public String getLexema() {
        return lexema;
    }

    public void setLexema(String lexema) {
        this.lexema = lexema;
    }

    @Override
    public String toString() {
        return "compLex: " + this.componenteLexico + ", lexema: " + this.lexema;
    }
    
    
}
