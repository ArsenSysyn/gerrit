/**
 * @license
 * Copyright (C) 2017 The Android Open Source Project
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

import '../../shared/gr-account-link/gr-account-link';
import {GerritNav} from '../../core/gr-navigation/gr-navigation';
import {
  GroupInfo,
  AccountInfo,
  EncodedGroupId,
  GroupAuditEventInfo,
  GroupAuditGroupEventInfo,
  isGroupAuditGroupEventInfo,
} from '../../../types/common';
import {firePageError, fireTitleChange} from '../../../utils/event-util';
import {getAppContext} from '../../../services/app-context';
import {ErrorCallback} from '../../../api/rest';
import {sharedStyles} from '../../../styles/shared-styles';
import {tableStyles} from '../../../styles/gr-table-styles';
import {LitElement, PropertyValues, css, html} from 'lit';
import {customElement, property, state} from 'lit/decorators';

declare global {
  interface HTMLElementTagNameMap {
    'gr-group-audit-log': GrGroupAuditLog;
  }
}

@customElement('gr-group-audit-log')
export class GrGroupAuditLog extends LitElement {
  @property({type: String})
  groupId?: EncodedGroupId;

  @state() private auditLog?: GroupAuditEventInfo[];

  @state() private loading = true;

  private readonly restApiService = getAppContext().restApiService;

  override connectedCallback() {
    super.connectedCallback();
    fireTitleChange(this, 'Audit Log');
  }

  static override get styles() {
    return [
      sharedStyles,
      tableStyles,
      css`
        /* GenericList style centers the last column, but we don't want that here. */
        .genericList tr th:last-of-type,
        .genericList tr td:last-of-type {
          text-align: left;
        }
      `,
    ];
  }

  override render() {
    return html`
      <table id="list" class="genericList">
        <tbody>
          <tr class="headerRow">
            <th class="date topHeader">Date</th>
            <th class="type topHeader">Type</th>
            <th class="member topHeader">Member</th>
            <th class="by-user topHeader">By User</th>
          </tr>
          ${this.renderLoading()}
        </tbody>
        ${this.renderAuditLogTable()}
      </table>
    `;
  }

  private renderLoading() {
    if (!this.loading) return;

    return html`
      <tr id="loading" class="loadingMsg loading">
        <td>Loading...</td>
      </tr>
    `;
  }

  private renderAuditLogTable() {
    if (this.loading) return;

    return html`
      <tbody>
        ${this.auditLog?.map(audit => this.renderAuditLog(audit))}
      </tbody>
    `;
  }

  private renderAuditLog(audit: GroupAuditEventInfo) {
    return html`
      <tr class="table">
        <td class="date">
          <gr-date-formatter withTooltip .dateStr=${audit.date}>
          </gr-date-formatter>
        </td>
        <td class="type">${this.itemType(audit.type)}</td>
        <td class="member">
          ${this.isGroupEvent(audit)
            ? html`<a href=${this.computeGroupUrl(audit.member)}
                >${this.getNameForGroup(audit.member)}</a
              >`
            : html`<gr-account-link .account=${audit.member}></gr-account-link
                >${this.getIdForUser(audit.member)}`}
        </td>
        <td class="by-user">
          <gr-account-link .account=${audit.user}></gr-account-link>
          ${this.getIdForUser(audit.user)}
        </td>
      </tr>
    `;
  }

  override willUpdate(changedProperties: PropertyValues) {
    if (changedProperties.has('groupId')) {
      this.getAuditLogs();
    }
  }

  // private but used in test
  getAuditLogs() {
    if (!this.groupId) return;

    const errFn: ErrorCallback = response => {
      firePageError(response);
    };

    this.loading = true;
    return this.restApiService
      .getGroupAuditLog(this.groupId, errFn)
      .then(auditLog => {
        this.auditLog = auditLog ?? [];
      })
      .finally(() => {
        this.loading = false;
      });
  }

  private itemType(type: string) {
    let item;
    switch (type) {
      case 'ADD_GROUP':
      case 'ADD_USER':
        item = 'Added';
        break;
      case 'REMOVE_GROUP':
      case 'REMOVE_USER':
        item = 'Removed';
        break;
      default:
        item = '';
    }
    return item;
  }

  // private but used in test
  isGroupEvent(event: GroupAuditEventInfo): event is GroupAuditGroupEventInfo {
    return isGroupAuditGroupEventInfo(event);
  }

  private computeGroupUrl(group: GroupInfo) {
    if (group && group.url && group.id) {
      return GerritNav.getUrlForGroup(group.id);
    }

    return '';
  }

  // private but used in test
  getIdForUser(account: AccountInfo) {
    return account._account_id ? ` (${account._account_id})` : '';
  }

  // private but used in test
  getNameForGroup(group: GroupInfo) {
    if (group && group.name) {
      return group.name;
    } else if (group && group.id) {
      // The URL encoded id of the member
      return decodeURIComponent(group.id);
    }

    return '';
  }
}
