/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */
@file:UseSerializers(MyNullableIntSerializer::class, MyNonNullableIntSerializer::class)

package kotlinx.serialization

import kotlinx.serialization.SerializationForNullableTypeOnFileTest.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*
import kotlin.test.*

class SerializationForNullableTypeOnFileTest {

    @Serializable
    data class Holder(val nullable: Int?, val nonNullable: Int)

    @Serializer(forClass = Int::class)
    object MyNullableIntSerializer : KSerializer<Int?> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("MIS", PrimitiveKind.INT).nullable

        override fun serialize(encoder: Encoder, value: Int?) {
            if (value == null || value == 0) encoder.encodeNull()
            else encoder.encodeInt(value)
        }

        override fun deserialize(decoder: Decoder): Int? {
            val value = decoder.decodeInt()
            return if (value == 0) null else value
        }
    }

    @Serializer(forClass = Int::class)
    object MyNonNullableIntSerializer : KSerializer<Int> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("MIS", PrimitiveKind.INT)

        override fun serialize(encoder: Encoder, value: Int) {
            return encoder.encodeInt(value)
        }

        override fun deserialize(decoder: Decoder): Int {
            return decoder.decodeInt()
        }
    }

    @Test
    fun testFileLevel() {
        assertEquals("""{"nullable":null,"nonNullable":51}""", Json.encodeToString(Holder(nullable = null, nonNullable = 51)))
        assertEquals("""{"nullable":314,"nonNullable":51}""", Json.encodeToString(Holder(nullable = 314, nonNullable = 51)))
        assertEquals(Holder(nullable = null, nonNullable = 51), Json.decodeFromString("""{"nullable":0,"nonNullable":51}"""))
        assertEquals(Holder(nullable = 314, nonNullable = 51), Json.decodeFromString("""{"nullable":314,"nonNullable":51}"""))
    }
}
