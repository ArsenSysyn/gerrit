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
    h1 {
      margin-bottom: var(--spacing-m);
    }
    h3 {
      margin-bottom: var(--spacing-m);
    }
    .agreementsUrl {
      border: 1px solid var(--border-color);
      margin-bottom: var(--spacing-xl);
      margin-left: var(--spacing-xl);
      margin-right: var(--spacing-xl);
      padding: var(--spacing-s);
    }
    #claNewAgreementsLabel {
      font-weight: var(--font-weight-bold);
    }
    #claNewAgreement {
      display: none;
    }
    #claNewAgreement.show {
      display: block;
    }
    .contributorAgreementButton {
      font-weight: var(--font-weight-bold);
    }
    .alreadySubmittedText {
      color: var(--error-text-color);
      margin: 0 var(--spacing-xxl);
      padding: var(--spacing-m);
    }
    .alreadySubmittedText.hide,
    .hideAgreementsTextBox {
      display: none;
    }
    main {
      margin: var(--spacing-xxl) auto;
      max-width: 50em;
    }
  </style>
  <style include="gr-form-styles">
    /* Workaround for empty style block - see https://github.com/Polymer/tools/issues/408 */
  </style>
  <main>
    <h1 class="heading-1">New Contributor Agreement</h1>
    <h3 class="heading-3">Select an agreement type:</h3>
    <template is="dom-repeat" items="[[_computeAgreements(_serverConfig)]]">
      <span class="contributorAgreementButton">
        <input
          id$="claNewAgreementsInput[[item.name]]"
          name="claNewAgreementsRadio"
          type="radio"
          data-name$="[[item.name]]"
          data-url$="[[item.url]]"
          on-click="_handleShowAgreement"
          disabled$="[[_disableAgreements(item, _groups, _signedAgreements)]]"
        />
        <label id="claNewAgreementsLabel">[[item.name]]</label>
      </span>
      <div
        class$="alreadySubmittedText [[_hideAgreements(item, _groups, _signedAgreements)]]"
      >
        Agreement already submitted.
      </div>
      <div class="agreementsUrl">[[item.description]]</div>
    </template>
    <div
      id="claNewAgreement"
      class$="[[_computeShowAgreementsClass(_showAgreements)]]"
    >
      <h3 class="heading-3">Review the agreement:</h3>
      <div id="agreementsUrl" class="agreementsUrl">
        <a href$="[[_agreementsUrl]]" target="blank" rel="noopener">
          Please review the agreement.</a
        >
      </div>
      <div
        class$="agreementsTextBox [[_computeHideAgreementClass(_agreementName, _serverConfig.auth.contributor_agreements)]]"
      >
        <h3 class="heading-3">Complete the agreement:</h3>
        <iron-input
          bind-value="{{_agreementsText}}"
          placeholder="Enter 'I agree' here"
        >
          <input id="input-agreements" placeholder="Enter 'I agree' here" />
        </iron-input>
        <gr-button
          on-click="_handleSaveAgreements"
          disabled="[[_disableAgreementsText(_agreementsText)]]"
        >
          Submit
        </gr-button>
      </div>
    </div>
  </main>
`;
