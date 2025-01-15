package com.github.dinbtechit.ngxs.common.models

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

@Serializable
data class SchematicsCollection (
    val schematics: Map<String, SchematicInfo>
)

@Serializable
data class SchematicInfo(
    val aliases: List<String>? = null,
    val factory: String? = null,
    val schema: String? = null,
    val description: String? = null
)


@Serializable
data class SchematicDetails(
    val title: String?,
    val type: String?,
    val properties: Map<String, SchematicParameters>? = null,
    val required: List<String>? = null
)

@Serializable
data class SchematicParameters(
    val type: String? = null,
    @Serializable(with = AnyValueSerializer::class)
    val default: AnyValue? = null,
    val description: String? = null
)


sealed class AnyValue {

    @Serializable
    data class StringValue(val value: String): AnyValue() {
        override fun toString(): String {
            return value
        }
    }

    @Serializable
    data class BooleanValue(val value: Boolean): AnyValue() {
        override fun toString(): String {
            return "$value"
        }
    }

    @Serializable
    data class NullValue(val value: String? = null): AnyValue() {
        override fun toString(): String {
            return ""
        }
    }
}

object AnyValueSerializer : KSerializer<AnyValue> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("AnyValue", PrimitiveKind.STRING)

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: AnyValue) = when (value) {
        is AnyValue.StringValue -> encoder.encodeString(value.value)
        is AnyValue.BooleanValue -> encoder.encodeBoolean(value.value)
        is AnyValue.NullValue -> encoder.encodeNull()
    }

    override fun deserialize(decoder: Decoder): AnyValue {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("This class can be loaded only by Json")
        val element = jsonDecoder.decodeJsonElement()
        return when {
            element is JsonPrimitive && element.isString -> AnyValue.StringValue(element.content)
            element is JsonPrimitive && element.booleanOrNull != null -> AnyValue.BooleanValue(element.boolean)
            element is JsonNull -> AnyValue.NullValue()
            else -> throw SerializationException("Unexpected data type.")
        }
    }
}
