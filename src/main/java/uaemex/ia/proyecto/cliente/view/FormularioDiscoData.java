package uaemex.ia.proyecto.cliente.view;

/**
 * Objeto simple que transporta los valores escritos en el formulario antes de validarlos.
 */
class FormularioDiscoData {
    final String titulo;
    final String artista;
    final String anio;
    final String genero;
    final String formato;

    /**
     * Crea una captura inmutable de los campos del formulario.
     *
     * @param titulo titulo capturado.
     * @param artista artista capturado.
     * @param anio anio capturado como texto para validarlo despues.
     * @param genero genero capturado.
     * @param formato formato fisico seleccionado.
     */
    FormularioDiscoData(String titulo, String artista, String anio, String genero, String formato) {
        this.titulo = titulo;
        this.artista = artista;
        this.anio = anio;
        this.genero = genero;
        this.formato = formato;
    }
}
