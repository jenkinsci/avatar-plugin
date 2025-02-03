package net.hurstfrost.jenkins.avatar.user.util;

import edu.umd.cs.findbugs.annotations.NonNull;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import net.sf.json.JsonConfig;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;
import org.kohsuke.stapler.export.Flavor;

/**
 * Mock response for uploads.
 */
public class MockResponse implements StaplerResponse2 {

    private String contentType;

    @Override
    public void forward(Object it, String url, StaplerRequest2 request) {
        // NOP
    }

    @Override
    public void forwardToPreviousPage(StaplerRequest2 request) {
        // NOP
    }

    @Override
    public void sendRedirect2(@NonNull String url) {
        // NOP
    }

    @Override
    public void sendRedirect(int statusCore, @NonNull String url) {
        // NOP
    }

    @Override
    public void serveFile(StaplerRequest2 request, URL res) {
        // NOP
    }

    @Override
    public void serveFile(StaplerRequest2 request, URL res, long expiration) {
        // NOP
    }

    @Override
    public void serveLocalizedFile(StaplerRequest2 request, URL res) {
        // NOP
    }

    @Override
    public void serveLocalizedFile(StaplerRequest2 request, URL res, long expiration) {
        // NOP
    }

    @Override
    public void serveFile(
            StaplerRequest2 req,
            InputStream data,
            long lastModified,
            long expiration,
            long contentLength,
            String fileName) {
        // NOP
    }

    @Override
    public void serveFile(
            StaplerRequest2 req,
            InputStream data,
            long lastModified,
            long expiration,
            int contentLength,
            String fileName) {
        // NOP
    }

    @Override
    public void serveFile(StaplerRequest2 req, InputStream data, long lastModified, long contentLength, String fileName)
            throws IOException {
        // NOP
    }

    @Override
    public void serveFile(
            StaplerRequest2 req, InputStream data, long lastModified, int contentLength, String fileName) {
        // NOP
    }

    @Override
    public void serveExposedBean(StaplerRequest2 req, Object exposedBean, Flavor flavor) {
        // NOP
    }

    @Override
    public OutputStream getCompressedOutputStream(HttpServletRequest req) {
        return null;
    }

    @Override
    public Writer getCompressedWriter(HttpServletRequest req) {
        return null;
    }

    @Override
    public int reverseProxyTo(URL url, StaplerRequest2 req) {
        return 0;
    }

    @Override
    public void setJsonConfig(JsonConfig config) {
        // NOP
    }

    @Override
    public JsonConfig getJsonConfig() {
        return null;
    }

    @Override
    public void addCookie(Cookie cookie) {
        // NOP
    }

    @Override
    public boolean containsHeader(String s) {
        return false;
    }

    @Override
    public String encodeURL(String s) {
        return "";
    }

    @Override
    public String encodeRedirectURL(String s) {
        return "";
    }

    @Override
    public String encodeUrl(String s) {
        return "";
    }

    @Override
    public String encodeRedirectUrl(String s) {
        return "";
    }

    @Override
    public void sendError(int i, String s) {
        // NOP
    }

    @Override
    public void sendError(int i) {
        // NOP
    }

    @Override
    public void sendRedirect(String s) {
        // NOP
    }

    @Override
    public void setDateHeader(String s, long l) {
        // NOP
    }

    @Override
    public void addDateHeader(String s, long l) {
        // NOP
    }

    @Override
    public void setHeader(String s, String s1) {
        // NOP
    }

    @Override
    public void addHeader(String s, String s1) {
        // NOP
    }

    @Override
    public void setIntHeader(String s, int i) {
        // NOP
    }

    @Override
    public void addIntHeader(String s, int i) {
        // NOP
    }

    @Override
    public void setStatus(int i) {
        // NOP
    }

    @Override
    public void setStatus(int i, String s) {
        // NOP
    }

    @Override
    public int getStatus() {
        return 0;
    }

    @Override
    public String getHeader(String s) {
        return "";
    }

    @Override
    public Collection<String> getHeaders(String s) {
        return List.of();
    }

    @Override
    public Collection<String> getHeaderNames() {
        return List.of();
    }

    @Override
    public String getCharacterEncoding() {
        return "";
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return null;
    }

    @Override
    public PrintWriter getWriter() {
        return null;
    }

    @Override
    public void setCharacterEncoding(String s) {
        // NOP
    }

    @Override
    public void setContentLength(int i) {
        // NOP
    }

    @Override
    public void setContentLengthLong(long l) {
        // NOP
    }

    @Override
    public void setContentType(String s) {
        contentType = s;
    }

    @Override
    public void setBufferSize(int i) {
        // NOP
    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public void flushBuffer() {
        // NOP
    }

    @Override
    public void resetBuffer() {
        // NOP
    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {
        // NOP
    }

    @Override
    public void setLocale(Locale locale) {
        // NOP
    }

    @Override
    public Locale getLocale() {
        return null;
    }
}
