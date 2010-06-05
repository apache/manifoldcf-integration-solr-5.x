package org.apache.lucene.search.regex;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.FilteredTermEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.util.ToStringUtils;

import java.io.IOException;

/** Implements the regular expression term search query.
 * The expressions supported depend on the regular expression implementation
 * used by way of the {@link RegexCapabilities} interface.
 *
 * @see RegexTermEnum
 */
public class RegexQuery extends MultiTermQuery implements RegexQueryCapable {
  private RegexCapabilities regexImpl = new JavaUtilRegexCapabilities();
  private Term term;

  /** Constructs a query for terms matching <code>term</code>. */
  public RegexQuery(Term term) {
    super(term.field());
    this.term = term;
  }
  
  public Term getTerm() { return term; }

  /**
   * Defines which {@link RegexCapabilities} implementation is used by this instance.
   *
   * @param impl
   */
  public void setRegexImplementation(RegexCapabilities impl) {
    this.regexImpl = impl;
  }

  /**
   * @return The implementation used by this instance.
   */
  public RegexCapabilities getRegexImplementation() {
    return regexImpl;
  }

  @Override
  protected FilteredTermEnum getEnum(IndexReader reader) throws IOException {
    return new RegexTermEnum(reader, term, regexImpl);
  }

  @Override
  public String toString(String field) {
    StringBuilder buffer = new StringBuilder();
    if (!term.field().equals(field)) {
      buffer.append(term.field());
      buffer.append(":");
    }
    buffer.append(term.text());
    buffer.append(ToStringUtils.boost(getBoost()));
    return buffer.toString();
  }

  /* generated by IntelliJ IDEA */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    final RegexQuery that = (RegexQuery) o;

    return regexImpl.equals(that.regexImpl);
  }

  /* generated by IntelliJ IDEA */
  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 29 * result + regexImpl.hashCode();
    return result;
  }
}
