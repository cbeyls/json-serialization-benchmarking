/*
 * Copyright (c) 2019 Zac Sweers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.zacsweers.jsonserialization.android

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.filters.LargeTest
import com.google.common.base.Charsets
import com.google.common.io.Resources
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dev.zacsweers.jsonserialization.models.adapter.GeneratedJsonAdapterFactory
import dev.zacsweers.jsonserialization.models.adapter.GeneratedTypeAdapterFactory
import dev.zacsweers.jsonserialization.models.kotlinx_serialization.Response
import dev.zacsweers.jsonserialization.models.java_serialization.ResponseJ
import dev.zacsweers.jsonserialization.models.model_av.ResponseAV
import dev.zacsweers.jsonserialization.models.moshiKotlinCodegen.KCGResponse
import dev.zacsweers.jsonserialization.models.moshiKotlinReflective.KRResponse
import kotlinx.serialization.KSerializer
import okio.Buffer
import okio.BufferedSink
import okio.BufferedSource
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Reader
import java.io.Writer

@LargeTest
@RunWith(Parameterized::class)
class AndroidBenchmark(
    minified: Boolean
) {

  companion object {
    @JvmStatic
    @Parameters(name = "minified={0}")
    fun data(): List<Array<*>> {
      return listOf(
          arrayOf(true),
          arrayOf(false)
      )
    }
  }

  class KotlinxSerialization(json: String) {

    val response: Response
    val kSerializer: KSerializer<Response> = Response.underlyingSerializer()

    init {
      response = Response.parse(kSerializer, json)
    }
  }

  class KotlinxSerializationBuffer(private val json: String) {

    val response: Response
    val kSerializer: KSerializer<Response> = Response.underlyingSerializer()
    lateinit var bufferedSource: BufferedSource
    lateinit var bufferedSink: BufferedSink
    lateinit var inputStream: InputStream
    lateinit var outputStream: OutputStream

    init {
      response = Response.parse(kSerializer, json)
    }

    fun setupIteration() {
      bufferedSource = Buffer().write(json.toByteArray())
      inputStream = bufferedSource.inputStream()
      bufferedSink = Buffer()
      outputStream = bufferedSink.outputStream()
    }
  }

  class ReflectiveMoshi(json: String) {

    private val moshi: Moshi = Moshi.Builder().build()
    val response: ResponseJ
    val adapter: JsonAdapter<ResponseJ>

    init {
      adapter = moshi.adapter(ResponseJ::class.java)
      response = adapter.fromJson(json)!!
    }
  }

  class ReflectiveGson(json: String) {

    private val gson: Gson = GsonBuilder().create()
    val response: Response
    val adapter: TypeAdapter<Response>

    init {
      adapter = gson.getAdapter(Response::class.java)
      response = adapter.fromJson(json)
    }
  }

  class ReflectiveGsonBuffer(private val json: String) {

    private val gson: Gson = GsonBuilder().create()
    lateinit var source: Reader
    lateinit var sink: Writer
    val response: Response
    val adapter: TypeAdapter<Response>

    init {
      adapter = gson.getAdapter(Response::class.java)
      response = adapter.fromJson(json)
    }

    fun setupIteration() {
      source = InputStreamReader(Buffer().write(json.toByteArray()).inputStream(), Charsets.UTF_8)
      sink = OutputStreamWriter(Buffer().outputStream(), Charsets.UTF_8)
    }
  }

  class AVMoshi(json: String) {

    private val moshi: Moshi = Moshi.Builder()
        .add(GeneratedJsonAdapterFactory.create())
        .build()
    val response: ResponseAV
    val adapter: JsonAdapter<ResponseAV>

    init {
      adapter = moshi.adapter(ResponseAV::class.java)
      response = adapter.fromJson(json)!!
    }
  }

  class ReflectiveMoshiKotlin(json: String) {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    val response: KRResponse
    val adapter: JsonAdapter<KRResponse>

    init {
      adapter = moshi.adapter(KRResponse::class.java)
      response = adapter.fromJson(json)!!
    }
  }

  class CodegenMoshiKotlin(json: String) {

    private val moshi: Moshi = Moshi.Builder()
        .build()
    val response: KCGResponse
    val adapter: JsonAdapter<KCGResponse>

    init {
      adapter = moshi.adapter(KCGResponse::class.java)
      response = adapter.fromJson(json)!!
    }
  }

  class AVMoshiBuffer(private val json: String) {

    private val moshi: Moshi = Moshi.Builder()
        .add(GeneratedJsonAdapterFactory.create())
        .build()
    lateinit var bufferedSource: BufferedSource
    lateinit var bufferedSink: BufferedSink
    val response: ResponseAV
    val adapter: JsonAdapter<ResponseAV>

    init {
      adapter = moshi.adapter(ResponseAV::class.java)
      response = adapter.fromJson(json)!!
    }

    fun setupIteration() {
      bufferedSource = Buffer().write(json.toByteArray())
      bufferedSink = Buffer()
    }
  }

  class ReflectiveMoshiKotlinBuffer(private val json: String) {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    lateinit var bufferedSource: BufferedSource
    lateinit var bufferedSink: BufferedSink
    val response: KRResponse
    val adapter: JsonAdapter<KRResponse>

    init {
      adapter = moshi.adapter(KRResponse::class.java)
      response = adapter.fromJson(json)!!
    }

    fun setupIteration() {
      bufferedSource = Buffer().write(json.toByteArray())
      bufferedSink = Buffer()
    }
  }

  class CodegenMoshiKotlinBuffer(private val json: String) {

    private val moshi: Moshi = Moshi.Builder()
        .build()
    lateinit var bufferedSource: BufferedSource
    lateinit var bufferedSink: BufferedSink
    val response: KCGResponse
    val adapter: JsonAdapter<KCGResponse>

    init {
      adapter = moshi.adapter(KCGResponse::class.java)
      response = adapter.fromJson(json)!!
    }

    fun setupIteration() {
      bufferedSource = Buffer().write(json.toByteArray())
      bufferedSink = Buffer()
    }
  }

  class AVGson(json: String) {

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapterFactory(GeneratedTypeAdapterFactory.create())
        .create()
    val response: ResponseAV
    val adapter: TypeAdapter<ResponseAV>

    init {
      adapter = gson.getAdapter(ResponseAV::class.java)
      response = adapter.fromJson(json)
    }
  }

  class AVGsonBuffer(private val json: String) {

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapterFactory(GeneratedTypeAdapterFactory.create())
        .create()
    lateinit var source: Reader
    lateinit var sink: Writer
    val response: ResponseAV
    val adapter: TypeAdapter<ResponseAV>

    init {
      adapter = gson.getAdapter(ResponseAV::class.java)
      response = adapter.fromJson(json)
    }

    fun setupIteration() {
      source = InputStreamReader(Buffer().write(json.toByteArray()).inputStream(), Charsets.UTF_8)
      sink = OutputStreamWriter(Buffer().outputStream(), Charsets.UTF_8)
    }
  }

  @get:Rule
  val benchmarkRule = BenchmarkRule()
  @Suppress("UnstableApiUsage")
  private val json = Resources.getResource(
      "largesample" + (if (minified) "_minified" else "") + ".json")
      .readText()

  @Test
  fun kserializer_string_toJson() = benchmarkRule.measureRepeated {
    val param = runWithTimingDisabled { KotlinxSerialization(json) }
    param.response.stringify(param.kSerializer)
  }

  @Test
  fun kserializer_buffer_toJson() = benchmarkRule.measureRepeated {
    val param = runWithTimingDisabled { KotlinxSerializationBuffer(json).also { it.setupIteration() } }
    param.response.encode(param.kSerializer, param.outputStream)
  }

  @Test
  fun kserializer_okiobuffer_toJson() = benchmarkRule.measureRepeated {
    val param = runWithTimingDisabled { KotlinxSerializationBuffer(json).also { it.setupIteration() } }
    param.response.encode(param.kSerializer, param.bufferedSink)
  }

  @Test
  fun moshi_reflective_string_toJson() = benchmarkRule.measureRepeated {
    val param = runWithTimingDisabled { ReflectiveMoshi(json) }
    param.adapter.toJson(param.response)
  }

  @Test
  fun moshi_autovalue_string_toJson() = benchmarkRule.measureRepeated {
    val param = runWithTimingDisabled { AVMoshi(json) }
    param.adapter.toJson(param.response)
  }

  @Test
  fun moshi_kotlin_reflective_string_toJson() = benchmarkRule.measureRepeated {
    val param = runWithTimingDisabled { ReflectiveMoshiKotlin(json) }
    param.adapter.toJson(param.response)
  }

  @Test
  fun moshi_kotlin_codegen_string_toJson() = benchmarkRule.measureRepeated {
    val param = runWithTimingDisabled { CodegenMoshiKotlin(json) }
    param.adapter.toJson(param.response)
  }

  @Test
  fun moshi_autovalue_buffer_toJson() = benchmarkRule.measureRepeated {
    val param = runWithTimingDisabled { AVMoshiBuffer(json).also { it.setupIteration() } }
    param.adapter.toJson(param.bufferedSink, param.response)
  }

  @Test
  fun moshi_kotlin_reflective_buffer_toJson() = benchmarkRule.measureRepeated {
    val param = runWithTimingDisabled {
      ReflectiveMoshiKotlinBuffer(json).also { it.setupIteration() }
    }
    param.adapter.toJson(param.bufferedSink, param.response)
  }

  @Test
  fun moshi_kotlin_codegen_buffer_toJson() = benchmarkRule.measureRepeated {
    val param = runWithTimingDisabled {
      CodegenMoshiKotlinBuffer(json).also { it.setupIteration() }
    }
    param.adapter.toJson(param.bufferedSink, param.response)
  }

  @Test
  fun gson_reflective_string_toJson() = benchmarkRule.measureRepeated {
    val param = runWithTimingDisabled { ReflectiveGson(json) }
    param.adapter.toJson(param.response)
  }

  @Test
  fun gson_reflective_buffer_toJson() = benchmarkRule.measureRepeated {
    val param = runWithTimingDisabled { ReflectiveGsonBuffer(json).also { it.setupIteration() } }
    param.adapter.toJson(param.sink, param.response)
  }

  @Test
  fun gson_autovalue_string_toJson() = benchmarkRule.measureRepeated {
    val param = runWithTimingDisabled { AVGson(json) }
    param.adapter.toJson(param.response)
  }

  @Test
  fun gson_autovalue_buffer_toJson() = benchmarkRule.measureRepeated {
    val param = runWithTimingDisabled { AVGsonBuffer(json).also { it.setupIteration() } }
    param.adapter.toJson(param.sink, param.response)
  }

  @Test
  fun kserializer_string_fromJson() = benchmarkRule.measureRepeated {
    val param = runWithTimingDisabled { KotlinxSerialization(json) }
    Response.parse(param.kSerializer, json)
  }

  @Test
  fun kserializer_buffer_fromJson() = benchmarkRule.measureRepeated {
    val param = runWithTimingDisabled { KotlinxSerializationBuffer(json).also { it.setupIteration() } }
    Response.parse(param.kSerializer, param.inputStream)
  }

  @Test
  fun kserializer_okiobuffer_fromJson() = benchmarkRule.measureRepeated {
    val param = runWithTimingDisabled { KotlinxSerializationBuffer(json).also { it.setupIteration() } }
    Response.parse(param.kSerializer, param.bufferedSource)
  }

  @Test
  fun moshi_reflective_string_fromJson() = benchmarkRule.measureRepeated {
    val param = runWithTimingDisabled { ReflectiveMoshi(json) }
    param.adapter.fromJson(json)
  }

  @Test
  fun moshi_autovalue_string_fromJson() = benchmarkRule.measureRepeated {
    val param = runWithTimingDisabled { AVMoshi(json) }
    param.adapter.fromJson(json)
  }

  @Test
  fun moshi_kotlin_reflective_string_fromJson() = benchmarkRule.measureRepeated {
    val param = runWithTimingDisabled { ReflectiveMoshiKotlin(json) }
    param.adapter.fromJson(json)
  }

  @Test
  fun moshi_kotlin_codegen_string_fromJson() = benchmarkRule.measureRepeated {
    val param = runWithTimingDisabled { CodegenMoshiKotlin(json) }
    param.adapter.fromJson(json)
  }

  @Test
  fun moshi_autovalue_buffer_fromJson() = benchmarkRule.measureRepeated {
    val param = runWithTimingDisabled { AVMoshiBuffer(json).also { it.setupIteration() } }
    param.adapter.fromJson(param.bufferedSource)
  }

  @Test
  fun moshi_kotlin_reflective_buffer_fromJson() = benchmarkRule.measureRepeated {
    val param = runWithTimingDisabled {
      ReflectiveMoshiKotlinBuffer(json).also { it.setupIteration() }
    }
    param.adapter.fromJson(param.bufferedSource)
  }

  @Test
  fun moshi_kotlin_codegen_buffer_fromJson() = benchmarkRule.measureRepeated {
    val param = runWithTimingDisabled {
      CodegenMoshiKotlinBuffer(json).also { it.setupIteration() }
    }
    param.adapter.fromJson(param.bufferedSource)
  }

  @Test
  fun gson_reflective_string_fromJson() = benchmarkRule.measureRepeated {
    val param = runWithTimingDisabled { ReflectiveGson(json) }
    param.adapter.fromJson(json)
  }

  @Test
  fun gson_reflective_buffer_fromJson() = benchmarkRule.measureRepeated {
    val param = runWithTimingDisabled { ReflectiveGsonBuffer(json).also { it.setupIteration() } }
    param.adapter.fromJson(param.source)
  }

  @Test
  fun gson_autovalue_string_fromJson() = benchmarkRule.measureRepeated {
    val param = runWithTimingDisabled { AVGson(json) }
    param.adapter.fromJson(json)
  }

  @Test
  fun gson_autovalue_buffer_fromJson() = benchmarkRule.measureRepeated {
    val param = runWithTimingDisabled { AVGsonBuffer(json).also { it.setupIteration() } }
    param.adapter.fromJson(param.source)
  }
}