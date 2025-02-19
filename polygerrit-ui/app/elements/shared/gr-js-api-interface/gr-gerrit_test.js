/**
 * @license
 * Copyright (C) 2019 The Android Open Source Project
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

import '../../../test/common-test-setup-karma.js';
import {getPluginLoader} from './gr-plugin-loader.js';
import {resetPlugins} from '../../../test/test-utils.js';
import {_testOnly_getGerritInternalPluginApi} from './gr-gerrit.js';
import {stubRestApi} from '../../../test/test-utils.js';
import {getAppContext} from '../../../services/app-context.js';

suite('gr-gerrit tests', () => {
  let element;
  let clock;
  let pluginApi;

  setup(() => {
    clock = sinon.useFakeTimers();

    stubRestApi('getAccount').returns(Promise.resolve({name: 'Judy Hopps'}));
    stubRestApi('send').returns(Promise.resolve({status: 200}));
    element = getAppContext().jsApiService;
    pluginApi = _testOnly_getGerritInternalPluginApi();
  });

  teardown(() => {
    clock.restore();
    element._removeEventCallbacks();
    resetPlugins();
  });

  suite('proxy methods', () => {
    test('Gerrit._isPluginEnabled proxy to getPluginLoader()', () => {
      const stubFn = sinon.stub();
      sinon.stub(
          getPluginLoader(),
          'isPluginEnabled')
          .callsFake((...args) => stubFn(...args)
          );
      pluginApi._isPluginEnabled('test_plugin');
      assert.isTrue(stubFn.calledWith('test_plugin'));
    });

    test('Gerrit._isPluginLoaded proxy to getPluginLoader()', () => {
      const stubFn = sinon.stub();
      sinon.stub(
          getPluginLoader(),
          'isPluginLoaded')
          .callsFake((...args) => stubFn(...args));
      pluginApi._isPluginLoaded('test_plugin');
      assert.isTrue(stubFn.calledWith('test_plugin'));
    });
  });
});

