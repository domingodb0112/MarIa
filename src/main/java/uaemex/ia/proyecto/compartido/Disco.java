package uaemex.ia.proyecto.compartido;

/**
 * Modelo compartido que representa un album fisico dentro del sistema.
 * Se usa tanto en el cliente como en el servidor y se serializa por JSON.
 */
public class Disco {

    private String titulo;
    private String artista;
    private int anio;
    private String genero;
    private String formato; // "CD" o "Vinilo"

    /**
     * Constructor vacio requerido por Gson para reconstruir objetos desde JSON.
     */
    public Disco() {}

    /**
     * Crea un disco con todos sus datos principales.
     *
     * @param titulo nombre del album.
     * @param artista interprete o banda.
     * @param anio anio de publicacion.
     * @param genero genero musical.
     * @param formato formato fisico, por ejemplo CD o Vinilo.
     */
    public Disco(String titulo, String artista, int anio, String genero, String formato) {
        this.titulo = titulo;
        this.artista = artista;
        this.anio = anio;
        this.genero = genero;
        this.formato = formato;
    }

    /** @return titulo del album. */
    public String getTitulo() { return titulo; }
    /** @param titulo nuevo titulo del album. */
    public void setTitulo(String titulo) { this.titulo = titulo; }

    /** @return artista del album. */
    public String getArtista() { return artista; }
    /** @param artista nuevo artista del album. */
    public void setArtista(String artista) { this.artista = artista; }

    /** @return anio de publicacion. */
    public int getAnio() { return anio; }
    /** @param anio nuevo anio de publicacion. */
    public void setAnio(int anio) { this.anio = anio; }

    /** @return genero musical. */
    public String getGenero() { return genero; }
    /** @param genero nuevo genero musical. */
    public void setGenero(String genero) { this.genero = genero; }

    /** @return formato fisico del disco. */
    public String getFormato() { return formato; }
    /** @param formato nuevo formato fisico. */
    public void setFormato(String formato) { this.formato = formato; }

    /**
     * Genera una descripcion compacta para mostrar discos en el log del cliente.
     *
     * @return texto legible con formato, titulo, artista, anio y genero.
     */
    @Override
    public String toString() {
        return String.format("[%s] %s - %s (%d) | %s", formato, titulo, artista, anio, genero);
    }
}
