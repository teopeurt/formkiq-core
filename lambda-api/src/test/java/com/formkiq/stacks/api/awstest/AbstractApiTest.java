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
package com.formkiq.stacks.api.awstest;

import static com.formkiq.stacks.dynamodb.SiteIdKeyGenerator.DEFAULT_SITE_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.BeforeClass;
import com.formkiq.aws.cognito.CognitoConnectionBuilder;
import com.formkiq.aws.cognito.CognitoService;
import com.formkiq.aws.iam.IamConnectionBuilder;
import com.formkiq.aws.iam.IamService;
import com.formkiq.aws.ssm.SsmConnectionBuilder;
import com.formkiq.aws.ssm.SsmService;
import com.formkiq.aws.ssm.SsmServiceImpl;
import com.formkiq.aws.sts.Aws4SignerParamsBuilder;
import com.formkiq.aws.sts.StsConnectionBuilder;
import com.formkiq.aws.sts.StsService;
import com.formkiq.lambda.apigateway.util.GsonUtil;
import com.formkiq.stacks.client.FormKiqClient;
import com.formkiq.stacks.client.FormKiqClientConnection;
import com.formkiq.stacks.client.FormKiqClientV1;
import com.formkiq.stacks.client.requests.DeleteDocumentRequest;
import com.formkiq.stacks.client.requests.GetDocumentUploadRequest;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.signer.params.Aws4SignerParams;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserStatusType;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;
import software.amazon.awssdk.services.sts.StsClient;
import software.amazon.awssdk.services.sts.model.GetCallerIdentityResponse;

/**
 * Abstract API Test Helper.
 *
 */
public abstract class AbstractApiTest {

  /** Cognito FINANCE User Email. */
  protected static final String FINANCE_EMAIL = "testfinance@formkiq.com";
  /** Cognito User Email. */
  private static final String ADMIN_EMAIL = "testadminuser@formkiq.com";
  /** Cognito User Email. */
  protected static final String USER_EMAIL = "testuser@formkiq.com";
  /** Cognito User Email. */
  protected static final String READONLY_EMAIL = "readonly@formkiq.com";
  /** Temporary Cognito Password. */
  private static final String USER_TEMP_PASSWORD = "TEMPORARY_PASSWORd1!";
  /** Cognito User Password. */
  protected static final String USER_PASSWORD = USER_TEMP_PASSWORD + "!";
  /** Api Gateway Invoke Group. */
  private static String apiGatewayInvokeGroup;
  /** AWS Region. */
  private static Region awsregion;
  /** App Environment Name. */
  private static String appenvironment;
  /** {@link StsConnectionBuilder}. */
  private static StsConnectionBuilder stsBuilder;
  /** {@link StsService}. */
  private static StsService stsService;
  /** {@link SsmService}. */
  private static SsmService ssmService;
  /** API Root Http Url. */
  private static String rootHttpUrl;
  /** FormKiQ Http API Client. */
  private static FormKiqClientV1 httpClient;
  /** API Root Rest Url. */
  private static String rootRestUrl;
  /** FormKiQ Rest API Client. */
  private static FormKiqClientV1 restClient;
  /** {@link CognitoService}. */
  private static CognitoService adminCognitoService;
  /** {@link AuthenticationResultTypes}. */
  private static AuthenticationResultType adminToken;
  /** Cognito User Pool ID. */
  private static String cognitoUserPoolId;
  /** Cognito Cognito Client ID. */
  private static String cognitoClientId;
  /** Cognito AWS Identity Pool. */
  private static String cognitoIdentitypool;

  /** {@link Aws4SignerParams}. */
  private static Aws4SignerParams executeApiSigner;

  /**
   * Add User and/or Login Cognito.
   * 
   * @param username {@link String}
   * @param groupName {@link String}
   */
  private static void addAndLoginCognito(final String username, final String groupName) {
    if (!adminCognitoService.isUserExists(username)) {

      adminCognitoService.addUser(username, USER_TEMP_PASSWORD);
      adminCognitoService.loginWithNewPassword(username, USER_TEMP_PASSWORD, USER_PASSWORD);

      if (groupName != null) {
        if (!groupName.startsWith(DEFAULT_SITE_ID)) {
          adminCognitoService.addGroup(groupName);
        }
        adminCognitoService.addUserToGroup(username, groupName);
      }

    } else {

      AdminGetUserResponse user = adminCognitoService.getUser(username);
      if (UserStatusType.FORCE_CHANGE_PASSWORD.equals(user.userStatus())) {
        adminCognitoService.loginWithNewPassword(username, USER_TEMP_PASSWORD, USER_PASSWORD);
      }
    }
  }

  /**
   * Before Class.
   * 
   * @throws IOException IOException
   */
  @BeforeClass
  public static void beforeClass() throws IOException {

    awsregion = Region.of(System.getProperty("testregion"));

    String awsprofile = System.getProperty("testprofile");
    appenvironment = System.getProperty("testappenvironment");

    loadSsmParameterVariables(awsprofile);

    try (ProfileCredentialsProvider credentials =
        ProfileCredentialsProvider.builder().profileName(awsprofile).build()) {
      FormKiqClientConnection connection = new FormKiqClientConnection(rootRestUrl)
          .region(awsregion).credentials(credentials.resolveCredentials())
          .header("Origin", Arrays.asList("http://localhost"))
          .header("Access-Control-Request-Method", Arrays.asList("GET"));
      restClient = new FormKiqClientV1(connection);
    }

    CognitoConnectionBuilder adminBuilder =
        new CognitoConnectionBuilder(cognitoClientId, cognitoUserPoolId, cognitoIdentitypool)
            .setCredentials(awsprofile).setRegion(awsregion);
    adminCognitoService = new CognitoService(adminBuilder);

    stsBuilder = new StsConnectionBuilder().setCredentials(awsprofile).setRegion(awsregion);
    stsService = new StsService(stsBuilder);

    try (StsClient stsClient = stsBuilder.build()) {

      GetCallerIdentityResponse identity = stsClient.getCallerIdentity();
      String user = identity.arn().substring(identity.arn().lastIndexOf("/") + 1);

      IamConnectionBuilder iamBuilder = new IamConnectionBuilder().setCredentials(awsprofile);
      IamService iam = new IamService(iamBuilder);

      try (IamClient iamClient = iamBuilder.build()) {
        iam.addUserToGroup(iamClient, user, apiGatewayInvokeGroup);
      }

      executeApiSigner = new Aws4SignerParamsBuilder().setRegion(awsregion)
          .setSigningName("execute-api").setCredentials(awsprofile).build();
    }

    setupCognito();
  }

  /**
   * Create {@link FormKiqClientV1} from {@link AuthenticationResultType}.
   * 
   * @param token {@link AuthenticationResultType}
   * @return {@link FormKiqClientV1}
   */
  protected FormKiqClientV1 createHttpClient(final AuthenticationResultType token) {
    FormKiqClientConnection connection = new FormKiqClientConnection(getRootHttpUrl())
        .cognitoIdToken(token.idToken()).header("Origin", Arrays.asList("http://localhost"))
        .header("Access-Control-Request-Method", Arrays.asList("GET"));

    return new FormKiqClientV1(connection);
  }

  /**
   * Get Admin {@link CognitoService}.
   * 
   * @return {@link CognitoService}
   */
  public static CognitoService getAdminCognitoService() {
    return adminCognitoService;
  }

  /**
   * Get Admin {@link AuthenticationResultType}.
   * 
   * @return {@link AuthenticationResultType}
   */
  public static AuthenticationResultType getAdminToken() {
    return adminToken;
  }

  /**
   * Get App Environment.
   * 
   * @return {@link String}
   */
  public static String getAppenvironment() {
    return appenvironment;
  }

  /**
   * Get API Root Http Url.
   * 
   * @return {@link String}
   */
  protected static String getRootHttpUrl() {
    return rootHttpUrl;
  }

  /**
   * Get API Root Reset Url.
   * 
   * @return {@link String}
   */
  private static String getRootRestUrl() {
    return rootRestUrl;
  }

  /**
   * Get FormKiq Clients.
   * 
   * @return {@link List} {@link FormKiqClient}
   */
  public static List<FormKiqClientV1> getFormKiqClients() {
    return Arrays.asList(httpClient, restClient);
  }

  /**
   * Get {@link StsService}.
   * 
   * @param url {@link String}
   * @return {@link StsService}
   */
  public static StsService getStsService(final String url) {
    return isIamAuthentication(url) ? stsService : null;
  }

  /**
   * Is URL require IAM Authentication.
   * 
   * @param url {@link String}
   * @return boolean
   */
  public static boolean isIamAuthentication(final String url) {
    return url.startsWith(getRootRestUrl());
  }

  /**
   * Load SSM Parameter Store Variables.
   * 
   * @param awsprofile {@link String}
   */
  private static void loadSsmParameterVariables(final String awsprofile) {

    final SsmConnectionBuilder ssmBuilder =
        new SsmConnectionBuilder().setCredentials(awsprofile).setRegion(awsregion);
    ssmService = new SsmServiceImpl(ssmBuilder);

    rootHttpUrl =
        ssmService.getParameterValue("/formkiq/" + appenvironment + "/api/DocumentsHttpUrl");

    rootRestUrl =
        ssmService.getParameterValue("/formkiq/" + appenvironment + "/api/DocumentsIamUrl");

    cognitoUserPoolId =
        ssmService.getParameterValue("/formkiq/" + appenvironment + "/cognito/UserPoolId");

    cognitoClientId =
        ssmService.getParameterValue("/formkiq/" + appenvironment + "/cognito/UserPoolClientId");

    apiGatewayInvokeGroup =
        ssmService.getParameterValue("/formkiq/" + appenvironment + "/iam/ApiGatewayInvokeGroup");

    cognitoIdentitypool =
        ssmService.getParameterValue("/formkiq/" + appenvironment + "/cognito/IdentityPoolId");
  }

  /**
   * Login to Cognito.
   * 
   * @param username {@link String}
   * @param password {@link String}
   * @return {@link AuthenticationResultType}
   */
  public static AuthenticationResultType login(final String username, final String password) {
    return adminCognitoService.login(username, password);
  }

  /**
   * Setup Cognito.
   */
  private static void setupCognito() {

    if (!adminCognitoService.isUserExists(ADMIN_EMAIL)) {

      adminCognitoService.addUser(ADMIN_EMAIL, USER_TEMP_PASSWORD);
      adminCognitoService.addUserToGroup(ADMIN_EMAIL, "Admins");

      adminCognitoService.loginWithNewPassword(ADMIN_EMAIL, USER_TEMP_PASSWORD, USER_PASSWORD);

    } else {

      adminCognitoService.updateUserAttributes(ADMIN_EMAIL,
          Arrays.asList(AttributeType.builder().name("email_verified").value("true").build()));
      adminCognitoService.setUserPassword(ADMIN_EMAIL, USER_TEMP_PASSWORD, false);
      adminCognitoService.loginWithNewPassword(ADMIN_EMAIL, USER_TEMP_PASSWORD, USER_PASSWORD);
    }

    addAndLoginCognito(USER_EMAIL, DEFAULT_SITE_ID);
    addAndLoginCognito(FINANCE_EMAIL, "finance");
    addAndLoginCognito(READONLY_EMAIL, "default_read");

    adminToken = login(ADMIN_EMAIL, USER_PASSWORD);
    FormKiqClientConnection connection = new FormKiqClientConnection(rootHttpUrl)
        .cognitoIdToken(adminToken.idToken()).header("Origin", Arrays.asList("http://localhost"))
        .header("Access-Control-Request-Method", Arrays.asList("GET"));
    httpClient = new FormKiqClientV1(connection);
  }

  /** {@link HttpClient}. */
  private HttpClient http = HttpClient.newHttpClient();

  /**
   * Add "file" but this just creates DynamoDB record and not the S3 file.
   * 
   * @param client {@link FormKiqClientV1}
   * @return {@link String}
   * @throws IOException IOException
   * @throws URISyntaxException URISyntaxException
   * @throws InterruptedException InterruptedException
   */
  protected String addDocumentWithoutFile(final FormKiqClientV1 client)
      throws IOException, URISyntaxException, InterruptedException {
    // given
    final int status = 200;
    final String content = "sample content";
    GetDocumentUploadRequest request =
        new GetDocumentUploadRequest().contentLength(content.length());

    // when
    HttpResponse<String> response = client.getDocumentUploadAsHttpResponse(request);

    // then
    assertEquals(status, response.statusCode());
    assertRequestCorsHeaders(response.headers());

    Map<String, Object> map = toMap(response);
    assertNotNull(map.get("documentId"));
    assertNotNull(map.get("url"));

    String s3url = map.get("url").toString();
    response = this.http.send(HttpRequest.newBuilder(new URI(s3url))
        .method("PUT", BodyPublishers.ofString(content)).build(), BodyHandlers.ofString());

    assertEquals(status, response.statusCode());

    return map.get("documentId").toString();
  }

  /**
   * Assert CORS headers.
   *
   * @param headers {@link HttpHeaders}
   */
  protected void assertPreflightedCorsHeaders(final HttpHeaders headers) {
    assertEquals(1, headers.allValues("access-control-allow-headers").size());
    assertEquals(1, headers.allValues("access-control-allow-methods").size());
    assertEquals(1, headers.allValues("access-control-allow-origin").size());
    assertEquals(1, headers.allValues("Content-Type").size());

    if ("*".equals(headers.allValues("access-control-allow-headers").get(0))) {
      assertEquals("*", headers.allValues("access-control-allow-headers").get(0));
    } else {
      assertEquals("Content-Type,X-Amz-Date,Authorization,X-Api-Key",
          headers.allValues("access-control-allow-headers").get(0));
    }
    assertEquals("*", headers.allValues("access-control-allow-methods").get(0));
    assertEquals("*", headers.allValues("access-control-allow-origin").get(0));
  }

  /**
   * Assert CORS headers.
   *
   * @param headers {@link HttpHeaders}
   */
  protected void assertRequestCorsHeaders(final HttpHeaders headers) {
    assertEquals(1, headers.allValues("access-control-allow-origin").size());
    assertEquals(1, headers.allValues("Content-Type").size());
    assertEquals("*", headers.allValues("access-control-allow-origin").get(0));
    assertEquals("application/json", headers.allValues("Content-Type").get(0));
  }

  /**
   * Create {@link CognitoConnectionBuilder}.
   * 
   * @return {@link CognitoConnectionBuilder}
   */
  public CognitoConnectionBuilder createCognitoConnectionBuilder() {
    return new CognitoConnectionBuilder(cognitoClientId, cognitoUserPoolId, cognitoIdentitypool)
        .setRegion(awsregion);
  }

  /**
   * Delete Document.
   * 
   * @param client {@link FormKiqClient}
   * @param documentId {@link String}
   * @throws IOException IOException
   * @throws URISyntaxException URISyntaxException
   * @throws InterruptedException InterruptedException
   */
  protected void deleteDocument(final FormKiqClientV1 client, final String documentId)
      throws IOException, URISyntaxException, InterruptedException {
    // given
    final int status = 200;
    DeleteDocumentRequest request = new DeleteDocumentRequest().documentId(documentId);
    // when
    HttpResponse<String> response = client.deleteDocumentAsHttpResponse(request);
    // then
    assertEquals(status, response.statusCode());
    assertRequestCorsHeaders(response.headers());
  }

  /**
   * Get {@link Aws4SignerParams}.
   * 
   * @return {@link Aws4SignerParams}
   */
  public Aws4SignerParams getExecuteApiSigner() {
    return executeApiSigner;
  }

  /**
   * Get Parameter Store Value.
   * 
   * @param key {@link String}
   * @return {@link String}
   */
  public String getParameterStoreValue(final String key) {
    return ssmService.getParameterValue(key);
  }

  /**
   * Put SSM Parameter.
   * 
   * @param key {@link String}
   * @param value {@link String}
   */
  public void putParameter(final String key, final String value) {
    ssmService.putParameter(key, value);
  }

  /**
   * Remove SSM Parameter.
   * 
   * @param key {@link String}
   */
  public static void removeParameterStoreValue(final String key) {
    try {
      ssmService.removeParameter(key);
    } catch (ParameterNotFoundException e) {
      // ignore error
    }
  }

  /**
   * Convert {@link HttpResponse} to {@link Map}.
   * 
   * @param response {@link HttpResponse}
   * @return {@link Map}
   * @throws IOException IOException
   */
  @SuppressWarnings("unchecked")
  protected Map<String, Object> toMap(final HttpResponse<String> response) throws IOException {
    Map<String, Object> m = GsonUtil.getInstance().fromJson(response.body(), Map.class);
    return m;
  }

  /**
   * Convert {@link String} to {@link Map}.
   * 
   * @param response {@link HttpResponse}
   * @return {@link Map}
   * @throws IOException IOException
   */
  @SuppressWarnings("unchecked")
  protected Map<String, Object> toMap(final String response) throws IOException {
    Map<String, Object> m = GsonUtil.getInstance().fromJson(response, Map.class);
    return m;
  }
}
