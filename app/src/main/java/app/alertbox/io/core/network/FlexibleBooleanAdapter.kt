package app.alertbox.io.core.network

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class FlexibleBooleanAdapter : TypeAdapter<Boolean>() {
    override fun write(out: JsonWriter, value: Boolean?) {
        if (value == null) out.nullValue() else out.value(value)
    }

    override fun read(input: JsonReader): Boolean? = when (input.peek()) {
        JsonToken.NULL -> input.nextNull().let { null }
        JsonToken.BOOLEAN -> input.nextBoolean()
        JsonToken.NUMBER -> input.nextInt() != 0
        JsonToken.STRING -> input.nextString().let { value ->
            when (value.lowercase()) {
                "true", "1", "yes" -> true
                "false", "0", "no", "" -> false
                else -> throw IllegalStateException("Invalid boolean value")
            }
        }
        else -> throw IllegalStateException("Expected a boolean-compatible value")
    }
}

