package compiladores.analizadorlexico.lexer;

import compiladores.enums.TokenEnum;
import compiladores.tabla.TablaSimbolo;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.ArrayList;

/**
 *
 * @author Miguel_Mendoza
 */
public class AnalizadorLexico {

    private int numeroLinea;
    private int numeroColumna;
    private String lexema;
    private String fuente;
    private Token token;
    private TablaSimbolo tablaSimbolo;
    private ArrayList<Token> arrayTokens; //Arreglo de tokens
    private boolean error;
    private PushbackInputStream fr;

    public AnalizadorLexico(TablaSimbolo tablaSimbolo) {
        this.numeroLinea = 1;
        this.numeroColumna = 0;
        this.token = new Token();
        this.tablaSimbolo = tablaSimbolo;
        this.arrayTokens = new ArrayList<>();
        this.error = false;
    }

    public void error(String mensajeError) {
        System.err.printf("Linea %d columna %d: Error Léxico. %s.\n", getNumeroLinea(), getNumeroColumna(), mensajeError);
        this.error = true;
    }

    public void leerFuente() {
        //Declarar una variable FileReader
        fr = null;
        try {
            if (getFuente() != null) {
                //Abrir el fichero indicado en la variable nombreFichero
                fr = new PushbackInputStream(new FileInputStream(getFuente()));
                //Leer el primer carácter
                //Se recorre el fichero hasta encontrar el carácter -1 que marca el final del fichero
                while (token.getComponenteLexico() != -1) {
                    //Mostrar en pantalla el carácter leído convertido a char
                    obtenerLexemas();
                }
            } else {
                System.err.println("Error. No se le pasó el fuente.");
            }
        } catch (FileNotFoundException e) {
            //En caso de no encontrar el fichero lanza la excepción
            //Mostrar el error producido por la excepción
            System.err.println("Error: Fichero no encontrado.");
            System.err.println(e.getMessage());
        } catch (IOException e) {
            //Operaciones en caso de error general
            System.err.println("Error de lectura del fichero.");
            System.err.println(e.getMessage());
        } finally {
            //Operaciones que se harán en cualquier caso. Si hay error o no.
            try {
                //Cerrar el fichero si se ha abierto
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException e) {
                System.err.println("Error al cerrar el fichero.");
                System.err.println(e.getMessage());
            }
        }
    }

    public void obtenerLexemas() throws IOException {
        char c;
        lexema = "";
        Entrada entrada;
        int cInt = fr.read();
        boolean acepto;
        int estado;
        int index;

        while (cInt != -1) {
            numeroColumna++;
            switch (c = (char) cInt) {
                case ' ':
                    numeroColumna++;
                    break;
                case '\t':
                    numeroColumna++;
                    break;
                case '\n':
                    numeroColumna = 0;
                    numeroLinea++;//Si es el fin de la línea incrementa el número de línea, lo imprime y sigue continua con el siguiente caracter
                    break;
                case '"':
                    lexema = "";
                    lexema += c;
                    do {
                        cInt = fr.read();
                        c = (char) cInt;
                        if (c == '"') {
                            cInt = fr.read();
                            c = (char) cInt;
                            if (c == '"') {
                                lexema += c;
                                lexema += c;
                            } else {
                                lexema += '"';
                                break;
                            }
                        } else if (cInt == -1) {
                            error("Se llegó al fin del archivo sin finalizar un literal");
                        } else {
                            lexema += c;
                        }
                    } while (isLiteralCadena(cInt));
                    lexema += '\0';
                    if (cInt != -1) {
                        fr.unread(cInt);
                    }
                    token.setPunteroEntrada(tablaSimbolo.buscar(lexema));
                    if (token.getPunteroEntrada() == null) {
                        entrada = new Entrada();
                        entrada.setLexema(lexema);
                        if (lexema.length() == 3 || lexema.equals("\"\"\"")) {

                        } else {
                            entrada.setComponenteLexico(TokenEnum.STRING.getId());
                        }
                        tablaSimbolo.insertar(entrada);
                    }
                    token.setPunteroEntrada(tablaSimbolo.buscar(lexema));
                    token.setComponenteLexico(TokenEnum.STRING.getId());
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    boolean err = false;
                    estado = 0;
                    acepto = false;
                    lexema = "";
                    lexema += c;
                    while (!acepto) {
                        switch (estado) {
                            case 0://Una secuencia netamente de dígitos, puede ocurrir . o e
                                cInt = fr.read();
                                c = (char) cInt;
                                if (isNumerico(c)) {
                                    lexema += c;
                                    estado = 0;
                                } else if (c == '.') {
                                    lexema += c;
                                    estado = 1;
                                } else if ("e".equals(String.valueOf(c))) {
                                    lexema += c;
                                    estado = 3;
                                } else {
                                    estado = 6;
                                }
                                break;
                            case 1://Un punto, debe seguir un dígito
                                cInt = fr.read();
                                c = (char) cInt;
                                if (isNumerico(c)) {
                                    lexema += c;
                                    estado = 2;
                                } else {
                                    error("No se esperaba \'" + c + "\'");
                                    estado = -1;
                                }
                                break;
                            case 2://La fraccion decimal, pueden seguir los digitos o e
                                cInt = fr.read();
                                c = (char) cInt;
                                if (isNumerico(c)) {
                                    lexema += c;
                                    estado = 2;
                                } else if ("e".equals(String.valueOf(c).toLowerCase())) {
                                    lexema += c;
                                    estado = 3;
                                } else {
                                    estado = 6;
                                }
                                break;
                            case 3: //Una e, puede seguir +, - o una secuencia de digitos
                                cInt = fr.read();
                                c = (char) cInt;
                                if (c == '+' || c == '-') {
                                    lexema += c;
                                    estado = 4;
                                } else if (isNumerico(c)) {
                                    lexema += c;
                                    estado = 5;
                                } else {
                                    error("No se esperaba '" + c + "'");
                                    estado = -1;
                                }
                                break;
                            case 4: //Necesariamente debe venir por lo menos un digito
                                cInt = fr.read();
                                c = (char) cInt;
                                if (isNumerico(c)) {
                                    lexema += c;
                                    estado = 5;
                                } else {
                                    error("No se esperaba '" + c + "'");
                                    estado = -1;
                                }
                                break;
                            case 5: //Secuencia de digitos correspondiente al exponente
                                cInt = fr.read();
                                c = (char) cInt;
                                if (isNumerico(c)) {
                                    lexema += c;
                                    estado = 5;
                                } else {
                                    estado = 6;
                                }
                                break;
                            case 6:
                                if (cInt != 1) {
                                    fr.unread(cInt);
                                }
                                acepto = true;
                                token.setPunteroEntrada(tablaSimbolo.buscar(lexema));
                                if (token.getPunteroEntrada() == null) {
                                    entrada = new Entrada();
                                    entrada.setLexema(lexema);
                                    entrada.setComponenteLexico(TokenEnum.NUM.getId());
                                    tablaSimbolo.insertar(entrada);
                                    token.setPunteroEntrada(tablaSimbolo.buscar(lexema));
                                }
                                token.setComponenteLexico(TokenEnum.NUM.getId());
                                break;
                            case -1:
                                if (cInt == -1) {
                                    error("No se esperaba el fin del archivo");
                                }
                                acepto = true;
                                err = true;
                                break;
                        }
                    }
                    lexema += '\0';
                    if (!err) {
                        token.setPunteroEntrada(tablaSimbolo.buscar(lexema));
                        if (token.getPunteroEntrada() == null) {
                            entrada = new Entrada();
                            entrada.setLexema(lexema);
                            entrada.setComponenteLexico(TokenEnum.NUM.getId());
                            tablaSimbolo.insertar(entrada);
                            token.setPunteroEntrada(tablaSimbolo.buscar(lexema));
                        }
                        token.setComponenteLexico(TokenEnum.NUM.getId());
                    }

                    break;
                case '{':
                    token.setComponenteLexico(TokenEnum.LLAVE_IZQ.getId());
                    token.setPunteroEntrada(tablaSimbolo.buscar("{"));
                    break;
                case '}':
                    token.setComponenteLexico(TokenEnum.LLAVE_DER.getId());
                    token.setPunteroEntrada(tablaSimbolo.buscar("}"));
                    break;
                case '[':
                    token.setComponenteLexico(TokenEnum.CORCHETE_IZQ.getId());
                    token.setPunteroEntrada(tablaSimbolo.buscar("["));
                    break;
                case ']':
                    token.setComponenteLexico(TokenEnum.CORCHETE_DER.getId());
                    token.setPunteroEntrada(tablaSimbolo.buscar("]"));
                    break;
                case ',':
                    token.setComponenteLexico(TokenEnum.COMA.getId());
                    token.setPunteroEntrada(tablaSimbolo.buscar(","));
                    break;
                case ':':
                    token.setComponenteLexico(TokenEnum.DOS_PUNTOS.getId());
                    token.setPunteroEntrada(tablaSimbolo.buscar(":"));
                    break;
                case 't':
                case 'T':
                    lexema = "";
                    index = 0;
                    while (index < 4) {
                        lexema += c;
                        cInt = fr.read();
                        c = (char) cInt;
                        index++;
                    }
                    if (cInt != -1) {
                        fr.unread(cInt);
                    }
                    if ("true".equals(lexema) || "TRUE".equals(lexema)) {
                        token.setComponenteLexico(TokenEnum.PR_BOOLEANO_TRUE.getId());
                        token.setPunteroEntrada(tablaSimbolo.buscar("true"));
                    } else {
                        error("no es valor booleano válido");
                    }
                    break;
                case 'f':
                case 'F':
                    lexema = "";
                    index = 0;
                    while (index < 5) {
                        lexema += c;
                        cInt = fr.read();
                        c = (char) cInt;
                        index++;
                    }
                    if (cInt != -1) {
                        fr.unread(cInt);
                    }
                    if ("false".equals(lexema) || "FALSE".equals(lexema)) {
                        token.setComponenteLexico(TokenEnum.PR_BOOLEANO_FALSE.getId());
                        token.setPunteroEntrada(tablaSimbolo.buscar("false"));
                    } else {
                        error("no es valor booleano válido");
                    }
                    break;
                case 'n':
                case 'N':
                    lexema = "";
                    index = 0;
                    while (index < 4) {
                        lexema += c;
                        cInt = fr.read();
                        c = (char) cInt;
                        index++;
                    }
                    if (cInt != -1) {
                        fr.unread(cInt);
                    }
                    if ("null".equals(lexema) || "NULL".equals(lexema)) {
                        token.setComponenteLexico(TokenEnum.PR_NULL.getId());
                        token.setPunteroEntrada(tablaSimbolo.buscar("null"));
                    } else {
                        error("no es valor nulo válido");
                    }
                    break;
                default:
                    error(c + " no esperado");
                    break;
            }
            if (token.getPunteroEntrada() != null) {
                arrayTokens.add(token);
                token = new Token();
            }
            cInt = fr.read();
        }

        if (cInt == -1) {
            entrada = new Entrada();
            entrada.setLexema(TokenEnum.EOF.getNombreToken());
            entrada.setComponenteLexico(TokenEnum.EOF.getId());
            tablaSimbolo.insertar(entrada);
            token.setComponenteLexico(TokenEnum.EOF.getId());
            token.setPunteroEntrada(entrada);
            arrayTokens.add(token);
        }
    }

    private boolean isNumerico(int cInt) {
        String s = "";
        char c = (char) cInt;
        s += c;
        return s.matches("[0-9]");
    }

    private boolean isLiteralCadena(int cInt) {
        String s = "";
        char c = (char) cInt;
        s += c;
        return s.matches(".*");
    }

    public int getNumeroLinea() {
        return numeroLinea;
    }

    public void setNumeroLinea(int numeroLinea) {
        this.numeroLinea = numeroLinea;
    }

    public int getNumeroColumna() {
        return numeroColumna;
    }

    public void setNumeroColumna(int numeroColumna) {
        this.numeroColumna = numeroColumna;
    }

    public String getFuente() {
        return fuente;
    }

    public void setFuente(String fuente) {
        this.fuente = fuente;
    }

    public ArrayList<Token> getArrayTokens() {
        return arrayTokens;
    }

    public void setArrayTokens(ArrayList<Token> arrayTokens) {
        this.arrayTokens = arrayTokens;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }
}
