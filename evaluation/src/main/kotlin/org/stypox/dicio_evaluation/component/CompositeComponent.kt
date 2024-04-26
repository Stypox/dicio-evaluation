package org.stypox.dicio_evaluation.component

class CompositeComponent(
    private val components: List<Component>
) : Component {
    fun match(userInput: String): MatchResult {
        return match(0, userInput.length, Tokenizations(userInput))
    }

    override fun match(start: Int, end: Int, tokenizations: Tokenizations): MatchResult {
        // TODO
        return components[0].match(start, end, tokenizations)
    }
}
