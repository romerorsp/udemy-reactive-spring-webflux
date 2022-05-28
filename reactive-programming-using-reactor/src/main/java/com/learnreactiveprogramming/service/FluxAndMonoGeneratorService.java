package com.learnreactiveprogramming.service;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class FluxAndMonoGeneratorService {

  public static void main(String[] args) {
    var service = new FluxAndMonoGeneratorService();

    service.namesFlux()
        .subscribe(name -> System.out.printf("Name is: %s\n", name));
    service.nameMono()
        .subscribe(name -> System.out.printf("Family is: %s\n", name));
  }

  public Flux<String> namesFlux() {
    return Flux.fromIterable(List.of("Rômero", "Bianca", "Shauana"));
  }

  public Flux<String> namesFluxMap() {
    return namesFlux()
        .map(String::toUpperCase)
        .log();
  }

  public Flux<String> namesFluxTransform() {
    Function<Flux<String>, Flux<String>> transformer = flux -> flux.map(String::toUpperCase);
    return namesFlux()
        .transform(transformer)
        .log();
  }

  public Flux<String> namesFluxFiltered() {
    return namesFlux()
        .filter(name -> name.length() == 6)
        .map(String::toUpperCase)
        .log();
  }

  public Flux<String> namesFluxFilteredDefaultIfEmpty(int nameLength) {
    return namesFlux()
        .filter(name -> name.length() == nameLength)
        .map(String::toUpperCase)
        .defaultIfEmpty("default")
        .log();
  }

  public Flux<String> namesFluxFilteredSwitchIfEmpty(int nameLength) {
    Function<Flux<String>, Flux<String>> mapper = flux -> flux.map(String::toUpperCase);
    Flux<String> transform = Flux.just("default").transform(mapper);
    return namesFlux()
        .filter(name -> name.length() == nameLength)
        .transform(mapper)
        .switchIfEmpty(transform)
        .log();
  }

  public Flux<String> namesFluxMappedFiltered() {
    return namesFlux()
        .filter(name -> name.length() == 6)
        .map(String::toUpperCase)
        .map(name -> "%d-%s".formatted(name.length(), name))
        .log();
  }

  public Flux<String> namesFluxImmutability() {
    var namesFlux = namesFlux().log();
    //noinspection ReactiveStreamsUnusedPublisher
    namesFlux.map(String::toUpperCase);
    return namesFlux;
  }

  public Mono<String> nameMono() {
    return namesFlux()
        .reduce("%s, %s"::formatted)
        .log();
  }

  public Mono<List<Character>> nameMonoFlatMapped() {
    return nameMono()
        .flatMap(name -> Mono.just(name.chars().mapToObj(c -> (char) c).toList()))
        .log();
  }

  public Flux<Character> nameMonoFlatMappedMany() {
    return nameMono()
        .flatMapMany(name -> Flux.fromStream(name.chars().mapToObj(c -> (char) c)))
        .log();
  }

  public Flux<Character> namesFluxFlatMappedFiltered() {
    return namesFluxFiltered()
        .flatMap(this::nameToCharacterFlux)
        .log();
  }

  public Flux<Character> namesFluxFlatMappedFilteredAsync() {
    return namesFluxFiltered()
        .flatMap(name -> nameToCharacterFlux(name)
            .delayElements(
                Duration.ofMillis(
                    new Random(1000).nextInt()
                )
            )
        ).log();
  }

  public Flux<Character> namesFluxConcatMappedFiltered() {
    return namesFluxFiltered()
        .concatMap(name -> nameToCharacterFlux(name)
            .delayElements(
                Duration.ofMillis(
                    new Random(1000).nextInt()
                )
            )
        ).log();
  }

  public Flux<Character> alphabetFluxConcat() {
    return Flux.concat(Flux.just('A', 'B', 'C'), Flux.just('D', 'E', 'F'));
  }

  public Flux<Character> alphabetFluxConcatWith() {
    return Flux.just('A', 'B', 'C').concatWith(Flux.just('D', 'E', 'F'));
  }

  public Flux<Character> alphabetMonoConcatWith() {
    return Mono.just('A').concatWith(Mono.just('B'));
  }

  private Flux<Character> nameToCharacterFlux(String name) {
    return Flux.fromStream(name.chars().mapToObj(c -> (char) c));
  }

  public Flux<Character> alphabetFluxMerge() {
    return Flux.merge(
        Flux.just('A', 'B', 'C')
            .delayElements(Duration.ofMillis(100)),
        Flux.just('D', 'E', 'F')
            .delayElements(Duration.ofMillis(125))
    ).log();
  }

  public Flux<Character> alphabetFluxMergeSequential() {
    return Flux.mergeSequential(
        Flux.just('A', 'B', 'C')
            .delayElements(Duration.ofMillis(100)),
        Flux.just('D', 'E', 'F')
            .delayElements(Duration.ofMillis(125))
    ).log();
  }

  public Flux<Character> alphabetFluxMergeWith() {
    return Flux.just('A', 'B', 'C')
        .delayElements(Duration.ofMillis(100))
        .mergeWith(Flux.just('D', 'E', 'F').delayElements(Duration.ofMillis(125)))
        .log();
  }

  public Flux<Character> alphabetMonoMergeWith() {
    return Mono.just('A').mergeWith(Mono.just('B')).log();
  }

  public Flux<String> alphabetFluxZip() {
    return Flux.zip(
        Flux.just('A', 'B', 'C')
            .delayElements(Duration.ofMillis(100)),
        Flux.just('D', 'E', 'F')
            .delayElements(Duration.ofMillis(125)),
        (char1, char2) -> "%c%c".formatted(char1, char2)
    ).log();
  }

  public Flux<String> alphabetFluxZipWith() {
    return Flux.just('A', 'B', 'C')
        .delayElements(Duration.ofMillis(100))
        .zipWith(
            Flux.just('D', 'E', 'F').delayElements(Duration.ofMillis(125)),
            "%c%c"::formatted)
        .log();
  }

  public Mono<String> alphabetMonoZipWith() {
    return Mono.just('A').zipWith(Mono.just('B'), "%c%c"::formatted).log();
  }
}