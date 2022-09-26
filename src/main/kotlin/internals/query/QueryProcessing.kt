package internals.query

import habitat.racoons.parameters.ParameterMapping
import habitat.racoons.parameters.Parameters
import internals.extensions.isInQuotes

object QueryProcessing {
    /**
     * Converts a query with mixed named and indexed parameters to an indexed one only.
     * @param _query the query to be converted.
     * @return the converted query, the indexed parameter mappings and the named parameter mappings.
     */
    fun reconstructQuery(_query: String, parameters: Parameters): Pair<String, ParameterMapping> {
        var query = _query

        query = replaceLists(query, parameters)

        // Generating the mapping for the parameters
        val mapping = generateParametersMapping(query)

        // Generating the query without the parameters
        val processedQuery = removeNamedParameters(query)

        return Pair(processedQuery, mapping)
    }

    private fun calculateMatches(query: String): Pair<List<MatchResult>, List<MatchResult>> {
        // Regex to find the parameters in the query
        val indexRegex = Regex("\\?")
        val namedRegex = Regex(":[\\w\\u0080-\\u00FF]+")

        // Finding only the parameters that are not quoted
        val indexMatches = indexRegex.findAll(query).toList().filter { !query.isInQuotes(it.range.first) }
        val namedMatches = namedRegex.findAll(query).toList().filter { !query.isInQuotes(it.range.first) }

        return indexMatches to namedMatches
    }

    fun replaceLists(_query: String, parameters: Parameters): String {
        // Creating a mutable query and matches
        var query = _query
        var matches = calculateMatches(query)

        // Cycle every match
        var i = 0  // Iteration counter
        var offset = 0  // Changes counter
        while (i - offset < matches.first.size) {
            // Get the match
            val m = matches.first[i - offset]

            // Get the value of the parameter
            val v = parameters.indexedParameters[i + 1]

            // Check if the value is a list
            if (v is List<*>) {
                // Create the replacement string
                val s = v.withIndex().joinToString(",") { ":racoon_internal_ip_${i + 1}_${it.index}" }
                // Replace in the query
                query = query.replaceRange(m.range, s)

                // Recalculate the matches
                matches = calculateMatches(query)

                // Increase the changes counter
                offset++
            }
            // Increase the iteration counter
            i++
        }

        // Cycle every match
        i = 0  // Iteration counter
        offset = 0  // Changes counter (works differently than the above)
        while (i + offset < matches.second.size) {
            // Get the match
            val m = matches.second[i + offset]
            val sub = m.value.substring(1)

            // Skipping racoon_internal_ip_ parameters
            if (sub.startsWith("racoon_internal_ip_")) {
                i++
                continue
            }

            // Get the value of the parameter
            val v = parameters.namedParameters[sub]

            // Check if the value is a list
            if (v is List<*>) {
                // Create the replacement string
                val s = v.withIndex().joinToString(",") { ":racoon_internal_ni_${sub}_${it.index}" }
                // Replace in the query
                query = query.replaceRange(m.range, s)

                // Recalculate the matches
                matches = calculateMatches(query)

                // Update the counters
                i--
                offset += v.size
            }
            // Increase the iteration counter
            i++
        }

        // Return the converted query
        return query
    }

    /**
     * Generates the mapping from the new query to the old query for each parameter.
     * @param query the query to generate the mappings from.
     * @return A [Pair] containing the indexed and named mappings.
     */
    private fun generateParametersMapping(query: String): ParameterMapping {
        val (indexMatches, namedMatches) = calculateMatches(query)
        // Merging the matches into one list
        val matches = (indexMatches + namedMatches).sortedBy { it.range.first }

        // Creating the mapping to keep track of the correspondence between the new query and the original query
        val parameterMapping = ParameterMapping()

        var offset = 0  // Counter of named parameters encountered so far
        var counter = 1  // Counter of the indexed parameters encountered so far (starting from 1)
        for (m in matches) {
            // Checks if is an indexed parameter
            if (m.range.first == m.range.last) {
                parameterMapping.addIndexed(counter, counter + offset)
                counter++
            } else {
                parameterMapping.addNamed(m.value.substring(1), counter + offset)
                offset++
            }
        }

        return parameterMapping
    }

    /**
     * Removes the named parameters from the query and replaces them with a question mark
     * @param query The query to be processed
     * @return The query where the named parameters have been replaced with a question mark
     */
    private fun removeNamedParameters(query: String): String {
        val (_, namedMatches) = calculateMatches(query)

        // The offset of the namedMatches. This needs to be tracked because the string is modified in the loop.
        var offset = 0

        var q = query
        for (m in namedMatches) {
            // Replace the named parameter with a question mark
            q = q.replaceRange(m.range.first - offset..m.range.last - offset, "?")
            // Update the offset
            offset += m.range.last - m.range.first
        }
        return q
    }
}