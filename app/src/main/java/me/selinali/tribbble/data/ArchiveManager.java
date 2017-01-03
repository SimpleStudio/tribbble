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

package me.selinali.tribbble.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import me.selinali.tribbble.TribbbleApp;
import me.selinali.tribbble.model.Shot;

public class ArchiveManager {

  private static ArchiveManager sInstance;

  public static ArchiveManager instance() {
    return sInstance == null ? sInstance = new ArchiveManager() : sInstance;
  }

  private static final int PREFERENCE_VERSION = 1;
  private static final String ARCHIVED_PREF = "ARCHIVED" + PREFERENCE_VERSION;
  private static final String DISCARDED_PREF = "DISCARDED" + PREFERENCE_VERSION;

  private final SharedPreferences mArchivedPreferences;
  private final SharedPreferences mDiscardedPreferences;
  private final Gson mGson;

  private ArchiveManager() {
    mArchivedPreferences = TribbbleApp.context().getSharedPreferences(ARCHIVED_PREF, Context.MODE_PRIVATE);
    mDiscardedPreferences = TribbbleApp.context().getSharedPreferences(DISCARDED_PREF, Context.MODE_PRIVATE);
    mGson = new Gson();
  }

  @SuppressLint("CommitPrefEdits")
  public void archive(Shot shot) {
    mArchivedPreferences.edit()
        .putString(shot.getObjectId(), mGson.toJson(shot.withArchivedAt(new Date())))
        .commit();
  }

  public boolean isArchived(Shot shot) {
    return mArchivedPreferences.contains(shot.getObjectId());
  }

  public List<Shot> getArchivedShots() {
    List<Shot> shots = new ArrayList<>();
    for (Map.Entry<String, ?> entry : mArchivedPreferences.getAll().entrySet()) {
      shots.add(mGson.fromJson(String.valueOf(entry.getValue()), Shot.class));
    }

    Collections.sort(shots, (lhs, rhs) -> {
      if (lhs.getArchivedAt().equals(rhs.getArchivedAt())) return 0;
      else if (lhs.getArchivedAt().before(rhs.getArchivedAt())) return 1;
      else return -1;
    });

    return shots;
  }

  @SuppressLint("CommitPrefEdits")
  public void discard(Shot shot) {
    mDiscardedPreferences.edit()
        .putString(shot.getObjectId(), shot.getObjectId())
        .commit();
  }

  public boolean isDiscarded(Shot shot) {
    return mDiscardedPreferences.contains(shot.getObjectId());
  }

  @SuppressLint("CommitPrefEdits")
  public void unarchive(Shot shot) {
    mArchivedPreferences.edit()
        .remove(shot.getObjectId())
        .commit();
  }
}
