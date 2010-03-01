// Copyright (C) 2010 The Android Open Source Project
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

package com.google.gerrit.prettify.common;

import com.google.gwtexpui.safehtml.client.SafeHtml;
import com.google.gwtexpui.safehtml.client.SafeHtmlBuilder;

import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.ReplaceEdit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class PrettyFormatter implements SparseHtmlFile {
  public static abstract class EditFilter {
    abstract String getStyleName();

    abstract int getBegin(Edit edit);

    abstract int getEnd(Edit edit);
  }

  public static final EditFilter A = new EditFilter() {
    @Override
    String getStyleName() {
      return "wdd";
    }

    @Override
    int getBegin(Edit edit) {
      return edit.getBeginA();
    }

    @Override
    int getEnd(Edit edit) {
      return edit.getEndA();
    }
  };

  public static final EditFilter B = new EditFilter() {
    @Override
    String getStyleName() {
      return "wdi";
    }

    @Override
    int getBegin(Edit edit) {
      return edit.getBeginB();
    }

    @Override
    int getEnd(Edit edit) {
      return edit.getEndB();
    }
  };

  protected SparseFileContent content;
  protected EditFilter side;
  protected List<Edit> edits;
  protected PrettySettings settings;
  protected Set<Integer> trailingEdits;

  private int col;
  private int lineIdx;
  private Tag lastTag;
  private StringBuilder buf;

  public SafeHtml getSafeHtmlLine(int lineNo) {
    return SafeHtml.asis(content.get(lineNo));
  }

  public int size() {
    return content.size();
  }

  @Override
  public boolean contains(int idx) {
    return content.contains(idx);
  }

  @Override
  public boolean hasTrailingEdit(int idx) {
    return trailingEdits.contains(idx);
  }

  public void setEditFilter(EditFilter f) {
    side = f;
  }

  public void setEditList(List<Edit> all) {
    edits = all;
  }

  public void setPrettySettings(PrettySettings how) {
    settings = how;
  }

  /**
   * Parse and format a complete source code file.
   *
   * @param src raw content of the file to format. The line strings will be HTML
   *        escaped before processing, so it must be the raw text.
   */
  public void format(SparseFileContent src) {
    content = new SparseFileContent();
    content.setSize(src.size());
    trailingEdits = new HashSet<Integer>();

    String html = toHTML(src);

    if (settings.isSyntaxHighlighting() && getFileType() != null
        && src.isWholeFile()) {
      // The prettify parsers don't like &#39; as an entity for the
      // single quote character. Replace them all out so we don't
      // confuse the parser.
      //
      html = html.replaceAll("&#39;", "'");
      html = prettify(html, getFileType());

    } else {
      html = expandTabs(html);
      html = html.replaceAll("\n", "<br />");
    }

    int pos = 0;
    int textChunkStart = 0;

    lastTag = Tag.NULL;
    col = 0;
    lineIdx = 0;

    buf = new StringBuilder();
    while (pos <= html.length()) {
      int tagStart = html.indexOf('<', pos);

      if (tagStart < 0) {
        // No more tags remaining. What's left is plain text.
        //
        assert lastTag == Tag.NULL;
        pos = html.length();
        if (textChunkStart < pos) {
          htmlText(html.substring(textChunkStart, pos));
        }
        if (0 < buf.length()) {
          content.addLine(src.mapIndexToLine(lineIdx), buf.toString());
        }
        break;
      }

      // Assume no attribute contains '>' and that all tags
      // within the HTML will be well-formed.
      //
      int tagEnd = html.indexOf('>', tagStart);
      assert tagStart < tagEnd;
      pos = tagEnd + 1;

      // Handle any text between the end of the last tag,
      // and the start of this tag.
      //
      if (textChunkStart < tagStart) {
        lastTag.open(buf, html);
        htmlText(html.substring(textChunkStart, tagStart));
      }
      textChunkStart = pos;

      if (isBR(html, tagStart, tagEnd)) {
        lastTag.close(buf, html);
        content.addLine(src.mapIndexToLine(lineIdx), buf.toString());
        buf = new StringBuilder();
        col = 0;
        lineIdx++;

      } else if (html.charAt(tagStart + 1) == '/') {
        lastTag = lastTag.pop(buf, html);

      } else if (html.charAt(tagEnd - 1) != '/') {
        lastTag = new Tag(lastTag, tagStart, tagEnd);
      }
    }
    buf = null;
  }

  private void htmlText(String txt) {
    int pos = 0;
    while (pos < txt.length()) {
      int start = txt.indexOf('&', pos);
      if (start < 0) {
        break;
      }

      cleanText(txt, pos, start);
      pos = txt.indexOf(';', start + 1) + 1;

      if (settings.getLineLength() <= col) {
        buf.append("<br />");
        col = 0;
      }

      buf.append(txt.substring(start, pos));
      col++;
    }

    cleanText(txt, pos, txt.length());
  }

  private void cleanText(String txt, int pos, int end) {
    while (pos < end) {
      int free = settings.getLineLength() - col;
      if (free <= 0) {
        // The current line is full. Throw an explicit line break
        // onto the end, and we'll continue on the next line.
        //
        buf.append("<br />");
        col = 0;
        free = settings.getLineLength();
      }

      int n = Math.min(end - pos, free);
      buf.append(txt.substring(pos, pos + n));
      col += n;
      pos += n;
    }
  }

  /** Run the prettify engine over the text and return the result. */
  protected abstract String prettify(String html, String type);

  private static boolean isBR(String html, int tagStart, int tagEnd) {
    return tagEnd - tagStart == 5 //
        && html.charAt(tagStart + 1) == 'b' //
        && html.charAt(tagStart + 2) == 'r' //
        && html.charAt(tagStart + 3) == ' ';
  }

  private static class Tag {
    static final Tag NULL = new Tag(null, 0, 0) {
      @Override
      void open(StringBuilder buf, String html) {
      }

      @Override
      void close(StringBuilder buf, String html) {
      }

      @Override
      Tag pop(StringBuilder buf, String html) {
        return this;
      }
    };

    final Tag parent;
    final int start;
    final int end;
    boolean open;

    Tag(Tag p, int s, int e) {
      parent = p;
      start = s;
      end = e;
    }

    void open(StringBuilder buf, String html) {
      if (!open) {
        parent.open(buf, html);
        buf.append(html.substring(start, end + 1));
        open = true;
      }
    }

    void close(StringBuilder buf, String html) {
      pop(buf, html);
      parent.close(buf, html);
    }

    Tag pop(StringBuilder buf, String html) {
      if (open) {
        int sp = html.indexOf(' ', start + 1);
        if (sp < 0 || end < sp) {
          sp = end;
        }

        buf.append("</");
        buf.append(html.substring(start + 1, sp));
        buf.append('>');
        open = false;
      }
      return parent;
    }
  }

  private String toHTML(SparseFileContent src) {
    SafeHtml html;

    if (settings.isIntralineDifference()) {
      html = colorLineEdits(src);
    } else {
      SafeHtmlBuilder b = new SafeHtmlBuilder();
      for (int index = src.first(); index < src.size(); index = src.next(index)) {
        b.append(src.get(index));
        b.append('\n');
      }
      html = b;
    }

    if (settings.isShowWhiteSpaceErrors()) {
      // We need to do whitespace errors before showing tabs, because
      // these patterns rely on \t as a literal, before it expands.
      //
      html = showTabAfterSpace(html);
      html = showTrailingWhitespace(html);
    }

    if (settings.isShowTabs()) {
      String t = 1 < settings.getTabSize() ? "\t" : "";
      html = html.replaceAll("\t", "<span class=\"vt\">&#187;</span>" + t);
    }

    return html.asString();
  }

  private SafeHtml colorLineEdits(SparseFileContent src) {
    // Make a copy of the edits with a sentinel that is after all lines
    // in the source. That simplifies our loop below because we'll never
    // run off the end of the edit list.
    //
    List<Edit> edits = new ArrayList<Edit>(this.edits.size() + 1);
    edits.addAll(this.edits);
    edits.add(new Edit(src.size(), src.size()));

    SafeHtmlBuilder buf = new SafeHtmlBuilder();

    int curIdx = 0;
    Edit curEdit = edits.get(curIdx);

    ReplaceEdit lastReplace = null;
    List<Edit> charEdits = null;
    int lastPos = 0;
    int lastIdx = 0;

    for (int index = src.first(); index < src.size(); index = src.next(index)) {
      int cmp = compare(index, curEdit);
      while (0 < cmp) {
        // The index is after the edit. Skip to the next edit.
        //
        curEdit = edits.get(curIdx++);
        cmp = compare(index, curEdit);
      }

      if (cmp < 0) {
        // index occurs before the edit. This is a line of context.
        //
        buf.append(src.get(index));
        buf.append('\n');
        continue;
      }

      // index occurs within the edit. The line is a modification.
      //
      if (curEdit instanceof ReplaceEdit) {
        if (lastReplace != curEdit) {
          lastReplace = (ReplaceEdit) curEdit;
          charEdits = lastReplace.getInternalEdits();
          lastPos = 0;
          lastIdx = 0;
        }

        final String line = src.get(index) + "\n";
        for (int c = 0; c < line.length();) {
          if (charEdits.size() <= lastIdx) {
            buf.append(line.substring(c));
            break;
          }

          final Edit edit = charEdits.get(lastIdx);
          final int b = side.getBegin(edit) - lastPos;
          final int e = side.getEnd(edit) - lastPos;

          if (c < b) {
            // There is text at the start of this line that is common
            // with the other side. Copy it with no style around it.
            //
            final int cmnLen = Math.min(b, line.length());
            buf.openSpan();
            buf.setStyleName("wdc");
            buf.append(line.substring(c, cmnLen));
            buf.closeSpan();
            c = cmnLen;
          }

          final int modLen = Math.min(e, line.length());
          if (c < e && c < modLen) {
            buf.openSpan();
            buf.setStyleName(side.getStyleName());
            buf.append(line.substring(c, modLen));
            buf.closeSpan();
            if (modLen == line.length()) {
              trailingEdits.add(index);
            }
            c = modLen;
          }

          if (e <= c) {
            lastIdx++;
          }
        }
        lastPos += line.length();

      } else {
        buf.append(src.get(index));
        buf.append('\n');
      }
    }
    return buf;
  }

  private int compare(int index, Edit edit) {
    if (index < side.getBegin(edit)) {
      return -1; // index occurs before the edit.

    } else if (index < side.getEnd(edit)) {
      return 0; // index occurs within the edit.

    } else {
      return 1; // index occurs after the edit.
    }
  }

  private SafeHtml showTabAfterSpace(SafeHtml src) {
    final String m = "( ( |<span[^>]*>|</span>)*\t)";
    final String r = "<span class=\"wse\">$1</span>";
    src = src.replaceFirst("^" + m, r);
    src = src.replaceAll("\n" + m, "\n" + r);
    return src;
  }

  private SafeHtml showTrailingWhitespace(SafeHtml src) {
    final String r = "<span class=\"wse\">$1</span>$2";
    src = src.replaceAll("([ \t][ \t]*)(\r?(</span>)?\n)", r);
    src = src.replaceFirst("([ \t][ \t]*)(\r?(</span>)?\n?)$", r);
    return src;
  }

  private String expandTabs(String html) {
    StringBuilder tmp = new StringBuilder();
    int i = 0;
    if (settings.isShowTabs()) {
      i = 1;
    }
    for (; i < settings.getTabSize(); i++) {
      tmp.append("&nbsp;");
    }
    return html.replaceAll("\t", tmp.toString());
  }

  private String getFileType() {
    String srcType = settings.getFilename();
    if (srcType == null) {
      return null;
    }

    int dot = srcType.lastIndexOf('.');
    if (dot < 0) {
      return null;
    }

    if (0 < dot) {
      srcType = srcType.substring(dot + 1);
    }

    if ("txt".equalsIgnoreCase(srcType)) {
      return null;
    }

    return srcType;
  }
}
