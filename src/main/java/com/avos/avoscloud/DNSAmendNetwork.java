package com.avos.avoscloud;


import com.avos.avoscloud.internal.InternalConfigurationController;
import com.avos.avoscloud.okhttp.Dns;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by lbt05 on 9/14/15.
 */
public class DNSAmendNetwork implements Dns {

  static final long TWENTY_MIN_IN_MILLS = 20 * 60 * 1000L;
  static final String AVOS_SERVER_HOST_ZONE = "avoscloud_server_host_zone";
  public static final String EXPIRE_TIME = ".expireTime";

  private static DNSAmendNetwork instance = new DNSAmendNetwork();

  private DNSAmendNetwork() {

  }

  public static DNSAmendNetwork getInstance() {
    return instance;
  }


  @Override
  public List<InetAddress> lookup(String host) throws UnknownHostException {

    try {
      InetAddress[] addresses = InetAddress.getAllByName(host);
      return Arrays.asList(addresses);
    } catch (UnknownHostException e) {
      URL url = null;
      try {
        String response = getCacheDNSResult(host);
        boolean isCacheValid = !AVUtils.isBlankString(response);
        if (!isCacheValid) {
          url = new URL("http://119.29.29.29/d?dn=" + host);
          URLConnection urlConnection = url.openConnection();
          InputStream in = new BufferedInputStream(urlConnection.getInputStream());
          response = readStream(in);
        }
        InetAddress[] addresses = getIPAddress(host, response);
        if (!isCacheValid) {
          cacheDNS(host, response);
        }
        return Arrays.asList(addresses);
      } catch (Exception e1) {
        throw new UnknownHostException();
      }
    }
  }

  private void cacheDNS(String host, String response) {
    InternalConfigurationController.globalInstance().getInternalPersistence()
        .savePersistentSettingString(AVOS_SERVER_HOST_ZONE, host, response);
    InternalConfigurationController
        .globalInstance()
        .getInternalPersistence()
        .savePersistentSettingString(AVOS_SERVER_HOST_ZONE, host + EXPIRE_TIME,
            String.valueOf(System.currentTimeMillis() + TWENTY_MIN_IN_MILLS));
  }

  private String getCacheDNSResult(String url) {
    String response =
        InternalConfigurationController.globalInstance().getInternalPersistence()
            .getPersistentSettingString(AVOS_SERVER_HOST_ZONE, url, null);
    String expiredAt =
        InternalConfigurationController.globalInstance().getInternalPersistence()
            .getPersistentSettingString(AVOS_SERVER_HOST_ZONE, url + EXPIRE_TIME, "0");

    if (!AVUtils.isBlankString(response) && System.currentTimeMillis() < Long.parseLong(expiredAt)) {
      return response;
    } else {
      return null;
    }
  }

  private String readStream(InputStream is) {
    try {
      ByteArrayOutputStream bo = new ByteArrayOutputStream();
      int i = is.read();
      while (i != -1) {
        bo.write(i);
        i = is.read();
      }
      return bo.toString();
    } catch (IOException e) {
      return "";
    }
  }

  private static InetAddress[] getIPAddress(String url, String response) throws Exception {
    String[] ips = response.split(";");
    InetAddress[] addresses = new InetAddress[ips.length];
    Constructor constructor =
        InetAddress.class.getDeclaredConstructor(int.class, byte[].class, String.class);
    constructor.setAccessible(true);
    for (int i = 0; i < ips.length; i++) {
      String ip = ips[i];
      String[] ipSegment = ip.split("\\.");
      if (ipSegment.length == 4) {
        byte[] ipInBytes =
            {(byte) Integer.parseInt(ipSegment[0]), (byte) Integer.parseInt(ipSegment[1]),
                (byte) Integer.parseInt(ipSegment[2]), (byte) Integer.parseInt(ipSegment[3])};
        addresses[i] = (InetAddress) constructor.newInstance(2, ipInBytes, url);
      }
    }
    return addresses;
  }
}
