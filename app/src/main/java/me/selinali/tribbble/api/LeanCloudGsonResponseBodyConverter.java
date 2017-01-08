/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.selinali.tribbble.api;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import okhttp3.ResponseBody;
import retrofit2.Converter;

final class LeanCloudGsonResponseBodyConverter<T> implements Converter<ResponseBody, T> {
  private final Gson gson;
  private final TypeAdapter<T> adapter;

  LeanCloudGsonResponseBodyConverter(Gson gson, TypeAdapter<T> adapter) {
    this.gson = gson;
    this.adapter = adapter;
  }

  @Override public T convert(ResponseBody value) throws IOException {
    // region EDIT 从原始数据中取出key为results的JSONArray，作为响应数据的返回
    final int bufferSize = 1024;
    final char[] buffer = new char[bufferSize];
    final StringBuilder out = new StringBuilder();
    Reader in = value.charStream();
    for ( ; ; ) {
      int rsz = in.read(buffer, 0, buffer.length);
      if (rsz < 0)
        break;
      out.append(buffer, 0, rsz);
    }
    String json = out.toString();
    json = json.substring("{\"results\":".length(), json.length() - 1);
    json = json.replaceAll("objectId", "object_id");
    json = json.replaceAll("createdAt", "created_at");
    // 将所有create_at这个jsonObject替换为仅日期字符串
    while(true) {
      String createAtKey = "Date\",\"iso\":";
      int createAtIndex = json.indexOf(createAtKey);
      if (createAtIndex == -1) {
        break;
      }
      String createAtReg = "\\{\"__type\":\"Date\",\"iso\":(.*?)\\}";
      int dateStart = json.indexOf(createAtKey) + createAtKey.length();
      int dateEnd = json.indexOf("\"}", dateStart);
      String dateStr = json.substring(dateStart, dateEnd + 1);
      json = json.replaceFirst(createAtReg, dateStr);
    }
    // endregion

    JsonReader jsonReader = gson.newJsonReader(new StringReader(json));
    try {
      return adapter.read(jsonReader);
    } finally {
      value.close();
    }
  }
}
