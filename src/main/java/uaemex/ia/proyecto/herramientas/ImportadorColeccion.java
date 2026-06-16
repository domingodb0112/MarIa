package uaemex.ia.proyecto.herramientas;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import uaemex.ia.proyecto.compartido.Disco;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Genera la base local de discos usada por el servidor para pruebas de busqueda,
 * perfilado de gustos y recomendaciones.
 *
 * <p>El flujo usa un catalogo curado interno hispanohablante, del cual se
 * seleccionan 500 albumes por defecto. La seleccion esta balanceada en cuatro
 * generos principales: Rock en Espanol, Pop Latino, Salsa y Cumbia.
 *
 * <p>Uso:
 * mvn exec:java -Dexec.mainClass="uaemex.ia.proyecto.herramientas.ImportadorColeccion"
 * mvn exec:java -Dexec.mainClass="uaemex.ia.proyecto.herramientas.ImportadorColeccion" -Dexec.args="--limit 500"
 *
 * <p>Alternativa sin exec-maven-plugin:
 * java -cp target/classes:lib/gson-2.10.1.jar uaemex.ia.proyecto.herramientas.ImportadorColeccion
 */
public class ImportadorColeccion {

    private static final Path JSON_POR_DEFECTO = Paths.get("data", "coleccion.json");
    private static final int LIMITE_POR_DEFECTO = 500;

    /**
     * Punto de entrada de la herramienta de generacion de datos.
     *
     * <p>Lee argumentos simples, construye la coleccion curada y reemplaza el
     * JSON de salida. No arranca el servidor ni modifica la base en memoria; solo
     * prepara el archivo que luego carga {@code Database}.
     *
     * @param args argumentos opcionales: {@code --limit N} y {@code --output ruta}.
     * @throws IOException si no se puede escribir el archivo JSON de destino.
     */
    public static void main(String[] args) throws IOException {
        Opciones opciones = Opciones.desde(args);
        List<Disco> discos = crearCatalogoCurado(opciones.limite);

        escribirJson(opciones.jsonSalida, discos);
        System.out.println("Coleccion escrita en " + opciones.jsonSalida + " con " + discos.size() + " discos.");
    }

    /**
     * Construye el catalogo hispanohablante usado para poblar data/coleccion.json.
     *
     * <p>Cada linea usa cuatro campos separados por tabuladores:
     * {@code titulo, artista, anio, genero}. El formato fisico se calcula de
     * forma determinista: albumes previos a 1990 se marcan como Vinilo y los de
     * 1990 en adelante como CD. Esta regla mantiene variedad historica sin tener
     * que repetir una quinta columna en cientos de entradas.
     *
     * @param limite cantidad maxima de discos a devolver.
     * @return lista balanceada por genero, lista para serializarse como JSON.
     */
    private static List<Disco> crearCatalogoCurado(int limite) {
        List<Disco> discos = new ArrayList<>();
        // El orden del bloque agrupa artistas y escenas para facilitar revision
        // humana; la seleccion final se balancea por genero mas abajo.
        String[] lineas = {
                "Signos	Soda Stereo	1986	Rock en Espanol",
                "Cancion Animal	Soda Stereo	1990	Rock en Espanol",
                "Doble Vida	Soda Stereo	1988	Rock en Espanol",
                "Nada Personal	Soda Stereo	1985	Rock en Espanol",
                "Soda Stereo	Soda Stereo	1984	Rock en Espanol",
                "Dynamo	Soda Stereo	1992	Rock en Espanol",
                "Suenos Stereo	Soda Stereo	1995	Rock en Espanol",
                "Comfort y Musica Para Volar	Soda Stereo	1996	Rock en Espanol",
                "Colores Santos	Cerati y Melero	1992	Rock en Espanol",
                "Amor Amarillo	Gustavo Cerati	1993	Rock en Espanol",
                "Bocanada	Gustavo Cerati	1999	Rock en Espanol",
                "Siempre Es Hoy	Gustavo Cerati	2002	Rock en Espanol",
                "Ahi Vamos	Gustavo Cerati	2006	Rock en Espanol",
                "Fuerza Natural	Gustavo Cerati	2009	Rock en Espanol",
                "11 Episodios Sinfonicos	Gustavo Cerati	2001	Rock en Espanol",
                "Canciones Elegidas 93-04	Gustavo Cerati	2004	Rock en Espanol",
                "La La La	Luis Alberto Spinetta y Fito Paez	1986	Rock en Espanol",
                "Artaud	Pescado Rabioso	1973	Rock en Espanol",
                "El Jardin de los Presentes	Invisible	1976	Rock en Espanol",
                "Almendra	Almendra	1969	Rock en Espanol",
                "Kamikaze	Luis Alberto Spinetta	1982	Rock en Espanol",
                "Prive	Luis Alberto Spinetta	1986	Rock en Espanol",
                "Tester de Violencia	Luis Alberto Spinetta	1988	Rock en Espanol",
                "Peluson of Milk	Luis Alberto Spinetta	1991	Rock en Espanol",
                "Para Los Arboles	Luis Alberto Spinetta	2003	Rock en Espanol",
                "Pan	Luis Alberto Spinetta	2005	Rock en Espanol",
                "Clics Modernos	Charly Garcia	1983	Rock en Espanol",
                "Piano Bar	Charly Garcia	1984	Rock en Espanol",
                "Yendo de la Cama al Living	Charly Garcia	1982	Rock en Espanol",
                "Parte de la Religion	Charly Garcia	1987	Rock en Espanol",
                "Como Conseguir Chicas	Charly Garcia	1989	Rock en Espanol",
                "Filosofia Barata y Zapatos de Goma	Charly Garcia	1990	Rock en Espanol",
                "La Hija de la Lagrima	Charly Garcia	1994	Rock en Espanol",
                "Say No More	Charly Garcia	1996	Rock en Espanol",
                "Vida	Sui Generis	1972	Rock en Espanol",
                "Confesiones de Invierno	Sui Generis	1973	Rock en Espanol",
                "Pequenas Anecdotas Sobre las Instituciones	Sui Generis	1974	Rock en Espanol",
                "La Grasa de las Capitales	Seru Giran	1979	Rock en Espanol",
                "Bicicleta	Seru Giran	1980	Rock en Espanol",
                "Peperina	Seru Giran	1981	Rock en Espanol",
                "Seru 92	Seru Giran	1992	Rock en Espanol",
                "Giros	Fito Paez	1985	Rock en Espanol",
                "Ciudad de Pobres Corazones	Fito Paez	1987	Rock en Espanol",
                "Tercer Mundo	Fito Paez	1990	Rock en Espanol",
                "El Amor Despues del Amor	Fito Paez	1992	Rock en Espanol",
                "Circo Beat	Fito Paez	1994	Rock en Espanol",
                "Euforia	Fito Paez	1996	Rock en Espanol",
                "Abre	Fito Paez	1999	Rock en Espanol",
                "Naturaleza Sangre	Fito Paez	2003	Rock en Espanol",
                "Rodolfo	Fito Paez	2007	Rock en Espanol",
                "Confia	Fito Paez	2010	Rock en Espanol",
                "Del 63	Fito Paez	1984	Rock en Espanol",
                "Tinta Roja	Andres Calamaro	2006	Rock en Espanol",
                "Alta Suciedad	Andres Calamaro	1997	Rock en Espanol",
                "Honestidad Brutal	Andres Calamaro	1999	Rock en Espanol",
                "El Salmon	Andres Calamaro	2000	Rock en Espanol",
                "El Cantante	Andres Calamaro	2004	Rock en Espanol",
                "La Lengua Popular	Andres Calamaro	2007	Rock en Espanol",
                "Bohemio	Andres Calamaro	2013	Rock en Espanol",
                "Nadie Sale Vivo de Aqui	Andres Calamaro	1989	Rock en Espanol",
                "Vasos y Besos	Los Abuelos de la Nada	1983	Rock en Espanol",
                "Himno de Mi Corazon	Los Abuelos de la Nada	1984	Rock en Espanol",
                "Los Abuelos de la Nada	Los Abuelos de la Nada	1982	Rock en Espanol",
                "After Chabon	Sumo	1987	Rock en Espanol",
                "Llegando los Monos	Sumo	1986	Rock en Espanol",
                "Divididos por la Felicidad	Sumo	1985	Rock en Espanol",
                "Acariciando lo Aspero	Divididos	1991	Rock en Espanol",
                "La Era de la Boludez	Divididos	1993	Rock en Espanol",
                "Otro Le Travaladna	Divididos	1995	Rock en Espanol",
                "Narigon del Siglo	Divididos	2000	Rock en Espanol",
                "Vengo del Placard de Otro	Divididos	2002	Rock en Espanol",
                "La Renga	La Renga	1998	Rock en Espanol",
                "Despedazado por Mil Partes	La Renga	1996	Rock en Espanol",
                "Bailando en Una Pata	La Renga	1995	Rock en Espanol",
                "Esquivando Charcos	La Renga	1991	Rock en Espanol",
                "Detonador de Suenos	La Renga	2003	Rock en Espanol",
                "Truenotierra	La Renga	2006	Rock en Espanol",
                "Carajo	Carajo	2002	Rock en Espanol",
                "Atrapasuenos	Carajo	2004	Rock en Espanol",
                "Inmundo	Carajo	2007	Rock en Espanol",
                "El Leon	Los Fabulosos Cadillacs	1992	Rock en Espanol",
                "Vasos Vacios	Los Fabulosos Cadillacs	1993	Rock en Espanol",
                "Rey Azucar	Los Fabulosos Cadillacs	1995	Rock en Espanol",
                "Fabulosos Calavera	Los Fabulosos Cadillacs	1997	Rock en Espanol",
                "La Marcha del Golazo Solitario	Los Fabulosos Cadillacs	1999	Rock en Espanol",
                "Yo Te Avise	Los Fabulosos Cadillacs	1987	Rock en Espanol",
                "Bares y Fondas	Los Fabulosos Cadillacs	1986	Rock en Espanol",
                "Los Cadillacs Volumen 5	Los Fabulosos Cadillacs	1990	Rock en Espanol",
                "Big Yuyo	Los Pericos	1992	Rock en Espanol",
                "Ritual	Los Pericos	1994	Rock en Espanol",
                "Pampas Reggae	Los Pericos	1994	Rock en Espanol",
                "Desde Cero	Los Pericos	2002	Rock en Espanol",
                "Pericos and Friends	Los Pericos	2010	Rock en Espanol",
                "El Silencio	Caifanes	1992	Rock en Espanol",
                "Caifanes	Caifanes	1988	Rock en Espanol",
                "El Diablito	Caifanes	1990	Rock en Espanol",
                "El Nervio del Volcan	Caifanes	1994	Rock en Espanol",
                "La Historia	Caifanes	1997	Rock en Espanol",
                "Bajo el Azul de Tu Misterio	Jaguares	1999	Rock en Espanol",
                "El Equilibrio de los Jaguares	Jaguares	1996	Rock en Espanol",
                "Cuando la Sangre Galopa	Jaguares	2001	Rock en Espanol",
                "Cronicas de un Laberinto	Jaguares	2005	Rock en Espanol",
                "45	Jaguares	2008	Rock en Espanol",
                "Re	Cafe Tacvba	1994	Rock en Espanol",
                "Cafe Tacuba	Cafe Tacvba	1992	Rock en Espanol",
                "Avalancha de Exitos	Cafe Tacvba	1996	Rock en Espanol",
                "Reves/Yo Soy	Cafe Tacvba	1999	Rock en Espanol",
                "Cuatro Caminos	Cafe Tacvba	2003	Rock en Espanol",
                "Sino	Cafe Tacvba	2007	Rock en Espanol",
                "El Objeto Antes Llamado Disco	Cafe Tacvba	2012	Rock en Espanol",
                "Jei Beibi	Cafe Tacvba	2017	Rock en Espanol",
                "Donde Jugaran los Ninos?	Mana	1992	Rock en Espanol",
                "Falta Amor	Mana	1990	Rock en Espanol",
                "Cuando los Angeles Lloran	Mana	1995	Rock en Espanol",
                "Suenos Liquidos	Mana	1997	Rock en Espanol",
                "Revolucion de Amor	Mana	2002	Rock en Espanol",
                "Amar Es Combatir	Mana	2006	Rock en Espanol",
                "Drama y Luz	Mana	2011	Rock en Espanol",
                "Cama Incendiada	Mana	2015	Rock en Espanol",
                "Senderos de Traicion	Heroes del Silencio	1990	Rock en Espanol",
                "El Mar No Cesa	Heroes del Silencio	1988	Rock en Espanol",
                "El Espiritu del Vino	Heroes del Silencio	1993	Rock en Espanol",
                "Avalancha	Heroes del Silencio	1995	Rock en Espanol",
                "Parasiempre	Heroes del Silencio	1996	Rock en Espanol",
                "Pequeno	Enrique Bunbury	1999	Rock en Espanol",
                "Flamingos	Enrique Bunbury	2002	Rock en Espanol",
                "El Viaje a Ninguna Parte	Enrique Bunbury	2004	Rock en Espanol",
                "Hellville de Luxe	Enrique Bunbury	2008	Rock en Espanol",
                "Las Consecuencias	Enrique Bunbury	2010	Rock en Espanol",
                "Palosanto	Enrique Bunbury	2013	Rock en Espanol",
                "Expectativas	Enrique Bunbury	2017	Rock en Espanol",
                "La Hija del Loco	Enrique Bunbury	2002	Rock en Espanol",
                "Rockas Vivas	Miguel Mateos/Zas	1985	Rock en Espanol",
                "Solos en America	Miguel Mateos/Zas	1986	Rock en Espanol",
                "Atado a un Sentimiento	Miguel Mateos	1987	Rock en Espanol",
                "Obsesion	Miguel Mateos	1990	Rock en Espanol",
                "Huevos	Miguel Mateos	1983	Rock en Espanol",
                "La Voz de los 80	Los Prisioneros	1984	Rock en Espanol",
                "Pateando Piedras	Los Prisioneros	1986	Rock en Espanol",
                "La Cultura de la Basura	Los Prisioneros	1987	Rock en Espanol",
                "Corazones	Los Prisioneros	1990	Rock en Espanol",
                "Los Prisioneros	Los Prisioneros	2003	Rock en Espanol",
                "Fome	Los Tres	1997	Rock en Espanol",
                "Los Tres	Los Tres	1991	Rock en Espanol",
                "Se Remata el Siglo	Los Tres	1993	Rock en Espanol",
                "La Espada y la Pared	Los Tres	1995	Rock en Espanol",
                "La Sangre en el Cuerpo	Los Tres	1999	Rock en Espanol",
                "Vida de Perros	Los Bunkers	2005	Rock en Espanol",
                "Los Bunkers	Los Bunkers	2001	Rock en Espanol",
                "Cancion de Lejos	Los Bunkers	2002	Rock en Espanol",
                "La Culpa	Los Bunkers	2003	Rock en Espanol",
                "Barrio Estacion	Los Bunkers	2008	Rock en Espanol",
                "Musica Libre	Los Bunkers	2010	Rock en Espanol",
                "El Dorado	Aterciopelados	1995	Rock en Espanol",
                "La Pipa de la Paz	Aterciopelados	1996	Rock en Espanol",
                "Caribe Atomico	Aterciopelados	1998	Rock en Espanol",
                "Gozo Poderoso	Aterciopelados	2000	Rock en Espanol",
                "Oye	Aterciopelados	2006	Rock en Espanol",
                "Rio	Aterciopelados	2008	Rock en Espanol",
                "La Cabeza	La Gusana Ciega	1997	Rock en Espanol",
                "Superbee	La Gusana Ciega	1999	Rock en Espanol",
                "Correspondencia Interna	La Gusana Ciega	2008	Rock en Espanol",
                "El Camino Mas Largo	La Gusana Ciega	2015	Rock en Espanol",
                "Rocanlover	Zoe	2003	Rock en Espanol",
                "Memo Rex Commander y el Corazon Atomico de la Via Lactea	Zoe	2006	Rock en Espanol",
                "Reptilectric	Zoe	2008	Rock en Espanol",
                "Programaton	Zoe	2013	Rock en Espanol",
                "Aztlan	Zoe	2018	Rock en Espanol",
                "Sonidos de Karmatica Resonancia	Zoe	2021	Rock en Espanol",
                "Moctezuma	Porter	2014	Rock en Espanol",
                "Donde los Ponys Pastan	Porter	2005	Rock en Espanol",
                "Atemahawke	Porter	2007	Rock en Espanol",
                "Las Batallas	Porter	2019	Rock en Espanol",
                "Kinky	Kinky	2002	Rock en Espanol",
                "Atlas	Kinky	2003	Rock en Espanol",
                "Reina	Kinky	2006	Rock en Espanol",
                "Barracuda	Kinky	2008	Rock en Espanol",
                "Sueno de la Maquina	Kinky	2011	Rock en Espanol",
                "Dance and Dense Denso	Molotov	2003	Rock en Espanol",
                "Donde Jugaran las Ninas?	Molotov	1997	Rock en Espanol",
                "Apocalypshit	Molotov	1999	Rock en Espanol",
                "Con Todo Respeto	Molotov	2004	Rock en Espanol",
                "Eternamiente	Molotov	2007	Rock en Espanol",
                "Agua Maldita	Molotov	2014	Rock en Espanol",
                "Fobia	Fobia	1990	Rock en Espanol",
                "Mundo Feliz	Fobia	1991	Rock en Espanol",
                "Leche	Fobia	1993	Rock en Espanol",
                "Amor Chiquito	Fobia	1995	Rock en Espanol",
                "Rosa Venus	Fobia	2005	Rock en Espanol",
                "Destruye Hogares	Fobia	2012	Rock en Espanol",
                "Dulce Beat	Belanova	2005	Pop Latino",
                "Cocktail	Belanova	2003	Pop Latino",
                "Fantasia Pop	Belanova	2007	Pop Latino",
                "Sueno Electro I	Belanova	2010	Pop Latino",
                "Sueno Electro II	Belanova	2011	Pop Latino",
                "Viaje al Centro del Corazon	Belanova	2018	Pop Latino",
                "Pies Descalzos	Shakira	1995	Pop Latino",
                "Donde Estan los Ladrones?	Shakira	1998	Pop Latino",
                "Fijacion Oral Vol. 1	Shakira	2005	Pop Latino",
                "Sale el Sol	Shakira	2010	Pop Latino",
                "El Dorado	Shakira	2017	Pop Latino",
                "Las Mujeres Ya No Lloran	Shakira	2024	Pop Latino",
                "Magia	Shakira	1991	Pop Latino",
                "Peligro	Shakira	1993	Pop Latino",
                "Mi Tierra	Gloria Estefan	1993	Pop Latino",
                "Abriendo Puertas	Gloria Estefan	1995	Pop Latino",
                "Alma Caribena	Gloria Estefan	2000	Pop Latino",
                "90 Millas	Gloria Estefan	2007	Pop Latino",
                "Mi Reflejo	Christina Aguilera	2000	Pop Latino",
                "A Mi Manera	Gipsy Kings	1987	Pop Latino",
                "Este Mundo	Gipsy Kings	1991	Pop Latino",
                "Cosas del Amor	Enrique Iglesias	1998	Pop Latino",
                "Vivir	Enrique Iglesias	1997	Pop Latino",
                "Enrique Iglesias	Enrique Iglesias	1995	Pop Latino",
                "Quizas	Enrique Iglesias	2002	Pop Latino",
                "Sex and Love	Enrique Iglesias	2014	Pop Latino",
                "Subeme la Radio	Enrique Iglesias	2017	Pop Latino",
                "Vuelve	Ricky Martin	1998	Pop Latino",
                "A Medio Vivir	Ricky Martin	1995	Pop Latino",
                "Me Amaras	Ricky Martin	1993	Pop Latino",
                "Ricky Martin	Ricky Martin	1991	Pop Latino",
                "Almas del Silencio	Ricky Martin	2003	Pop Latino",
                "Musica + Alma + Sexo	Ricky Martin	2011	Pop Latino",
                "A Quien Quiera Escuchar	Ricky Martin	2015	Pop Latino",
                "La Copa de la Vida	Ricky Martin	1998	Pop Latino",
                "Romance	Luis Miguel	1991	Pop Latino",
                "Segundo Romance	Luis Miguel	1994	Pop Latino",
                "Nada Es Igual	Luis Miguel	1996	Pop Latino",
                "Romances	Luis Miguel	1997	Pop Latino",
                "Amarte Es un Placer	Luis Miguel	1999	Pop Latino",
                "Mis Romances	Luis Miguel	2001	Pop Latino",
                "Mexico en la Piel	Luis Miguel	2004	Pop Latino",
                "Complices	Luis Miguel	2008	Pop Latino",
                "Luis Miguel	Luis Miguel	2010	Pop Latino",
                "Busca una Mujer	Luis Miguel	1988	Pop Latino",
                "Aries	Luis Miguel	1993	Pop Latino",
                "20 Anos	Luis Miguel	1990	Pop Latino",
                "Mas	Alejandro Sanz	1997	Pop Latino",
                "Viviendo Deprisa	Alejandro Sanz	1991	Pop Latino",
                "Si Tu Me Miras	Alejandro Sanz	1993	Pop Latino",
                "3	Alejandro Sanz	1995	Pop Latino",
                "El Alma al Aire	Alejandro Sanz	2000	Pop Latino",
                "No Es Lo Mismo	Alejandro Sanz	2003	Pop Latino",
                "El Tren de los Momentos	Alejandro Sanz	2006	Pop Latino",
                "Paraiso Express	Alejandro Sanz	2009	Pop Latino",
                "La Musica No Se Toca	Alejandro Sanz	2012	Pop Latino",
                "Sirope	Alejandro Sanz	2015	Pop Latino",
                "11 Razones	Aitana	2020	Pop Latino",
                "Spoiler	Aitana	2019	Pop Latino",
                "Alpha	Aitana	2023	Pop Latino",
                "Un Susurro en la Tormenta	La Oreja de Van Gogh	2020	Pop Latino",
                "Dile al Sol	La Oreja de Van Gogh	1998	Pop Latino",
                "El Viaje de Copperpot	La Oreja de Van Gogh	2000	Pop Latino",
                "Lo Que Te Conte Mientras Te Hacias la Dormida	La Oreja de Van Gogh	2003	Pop Latino",
                "Guapa	La Oreja de Van Gogh	2006	Pop Latino",
                "A las Cinco en el Astoria	La Oreja de Van Gogh	2008	Pop Latino",
                "Cometas por el Cielo	La Oreja de Van Gogh	2011	Pop Latino",
                "El Planeta Imaginario	La Oreja de Van Gogh	2016	Pop Latino",
                "Descanso Dominical	Mecano	1988	Pop Latino",
                "Entre el Cielo y el Suelo	Mecano	1986	Pop Latino",
                "Mecano	Mecano	1982	Pop Latino",
                "Donde Esta el Pais de las Hadas?	Mecano	1983	Pop Latino",
                "Ya Viene el Sol	Mecano	1984	Pop Latino",
                "Aidalai	Mecano	1991	Pop Latino",
                "Hombres G	Hombres G	1985	Pop Latino",
                "La Cagaste... Burt Lancaster	Hombres G	1986	Pop Latino",
                "Estamos Locos... o Que?	Hombres G	1987	Pop Latino",
                "Agitar Antes de Usar	Hombres G	1988	Pop Latino",
                "Esta Es Tu Vida	Hombres G	1990	Pop Latino",
                "Historia del Bikini	Hombres G	1992	Pop Latino",
                "Devuelveme a Mi Chica	Hombres G	1985	Pop Latino",
                "Un Hombre Solo	Julio Iglesias	1987	Pop Latino",
                "De Nina a Mujer	Julio Iglesias	1981	Pop Latino",
                "1100 Bel Air Place	Julio Iglesias	1984	Pop Latino",
                "Tango	Julio Iglesias	1996	Pop Latino",
                "Mexico	Julio Iglesias	2015	Pop Latino",
                "Querida	Juan Gabriel	1984	Pop Latino",
                "Recuerdos II	Juan Gabriel	1984	Pop Latino",
                "Pensamientos	Juan Gabriel	1986	Pop Latino",
                "Gracias Por Esperar	Juan Gabriel	1994	Pop Latino",
                "Abrazame Muy Fuerte	Juan Gabriel	2000	Pop Latino",
                "Inocente de Ti	Juan Gabriel	2003	Pop Latino",
                "Los Duo	Juan Gabriel	2015	Pop Latino",
                "Hasta Que Te Conoci	Juan Gabriel	1986	Pop Latino",
                "Secretos	Jose Jose	1983	Pop Latino",
                "Volcan	Jose Jose	1978	Pop Latino",
                "Si Me Dejas Ahora	Jose Jose	1979	Pop Latino",
                "Amor Amor	Jose Jose	1980	Pop Latino",
                "Gracias	Jose Jose	1981	Pop Latino",
                "Reflexiones	Jose Jose	1984	Pop Latino",
                "Promesas	Jose Jose	1985	Pop Latino",
                "Siempre Contigo	Jose Jose	1986	Pop Latino",
                "Soy Asi	Jose Jose	1987	Pop Latino",
                "Que Es el Amor	Jose Jose	1989	Pop Latino",
                "Otra Vez	Jose Jose	1990	Pop Latino",
                "Amiga Mia	Yuri	1980	Pop Latino",
                "Yo Te Amo Te Amo	Yuri	1981	Pop Latino",
                "Llena de Dulzura	Yuri	1981	Pop Latino",
                "Isla del Sol	Yuri	1988	Pop Latino",
                "Soy Libre	Yuri	1991	Pop Latino",
                "Nueva Era	Yuri	1993	Pop Latino",
                "Primera Fila	Yuri	2017	Pop Latino",
                "Mundo de Cristal	Thalia	1991	Pop Latino",
                "Love	Thalia	1992	Pop Latino",
                "En Extasis	Thalia	1995	Pop Latino",
                "Amor a la Mexicana	Thalia	1997	Pop Latino",
                "Arrasando	Thalia	2000	Pop Latino",
                "Thalia	Thalia	2002	Pop Latino",
                "El Sexto Sentido	Thalia	2005	Pop Latino",
                "Primera Fila	Thalia	2009	Pop Latino",
                "Desamorfosis	Thalia	2021	Pop Latino",
                "Tierra de Nadie	Ana Gabriel	1988	Pop Latino",
                "Quien Como Tu	Ana Gabriel	1989	Pop Latino",
                "Mi Mexico	Ana Gabriel	1991	Pop Latino",
                "Luna	Ana Gabriel	1993	Pop Latino",
                "Vivencias	Ana Gabriel	1996	Pop Latino",
                "Soy Como Soy	Ana Gabriel	1999	Pop Latino",
                "Altos de Chavon	Ana Gabriel	2013	Pop Latino",
                "Cama y Mesa	Roberto Carlos	1981	Pop Latino",
                "Roberto Carlos	Roberto Carlos	1974	Pop Latino",
                "Amigo	Roberto Carlos	1977	Pop Latino",
                "Emociones	Roberto Carlos	1981	Pop Latino",
                "Aguanta Corazon	Amanda Miguel	1984	Pop Latino",
                "El Sonido Vol. 1	Amanda Miguel	1981	Pop Latino",
                "El Pecado	Amanda Miguel	1983	Pop Latino",
                "Rompecorazones	Gualberto Castro	1982	Pop Latino",
                "De Mi Enamorate	Daniela Romo	1986	Pop Latino",
                "Mujer de Todos, Mujer de Nadie	Daniela Romo	1986	Pop Latino",
                "Quiero Amanecer con Alguien	Daniela Romo	1989	Pop Latino",
                "Mentiras	Daniela Romo	1983	Pop Latino",
                "Azul	Cristian Castro	2001	Pop Latino",
                "Agua Nueva	Cristian Castro	1992	Pop Latino",
                "El Camino del Alma	Cristian Castro	1994	Pop Latino",
                "Lo Mejor de Mi	Cristian Castro	1997	Pop Latino",
                "Mi Vida Sin Tu Amor	Cristian Castro	1999	Pop Latino",
                "Amar Es	Cristian Castro	2003	Pop Latino",
                "El Culpable Soy Yo	Cristian Castro	2009	Pop Latino",
                "Tiempo de Vals	Chayanne	1990	Pop Latino",
                "Provocame	Chayanne	1992	Pop Latino",
                "Influencias	Chayanne	1994	Pop Latino",
                "Volver a Nacer	Chayanne	1996	Pop Latino",
                "Atado a Tu Amor	Chayanne	1998	Pop Latino",
                "Simplemente	Chayanne	2000	Pop Latino",
                "Sincero	Chayanne	2003	Pop Latino",
                "Mi Tiempo	Chayanne	2007	Pop Latino",
                "A Solas con Chayanne	Chayanne	2012	Pop Latino",
                "Natalia Lafourcade	Natalia Lafourcade	2002	Pop Latino",
                "Casa	Natalia y La Forquetina	2005	Pop Latino",
                "Hu Hu Hu	Natalia Lafourcade	2009	Pop Latino",
                "Mujer Divina	Natalia Lafourcade	2012	Pop Latino",
                "Hasta la Raiz	Natalia Lafourcade	2015	Pop Latino",
                "Musas Vol. 1	Natalia Lafourcade	2017	Pop Latino",
                "Musas Vol. 2	Natalia Lafourcade	2018	Pop Latino",
                "Un Canto por Mexico Vol. 1	Natalia Lafourcade	2020	Pop Latino",
                "De Todas las Flores	Natalia Lafourcade	2022	Pop Latino",
                "Aqui	Julieta Venegas	1997	Pop Latino",
                "Bueninvento	Julieta Venegas	2000	Pop Latino",
                "Si	Julieta Venegas	2003	Pop Latino",
                "Limon y Sal	Julieta Venegas	2006	Pop Latino",
                "Otra Cosa	Julieta Venegas	2010	Pop Latino",
                "Los Momentos	Julieta Venegas	2013	Pop Latino",
                "Algo Sucede	Julieta Venegas	2015	Pop Latino",
                "Tu Historia	Julieta Venegas	2022	Pop Latino",
                "Amor Supremo	Carla Morrison	2015	Pop Latino",
                "Dejenme Llorar	Carla Morrison	2012	Pop Latino",
                "Mientras Tu Dormias	Carla Morrison	2010	Pop Latino",
                "El Renacimiento	Carla Morrison	2022	Pop Latino",
                "Mon Laferte Vol. 1	Mon Laferte	2015	Pop Latino",
                "La Trenza	Mon Laferte	2017	Pop Latino",
                "Norma	Mon Laferte	2018	Pop Latino",
                "Seis	Mon Laferte	2021	Pop Latino",
                "1940 Carmen	Mon Laferte	2021	Pop Latino",
                "Autopoietica	Mon Laferte	2023	Pop Latino",
                "Sin Miedo	Kali Uchis	2020	Pop Latino",
                "Orquideas	Kali Uchis	2024	Pop Latino",
                "Amor Prohibido	Selena	1994	Cumbia",
                "Entre a Mi Mundo	Selena	1992	Cumbia",
                "Ven Conmigo	Selena	1990	Cumbia",
                "Dreaming of You	Selena	1995	Pop Latino",
                "Selena	Selena	1989	Cumbia",
                "Dulce Amor	Selena y Los Dinos	1988	Cumbia",
                "Preciosa	Selena y Los Dinos	1988	Cumbia",
                "Alpha	Selena y Los Dinos	1986	Cumbia",
                "Mis Primeras Grabaciones	Selena	1984	Cumbia",
                "Quiero Ser	Menudo	1981	Pop Latino",
                "Por Amor	Menudo	1982	Pop Latino",
                "A Todo Rock	Menudo	1983	Pop Latino",
                "Evolucion	Menudo	1984	Pop Latino",
                "Ayer y Hoy	Menudo	1985	Pop Latino",
                "Somos los Hijos del Rock	Menudo	1987	Pop Latino",
                "Dulce Beat Live	Belanova	2006	Pop Latino",
                "Salsa Caliente de Nu York	Willie Colon	1970	Salsa",
                "El Malo	Willie Colon	1967	Salsa",
                "The Hustler	Willie Colon	1968	Salsa",
                "Guisando	Willie Colon	1969	Salsa",
                "Cosa Nuestra	Willie Colon	1969	Salsa",
                "Asalto Navideno	Willie Colon	1971	Salsa",
                "La Gran Fuga	Willie Colon	1971	Salsa",
                "El Juicio	Willie Colon	1972	Salsa",
                "Lo Mato	Willie Colon	1973	Salsa",
                "Celia y Willie	Celia Cruz y Willie Colon	1981	Salsa",
                "Siembra	Willie Colon y Ruben Blades	1978	Salsa",
                "Maestra Vida	Ruben Blades	1980	Salsa",
                "Buscando America	Ruben Blades y Seis del Solar	1984	Salsa",
                "Escenas	Ruben Blades	1985	Salsa",
                "Agua de Luna	Ruben Blades	1987	Salsa",
                "Antecedente	Ruben Blades	1988	Salsa",
                "Caminando	Ruben Blades	1991	Salsa",
                "Amor y Control	Ruben Blades	1992	Salsa",
                "La Rosa de los Vientos	Ruben Blades	1996	Salsa",
                "Tiempos	Ruben Blades	1999	Salsa",
                "Canciones del Solar de los Aburridos	Ruben Blades y Willie Colon	1981	Salsa",
                "Metiendo Mano!	Willie Colon y Ruben Blades	1977	Salsa",
                "De Ti Depende	Hector Lavoe	1976	Salsa",
                "La Voz	Hector Lavoe	1975	Salsa",
                "Comedia	Hector Lavoe	1978	Salsa",
                "Recordando a Felipe Pirela	Hector Lavoe	1979	Salsa",
                "El Sabio	Hector Lavoe	1980	Salsa",
                "Que Sentimiento!	Hector Lavoe	1981	Salsa",
                "Vigilante	Willie Colon y Hector Lavoe	1983	Salsa",
                "Revento	Hector Lavoe	1985	Salsa",
                "Strikes Back	Hector Lavoe	1987	Salsa",
                "Celia & Johnny	Celia Cruz y Johnny Pacheco	1974	Salsa",
                "Tremendo Cache	Celia Cruz y Johnny Pacheco	1975	Salsa",
                "Recordando el Ayer	Celia Cruz y Johnny Pacheco	1976	Salsa",
                "Celia Cruz & Willie Colon	Celia Cruz y Willie Colon	1977	Salsa",
                "Only They Could Have Made This Album	Celia Cruz y Willie Colon	1977	Salsa",
                "La Ceiba	Celia Cruz y La Sonora Poncena	1979	Salsa",
                "Ritmo en el Corazon	Celia Cruz y Ray Barretto	1988	Salsa",
                "Mi Vida Es Cantar	Celia Cruz	1998	Salsa",
                "La Negra Tiene Tumbao	Celia Cruz	2001	Salsa",
                "Regalo del Alma	Celia Cruz	2003	Salsa",
                "Azucar Negra	Celia Cruz	1993	Salsa",
                "Celia Cruz and Friends	Celia Cruz	1999	Salsa",
                "El Raton	Cheo Feliciano	1974	Salsa",
                "Cheo	Cheo Feliciano	1971	Salsa",
                "With a Little Help from My Friend	Cheo Feliciano	1973	Salsa",
                "Estampas	Cheo Feliciano	1979	Salsa",
                "Sentimiento, Tu	Cheo Feliciano	1980	Salsa",
                "Profundo	Cheo Feliciano	1982	Salsa",
                "25 Aniversario	Cheo Feliciano	1983	Salsa",
                "La Herencia	Cheo Feliciano	1987	Salsa",
                "Indestructible	Ray Barretto	1973	Salsa",
                "Acid	Ray Barretto	1968	Salsa",
                "Together	Ray Barretto	1969	Salsa",
                "Barretto Power	Ray Barretto	1970	Salsa",
                "Que Viva la Musica	Ray Barretto	1972	Salsa",
                "Rican/Struction	Ray Barretto	1979	Salsa",
                "La Cuna	Ray Barretto	1981	Salsa",
                "Soy Dichoso	Ismael Rivera	1974	Salsa",
                "Traigo de Todo	Ismael Rivera	1974	Salsa",
                "Esto Fue Lo Que Trajo el Barco	Ismael Rivera	1972	Salsa",
                "De Todas Maneras Rosas	Ismael Rivera	1977	Salsa",
                "Feliz Navidad	Ismael Rivera	1975	Salsa",
                "Maelo	Ismael Rivera	1981	Salsa",
                "Esto Si Es Lo Mio	Ismael Rivera	1978	Salsa",
                "El Sonero Mayor	Ismael Rivera	1973	Salsa",
                "El Cantante	Hector Lavoe	1978	Salsa",
                "Inconfundible	Ismael Rivera	1977	Salsa",
                "El Gran Combo de Puerto Rico	El Gran Combo de Puerto Rico	1963	Salsa",
                "Acangana	El Gran Combo de Puerto Rico	1963	Salsa",
                "Ojos Chinos	El Gran Combo de Puerto Rico	1964	Salsa",
                "El Caballo Pelotero	El Gran Combo de Puerto Rico	1965	Salsa",
                "Boogaloo con El Gran Combo	El Gran Combo de Puerto Rico	1967	Salsa",
                "De Punta a Punta	El Gran Combo de Puerto Rico	1971	Salsa",
                "Por el Libro	El Gran Combo de Puerto Rico	1972	Salsa",
                "En Accion	El Gran Combo de Puerto Rico	1973	Salsa",
                "7	El Gran Combo de Puerto Rico	1975	Salsa",
                "Mejor Que Nunca	El Gran Combo de Puerto Rico	1976	Salsa",
                "Internacional	El Gran Combo de Puerto Rico	1977	Salsa",
                "Happy Days	El Gran Combo de Puerto Rico	1981	Salsa",
                "Nuestro Aniversario	El Gran Combo de Puerto Rico	1982	Salsa",
                "Y Su Pueblo	El Gran Combo de Puerto Rico	1984	Salsa",
                "Romantico y Sabroso	El Gran Combo de Puerto Rico	1988	Salsa",
                "Latin Up	El Gran Combo de Puerto Rico	1990	Salsa",
                "25th Anniversary	El Gran Combo de Puerto Rico	1987	Salsa",
                "Sin Salsa No Hay Paraiso	El Gran Combo de Puerto Rico	2010	Salsa",
                "Niche	Grupo Niche	1981	Salsa",
                "Querer Es Poder	Grupo Niche	1981	Salsa",
                "Preparate	Grupo Niche	1982	Salsa",
                "Directo Desde New York	Grupo Niche	1983	Salsa",
                "No Hay Quinto Malo	Grupo Niche	1984	Salsa",
                "Triunfo	Grupo Niche	1985	Salsa",
                "Me Huele a Matrimonio	Grupo Niche	1986	Salsa",
                "Con Cuerdas	Grupo Niche	1987	Salsa",
                "Tapando el Hueco	Grupo Niche	1988	Salsa",
                "Sutil y Contundente	Grupo Niche	1989	Salsa",
                "Cielo de Tambores	Grupo Niche	1990	Salsa",
                "Llegando al 100%	Grupo Niche	1991	Salsa",
                "Un Alto en el Camino	Grupo Niche	1993	Salsa",
                "Huellas del Pasado	Grupo Niche	1995	Salsa",
                "A Prueba de Fuego	Grupo Niche	1997	Salsa",
                "Senales de Humo	Grupo Niche	1998	Salsa",
                "Propuesta	Gilberto Santa Rosa	1994	Salsa",
                "Perspectiva	Gilberto Santa Rosa	1991	Salsa",
                "A Dos Tiempos de un Tiempo	Gilberto Santa Rosa	1992	Salsa",
                "Esencia	Gilberto Santa Rosa	1996	Salsa",
                "De Cara al Viento	Gilberto Santa Rosa	1997	Salsa",
                "Expresion	Gilberto Santa Rosa	1999	Salsa",
                "Romantico	Gilberto Santa Rosa	2002	Salsa",
                "Autentico	Gilberto Santa Rosa	2004	Salsa",
                "Contraste	Gilberto Santa Rosa	2007	Salsa",
                "Irrepetible	Gilberto Santa Rosa	2010	Salsa",
                "Otra Nota	Marc Anthony	1993	Salsa",
                "Todo a Su Tiempo	Marc Anthony	1995	Salsa",
                "Contra la Corriente	Marc Anthony	1997	Salsa",
                "Libre	Marc Anthony	2001	Salsa",
                "Valio la Pena	Marc Anthony	2004	Salsa",
                "El Cantante	Marc Anthony	2007	Salsa",
                "Iconos	Marc Anthony	2010	Salsa",
                "3.0	Marc Anthony	2013	Salsa",
                "Opus	Marc Anthony	2019	Salsa",
                "Pa'lla Voy	Marc Anthony	2022	Salsa",
                "Justo a Tiempo	Victor Manuelle	1993	Salsa",
                "Solo Contigo	Victor Manuelle	1994	Salsa",
                "Victor Manuelle	Victor Manuelle	1996	Salsa",
                "A Pesar de Todo	Victor Manuelle	1997	Salsa",
                "Ironias	Victor Manuelle	1998	Salsa",
                "Inconfundible	Victor Manuelle	1999	Salsa",
                "Instinto y Deseo	Victor Manuelle	2001	Salsa",
                "Le Preguntaba a la Luna	Victor Manuelle	2002	Salsa",
                "Travesia	Victor Manuelle	2004	Salsa",
                "Decision Unanime	Victor Manuelle	2006	Salsa",
                "Soy	Victor Manuelle	2008	Salsa",
                "Busco un Pueblo	Victor Manuelle	2011	Salsa",
                "Me Llamare Tuyo	Victor Manuelle	2013	Salsa",
                "Que Suenen los Tambores	Victor Manuelle	2015	Salsa",
                "25/7	Victor Manuelle	2018	Salsa",
                "Fruko el Bueno	Fruko y Sus Tesos	1975	Salsa",
                "Ayunando	Fruko y Sus Tesos	1973	Salsa",
                "El Violento	Fruko y Sus Tesos	1973	Salsa",
                "El Grande	Fruko y Sus Tesos	1974	Salsa",
                "El Patillero	Fruko y Sus Tesos	1975	Salsa",
                "El Cocinero Mayor	Fruko y Sus Tesos	1976	Salsa",
                "El Caminante	Fruko y Sus Tesos	1978	Salsa",
                "El Preso	Fruko y Sus Tesos	1975	Salsa",
                "Rebellion	Joe Arroyo	1986	Salsa",
                "Fuego en Mi Mente	Joe Arroyo	1988	Salsa",
                "Echao Pa'lante	Joe Arroyo	1987	Salsa",
                "En Accion	Joe Arroyo	1983	Salsa",
                "Musa Original	Joe Arroyo	1990	Salsa",
                "Toque de Clase	Joe Arroyo	1991	Salsa",
                "Fuego	Joe Arroyo	1992	Salsa",
                "Sus Razones Tendra	Joe Arroyo	1993	Salsa",
                "Mi Libertad	Frankie Ruiz	1992	Salsa",
                "Solista Pero No Solo	Frankie Ruiz	1985	Salsa",
                "Voy Pa' Encima	Frankie Ruiz	1987	Salsa",
                "En Vivo y a Todo Color	Frankie Ruiz	1988	Salsa",
                "Mas Grande Que Nunca	Frankie Ruiz	1989	Salsa",
                "A Go Go	Sonora Dinamita	1961	Cumbia",
                "Ritmo	Sonora Dinamita	1962	Cumbia",
                "Dinamita	Sonora Dinamita	1963	Cumbia",
                "La Sonora Dinamita	Sonora Dinamita	1964	Cumbia",
                "Cumbias Con La Sonora Dinamita	Sonora Dinamita	1977	Cumbia",
                "El Meneito	Sonora Dinamita	1981	Cumbia",
                "La Cumbia Nacio en Baru	Sonora Dinamita	1984	Cumbia",
                "La Cumbia del Sida	Sonora Dinamita	1988	Cumbia",
                "La Parabolica	Sonora Dinamita	1991	Cumbia",
                "Las Brujas	Sonora Dinamita	1992	Cumbia",
                "30 Pegaditas de Oro	Sonora Dinamita	1994	Cumbia",
                "Cumbia Universal	Sonora Dinamita	1996	Cumbia",
                "Con Todo	Los Angeles Azules	1994	Cumbia",
                "Inolvidables	Los Angeles Azules	1996	Cumbia",
                "Una Lluvia de Rosas	Los Angeles Azules	1999	Cumbia",
                "Alas al Mundo	Los Angeles Azules	2000	Cumbia",
                "Nunca Te Olvidare	Los Angeles Azules	2001	Cumbia",
                "Juventud Dinamita	Los Angeles Azules	2002	Cumbia",
                "Sin Pecado	Los Angeles Azules	2004	Cumbia",
                "Tu Juguete	Los Angeles Azules	2007	Cumbia",
                "A Ritmo de Cumbia	Los Angeles Azules	2012	Cumbia",
                "Como Te Voy a Olvidar	Los Angeles Azules	2013	Cumbia",
                "De Plaza en Plaza	Los Angeles Azules	2016	Cumbia",
                "Esto Si Es Cumbia	Los Angeles Azules	2018	Cumbia",
                "De Buenos Aires Para el Mundo	Los Angeles Azules	2020	Cumbia",
                "Cumbia del Corazon	Los Angeles Azules	2022	Cumbia",
                "Rebeldia	Damas Gratis	2004	Cumbia",
                "Para los Pibes	Damas Gratis	2000	Cumbia",
                "Operacion Damas Gratis	Damas Gratis	2002	Cumbia",
                "100% Negro Cumbiero	Damas Gratis	2006	Cumbia",
                "Sin Remedio	Damas Gratis	2008	Cumbia",
                "Esquivando el Exito	Damas Gratis	2011	Cumbia",
                "Vivo en el Luna Park	Damas Gratis	2012	Cumbia",
                "Para los Pibes II	Damas Gratis	2016	Cumbia",
                "Vivo en el Gran Rex	Damas Gratis	2018	Cumbia",
                "Un Sentimiento	Los Palmeras	1991	Cumbia",
                "Tropibaile Santafesino	Los Palmeras	1986	Cumbia",
                "Por Siempre	Los Palmeras	1993	Cumbia",
                "Cumbia y Luna	Los Palmeras	1995	Cumbia",
                "El Bombon Asesino	Los Palmeras	2002	Cumbia",
                "Sean Eternos Los Palmeras	Los Palmeras	2004	Cumbia",
                "Saboreando Cumbias	Los Palmeras	2006	Cumbia",
                "40 Anos	Los Palmeras	2012	Cumbia",
                "Un Poco de Ruido	Los Palmeras	2020	Cumbia",
                "Alma de Cumbia	Los Palmeras	2022	Cumbia",
                "Barrio Bravo	Celso Pina	2001	Cumbia",
                "El Canto de un Rebelde Para un Rebelde	Celso Pina	2003	Cumbia",
                "Una Vision	Celso Pina	2003	Cumbia",
                "Mundo Colombia	Celso Pina	2002	Cumbia",
                "Cumbia Sobre el Rio	Celso Pina	2001	Cumbia",
                "Sin Fecha de Caducidad	Celso Pina	2009	Cumbia",
                "Aqui Presente Compa	Celso Pina	2014	Cumbia",
                "Tierra de Nadie	Celso Pina	2012	Cumbia",
                "El Rebelde del Acordeon	Celso Pina	1994	Cumbia",
                "Cumbia Amazonica	Los Mirlos	1972	Cumbia",
                "El Poder Verde	Los Mirlos	1973	Cumbia",
                "Los Charapas de Oro	Los Mirlos	1975	Cumbia",
                "Los Mirlos	Los Mirlos	1977	Cumbia",
                "Cumbia de los Pajaritos	Los Mirlos	1978	Cumbia",
                "Sonido Amazonico	Los Mirlos	1980	Cumbia",
                "Ritmo Caliente	Los Mirlos	1982	Cumbia",
                "Tropicalisimo	Los Mirlos	1985	Cumbia",
                "La Danza del Petrolero	Los Mirlos	1991	Cumbia",
                "Selva	Los Mirlos	2010	Cumbia",
                "Los Destellos	Los Destellos	1968	Cumbia",
                "Constelacion	Los Destellos	1971	Cumbia",
                "En Orbita	Los Destellos	1972	Cumbia",
                "Cumbia Peruana	Los Destellos	1973	Cumbia",
                "Ojos Azules	Los Destellos	1974	Cumbia",
                "Para Todo el Mundo	Los Destellos	1975	Cumbia",
                "Los Destellos en Accion	Los Destellos	1976	Cumbia",
                "Los Destellos del Peru	Los Destellos	1977	Cumbia",
                "La Nueva Crema	Chacalon y la Nueva Crema	1981	Cumbia",
                "Soy Provinciano	Chacalon y la Nueva Crema	1982	Cumbia",
                "Muchacho Provinciano	Chacalon y la Nueva Crema	1983	Cumbia",
                "El Rey de la Carretera	Chacalon y la Nueva Crema	1984	Cumbia",
                "Los Shapis	Los Shapis	1981	Cumbia",
                "El Aguajal	Los Shapis	1982	Cumbia",
                "Los Autenticos	Los Shapis	1983	Cumbia",
                "Corazon Andino	Los Shapis	1984	Cumbia",
                "Peru Campeon	Los Shapis	1985	Cumbia",
                "Basta de Llamarme Asi	Los Fabulosos Cadillacs	1986	Rock en Espanol",
                "Cumbias y Porros	Lucho Bermudez	1950	Cumbia",
                "Danza Negra	Lucho Bermudez	1956	Cumbia",
                "Colombia Tierra Querida	Lucho Bermudez	1964	Cumbia",
                "Fiesta Colombiana	Lucho Bermudez	1968	Cumbia",
                "Cumbias Colombianas	Lucho Bermudez	1970	Cumbia",
                "La Pollera Colora	Wilson Choperena	1961	Cumbia",
                "Cumbia Cienaguera	Alberto Pacheco	1953	Cumbia",
                "La Piragua	Jose Barros	1969	Cumbia",
                "Yo Me Llamo Cumbia	Mario Garena	1970	Cumbia",
                "Cumbia Sampuesana	Aniceto Molina	1964	Cumbia",
                "El Campanero	Aniceto Molina	1975	Cumbia",
                "La Cumbia Sampuesana	Aniceto Molina	1980	Cumbia",
                "El Tigre Sabanero	Aniceto Molina	1984	Cumbia",
                "Cumbia de Colombia	Aniceto Molina	1988	Cumbia",
                "El Peluquero	Aniceto Molina	1994	Cumbia",
                "Cumbias Para Bailar	Aniceto Molina	1996	Cumbia",
                "20 Exitos Bailables	Aniceto Molina	1998	Cumbia",
                "Cumbia Sobre el Mar	Quantic y Frente Cumbiero	2011	Cumbia",
                "Ondatropica	Ondatropica	2012	Cumbia",
                "Baile Bucanero	Ondatropica	2017	Cumbia",
                "Frente Cumbiero Meets Mad Professor	Frente Cumbiero	2010	Cumbia",
                "Ayo	Bomba Estereo	2017	Cumbia",
                "Elegancia Tropical	Bomba Estereo	2012	Cumbia",
                "Estalla	Bomba Estereo	2008	Cumbia",
                "Amanecer	Bomba Estereo	2015	Cumbia",
                "Deja	Bomba Estereo	2021	Cumbia",
                "Cumbias Villera	Yerba Brava	1999	Cumbia",
                "Los Duenos del Pabellon	Yerba Brava	2000	Cumbia",
                "100% Villera	Yerba Brava	2001	Cumbia",
                "De Culo	Yerba Brava	2002	Cumbia",
                "Hasta las Manos	Yerba Brava	2004	Cumbia",
                "Flor de Piedra	Flor de Piedra	1999	Cumbia",
                "La Vanda Mas Loca	Flor de Piedra	2000	Cumbia",
                "Cumbia Cabeza	Flor de Piedra	2001	Cumbia",
                "Alta Fiesta	Flor de Piedra	2002	Cumbia",
                "Nestor en Bloque	Nestor en Bloque	2003	Cumbia",
                "Rompiendo el Silencio	Nestor en Bloque	2004	Cumbia",
                "Mi Unico Amor	Nestor en Bloque	2005	Cumbia",
                "El Original	Nestor en Bloque	2007	Cumbia",
                "El Polaco	El Polaco	2006	Cumbia",
                "Vuelve Te Lo Pido	El Polaco	2007	Cumbia",
                "Una Nueva Vida	El Polaco	2008	Cumbia",
                "Molestando a los Vecinos	El Polaco	2009	Cumbia",
                "Sigo Siendo Yo	El Polaco	2011	Cumbia",
                "Grupo 5	Grupo 5	1973	Cumbia",
                "El Ritmo de Mi Corazon	Grupo 5	2008	Cumbia",
                "Motor y Motivo	Grupo 5	2005	Cumbia",
                "Puro Corazon	Grupo 5	2009	Cumbia",
                "La Culebritica	Grupo 5	2011	Cumbia",
                "Vivir Mi Vida	Grupo 5	2013	Cumbia",
                "Para Ti	Grupo 5	2015	Cumbia",
                "El Amor de Mis Amores	Grupo 5	2018	Cumbia",
                "En Vivo en el Estadio San Marcos	Grupo 5	2023	Cumbia",
                "Agua Marina	Agua Marina	1976	Cumbia",
                "Asi Es el Amor	Agua Marina	1981	Cumbia",
                "A Mover la Colita	Agua Marina	1985	Cumbia",
                "Amor Amor	Agua Marina	1990	Cumbia",
                "Paloma Ajena	Agua Marina	1994	Cumbia",
                "Tu Amor Fue una Mentira	Agua Marina	1998	Cumbia",
                "Cumbia y Sentimiento	Agua Marina	2004	Cumbia",
                "Mix Agua Marina	Agua Marina	2008	Cumbia",
                "Gracias Amor	Agua Marina	2013	Cumbia",
                "Te Vas	Los Bukis	1980	Pop Latino",
                "Me Volvi a Acordar de Ti	Los Bukis	1986	Pop Latino",
                "Si Me Recuerdas	Los Bukis	1988	Pop Latino",
                "Y Para Siempre	Los Bukis	1989	Pop Latino",
                "Quiereme	Los Bukis	1992	Pop Latino",
                "Inalcanzable	Los Bukis	1993	Pop Latino",
                "Trozos de Mi Alma	Marco Antonio Solis	1999	Pop Latino",
                "Mas de Mi Alma	Marco Antonio Solis	2001	Pop Latino",
                "Razon de Sobra	Marco Antonio Solis	2004	Pop Latino",
                "Trozos de Mi Alma 2	Marco Antonio Solis	2006	Pop Latino",
                "No Molestar	Marco Antonio Solis	2008	Pop Latino",
                "Gracias Por Estar Aqui	Marco Antonio Solis	2013	Pop Latino",
                "Por Amor a Morelia Michoacan	Marco Antonio Solis	2015	Pop Latino",
                "Bronco	Bronco	1985	Cumbia",
                "Pura Sangre	Bronco	1986	Cumbia",
                "Un Golpe Mas	Bronco	1988	Cumbia",
                "Amigo Bronco	Bronco	1989	Cumbia",
                "Corazon Duro	Bronco	1990	Cumbia",
                "Salvaje y Tierno	Bronco	1991	Cumbia",
                "Por el Mundo	Bronco	1992	Cumbia",
                "Pura Sangre Bronco	Bronco	1993	Cumbia",
                "Rompiendo Barreras	Bronco	1994	Cumbia",
                "Siempre Arriba	Bronco	1995	Cumbia",
                "La Sangre Nueva	Bronco	1996	Cumbia",
                "El Gigante de America	Bronco	1997	Cumbia"
        };

        for (String linea : lineas) {
            String[] partes = linea.split("\\t");
            String formato = partes[2].compareTo("1990") < 0 ? "Vinilo" : "CD";
            agregar(discos, partes[0], partes[1], Integer.parseInt(partes[2]), partes[3], formato);
        }
        return seleccionarBalanceado(discos, limite);
    }

    /**
     * Intercala los generos del catalogo para que el limite por defecto produzca
     * una coleccion equilibrada: 125 Rock en Espanol, 125 Pop Latino, 125 Salsa
     * y 125 Cumbia cuando {@code limite} es 500.
     *
     * @param discos catalogo completo agrupado por escenas y artistas.
     * @param limite cantidad maxima solicitada por linea de comandos.
     * @return seleccion intercalada que conserva el orden relativo dentro de cada genero.
     */
    private static List<Disco> seleccionarBalanceado(List<Disco> discos, int limite) {
        Map<String, List<Disco>> porGenero = new LinkedHashMap<>();
        porGenero.put("Rock en Espanol", new ArrayList<>());
        porGenero.put("Pop Latino", new ArrayList<>());
        porGenero.put("Salsa", new ArrayList<>());
        porGenero.put("Cumbia", new ArrayList<>());

        for (Disco disco : discos) {
            List<Disco> grupo = porGenero.get(disco.getGenero());
            if (grupo != null) {
                grupo.add(disco);
            }
        }

        // Se toma el elemento N de cada genero antes de avanzar al siguiente N.
        // Asi, aunque el catalogo interno este agrupado por genero, el JSON
        // final queda repartido y las pruebas de busqueda/recomendacion ven
        // diversidad desde los primeros registros.
        List<Disco> seleccion = new ArrayList<>();
        int indice = 0;
        while (seleccion.size() < limite && seleccion.size() < discos.size()) {
            boolean agregado = false;
            for (List<Disco> grupo : porGenero.values()) {
                if (indice < grupo.size() && seleccion.size() < limite) {
                    seleccion.add(grupo.get(indice));
                    agregado = true;
                }
            }
            if (!agregado) {
                break;
            }
            indice++;
        }
        return seleccion;
    }

    /**
     * Agrega un disco al catalogo en construccion.
     *
     * @param discos lista mutable donde se acumulan los albumes.
     * @param titulo titulo del album.
     * @param artista artista principal o agrupacion.
     * @param anio anio de publicacion.
     * @param genero genero normalizado usado por los agentes.
     * @param formato formato fisico asignado para la coleccion.
     */
    private static void agregar(List<Disco> discos, String titulo, String artista, int anio,
            String genero, String formato) {
        discos.add(new Disco(titulo, artista, anio, genero, formato));
    }

    /**
     * Serializa la coleccion completa en JSON legible para humanos.
     *
     * <p>Se crean los directorios necesarios antes de escribir, de modo que
     * tambien funciona con rutas alternativas pasadas por {@code --output}.
     *
     * @param destino ruta del archivo JSON a escribir.
     * @param discos discos ya seleccionados y ordenados.
     * @throws IOException si falla la creacion de directorios o la escritura.
     */
    private static void escribirJson(Path destino, List<Disco> discos) throws IOException {
        Files.createDirectories(destino.toAbsolutePath().getParent());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (BufferedWriter writer = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {
            gson.toJson(discos, writer);
        }
    }

    /**
     * Opciones de ejecucion de la herramienta.
     *
     * <p>Se mantiene deliberadamente pequena para no introducir dependencias de
     * CLI: el proyecto solo necesita cambiar la ruta de salida o limitar la
     * cantidad de discos generados.
     */
    private static class Opciones {
        private Path jsonSalida = JSON_POR_DEFECTO;
        private int limite = LIMITE_POR_DEFECTO;

        /**
         * Interpreta argumentos de linea de comandos conocidos.
         *
         * @param args arreglo recibido por {@link ImportadorColeccion#main(String[])}.
         * @return opciones con valores por defecto o sobrescritos.
         */
        private static Opciones desde(String[] args) {
            Opciones opciones = new Opciones();
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if ("--output".equals(arg) && i + 1 < args.length) {
                    opciones.jsonSalida = Paths.get(args[++i]);
                } else if ("--limit".equals(arg) && i + 1 < args.length) {
                    opciones.limite = Math.max(1, Integer.parseInt(args[++i]));
                }
            }
            return opciones;
        }
    }
}
