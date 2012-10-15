/*******************************************************************************
 * Copyright 2012 Crazywater
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
 ******************************************************************************/
package de.knufficast.search;

import java.util.List;

import de.knufficast.util.BooleanCallback;

/**
 * An interface for podcast searches.
 * 
 * @author crazywater
 * 
 */
public interface PodcastSearch {
  /**
   * A result of a podcast search.
   * 
   * @author crazywater
   * 
   */
  public interface Result {
    public String getTitle();
    public String getDescription();
    public String getFeedUrl();
    public String getImgUrl();
    public String getWebsite();
  };

  /**
   * Execute a search for podcasts.
   */
  public void search(final String query,
      final BooleanCallback<List<Result>, String> callback);
}
