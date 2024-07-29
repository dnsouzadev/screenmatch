package com.dnsouzadev.screenmatch;

import com.dnsouzadev.screenmatch.model.DadosEpisodio;
import com.dnsouzadev.screenmatch.model.DadosSerie;
import com.dnsouzadev.screenmatch.model.DadosTemporada;
import com.dnsouzadev.screenmatch.service.ConsumoAPI;
import com.dnsouzadev.screenmatch.service.ConverteDados;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class ScreenmatchApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ScreenmatchApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		var consumoAPI = new ConsumoAPI();

		var json = consumoAPI.obterDados("https://omdbapi.com/?t=supernatural&season=15&apikey=99277d5");
		System.out.println(json);

		ConverteDados conversor = new ConverteDados();
		DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
		System.out.println(dados);

		json = consumoAPI.obterDados("https://omdbapi.com/?t=supernatural&season=15&episode=4&apikey=99277d5");
		DadosEpisodio dadosEpisodio = conversor.obterDados(json, DadosEpisodio.class);
		System.out.println(dadosEpisodio);

		List<DadosTemporada> temporadas = new ArrayList<>();

		for (int i = 1; i <= dados.totalTemporadas(); i++) {
			json = consumoAPI.obterDados("https://omdbapi.com/?t=supernatural&season=" + i + "&apikey=99277d5");
			DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
			temporadas.add(dadosTemporada);
		}

		temporadas.forEach(System.out::println);
	}
}
