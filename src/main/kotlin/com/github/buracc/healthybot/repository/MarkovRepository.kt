package com.github.buracc.healthybot.repository

import com.github.buracc.healthybot.discord.model.Markov
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import net.dv8tion.jda.api.JDA
import org.springframework.stereotype.Repository
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

@Repository
class MarkovRepository(
    private val jda: JDA
) {
    private val gson = GsonBuilder().enableComplexMapKeySerialization().create()
    private var lastSave = 0L
    private var storageFile = File("/healthybot/data/markovs")
    private var markovs = if (storageFile.exists()) {
        val json = ungzip(storageFile.readBytes())
        gson.fromJson(json, TypeToken.getParameterized(MutableMap::class.java, String::class.java, Markov::class.java).type)
    } else {
        mutableMapOf<String, Markov>()
    }

    fun purge(userId: String): String? {
        markovs.remove(userId)
        return get(userId)
    }

    fun get(userId: String): String? {
        val markov = markovs[userId] ?: return null
        return markov.generate()
    }

    fun store(userId: String, msgContent: String) {
        if (userId == jda.selfUser.id) {
            return
        }

        val authorMarkov = markovs.getOrPut(userId) { Markov() }
        val globalMarkov = markovs.getOrPut(jda.selfUser.id) { Markov() }
        val formatted = formatContent(msgContent)
        authorMarkov.addPhrase(formatted)
        globalMarkov.addPhrase(formatted)

        if (System.currentTimeMillis() - lastSave > TimeUnit.MINUTES.toMillis(15)) {
            val json = gson.toJson(markovs)
            storageFile.writeBytes(gzip(json))
            lastSave = System.currentTimeMillis()
        }
    }

    private fun formatContent(content: String): String {
        val capitalized = content.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        return if (capitalized.lastOrNull() in setOf('.', '?', '!')) capitalized else "$capitalized."
    }

    private fun gzip(content: String): ByteArray {
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).bufferedWriter().use { it.write(content) }
        return bos.toByteArray()
    }

    private fun ungzip(content: ByteArray) = GZIPInputStream(content.inputStream()).bufferedReader().use { it.readText() }
}