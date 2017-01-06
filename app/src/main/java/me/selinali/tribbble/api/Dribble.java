/*
 * Copyright 2016 Selina Li
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.selinali.tribbble.api;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import me.selinali.tribbble.BuildConfig;
import me.selinali.tribbble.model.Shot;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

public class Dribble {

  private static final int PAGE_LIMIT = 5;

  private static volatile Dribble sInstance;

  public static Dribble instance() {
    return sInstance == null ? sInstance = new Dribble() : sInstance;
  }

  private final Endpoints mEndpoints;
  private final Gson mGson = new GsonBuilder()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .create();

  private Dribble() {
    mEndpoints = new Retrofit.Builder()
        .baseUrl("https://api.leancloud.cn/1.1/classes/")
        .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
        .addConverterFactory(LeanCloudGsonConverterFactory.create(mGson))
        .client(new OkHttpClient.Builder().addInterceptor(chain ->
            chain.proceed(chain.request().newBuilder()
                .addHeader("X-LC-Id", BuildConfig.LEANCLOUD_ID)
                .addHeader("X-LC-Key", BuildConfig.LEANCLOUD_KEY)
                .build())
        ).build())
        .build()
        .create(Endpoints.class);
  }

  public Observable<List<Shot>> getShots(int page) {
    return mEndpoints.getShots(PAGE_LIMIT, PAGE_LIMIT * page);
  }

  /**
   * Continuously hits the shots endpoint, filtering each shot by the given function
   * and collecting until the predicate has been satisfied.
   */
  public Observable<List<Shot>> getShots(int page,
    /* Filters each shot           */    Func1<Shot, Boolean> f,
    /* Evaluates collected shots   */    Func1<List<Shot>, Boolean> p,
    /* Called when page increments */    Action1<Integer> onPageIncremented) {
    return getShots(page)
        .flatMapIterable(shots -> shots)
        .filter(f)
        .toList()
        .flatMap(shots -> {
          if (p.call(shots)) {
            return Observable.just(shots);
          } else {
            int incrementedPage = page + 1;
            onPageIncremented.call(incrementedPage);
            return Observable.just(shots)
                .concatWith(getShots(incrementedPage, f, p, onPageIncremented));
          }
        });
  }

  private interface Endpoints {
    @GET("Shots") Observable<List<Shot>> getShots(@Query("limit") int limit, @Query("skip") int skip);
  }
}
