package com.modefair.webreviewer.service.impl;

import com.modefair.webreviewer.service.UrlSafetyValidator;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

/**
 * Resolves the target host and rejects any URL that points at a non-public
 * address, blocking the common SSRF vectors: loopback, private ranges, and
 * link-local (which covers the cloud metadata endpoint 169.254.169.254).
 *
 * <p>This is a best-effort DNS-time check. It does not fully close DNS-rebinding
 * (the address could change between this resolution and the actual fetch); the
 * fetch/render layers apply short timeouts as a second line of defence.
 */
@Service
public class DefaultUrlSafetyValidator implements UrlSafetyValidator {

    @Override
    public void verifyPublic(String url) {
        URI uri;
        try {
            uri = URI.create(url.trim());
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("A valid http/https URL is required.");
        }

        String host = uri.getHost();
        if (host == null) {
            throw new IllegalArgumentException("A valid http/https URL is required.");
        }

        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("The host could not be resolved: " + host);
        }

        for (InetAddress address : addresses) {
            if (isNonPublic(address)) {
                throw new IllegalArgumentException(
                        "The URL resolves to a non-public address and cannot be analyzed.");
            }
        }
    }

    private static boolean isNonPublic(InetAddress address) {
        return address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress();
    }
}
