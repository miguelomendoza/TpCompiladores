package compiladores.enums;

/**
 *
 * @author Miguel_Mendoza
 */
public enum TokenEnum {
    LLAVE_IZQ('{', "L_LLAVE"),
    LLAVE_DER('}', "R_LLAVE"),
    CORCHETE_IZQ('[', "L_CORCHETE"),
    CORCHETE_DER(']', "R_CORCHETE"),
    COMA(',', "COMA"),
    DOS_PUNTOS(':', "DOS_PUNTOS"),
    PR_BOOLEANO_FALSE(256, "PR_FALSE"),
    PR_BOOLEANO_TRUE(257, "PR_TRUE"),
    STRING(258, "LITERAL_CADENA"),
    NUM(259, "LITERAL_NUM"),
    PR_NULL(260, "PR_NULL"),
    EOF(-1, "EOF");

    private final int id;
    private final String nombreToken;

    private TokenEnum(int id, String nombreToken) {
        this.id = id;
        this.nombreToken = nombreToken;
    }

    public int getId() {
        return id;
    }

    public String getNombreToken() {
        return nombreToken;
    }

}
