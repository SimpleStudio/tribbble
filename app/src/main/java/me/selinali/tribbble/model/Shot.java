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

package me.selinali.tribbble.model;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Wither;

@Parcel
@EqualsAndHashCode
@AllArgsConstructor
public class Shot {
  @Getter int id;
  @Getter String objectId;
  @Getter String title;
  @Getter String description;
  @Getter Images images;
  @Getter int viewsCount;
  @Getter int likesCount;
  @Getter Date createdAt;
  @Getter boolean animated;
  @Wither @Getter Date archivedAt;

  @ParcelConstructor Shot() {}

  @Parcel
  public static class Images {
    @Getter String hidpi;
    @Getter String teaser;
    @Getter String normal;

    public String getHighResImage() {
      return hidpi == null? normal : hidpi;
    }
  }
}