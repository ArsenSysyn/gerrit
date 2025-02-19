/**
 * @license
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import {html} from '@polymer/polymer/lib/utils/html-tag';

export const htmlTemplate = html`
  <style include="gr-font-styles">
    /* Workaround for empty style block - see https://github.com/Polymer/tools/issues/408 */
  </style>
  <style include="shared-styles">
    /* Workaround for empty style block - see https://github.com/Polymer/tools/issues/408 */
  </style>
  <style include="gr-subpage-styles">
    gr-button,
    #inheritsFrom,
    #editInheritFromInput,
    .editing #inheritFromName,
    .weblinks,
    .editing .invisible {
      display: none;
    }
    #inheritsFrom.show {
      display: flex;
      min-height: 2em;
      align-items: center;
    }
    .weblink {
      margin-right: var(--spacing-xs);
    }
    gr-access-section {
      margin-top: var(--spacing-l);
    }
    .weblinks.show,
    .referenceContainer {
      display: block;
    }
    .rightsText {
      margin-right: var(--spacing-s);
    }

    .editing gr-button,
    .admin #editBtn {
      display: inline-block;
      margin: var(--spacing-l) 0;
    }
    .editing #editInheritFromInput {
      display: inline-block;
    }
  </style>
  <style include="gr-menu-page-styles">
    /* Workaround for empty style block - see https://github.com/Polymer/tools/issues/408 */
  </style>
  <div class$="main [[_computeMainClass(_ownerOf, _canUpload, _editing)]]">
    <div id="loading" class$="[[_computeLoadingClass(_loading)]]">
      Loading...
    </div>
    <div id="loadedContent" class$="[[_computeLoadingClass(_loading)]]">
      <h3
        id="inheritsFrom"
        class$="heading-3 [[_computeShowInherit(_inheritsFrom)]]"
      >
        <span class="rightsText">Rights Inherit From</span>
        <a
          href$="[[_computeParentHref(_inheritsFrom.name)]]"
          rel="noopener"
          id="inheritFromName"
        >
          [[_inheritsFrom.name]]</a
        >
        <gr-autocomplete
          id="editInheritFromInput"
          text="{{_inheritFromFilter}}"
          query="[[_query]]"
          on-commit="_handleUpdateInheritFrom"
          on-bind-value-changed="_handleUpdateInheritFrom"
        ></gr-autocomplete>
      </h3>
      <div class$="weblinks [[_computeWebLinkClass(_weblinks)]]">
        History:
        <template is="dom-repeat" items="[[_weblinks]]" as="link">
          <a
            href="[[link.url]]"
            class="weblink"
            rel="noopener"
            target="[[link.target]]"
          >
            [[link.name]]
          </a>
        </template>
      </div>
      <template
        is="dom-repeat"
        items="{{_sections}}"
        initial-count="5"
        target-framerate="60"
        as="section"
      >
        <gr-access-section
          capabilities="[[_capabilities]]"
          section="{{section}}"
          labels="[[_labels]]"
          can-upload="[[_canUpload]]"
          editing="[[_editing]]"
          owner-of="[[_ownerOf]]"
          groups="[[_groups]]"
          repo="[[repo]]"
          on-added-section-removed="_handleAddedSectionRemoved"
        ></gr-access-section>
      </template>
      <div class="referenceContainer">
        <gr-button id="addReferenceBtn" on-click="_handleCreateSection"
          >Add Reference</gr-button
        >
      </div>
      <div>
        <gr-button id="editBtn" on-click="_handleEdit"
          >[[_editOrCancel(_editing)]]</gr-button
        >
        <gr-button
          id="saveBtn"
          primary=""
          class$="[[_computeSaveBtnClass(_ownerOf)]]"
          on-click="_handleSave"
          disabled="[[!_modified]]"
          >Save</gr-button
        >
        <gr-button
          id="saveReviewBtn"
          primary=""
          class$="[[_computeSaveReviewBtnClass(_canUpload)]]"
          on-click="_handleSaveForReview"
          disabled="[[!_modified]]"
          >Save for review</gr-button
        >
      </div>
    </div>
  </div>
`;
