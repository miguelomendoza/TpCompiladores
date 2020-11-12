package compiladores.main;

import compiladores.analizadorlexico.lexer.AnalizadorLexico;
import compiladores.analizadorsemantico.Traductor;
import compiladores.analizadorsintactico.parser.AnalizadorSintactico;
import compiladores.tabla.TablaSimbolo;

/**
 *
 * @author Miguel_Mendoza
 */
public class Main {

    /**
     * Método main del compilador
     *
     * @param args
     */
    public static void main(String[] args) {
        //Instancia la tabla de símbolos
        TablaSimbolo tablaSimbolo = new TablaSimbolo();
        //Inicializa la tabla de símbolos
        tablaSimbolo.initTablaSimbolos();
        //Inicializa el análisis léxico
        AnalizadorLexico lexer = new AnalizadorLexico(tablaSimbolo);
        //Se le pasa el fuente a leer
        lexer.setFuente("fuente.txt");
        //Se inicia la lectura del fuente
        lexer.leerFuente();
        //Inicializa el análisis sintáctico
        AnalizadorSintactico parser = new AnalizadorSintactico(tablaSimbolo, lexer);
        if (!lexer.isError()) {
            System.out.println("\033[32m1) Análisis léxico sin errores.");
            parser.init();
        }
        Traductor traductor = new Traductor(tablaSimbolo, parser);
        if (!parser.isError()) {
            System.out.println("\033[32m2) Análisis sintáctico sin errores.");
            traductor.init();
        }
        //Termina de ejecutarse el programa
        System.exit(0);
    }
}
