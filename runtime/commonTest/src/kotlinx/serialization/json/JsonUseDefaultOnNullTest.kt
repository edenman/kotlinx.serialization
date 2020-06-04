/*
 * Copyright 2017-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.serialization.json

import kotlinx.serialization.*
import kotlin.test.*

class JsonUseDefaultOnNullTest : JsonTestBase() {
    @Serializable
    data class WithBoolean(val b: Boolean = false)

    @Serializable
    data class WithEnum(val e: SampleEnum = SampleEnum.OptionC)

    @Serializable
    data class MultipleValues(
        val data: StringData,
        val data2: IntData = IntData(0),
        val i: Int = 42,
        val e: SampleEnum = SampleEnum.OptionA,
        val foo: String
    )

    val json = Json(JsonConfiguration.Default.copy(treatNullAsMissing = true, isLenient = true))

    private inline fun <reified T> doTest(inputs: List<String>, expected: T) {
        for (input in inputs) {
            // todo : this overload works incorrectly
//            parametrizedTest(json) { j ->
            val parsed = json.parse(serializer<T>(), input)
            assertEquals(expected, parsed, "Failed on input: $input")
//            }
        }
    }

    @Test
    fun testBoolean() = doTest(
        listOf(
            """{"b":false}""",
            """{"b":null}""",
            """{}""",
        ),
        WithBoolean()
    )

    @Test
    fun testEnum() {
        doTest(
            listOf(
                """{"e":unknown_value}""",
                """{"e":"unknown_value"}""",
                """{"e":null}""",
                """{}""",
            ),
            WithEnum()
        )
        assertFailsWith<JsonDecodingException> {
            json.parse(WithEnum.serializer(), """{"e":{"x":"definitely not a valid enum value"}}""")
        }
        assertFailsWith<JsonDecodingException> { // test user still sees exception on missing quotes
            Json(json.configuration.copy(isLenient = false)).parse(WithEnum.serializer(), """{"e":unknown_value}""")
        }
    }

    @Test
    fun testAll() {
        val testData = mapOf<String, MultipleValues>(
            """{"data":{"data":"foo"},"data2":null,"i":null,"e":null,"foo":"bar"}""" to MultipleValues(
                StringData("foo"),
                foo = "bar"
            ),
            """{"data":{"data":"foo"},"data2":{"intV":42},"i":null,"e":null,"foo":"bar"}""" to MultipleValues(
                StringData(
                    "foo"
                ), IntData(42), foo = "bar"
            ),
            """{"data":{"data":"foo"},"data2":{"intV":42},"i":0,"e":"NoOption","foo":"bar"}""" to MultipleValues(
                StringData("foo"),
                IntData(42),
                i = 0,
                foo = "bar"
            ),
            """{"data":{"data":"foo"},"data2":{"intV":42},"i":0,"e":"OptionC","foo":"bar"}""" to MultipleValues(
                StringData("foo"),
                IntData(42),
                i = 0,
                e = SampleEnum.OptionC,
                foo = "bar"
            ),
        )
        for ((input, expected) in testData) {
            assertEquals(expected, json.parse(MultipleValues.serializer(), input), "Failed on input: $input")
        }
    }

}
