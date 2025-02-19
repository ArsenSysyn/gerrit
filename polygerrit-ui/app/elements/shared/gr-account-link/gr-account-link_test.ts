/**
 * @license
 * Copyright (C) 2015 The Android Open Source Project
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

import '../../../test/common-test-setup-karma';
import {fixture} from '@open-wc/testing-helpers';
import {html} from 'lit';
import './gr-account-link';
import {GerritNav} from '../../core/gr-navigation/gr-navigation';
import {GrAccountLink} from './gr-account-link';
import {
  createAccountWithId,
  createAccountWithIdNameAndEmail,
} from '../../../test/test-data-generators';
import {AccountId, AccountInfo, EmailAddress} from '../../../types/common';

suite('gr-account-link tests', () => {
  let element: GrAccountLink;

  setup(async () => {
    const account = createAccountWithIdNameAndEmail();
    element = await fixture<GrAccountLink>(
      html`<gr-account-link .account=${account}></gr-account-link>`
    );
  });

  test('renders', () => {
    expect(element).shadowDom.to.equal(`<span>
      <a href="">
        <gr-account-label
          deselected=""
          exportparts="gr-account-label-text: gr-account-link-text"
        >
        </gr-account-label>
      </a>
    </span>
  `);
  });

  test('computed fields', () => {
    const url = 'test/url';
    const urlStub = sinon.stub(GerritNav, 'getUrlForOwner').returns(url);
    const account: AccountInfo = {
      ...createAccountWithId(),
      email: 'email' as EmailAddress,
      username: 'username',
      name: 'name',
      _account_id: 5 as AccountId,
    };
    assert.isNotOk(element._computeOwnerLink());
    assert.equal(element._computeOwnerLink(account), url);
    assert.isTrue(urlStub.lastCall.calledWithExactly('email'));

    delete account.email;
    assert.equal(element._computeOwnerLink(account), url);
    assert.isTrue(urlStub.lastCall.calledWithExactly('username'));

    delete account.username;
    assert.equal(element._computeOwnerLink(account), url);
    assert.isTrue(urlStub.lastCall.calledWithExactly('name'));

    delete account.name;
    assert.equal(element._computeOwnerLink(account), url);
    assert.isTrue(urlStub.lastCall.calledWithExactly('5'));
  });
});
