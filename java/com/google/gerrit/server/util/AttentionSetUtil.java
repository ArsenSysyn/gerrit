// Copyright (C) 2020 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.gerrit.server.util;

import com.google.common.collect.ImmutableSet;
import com.google.gerrit.entities.AttentionSetUpdate;
import com.google.gerrit.entities.AttentionSetUpdate.Operation;
import java.util.Collection;

/** Common helpers for dealing with attention set data structures. */
public class AttentionSetUtil {
  /** Returns only updates where the user was added. */
  public static ImmutableSet<AttentionSetUpdate> additionsOnly(
      Collection<AttentionSetUpdate> updates) {
    return updates.stream()
        .filter(u -> u.operation() == Operation.ADD)
        .collect(ImmutableSet.toImmutableSet());
  }

  private AttentionSetUtil() {}
}
