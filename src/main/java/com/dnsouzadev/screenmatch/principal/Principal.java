package com.dnsouzadev.screenmatch.principal;

import com.dnsouzadev.screenmatch.model.DadosSerie;
import com.dnsouzadev.screenmatch.service.ConsumoAPI;
import com.dnsouzadev.screenmatch.service.ConverteDados;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.dnsouzadev.screenmatch.service.ConsumoAPI;
import com.dnsouzadev.screenmatch.service.ConverteDados;
import com.dnsouzadev.screenmatch.model.DadosEpisodio;
import com.dnsouzadev.screenmatch.model.DadosSerie;
import com.dnsouzadev.screenmatch.model.DadosTemporada;
import java.util.Scanner;
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

        dadosEpisodios.stream()
                .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
                .limit(5)
                .forEach(System.out::println);

    }
}
