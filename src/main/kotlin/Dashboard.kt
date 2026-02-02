package com.tohid

class Dashboard(private val title: String) {
    private val components = mutableListOf<String>()

    fun addComponent(header: String, items: List<Any>) {
        val body = if (items.isEmpty()) {
            "  [ No data available ]"
        } else {
            items.joinToString("\n") { "  • $it" }
        }

        val card = """
        +--- $header ---+
        $body
        +-----------------------+
        """.trimIndent()

        components.add(card)
    }

    fun render() {
        println("\n" + "=".repeat(10) + " $title " + "=".repeat(10))
        components.forEach { println(it) }
        println("=".repeat(25 + title.length) + "\n")
    }
}

