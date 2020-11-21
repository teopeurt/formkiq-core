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
package com.formkiq.stacks.lambda.s3;

import com.formkiq.graalvm.annotations.Reflectable;

/**
 * Response to a Document Create Event.
 *
 */
@Reflectable
public class DocumentEvent {

  /** Document SiteId. */
  private String siteId;
  /** Document Id. */
  private String documentId;
  /** S3 Key. */
  private String s3key;
  /** S3 Bucket. */
  private String s3bucket;
  /** Document Type. */
  private String type;

  /**
   * constructor.
   */
  public DocumentEvent() {

  }

  /**
   * Get Document Id.
   * 
   * @return {@link String}
   */
  public String documentId() {
    return this.documentId;
  }

  /**
   * Set Document Id.
   * 
   * @param id {@link String}
   * @return {@link DocumentEvent}
   */
  public DocumentEvent documentId(final String id) {
    this.documentId = id;
    return this;
  }

  /**
   * Get S3 Bucket.
   * 
   * @return {@link String}
   */
  public String s3bucket() {
    return this.s3bucket;
  }

  /**
   * Set S3 Bucket.
   * 
   * @param bucket {@link String}
   * @return {@link DocumentEvent}
   */
  public DocumentEvent s3bucket(final String bucket) {
    this.s3bucket = bucket;
    return this;
  }

  /**
   * Get S3 Key.
   * 
   * @return {@link String}
   */
  public String s3key() {
    return this.s3key;
  }

  /**
   * Set S3 Key.
   * 
   * @param key {@link String}
   * @return {@link DocumentEvent}
   */
  public DocumentEvent s3key(final String key) {
    this.s3key = key;
    return this;
  }

  /**
   * Get Site Id.
   * 
   * @return {@link String}
   */
  public String siteId() {
    return this.siteId;
  }

  /**
   * Set Site Id.
   * 
   * @param id {@link String}
   * @return {@link DocumentEvent}
   */
  public DocumentEvent siteId(final String id) {
    this.siteId = id;
    return this;
  }

  /**
   * Get {@link DocumentEvent} type.
   * 
   * @return {@link String}
   */
  public String type() {
    return this.type;
  }

  /**
   * Set {@link DocumentEvent} type.
   * 
   * @param eventtype {@link String}
   */
  public void type(final String eventtype) {
    this.type = eventtype;
  }
}