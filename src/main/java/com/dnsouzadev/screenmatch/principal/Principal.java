package com.dnsouzadev.screenmatch.principal;

import com.dnsouzadev.screenmatch.model.*;
import com.dnsouzadev.screenmatch.repository.SerieRepository;
import com.dnsouzadev.screenmatch.service.ConverteDados;
import com.dnsouzadev.screenmatch.service.ConsumoAPI;
import com.dnsouzadev.screenmatch.model.DadosSerie;
import com.fasterxml.jackson.core.JsonProcessingException;

import javax.swing.text.html.Option;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private SerieRepository repository;

    private Scanner leitura = new Scanner(System.in);
    private ConsumoAPI consumo = new ConsumoAPI();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";
    private List<DadosSerie> dadosSeries = new ArrayList<>();
    private List<Serie> series = new ArrayList<>();

    public Principal(SerieRepository repository) {
        this.repository = repository;
    }

    public void exibeMenu() throws JsonProcessingException {
        var opcao = -1;
        while (opcao != 0) {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar series buscadas
                    4 - Buscar serie por titulo
                    5 - Buscar serie por ator
                    0 - Sair                                 
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriesPorAtor();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscarSeriesPorAtor() {
        System.out.println("Qual o nome para busca?");
        var nomeAtor = leitura.nextLine();

        List<Serie> seriesEncontradas = repository.findByAtoresContainingIgnoreCase(nomeAtor);
        if (seriesEncontradas.isEmpty()) {
            System.out.println("Nenhuma série encontrada");
        } else {
            seriesEncontradas.forEach(s -> System.out.println("Avaliacao: " + s.getAvaliacao() + " - " + s.getTitulo()));
        }
    }

    private void buscarSeriePorTitulo() {
        listarSeriesBuscadas();
        System.out.println("Digite o nome da série para busca: ");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serie = repository.findByTituloContainingIgnoreCase(nomeSerie);
        if (serie.isPresent()) {
            System.out.println("Dados da serie: " + serie.get());
        } else {
            System.out.println("Série não encontrada");
        }
    }

    private void listarSeriesBuscadas() {
        series = repository.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);

    }

    private void buscarSerieWeb() throws JsonProcessingException {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
        repository.save(serie);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() throws JsonProcessingException {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        return conversor.obterDados(json, DadosSerie.class);
    }

    private void buscarEpisodioPorSerie() throws JsonProcessingException {
        listarSeriesBuscadas();
        System.out.println("Digite o nome da série para busca: ");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serie = repository.findByTituloContainingIgnoreCase(nomeSerie);

        if (serie.isEmpty()) {
            System.out.println("Série não encontrada");
        }

        var serieEncontrada = serie.get();
        List<DadosTemporada> temporadas = new ArrayList<>();

        for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
            var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
            DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }
        temporadas.forEach(System.out::println);

        List<Episodio> episodios = temporadas.stream()
                .flatMap(d -> d.episodios().stream()
                        .map(e -> new Episodio(d.numero(), e)))
                .toList();
        serieEncontrada.setEpisodios(episodios);
        repository.save(serieEncontrada);
    }
}
