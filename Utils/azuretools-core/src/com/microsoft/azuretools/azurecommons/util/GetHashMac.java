/*
 * Copyright (c) Microsoft Corporation
 *
 * All rights reserved.
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azuretools.azurecommons.util;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetHashMac {

    private static final String MAC_REGEX = "([0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}";
    private static final Pattern MAC_PATTERN = Pattern.compile(MAC_REGEX);
    private static final String[] UNIX_COMMAND = {"/sbin/ifconfig -a || /sbin/ip link"};
    private static final String[] WINDOWS_COMMAND = {"getmac"};
    private static final String[] INVALID_MAC_ADDRESS = {"00:00:00:00:00:00", "ff:ff:ff:ff:ff:ff", "ac:de:48:00:11:22"};

    // Hashed mac address for iBridge device
    private static final String INVALID_HASHED_MAC_ADDRESS = "6c9d2bc8f91b89624add29c0abeae7fb42bf539fa1cdb2e3e57cd668fa9bcead";

    public static boolean isValidHashMac(String hashMac) {
        return isValidHashMacFormat(hashMac) && !isInstallationIdDepreciated(hashMac);
    }

    public static String getHashMac() {
        String ret = null;
        String mac_raw = getRawMacByCommand();
        mac_raw = isValidRawMac(mac_raw) ? mac_raw : getRawMacWithNetworkInterface();

        if (isValidRawMac(mac_raw)) {
            Matcher matcher = MAC_PATTERN.matcher(mac_raw);
            String mac = "";
            while (matcher.find()) {
                mac = matcher.group(0);
                if (isValidMac(mac)) {
                    break;
                }
            }
            ret = hash(mac);
        }

        return ret;
    }

    private static boolean isValidMac(String mac) {
        if (StringUtils.isEmpty(mac)) {
            return false;
        }
        final String fixedMac = mac.replaceAll("-", ":");
        final boolean isMacAddress = StringUtils.isNotEmpty(fixedMac) && MAC_PATTERN.matcher(fixedMac).find();
        final boolean isValidMacAddress = !Arrays.stream(INVALID_MAC_ADDRESS)
                .anyMatch(invalidateMacAddress -> StringUtils.equalsIgnoreCase(fixedMac, invalidateMacAddress));
        return isMacAddress && isValidMacAddress;
    }

    private static boolean isValidRawMac(String raw) {
        return StringUtils.isNotEmpty(raw) && MAC_PATTERN.matcher(raw).find();
    }

    private static String getRawMacByCommand() {
        final StringBuilder ret = new StringBuilder();
        try {
            final String os = System.getProperty("os.name").toLowerCase();
            final String[] command = StringUtils.startsWithIgnoreCase(os, "win") ?
                    WINDOWS_COMMAND : UNIX_COMMAND;
            final ProcessBuilder probuilder = new ProcessBuilder(command);
            final Process process = probuilder.start();
            try (final InputStream inputStream = process.getInputStream();
                 final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 final BufferedReader br = new BufferedReader(inputStreamReader)) {
                String tmp;
                while ((tmp = br.readLine()) != null) {
                    ret.append(tmp);
                }
            }
            if (process.waitFor() != 0) {
                throw new IOException(String.format("Command %s execute fail.", String.join(" ", command)));
            }
        } catch (IOException | InterruptedException ex) {
            return null;
        }

        return ret.toString();
    }

    private static String getRawMacWithNetworkInterface() {
        List<String> macSet = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (networkInterface.getHardwareAddress() != null) {
                    byte[] mac = networkInterface.getHardwareAddress();
                    // Refers https://www.mkyong.com/java/how-to-get-mac-address-in-java/
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
                    }
                    macSet.add(sb.toString());
                }
            }
        } catch (SocketException e) {
            return StringUtils.EMPTY;
        }
        Collections.sort(macSet);
        return String.join(" ", macSet);
    }

    private static boolean isValidHashMacFormat(String hashMac) {
        if (StringUtils.isEmpty(hashMac)) {
            return false;
        }

        Pattern hashmac_pattern = Pattern.compile("[0-9a-f]{64}");
        Matcher matcher = hashmac_pattern.matcher(hashMac);
        return matcher.matches();
    }

    private static boolean isInstallationIdDepreciated(String hashMac)
    {
        return Utils.isMac() && StringUtils.equalsIgnoreCase(INVALID_HASHED_MAC_ADDRESS, hashMac);
    }

    public static String hash(String mac) {
        if (StringUtils.isEmpty(mac)) {
            return null;
        }

        String ret = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = mac.getBytes("UTF-8");
            md.update(bytes);
            byte[] bytesAfterDigest = md.digest();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < bytesAfterDigest.length; i++) {
                sb.append(Integer.toString((bytesAfterDigest[i] & 0xff) + 0x100, 16).substring(1));
            }

            ret = sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            return null;
        } catch (UnsupportedEncodingException ex) {
            return null;
        }

        return ret;
    }
}
