package com.example.projectreactor.error.handling;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


/**
 * See <a href="https://devdojo.com/ketonemaniac/reactor-onerrorcontinue-vs-onerrorresume">reactor-onerrorcontinue-vs-onerrorresume</a>
 */
public class OnErrorContinueVsOnErrorResume {
    private final Logger log = LoggerFactory.getLogger(OnErrorContinueVsOnErrorResume.class);



    @Test
    void onErrorResume() {
        Flux.range(1,5)
                .doOnNext(i -> System.out.println("input=" + i))
                .map(i -> i == 2 ? i / 0 : i)
                .map(i -> i * 2)
                .onErrorResume(err -> {
                    log.info("onErrorResume");
                    return Flux.empty();
                })
                .subscribe(System.out::println);
    }


    @Test
    void onErrorContinue() {
        Flux.range(1,5)
                .doOnNext(i -> System.out.println("input=" + i))
                .map(i -> i == 2 ? i / 0 : i)
                .map(i -> i * 2)
                .onErrorContinue((err, i) -> {log.info("onErrorContinue={}", i);})
                .subscribe(System.out::println);
    }

    @Test
    void onErrorResumeThenOnErrorContinue() {
        Flux.range(1,5)
                .doOnNext(i -> System.out.println("input=" + i))
                .map(i -> i == 2 ? i / 0 : i)
                .map(i -> i * 2)
                .onErrorResume(err -> {
                    log.info("onErrorResume");
                    return Flux.empty();
                })
                .onErrorContinue((err, i) -> {log.info("onErrorContinue={}", i);})
                .subscribe(System.out::println);
    }


    /**
     * Get rid of onErrorContinue() at all and just use onErrorResume() in all scenarios.
     */
    @Test
    void justUseOnErrorResume() {
        Flux.range(1,5)
                .doOnNext(i -> System.out.println("input=" + i))
                .flatMap(i -> Mono.just(i)
                        .map(j -> j == 2 ? j / 0 : j)
                        .map(j -> j * 2)
                        .onErrorResume(err -> {
                            System.out.println("onErrorResume");
                            return Mono.empty();
                        })
                )
                .subscribe(System.out::println);
    }

    @Test
    void test2() {
        Flux<Integer> originalFlux = Flux.range(1, 5);

        originalFlux
                .flatMap(element -> {
                    if (element == 2) {
                        return Mono.error(new RuntimeException("Error processing element 2"));
                    } else {
                        return Mono.just(element);
                    }
                })
                .onErrorResume(ex -> {
                    log.error("Error while processing {}. Cause: {}", ex.getMessage());
                    // Provide a fallback value or alternative behavior here
                    return Mono.empty();
                })
                .subscribe(System.out::println);
    }



    /**
     * Mimic onErrorContinue() using onErrorResume() with downstream onErrorContinue()
     *
     * Sometimes, onErrorContinue() is put in the caller and you have no control over it. But you still want your onErrorResume(). What shall you do?
     *
     * The secret is to add onErrorStop() in the end of the onErrorResume() block -- this would block the onErrorContinue()
     * so that it would not take up the error before onErrorResume() does. Try removing onErrorStop() and you will see
     * onErrorContinue() pop up as before.
     */
    @Test
    void onErrorStop() {
        Flux.range(1,5)
                .doOnNext(i -> System.out.println("input=" + i))
                .flatMap(i -> Mono.just(i)
                        .map(j -> j == 2 ? j / 0 : j)
                        .map(j -> j * 2)
                        .onErrorResume(err -> {
                            System.out.println("onErrorResume");
                            return Mono.empty();
                        })
                      //  .onErrorStop()
                )
                .onErrorContinue((err, i) -> {log.info("onErrorContinue={}", i);})
                .subscribe(System.out::println);
    }
}
