package com.learnreactiveprogramming.service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class FluxAndMonoGeneratorServiceTest {

  private FluxAndMonoGeneratorService fixture;

  @BeforeEach
  public void setup() {
    fixture = new FluxAndMonoGeneratorService();
  }

  @Test
  public void testNamesFlux() {
    var namesFlux = fixture.namesFlux();

    StepVerifier.create(namesFlux)
        .expectNext("Rômero", "Bianca", "Shauana")
        .verifyComplete();

    StepVerifier.create(namesFlux)
        .expectNextCount(3)
        .verifyComplete();

    StepVerifier.create(namesFlux)
        .expectNext("Rômero")
        .expectNextCount(2)
        .verifyComplete();
  }

  @Test
  public void testNameMono() {
    var nameMono = fixture.nameMono();

    StepVerifier.create(nameMono)
        .expectNext("Rômero, Bianca, Shauana")
        .verifyComplete();
  }

  @Test
  public void testNameMonoFlatMapped() {
    var familyMono = fixture.nameMonoFlatMapped();

    StepVerifier.create(familyMono)
        .expectNext(
            List.of(
                'R', 'ô', 'm', 'e', 'r', 'o', ',', ' ', 'B', 'i', 'a', 'n', 'c', 'a', ',',
                ' ', 'S', 'h', 'a', 'u', 'a', 'n', 'a')
        ).verifyComplete();
  }

  @Test
  public void testNameMonoFlatMappedMany() {
    var familyFlux = fixture.nameMonoFlatMappedMany();

    StepVerifier.create(familyFlux)
        .expectNext(
            'R', 'ô', 'm', 'e', 'r', 'o', ',', ' ', 'B', 'i', 'a', 'n', 'c', 'a', ',',
            ' ', 'S', 'h', 'a', 'u', 'a', 'n', 'a')
        .verifyComplete();
  }

  @Test
  public void testNamesFluxMap() {
    var namesFlux = fixture.namesFluxMap();

    StepVerifier.create(namesFlux)
        .expectNext("RÔMERO", "BIANCA", "SHAUANA")
        .verifyComplete();
  }

  @Test
  public void testNamesFluxTransform() {
    var namesFlux = fixture.namesFluxTransform();

    StepVerifier.create(namesFlux)
        .expectNext("RÔMERO", "BIANCA", "SHAUANA")
        .verifyComplete();
  }

  @Test
  public void testNamesFluxImmutability() {
    var namesFlux = fixture.namesFluxImmutability();

    StepVerifier.create(namesFlux)
        .expectNext("Rômero", "Bianca", "Shauana")
        .verifyComplete();
  }

  @Test
  public void testNamesFluxFiltered() {
    var namesFlux = fixture.namesFluxFiltered();

    StepVerifier.create(namesFlux)
        .expectNext("RÔMERO", "BIANCA")
        .verifyComplete();
  }

  @Test
  public void testNamesFluxFilteredDefaultIfEmpty() {
    var namesFlux = fixture.namesFluxFilteredDefaultIfEmpty(10);

    StepVerifier.create(namesFlux)
        .expectNext("default")
        .verifyComplete();
  }

  @Test
  public void testNamesFluxFilteredSwitchIfEmpty() {
    var namesFlux = fixture.namesFluxFilteredSwitchIfEmpty(10);

    StepVerifier.create(namesFlux)
        .expectNext("DEFAULT")
        .verifyComplete();
  }

  @Test
  public void testNamesFluxMappedFiltered() {
    var namesFlux = fixture.namesFluxMappedFiltered();

    StepVerifier.create(namesFlux)
        .expectNext("6-RÔMERO", "6-BIANCA")
        .verifyComplete();
  }

  @Test
  public void testNamesFluxFlatMappedFiltered() {
    var namesFlux = fixture.namesFluxFlatMappedFiltered();

    StepVerifier.create(namesFlux)
        .expectNext('R', 'Ô', 'M', 'E', 'R', 'O', 'B', 'I', 'A', 'N', 'C', 'A')
        .verifyComplete();
  }

  @Test
  public void testNamesFluxFlatMappedFilteredAsync() {
    var namesFlux = fixture.namesFluxFlatMappedFilteredAsync();

    StepVerifier.create(namesFlux)
        .expectNextCount(12)
        .verifyComplete();

    final List<Character> expected = new ArrayList<>(
        List.of('R', 'Ô', 'M', 'E', 'R', 'O', 'B', 'I', 'A', 'N', 'C', 'A')
    );

    final Predicate<Character> matcher = next -> {
      try {
        return expected.contains(next);
      } finally {
        expected.remove(next);
      }
    };

    StepVerifier.create(namesFlux)
        .expectNextMatches(matcher)
        .expectNextMatches(matcher)
        .expectNextMatches(matcher)
        .expectNextMatches(matcher)
        .expectNextMatches(matcher)
        .expectNextMatches(matcher)
        .expectNextMatches(matcher)
        .expectNextMatches(matcher)
        .expectNextMatches(matcher)
        .expectNextMatches(matcher)
        .expectNextMatches(matcher)
        .expectNextMatches(matcher)
        .verifyComplete();
    Assertions.assertTrue(expected.isEmpty());
  }

  @Test
  public void testNamesFluxConcatMappedFiltered() {
    var namesFlux = fixture.namesFluxConcatMappedFiltered();

    StepVerifier.create(namesFlux)
        .expectNextCount(12)
        .verifyComplete();

    StepVerifier.create(namesFlux)
        .expectNext('R', 'Ô', 'M', 'E', 'R', 'O', 'B', 'I', 'A', 'N', 'C', 'A')
        .verifyComplete();
  }

  @Test
  public void testFluxConcat() {
    var alphabetFlux = fixture.alphabetFluxConcat();

    StepVerifier.create(alphabetFlux)
        .expectNext('A', 'B', 'C', 'D', 'E', 'F')
        .verifyComplete();
  }

  @Test
  public void testFluxConcatWith() {
    var alphabetFlux = fixture.alphabetFluxConcatWith();

    StepVerifier.create(alphabetFlux)
        .expectNext('A', 'B', 'C', 'D', 'E', 'F')
        .verifyComplete();
  }

  @Test
  public void testMonoConcatWith() {
    var alphabetFlux = fixture.alphabetMonoConcatWith();

    StepVerifier.create(alphabetFlux)
        .expectNext('A', 'B')
        .verifyComplete();
  }

  @Test
  public void testFluxMerge() {
    var alphabetFlux = fixture.alphabetFluxMerge();

    StepVerifier.create(alphabetFlux)
        .expectNext('A', 'D', 'B', 'E', 'C', 'F')
        .verifyComplete();
  }

  @Test
  public void testFluxMergeSequential() {
    var alphabetFlux = fixture.alphabetFluxMergeSequential();

    StepVerifier.create(alphabetFlux)
        .expectNext('A', 'B', 'C', 'D', 'E', 'F')
        .verifyComplete();
  }

  @Test
  public void testFluxMergeWith() {
    var alphabetFlux = fixture.alphabetFluxMergeWith();

    StepVerifier.create(alphabetFlux)
        .expectNext('A', 'D', 'B', 'E', 'C', 'F')
        .verifyComplete();
  }

  @Test
  public void testMonoMergeWith() {
    var alphabetFlux = fixture.alphabetMonoMergeWith();

    StepVerifier.create(alphabetFlux)
        .expectNext('A', 'B')
        .verifyComplete();
  }

  @Test
  public void testFluxZip() {
    var alphabetFlux = fixture.alphabetFluxZip();

    StepVerifier.create(alphabetFlux)
        .expectNext("AD", "BE", "CF")
        .verifyComplete();
  }

  @Test
  public void testFluxZipWith() {
    var alphabetFlux = fixture.alphabetFluxZipWith();

    StepVerifier.create(alphabetFlux)
        .expectNext("AD", "BE", "CF")
        .verifyComplete();
  }

  @Test
  public void testMonoZipWith() {
    var alphabetMono = fixture.alphabetMonoZipWith();

    StepVerifier.create(alphabetMono)
        .expectNext("AB")
        .verifyComplete();
  }
}