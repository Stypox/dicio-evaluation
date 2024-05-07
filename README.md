# Dicio evaluation

This repository contains **code** and **data** to evaluate various algorithms that compare a *user sentence* to a set of *reference sentences*, giving a score to each comparison in order to find the best match. The purpose of this repository is to choose one such algorithm and (incrementally) tune the parameters for use in the [Dicio assistant](https://github.com/Stypox/dicio-android).

## Definitions

### User sentence

A *user sentence* is a sentence that a user may provide as input to an assistant, e.g. "What's the weather like?".

### Reference sentence

A *reference sentence* is a sentence whose "meaning" is known, and can therefore be used to compare against the user sentence. Actually, a reference sentence is not really just a sentence, but is actually a [Component](#component) that takes care of matching.

### Component

A component is the basic block of [reference sentences](#reference-sentence). Its purpose is to hold some data (e.g. a reference word), and compare this data to a substring of the [user sentence](#user-sentence), providing one or more [`MatchResult`](#matchresult)s that can be used for scoring. Components are also supposed to extract data the user sentence (e.g. parse "one minute thirty seconds" into a duration), but that's not implemented in this repo.

The components available in this repo are only the most basic ones, namely:
- a `WordComponent` which just matches a word
- a `CapturingComponent` which can match any number of words or characters
- a `CompositeComponent` which takes care of matching multiple other components one after the other

#### `MatchResult`

A `MatchResult` is made of 4 parameters:
- `userMatched`: approximately counts the number of words in the *user sentence* that were matched correctly. Should roughly be increased by the same amount `userWeight` is increased by, every time a word from the user sentence *is consumed and matches*.
- `userWeight`: approximately counts the total number of words in the user sentence. Should roughly be increased by 1 every time a word from the user sentence *is consumed*.
- `refMatched`: approximately counts the number of words in the *reference sentence* that were matched correctly. Should roughly be increased by the same amount `refWeight` is increased by, every time a word from the reference sentence *is consumed and matches*.
- `refWeight`: approximately counts the total number of words in the reference sentence. Should roughly be increased by 1 every time a word from the reference sentence is consumed.

The terms "approximately" and "rougly" were used a lot here, because:
- characters can be matched too, and not only words, although a smaller user weight is assigned to them
- some important words may be assigned a user weight > 1, while less important words (e.g. "a", "the") may be assigned a user weight < 1
- some Components are not really words, e.g. the `CapturingComponent`, but still have a reference weight

## The algorithm

The main part of the algorithm is implemented in `CompositeComponent`, because it's that component's job to find the best way to match the components it manages, to the user sentence. The basic algorithm is quite dumb, as it just tries to match each sub-component to every possible substring of the user sentence. All of the possible ways to match each sub-component are combined in a list of `MatchResult`s by summing related fields in sub-`MatchResult`s, and `userWeight` is further increased in case of skipped characters. A layer of memoization and caching is applied to the algorithm to make it more efficient; moreover, `MatchResult`s have the `end` and `canGrow` fields that help avoid recalculations. In any case, the complexity of the algorithm is obviously quite bad, since the algorithm generates `(u + 1) * binom(u + r, r)` possible matches, where `u` is the number of characters in the user sentence, while `r` is the number of sub-components (assuming each sub-component only generates one `MatchResult`).

## Strategies

The algorithm's purpose is just to try explore all possible ways to arrange components and generate a list of `MatchResult`s. However, there still isn't anything that calculates a score! This is where `Strategy`ies come into play. A strategy is made of a scoring function and a pruning function.

### Scoring function

A scoring function takes a `MatchResult` as input and produces a scalar `Float` score as output. Higher scores mean "better match", lower scores mean "worse match". Scores will be used when comparing `MatchResult`s, and in particular when choosing the best `MatchResult` returned by the algorithm.

### Pruning function

A pruning function is used to make the algorithm more efficient, at the cost of possibly preventing the optimal solution from being discovered. Optimal solution means "`MatchResult` with the highest score". The pruning function is *greedy*, since it tries to discard intermediate solutions which it believes are not going to contribute to the final optimal solution.

### When pruning is safe (linear scoring functions)

If the scoring function $f: matchresult \to score$ respects the following property, the pruning function will be able to make the optimal choice at each step, making the algorithm fast without sacrificing the optimal solution.

$$f(m_1) > f(m_2) \implies f(m_3 + m_1) > f(m_3 + m_2) \space\space \forall m_1, m_2, m_3$$

<p align="center"><sup>(the sum between two <code>MatchResult</code>s is defined as the pairwise sum between corresponding fields)</sup></p>

This property is always true for **linear functions**, and more generally in functions of the form $g(f(m))$ where $f$ is linear with respect to `MatchResult` fields, and $g: R \to R$ is monotone. I don't know if there are other functions that respect this property, but all the non-linear scoring functions I came up with while testing don't respect it.
