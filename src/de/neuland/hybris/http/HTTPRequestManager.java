package de.neuland.hybris.http;

import de.neuland.hybris.http.helper.CookieParser;
import de.neuland.hybris.http.helper.JSessionCsrfPair;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class HTTPRequestManager {

    private static HTTPRequestManager ourInstance = new HTTPRequestManager();
    private String username;
    private String password;

    public static HTTPRequestManager getInstance() {
        return ourInstance;
    }

    public boolean isUserDataSet() {
        return username != null && password != null;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private HTTPRequestManager() {
    }

    public String doPostRequestWithCookie(String url, JSessionCsrfPair jSessionCsrfPair, List<NameValuePair> parameter) {
        try {
            HttpClient client = createAllowAllClient(3600);
            HttpPost request = new HttpPost(url);
            request.setEntity(new UrlEncodedFormEntity(parameter, "UTF-8"));
            request.setHeader("Cookie", jSessionCsrfPair.getJSession());
            request.setHeader("X-CSRF-TOKEN", jSessionCsrfPair.getCsrf());
            request.setHeader("Accept", "application/json, text/javascript, */*; q=0.01; charset=UTF-8");
            request.setHeader("Accept-Encoding", "gzip, deflate");
            request.setHeader("Accept-Charset", "UTF-8");

            HttpResponse response = client.execute(request);
            String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");
            return responseString;
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            return "{\"executionResult\":\"Connection Timeout\",\"outputText\":\",\"stacktraceText\":\" " + e.getLocalizedMessage() + "\"}";

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"executionResult\":\"Connection Error\",\"outputText\":\",\"stacktraceText\":\" " + e.getLocalizedMessage() + "\"}";
        }
    }

    public List<NameValuePair> createLoginDataPair(String usernameName, String passwordName) {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair(usernameName, username));
        nameValuePairs.add(new BasicNameValuePair(passwordName, password));
        return  nameValuePairs;
    }

    public JSessionCsrfPair doLoginForCookie(String url, List<NameValuePair> loginData) {
        try {
            JSessionCsrfPair jSessionCsrfPair =
                    getJSessionAndCsrfForLogin(HybrisHTTPRequest.getInstance().getLoginPageUrl(), null);

            HttpClient client = createAllowAllClient();
            HttpPost request = new HttpPost(url);
            request.setEntity(new UrlEncodedFormEntity(loginData));
            request.setHeader("Cookie", jSessionCsrfPair.getJSession());
            request.setHeader("X-CSRF-TOKEN", jSessionCsrfPair.getCsrf());

            HttpResponse response = client.execute(request);

            String jsession = response.getFirstHeader("Set-Cookie").getValue();
            jsession = CookieParser.getInstance().getSpecialCookie(jsession, "JSESSIONID");

            return getJSessionAndCsrfForLogin(HybrisHTTPRequest.getInstance().getLoginPageUrl(), jsession);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private JSessionCsrfPair getJSessionAndCsrfForLogin(String url, String jsession) {
        try {
            HttpClient client = createAllowAllClient();
            HttpGet request = new HttpGet(url);

            if (jsession != null) {
                request.setHeader("Cookie", jsession);
            }

            HttpResponse response = client.execute(request);
            if (jsession == null)
            {
                jsession = response.getFirstHeader("Set-Cookie").getValue();
                jsession = CookieParser.getInstance().getSpecialCookie(jsession, "JSESSIONID");
            }

            String csrf = findCsrf(response.getEntity());

            return new JSessionCsrfPair(jsession, csrf);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String findCsrf(HttpEntity entity)
    {
        final String csrfLocation = "name=\"_csrf\" content=\"";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent())))
        {
            String line;
            while ((line = reader.readLine()) != null) {
                int loc = line.indexOf(csrfLocation);
                if (loc > 0) {
                    line = line.substring(loc + csrfLocation.length());
                    return line.substring(0, line.indexOf("\""));
                }
            }
            throw new RuntimeException("csrf not found");
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public boolean isUserDataEqual(String otherUsername, String otherPassword) {
        return username.equals(otherUsername) && password.equals(otherPassword);
    }

    private HttpClient createAllowAllClient() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
       return createAllowAllClient(6);
    }

    /**
     * @param timeout
     *      Timeout in seconds
     */
    private HttpClient createAllowAllClient(Integer timeout) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslcontext = SSLContexts.custom()
                .loadTrustMaterial(null, new TrustStrategy() {
                    @Override
                    public boolean isTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
                        return true;
                    }
                }).build();

        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER
        );

        RequestConfig config = RequestConfig.custom()
                .setSocketTimeout(timeout * 1000)
                .setConnectTimeout(timeout * 1000)
                .build();

        return HttpClients.custom()
                .setSslcontext(sslcontext)
                .setSSLSocketFactory(sslsf)
                .setDefaultRequestConfig(config)
                .build();
    }

}
