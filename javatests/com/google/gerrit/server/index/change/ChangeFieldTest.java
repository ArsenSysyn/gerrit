// Copyright (C) 2016 The Android Open Source Project
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

package com.google.gerrit.server.index.change;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import com.google.gerrit.entities.Account;
import com.google.gerrit.entities.Change;
import com.google.gerrit.entities.LegacySubmitRequirement;
import com.google.gerrit.entities.Project;
import com.google.gerrit.entities.SubmitRecord;
import com.google.gerrit.index.testing.FakeStoredValue;
import com.google.gerrit.server.ReviewerSet;
import com.google.gerrit.server.notedb.ReviewerStateInternal;
import com.google.gerrit.server.query.change.ChangeData;
import com.google.gerrit.server.util.time.TimeUtil;
import com.google.gerrit.testing.TestTimeUtil;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ChangeFieldTest {
  @Before
  public void setUp() {
    TestTimeUtil.resetWithClockStep(1, TimeUnit.SECONDS);
  }

  @After
  public void tearDown() {
    TestTimeUtil.useSystemTime();
  }

  @Test
  public void reviewerFieldValues() {
    Table<ReviewerStateInternal, Account.Id, Instant> t = HashBasedTable.create();

    // Timestamps are stored as epoch millis in the reviewer field. Epoch millis are less precise
    // than Instants which have nanosecond precision. Create Instants with millisecond precision
    // here so that the comparison for the assertions works.
    Instant t1 = Instant.ofEpochMilli(TimeUtil.nowMs());
    Instant t2 = Instant.ofEpochMilli(TimeUtil.nowMs());

    t.put(ReviewerStateInternal.REVIEWER, Account.id(1), t1);
    t.put(ReviewerStateInternal.CC, Account.id(2), t2);
    ReviewerSet reviewers = ReviewerSet.fromTable(t);

    List<String> values = ChangeField.getReviewerFieldValues(reviewers);
    assertThat(values)
        .containsExactly(
            "REVIEWER,1", "REVIEWER,1," + t1.toEpochMilli(), "CC,2", "CC,2," + t2.toEpochMilli());

    assertThat(ChangeField.parseReviewerFieldValues(Change.id(1), values)).isEqualTo(reviewers);
  }

  @Test
  public void formatSubmitRecordValues() {
    assertThat(
            ChangeField.formatSubmitRecordValues(
                ImmutableList.of(
                    record(
                        SubmitRecord.Status.OK,
                        label(SubmitRecord.Label.Status.MAY, "Label-1", null),
                        label(SubmitRecord.Label.Status.OK, "Label-2", 1))),
                Account.id(1)))
        .containsExactly("OK", "MAY,label-1", "OK,label-2", "OK,label-2,0", "OK,label-2,1");
  }

  @Test
  public void storedSubmitRecords() {
    assertStoredRecordRoundTrip(record(SubmitRecord.Status.CLOSED));

    SubmitRecord r =
        record(
            SubmitRecord.Status.OK,
            label(SubmitRecord.Label.Status.MAY, "Label-1", null),
            label(SubmitRecord.Label.Status.OK, "Label-2", 1));

    assertStoredRecordRoundTrip(r);
  }

  @Test
  public void storedSubmitRecordsWithRequirement() {
    SubmitRecord r =
        record(
            SubmitRecord.Status.OK,
            label(SubmitRecord.Label.Status.MAY, "Label-1", null),
            label(SubmitRecord.Label.Status.OK, "Label-2", 1));

    LegacySubmitRequirement sr =
        LegacySubmitRequirement.builder()
            .setType("short_type")
            .setFallbackText("Fallback text may contain special symbols like < > \\ / ; :")
            .build();
    r.requirements = Collections.singletonList(sr);

    assertStoredRecordRoundTrip(r);
  }

  @Test
  public void storedSubmitRequirementWithoutCustomData() {
    SubmitRecord r =
        record(
            SubmitRecord.Status.OK,
            label(SubmitRecord.Label.Status.MAY, "Label-1", null),
            label(SubmitRecord.Label.Status.OK, "Label-2", 1));

    // Doesn't have any custom data value
    LegacySubmitRequirement sr =
        LegacySubmitRequirement.builder()
            .setFallbackText("short_type")
            .setType("ci_status")
            .build();
    r.requirements = Collections.singletonList(sr);

    assertStoredRecordRoundTrip(r);
  }

  @Test
  public void tolerateNullValuesForInsertion() {
    Project.NameKey project = Project.nameKey("project");
    ChangeData cd = ChangeData.createForTest(project, Change.id(1), 1, ObjectId.zeroId());
    assertThat(ChangeField.ADDED.setIfPossible(cd, new FakeStoredValue(null))).isTrue();
  }

  @Test
  public void tolerateNullValuesForDeletion() {
    Project.NameKey project = Project.nameKey("project");
    ChangeData cd = ChangeData.createForTest(project, Change.id(1), 1, ObjectId.zeroId());
    assertThat(ChangeField.DELETED.setIfPossible(cd, new FakeStoredValue(null))).isTrue();
  }

  private static SubmitRecord record(SubmitRecord.Status status, SubmitRecord.Label... labels) {
    SubmitRecord r = new SubmitRecord();
    r.status = status;
    if (labels.length > 0) {
      r.labels = ImmutableList.copyOf(labels);
    }
    return r;
  }

  private static SubmitRecord.Label label(
      SubmitRecord.Label.Status status, String label, Integer appliedBy) {
    SubmitRecord.Label l = new SubmitRecord.Label();
    l.status = status;
    l.label = label;
    if (appliedBy != null) {
      l.appliedBy = Account.id(appliedBy);
    }
    return l;
  }

  private static void assertStoredRecordRoundTrip(SubmitRecord... records) {
    List<SubmitRecord> recordList = ImmutableList.copyOf(records);
    List<String> stored =
        ChangeField.storedSubmitRecords(recordList).stream()
            .map(s -> new String(s, UTF_8))
            .collect(toList());
    assertWithMessage("JSON %s" + stored)
        .that(ChangeField.parseSubmitRecords(stored))
        .isEqualTo(recordList);
  }
}
