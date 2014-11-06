package hills.sms.texter.mms;

import android.content.Context;
import android.net.ConnectivityManager;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.NoConnectionReuseStrategyHC4;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

import hills.sms.texter.database.ApnDatabase;
import hills.sms.texter.util.Conversions;
import hills.sms.texter.util.TelephonyUtil;
import hills.sms.texter.util.Util;

/**
 * Created by alex on 11/5/2014.
 */
public abstract class MmsConnection {
    private static final String TAG = "MmsCommunication";

    protected final Context context;
    protected final Apn apn;

    protected MmsConnection(Context context, Apn apn) {
        this.context = context;
        this.apn = apn;
    }

    protected static Apn getLocalApn(Context context) throws ApnUnavailableException {
        try {
            Apn params = ApnDatabase.getInstance(context)
                    .getMmsConnectionParameters(TelephonyUtil.getMccMnc(context),
                            TelephonyUtil.getApn(context));

            if (params == null) {
                throw new ApnUnavailableException("No parameters available from ApnDefaults.");
            }

            return params;
        } catch (IOException ioe) {
            throw new ApnUnavailableException("ApnDatabase threw an IOException", ioe);
        }
    }

    public static Apn getApn(Context context, String apnName) throws ApnUnavailableException {
        Log.w(TAG, "Getting MMSC params for apn " + apnName);
        return getLocalApn(context);
    }

    protected static boolean checkRouteToHost(Context context, String host, boolean usingMmsRadio)
            throws IOException {
        InetAddress inetAddress = InetAddress.getByName(host);
        if (!usingMmsRadio) {
            if (inetAddress.isSiteLocalAddress()) {
                throw new IOException("RFC1918 address in non-MMS radio situation!");
            }
            Log.w(TAG, "returning vacuous success since MMS radio is not in use");
            return true;
        }
        byte[] ipAddressBytes = inetAddress.getAddress();
        if (ipAddressBytes == null || ipAddressBytes.length != 4) {
            Log.w(TAG, "returning vacuous success since android.net package doesn't support IPv6");
            return true;
        }

        Log.w(TAG, "Checking route to address: " + host + ", " + inetAddress.getHostAddress());
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        int ipAddress = Conversions.byteArrayToIntLittleEndian(ipAddressBytes, 0);
        boolean routeToHostObtained = manager.requestRouteToHost(MmsRadio.TYPE_MOBILE_MMS, ipAddress);
        Log.w(TAG, "requestRouteToHost result: " + routeToHostObtained);
        return routeToHostObtained;
    }

    protected static byte[] parseResponse(InputStream is) throws IOException {
        InputStream in = new BufferedInputStream(is);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Util.copy(in, baos);


        Log.w(TAG, "Received full server response, " + baos.size() + " bytes");

        return baos.toByteArray();
    }

    protected CloseableHttpClient constructHttpClient()
            throws IOException {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(20 * 1000)
                .setConnectionRequestTimeout(20 * 1000)
                .setSocketTimeout(20 * 1000)
                .setMaxRedirects(20)
                .build();

        return HttpClients.custom()
                .setConnectionReuseStrategy(new NoConnectionReuseStrategyHC4())
                .setRedirectStrategy(new LaxRedirectStrategy())
                .setUserAgent("Android-Mms/2.0")
                .setConnectionManager(new BasicHttpClientConnectionManager())
                .setDefaultRequestConfig(config)
                .build();
    }

    protected byte[] makeRequest(boolean useProxy) throws IOException {
        Log.w(TAG, "connecting to " + apn.getMmsc() + (useProxy ? " using proxy" : ""));

        HttpUriRequest request;
        CloseableHttpClient client = null;
        CloseableHttpResponse response = null;
        try {
            request = constructRequest(useProxy);
            client = constructHttpClient();
            response = client.execute(request);

            Log.w(TAG, "* response code: " + response.getStatusLine());

            if (response.getStatusLine().getStatusCode() == 200) {
                return parseResponse(response.getEntity().getContent());
            }
        } finally {
            if (response != null) response.close();
            if (client != null) client.close();
        }

        throw new IOException("unhandled response code");
    }

    protected abstract HttpUriRequest constructRequest(boolean useProxy) throws IOException;

    public static class Apn {
        private final String mmsc;
        private final String proxy;
        private final String port;

        public Apn(String mmsc, String proxy, String port) {
            this.mmsc = mmsc;
            this.proxy = proxy;
            this.port = port;
        }

        public boolean hasProxy() {
            return !TextUtils.isEmpty(proxy);
        }

        public String getMmsc() {
            return mmsc;
        }

        public String getProxy() {
            return hasProxy() ? proxy : null;
        }

        public int getPort() {
            return TextUtils.isEmpty(port) ? 80 : Integer.parseInt(port);
        }
    }
}
