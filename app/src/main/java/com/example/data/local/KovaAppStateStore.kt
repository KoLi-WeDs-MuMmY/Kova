package com.example.data.local

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PersistedLessonState(
    val statusName: String,
    val passScorePercent: Int? = null,
)

data class PersistedChapterState(
    val isUnlocked: Boolean,
    val assignmentUnlocked: Boolean,
    val assignmentCompleted: Boolean,
)

data class PersistedAppState(
    val schemaVersion: Int = 1,
    val themeModeName: String = "WARM_DAYLIGHT",
    val dynamicColorEnabled: Boolean = false,
    val expressiveMotionEnabled: Boolean = true,
    val soundAndHapticsEnabled: Boolean = true,
    val devUnlockAllOverride: Boolean = false,
    val lessonSplitRatioName: String = "RATIO_75_25",
    val expandedChapterIds: Set<String> = setOf("ch_1"),
    val dailyLessonsRemaining: Int = 3,
    val activeAiPoolRemaining: Int = 12,
    val lastQuotaDateIso: String = currentLocalDateIso(),
    val activeThreadId: String? = null,
    val chatComposerDraft: String = "",
    val sprintGotItCount: Int = 0,
    val sprintReviewAgainCount: Int = 0,
    val sprintCurrentIndex: Int = 0,
    val lessonStates: Map<String, PersistedLessonState> = emptyMap(),
    val chapterStates: Map<String, PersistedChapterState> = emptyMap(),
)

interface KovaAppStateStore {
    fun load(): PersistedAppState?
    fun save(state: PersistedAppState)
    fun clear()
}

fun currentLocalDateIso(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

/**
 * In-memory implementation for unit tests that don't need disk I/O.
 */
class InMemoryKovaAppStateStore(
    initial: PersistedAppState? = null,
) : KovaAppStateStore {
    private var snapshot: PersistedAppState? = initial

    override fun load(): PersistedAppState? = snapshot

    override fun save(state: PersistedAppState) {
        snapshot = state
    }

    override fun clear() {
        snapshot = null
    }
}

/**
 * Crash-safe atomic JSON file store inside the app's private filesDir.
 * Requires zero Android permissions and works identically on Android devices and JVM unit tests.
 */
class FileKovaAppStateStore(
    private val targetFile: File,
) : KovaAppStateStore {
    private val lock = Any()

    override fun load(): PersistedAppState? = synchronized(lock) {
        if (!targetFile.exists()) return null
        val raw = runCatching { targetFile.readText(Charsets.UTF_8) }.getOrNull() ?: return null
        if (raw.isBlank()) return null
        runCatching { decodeStateJson(raw) }.getOrNull()
    }

    override fun save(state: PersistedAppState) = synchronized(lock) {
        runCatching {
            targetFile.parentFile?.mkdirs()
            val encoded = encodeStateJson(state)
            val tmpFile = File(targetFile.parentFile, "${targetFile.name}.tmp")
            tmpFile.writeText(encoded, Charsets.UTF_8)
            if (!tmpFile.renameTo(targetFile)) {
                targetFile.writeText(encoded, Charsets.UTF_8)
                tmpFile.delete()
            }
        }
        Unit
    }

    override fun clear() = synchronized(lock) {
        runCatching { targetFile.delete() }
        Unit
    }

    companion object {
        const val DEFAULT_FILE_NAME = "kova_app_state_v1.json"

        fun encodeStateJson(state: PersistedAppState): String {
            val sb = StringBuilder(1024)
            sb.append('{')
            sb.appendField("schemaVersion", state.schemaVersion.toString()).append(',')
            sb.appendStringField("themeModeName", state.themeModeName).append(',')
            sb.appendField("dynamicColorEnabled", state.dynamicColorEnabled.toString()).append(',')
            sb.appendField("expressiveMotionEnabled", state.expressiveMotionEnabled.toString()).append(',')
            sb.appendField("soundAndHapticsEnabled", state.soundAndHapticsEnabled.toString()).append(',')
            sb.appendField("devUnlockAllOverride", state.devUnlockAllOverride.toString()).append(',')
            sb.appendStringField("lessonSplitRatioName", state.lessonSplitRatioName).append(',')
            sb.append("\"expandedChapterIds\":[")
            state.expandedChapterIds.forEachIndexed { idx, id ->
                if (idx > 0) sb.append(',')
                sb.appendJsonString(id)
            }
            sb.append("],")
            sb.appendField("dailyLessonsRemaining", state.dailyLessonsRemaining.toString()).append(',')
            sb.appendField("activeAiPoolRemaining", state.activeAiPoolRemaining.toString()).append(',')
            sb.appendStringField("lastQuotaDateIso", state.lastQuotaDateIso).append(',')
            sb.append("\"activeThreadId\":")
            if (state.activeThreadId == null) {
                sb.append("null")
            } else {
                sb.appendJsonString(state.activeThreadId)
            }
            sb.append(',')
            sb.appendStringField("chatComposerDraft", state.chatComposerDraft).append(',')
            sb.appendField("sprintGotItCount", state.sprintGotItCount.toString()).append(',')
            sb.appendField("sprintReviewAgainCount", state.sprintReviewAgainCount.toString()).append(',')
            sb.appendField("sprintCurrentIndex", state.sprintCurrentIndex.toString()).append(',')

            sb.append("\"lessonStates\":{")
            state.lessonStates.entries.forEachIndexed { idx, (lessonId, ls) ->
                if (idx > 0) sb.append(',')
                sb.appendJsonString(lessonId).append(":{")
                sb.appendStringField("statusName", ls.statusName).append(',')
                sb.append("\"passScorePercent\":")
                sb.append(ls.passScorePercent?.toString() ?: "null")
                sb.append('}')
            }
            sb.append("},")

            sb.append("\"chapterStates\":{")
            state.chapterStates.entries.forEachIndexed { idx, (chapterId, cs) ->
                if (idx > 0) sb.append(',')
                sb.appendJsonString(chapterId).append(":{")
                sb.appendField("isUnlocked", cs.isUnlocked.toString()).append(',')
                sb.appendField("assignmentUnlocked", cs.assignmentUnlocked.toString()).append(',')
                sb.appendField("assignmentCompleted", cs.assignmentCompleted.toString())
                sb.append('}')
            }
            sb.append("}}")
            return sb.toString()
        }

        @Suppress("UNCHECKED_CAST")
        fun decodeStateJson(raw: String): PersistedAppState {
            val root = MinimalJsonParser(raw).parseValue() as? Map<String, Any?>
                ?: error("Expected root JSON object")

            val expanded = (root["expandedChapterIds"] as? List<Any?>)
                ?.mapNotNull { it as? String }
                ?.toSet()
                ?: setOf("ch_1")

            val lessonMapRaw = root["lessonStates"] as? Map<String, Any?> ?: emptyMap()
            val lessonStates = buildMap {
                lessonMapRaw.forEach { (lessonId, value) ->
                    val obj = value as? Map<String, Any?> ?: return@forEach
                    val statusName = obj["statusName"] as? String ?: return@forEach
                    val score = (obj["passScorePercent"] as? Number)?.toInt()
                    put(lessonId, PersistedLessonState(statusName = statusName, passScorePercent = score))
                }
            }

            val chapterMapRaw = root["chapterStates"] as? Map<String, Any?> ?: emptyMap()
            val chapterStates = buildMap {
                chapterMapRaw.forEach { (chapterId, value) ->
                    val obj = value as? Map<String, Any?> ?: return@forEach
                    put(
                        chapterId,
                        PersistedChapterState(
                            isUnlocked = obj["isUnlocked"] as? Boolean ?: false,
                            assignmentUnlocked = obj["assignmentUnlocked"] as? Boolean ?: false,
                            assignmentCompleted = obj["assignmentCompleted"] as? Boolean ?: false,
                        ),
                    )
                }
            }

            return PersistedAppState(
                schemaVersion = (root["schemaVersion"] as? Number)?.toInt() ?: 1,
                themeModeName = root["themeModeName"] as? String ?: "WARM_DAYLIGHT",
                dynamicColorEnabled = root["dynamicColorEnabled"] as? Boolean ?: false,
                expressiveMotionEnabled = root["expressiveMotionEnabled"] as? Boolean ?: true,
                soundAndHapticsEnabled = root["soundAndHapticsEnabled"] as? Boolean ?: true,
                devUnlockAllOverride = root["devUnlockAllOverride"] as? Boolean ?: false,
                lessonSplitRatioName = root["lessonSplitRatioName"] as? String ?: "RATIO_75_25",
                expandedChapterIds = expanded,
                dailyLessonsRemaining = (root["dailyLessonsRemaining"] as? Number)?.toInt() ?: 3,
                activeAiPoolRemaining = (root["activeAiPoolRemaining"] as? Number)?.toInt() ?: 12,
                lastQuotaDateIso = root["lastQuotaDateIso"] as? String ?: currentLocalDateIso(),
                activeThreadId = root["activeThreadId"] as? String,
                chatComposerDraft = root["chatComposerDraft"] as? String ?: "",
                sprintGotItCount = (root["sprintGotItCount"] as? Number)?.toInt() ?: 0,
                sprintReviewAgainCount = (root["sprintReviewAgainCount"] as? Number)?.toInt() ?: 0,
                sprintCurrentIndex = (root["sprintCurrentIndex"] as? Number)?.toInt() ?: 0,
                lessonStates = lessonStates,
                chapterStates = chapterStates,
            )
        }

        private fun StringBuilder.appendField(key: String, rawValue: String): StringBuilder {
            appendJsonString(key).append(':').append(rawValue)
            return this
        }

        private fun StringBuilder.appendStringField(key: String, value: String): StringBuilder {
            appendJsonString(key).append(':').appendJsonString(value)
            return this
        }

        private fun StringBuilder.appendJsonString(value: String): StringBuilder {
            append('"')
            for (ch in value) {
                when (ch) {
                    '"' -> append("\\\"")
                    '\\' -> append("\\\\")
                    '\b' -> append("\\b")
                    '\u000C' -> append("\\f")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> {
                        if (ch.code < 0x20) {
                            append(String.format(Locale.US, "\\u%04x", ch.code))
                        } else {
                            append(ch)
                        }
                    }
                }
            }
            append('"')
            return this
        }
    }
}

private class MinimalJsonParser(
    private val input: String,
) {
    private var pos = 0

    fun parseValue(): Any? {
        skipWhitespace()
        if (pos >= input.length) error("Unexpected end of JSON")
        return when (val ch = input[pos]) {
            '{' -> parseObject()
            '[' -> parseArray()
            '"' -> parseString()
            't' -> consumeLiteral("true", true)
            'f' -> consumeLiteral("false", false)
            'n' -> consumeLiteral("null", null)
            '-', in '0'..'9' -> parseNumber()
            else -> error("Unexpected token '$ch' at $pos")
        }
    }

    private fun parseObject(): Map<String, Any?> {
        pos++ // '{'
        skipWhitespace()
        val result = LinkedHashMap<String, Any?>()
        if (peek() == '}') {
            pos++
            return result
        }
        while (pos < input.length) {
            skipWhitespace()
            val key = parseString()
            skipWhitespace()
            if (peek() != ':') error("Expected ':' at $pos")
            pos++
            result[key] = parseValue()
            skipWhitespace()
            when (peek()) {
                ',' -> pos++
                '}' -> {
                    pos++
                    return result
                }
                else -> error("Expected ',' or '}' at $pos")
            }
        }
        error("Unterminated JSON object")
    }

    private fun parseArray(): List<Any?> {
        pos++ // '['
        skipWhitespace()
        val list = ArrayList<Any?>()
        if (peek() == ']') {
            pos++
            return list
        }
        while (pos < input.length) {
            list.add(parseValue())
            skipWhitespace()
            when (peek()) {
                ',' -> pos++
                ']' -> {
                    pos++
                    return list
                }
                else -> error("Expected ',' or ']' at $pos")
            }
        }
        error("Unterminated JSON array")
    }

    private fun parseString(): String {
        if (peek() != '"') error("Expected '\"' at $pos")
        pos++
        val sb = StringBuilder()
        while (pos < input.length) {
            val ch = input[pos++]
            when (ch) {
                '"' -> return sb.toString()
                '\\' -> {
                    if (pos >= input.length) error("Unterminated escape")
                    when (val esc = input[pos++]) {
                        '"' -> sb.append('"')
                        '\\' -> sb.append('\\')
                        '/' -> sb.append('/')
                        'b' -> sb.append('\b')
                        'f' -> sb.append('\u000C')
                        'n' -> sb.append('\n')
                        'r' -> sb.append('\r')
                        't' -> sb.append('\t')
                        'u' -> {
                            val hex = input.substring(pos, pos + 4)
                            sb.append(hex.toInt(16).toChar())
                            pos += 4
                        }
                        else -> sb.append(esc)
                    }
                }
                else -> sb.append(ch)
            }
        }
        error("Unterminated JSON string")
    }

    private fun parseNumber(): Number {
        val start = pos
        if (peek() == '-') pos++
        while (pos < input.length && input[pos] in '0'..'9') pos++
        var isDouble = false
        if (peek() == '.') {
            isDouble = true
            pos++
            while (pos < input.length && input[pos] in '0'..'9') pos++
        }
        if (peek() == 'e' || peek() == 'E') {
            isDouble = true
            pos++
            if (peek() == '+' || peek() == '-') pos++
            while (pos < input.length && input[pos] in '0'..'9') pos++
        }
        val token = input.substring(start, pos)
        return if (isDouble) token.toDouble() else token.toLong()
    }

    private fun consumeLiteral(expected: String, value: Any?): Any? {
        if (!input.startsWith(expected, pos)) error("Expected $expected at $pos")
        pos += expected.length
        return value
    }

    private fun skipWhitespace() {
        while (pos < input.length && input[pos].isWhitespace()) pos++
    }

    private fun peek(): Char? = if (pos < input.length) input[pos] else null
}
