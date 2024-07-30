package com.dnsouzadev.screenmatch.principal;

import com.dnsouzadev.screenmatch.model.*;
import com.dnsouzadev.screenmatch.service.ConsumoAPI;
import com.dnsouzadev.screenmatch.service.ConverteDados;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.time.LocalDate;
import java.util.*;

import com.dnsouzadev.screenmatch.service.ConsumoAPI;
import com.dnsouzadev.screenmatch.service.ConverteDados;
import com.dnsouzadev.screenmatch.model.DadosSerie;

import java.util.stream.Collectors;

public class Principal {
    private final Scanner leitura = new Scanner(System.in);
    private final ConsumoAPI consumo = new ConsumoAPI();
    private final ConverteDados conversor = new ConverteDados();

    private final String ENDERECO = "https://omdbapi.com/?t=";
    private final String API_KEY = "&apikey=99277d5";

    public void exibeMenu() throws JsonProcessingException {
        System.out.println("Digite o nome da serie: ");
        String nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);

        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        System.out.println(dados);

        List<DadosTemporada> temporadas = new ArrayList<>();

		for (int i = 1; i <= dados.totalTemporadas(); i++) {
			json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY + "&season=" + i);
			DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
			temporadas.add(dadosTemporada);
		}

		temporadas.forEach(System.out::println);

        temporadas.forEach(t -> t.episodios().forEach(e -> System.out.println(e.titulo())));

        List<DadosEpisodio> dadosEpisodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream())
                .toList();
        System.out.println("Top 10 episodios");
        dadosEpisodios.stream()
                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
                .limit(10)
                .map(e -> e.titulo().toUpperCase())
                .forEach(System.out::println);

        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                    .map(d -> new Episodio(t.numero(), d))
                ).toList();

        episodios.forEach(System.out::println);

        System.out.println("Digite um trecho do titulo do episodio: ");
        var trechoTitulo = leitura.nextLine();

        Optional<Episodio> episodioBuscado = episodios.stream()
                .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
                .findFirst();
        if (episodioBuscado.isPresent()) {
            System.out.println(episodioBuscado.get());
        } else {
            System.out.println("Episodio nao encontrado");
        }

        System.out.println("A partir de qual ano deseja ver os episodios?");
        var ano = leitura.nextInt();
        leitura.nextLine();

        episodios.stream()
                .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(LocalDate.of(ano, 1, 1)))
                .forEach(
                        e -> System.out.println(
                                "Temporada: " + e.getTemporada() +
                                        " Episodio: " + e.getNumeroEpisodio() +
                                        " Titulo: " + e.getTitulo() +
                                        " Avaliacao: " + e.getAvaliacao() +
                                        " Data de Lancamento: " + e.getDataLancamento().format(Episodio.FORMATO_DATA)
                        )
                );

        System.out.println("*** Media de avaliacao por temporada ***");

        Map<Integer, Double> avalicaoesPorTemporada = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0)
                .collect(Collectors.groupingBy(Episodio::getTemporada,
                        Collectors.averagingDouble(Episodio::getAvaliacao)));

        avalicaoesPorTemporada.forEach((temporada, media) -> System.out.println("Temporada: " + temporada + " Media: " + media));

        DoubleSummaryStatistics est = episodios.stream()
                .filter(e -> e.getAvaliacao() > 0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));

        System.out.println("Estatisticas: Media: " + est.getAverage() + " Max: " + est.getMax() + " Min: " + est.getMin());


    }
}
