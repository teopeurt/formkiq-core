/**
 * MIT License
 * 
 * Copyright (c) 2018 - 2020 FormKiQ
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.formkiq.stacks.api;

import com.formkiq.graalvm.annotations.Reflectable;
import com.formkiq.lambda.apigateway.ApiResponse;

/** API Upload URL Response. */
@Reflectable
public class ApiUrlResponse implements ApiResponse {

  /** {@link String} URL. */
  @Reflectable
  private String url;
  /** {@link String} DocumentId. */
  @Reflectable
  private String documentId;

  /** constructor. */
  public ApiUrlResponse() {}

  /**
   * constructor.
   *
   * @param u {@link String}
   * @param id {@link String}
   */
  public ApiUrlResponse(final String u, final String id) {
    this();
    this.url = u;
    this.documentId = id;
  }

  @Override
  public String getNext() {
    return null;
  }

  @Override
  public String getPrevious() {
    return null;
  }

  /**
   * Get Url.
   *
   * @return {@link String}
   */
  public String getUrl() {
    return this.url;
  }

  /**
   * Set Url.
   *
   * @param u {@link String}
   */
  public void setUrl(final String u) {
    this.url = u;
  }

  /**
   * Get Document Id.
   *
   * @return {@link String}
   */
  public String getDocumentId() {
    return this.documentId;
  }

  /**
   * Set DocumentId.
   *
   * @param id {@link String}
   */
  public void setDocumentId(final String id) {
    this.documentId = id;
  }
}
