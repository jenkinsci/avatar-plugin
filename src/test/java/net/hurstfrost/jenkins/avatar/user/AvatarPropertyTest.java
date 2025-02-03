package net.hurstfrost.jenkins.avatar.user;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.model.Failure;
import hudson.model.User;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import net.hurstfrost.jenkins.avatar.user.util.MockRequest;
import net.hurstfrost.jenkins.avatar.user.util.MockResponse;
import org.apache.commons.fileupload2.core.FileItem;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.kohsuke.stapler.StaplerRequest2;

@WithJenkins
class AvatarPropertyTest {

    public static final String TEST_USER_NAME = "avatar";

    public static final String AVATAR_FILE_NAME = "avatar.png";
    public static final Path TEST_AVATAR_PATH = Path.of("./src/test/resources/" + AVATAR_FILE_NAME);
    public static final String AVATAR_CONTENT_TYPE = "image/png";

    @Test
    void getAvatarUrl(@SuppressWarnings("unused") JenkinsRule r) throws Exception {
        User user = User.get(TEST_USER_NAME, true, Collections.emptyMap());

        AvatarProperty avatarProperty = new AvatarProperty();
        user.addProperty(avatarProperty);

        // avatar property without avatar
        assertNull(avatarProperty.getAvatarUrl());

        // avatar property with avatar
        byte[] buffer = Files.readAllBytes(TEST_AVATAR_PATH);
        MockRequest request = new MockRequest(buffer, AVATAR_CONTENT_TYPE);
        avatarProperty.reconfigure(request, null);
        assertNotNull(avatarProperty.getAvatarUrl());
    }

    @Test
    void isHasAvatar(@SuppressWarnings("unused") JenkinsRule r) throws Exception {
        User user = User.get(TEST_USER_NAME, true, Collections.emptyMap());

        AvatarProperty avatarProperty = new AvatarProperty();
        user.addProperty(avatarProperty);

        // avatar property without avatar
        assertFalse(avatarProperty.isHasAvatar());

        // avatar property with avatar
        byte[] buffer = Files.readAllBytes(TEST_AVATAR_PATH);
        MockRequest request = new MockRequest(buffer, AVATAR_CONTENT_TYPE);
        avatarProperty.reconfigure(request, null);
        assertTrue(avatarProperty.isHasAvatar());
    }

    @Test
    void doImage(@SuppressWarnings("unused") JenkinsRule r) throws Exception {
        User user = User.get(TEST_USER_NAME, true, Collections.emptyMap());

        AvatarProperty avatarProperty = new AvatarProperty();
        user.addProperty(avatarProperty);

        // avatar property without avatar
        MockResponse response = new MockResponse();
        avatarProperty.doImage(null, response);
        assertNull(response.getContentType());

        // avatar property with avatar
        byte[] buffer = Files.readAllBytes(TEST_AVATAR_PATH);
        MockRequest request = new MockRequest(buffer, AVATAR_CONTENT_TYPE);
        avatarProperty.reconfigure(request, null);
        response = new MockResponse();
        avatarProperty.doImage(null, response);
        assertEquals(AVATAR_CONTENT_TYPE, response.getContentType());

        // avatar property with avatar throwing exception
        response = new MockResponse() {
            @Override
            public void serveFile(
                    StaplerRequest2 req, InputStream data, long lastModified, long contentLength, String fileName)
                    throws IOException {
                throw new IOException("Oh no!");
            }
        };
        avatarProperty.doImage(null, response);
        assertEquals(AVATAR_CONTENT_TYPE, response.getContentType());

        // avatar property with avatar but missing file
        new File(user.getUserFolder(), AVATAR_FILE_NAME).delete();
        response = new MockResponse();
        avatarProperty.doImage(null, response);
        assertNull(response.getContentType());
    }

    @Test
    void reconfigure(@SuppressWarnings("unused") JenkinsRule r) throws Exception {
        User user = User.get(TEST_USER_NAME, true, Collections.emptyMap());

        AvatarProperty avatarProperty = new AvatarProperty();
        user.addProperty(avatarProperty);

        // request without file
        MockRequest request1 = new MockRequest(null, null);
        assertDoesNotThrow(() -> avatarProperty.reconfigure(request1, null));

        // request with large file
        File upload = File.createTempFile("large", ".png");
        upload.deleteOnExit();
        try (RandomAccessFile raf = new RandomAccessFile(upload, "rw")) {
            raf.setLength(AvatarProperty.MAX_AVATAR_IMAGE_SIZE * 2);
        }
        byte[] buffer = Files.readAllBytes(upload.toPath());
        MockRequest request2 = new MockRequest(buffer, AVATAR_CONTENT_TYPE);
        assertThrows(Failure.class, () -> avatarProperty.reconfigure(request2, null));

        // request with invalid image file
        upload = File.createTempFile("invalid", ".svg");
        upload.deleteOnExit();
        try (RandomAccessFile raf = new RandomAccessFile(upload, "rw")) {
            raf.setLength(AvatarProperty.MAX_AVATAR_IMAGE_SIZE / 2);
        }
        buffer = Files.readAllBytes(upload.toPath());
        MockRequest request3 = new MockRequest(buffer, "image/svg+xml");
        assertThrows(Failure.class, () -> avatarProperty.reconfigure(request3, null));

        // request with file
        buffer = Files.readAllBytes(TEST_AVATAR_PATH);
        MockRequest request4 = new MockRequest(buffer, AVATAR_CONTENT_TYPE);
        assertDoesNotThrow(() -> avatarProperty.reconfigure(request4, null));
        assertTrue(new File(user.getUserFolder(), AVATAR_FILE_NAME).exists());

        // request for file removal
        MockRequest request5 = new MockRequest(null, null) {
            @Override
            public String getParameter(String name) {
                if (name.equals("existingAvatar")) {
                    return "NotPresent";
                }
                return super.getParameter(name);
            }
        };
        assertDoesNotThrow(() -> avatarProperty.reconfigure(request5, null));
        assertFalse(new File(user.getUserFolder(), AVATAR_FILE_NAME).exists());

        // request with exception
        MockRequest request6 = new MockRequest(null, null) {
            @Override
            public FileItem<?> getFileItem2(String name) throws IOException {
                throw new IOException("Oh no!");
            }
        };
        assertThrows(RuntimeException.class, () -> avatarProperty.reconfigure(request6, null));
    }

    @Test
    @WithoutJenkins
    void getDisplayName() {
        AvatarProperty avatarProperty = new AvatarProperty();
        assertEquals("Avatar", avatarProperty.getDisplayName());
    }

    @Test
    @WithoutJenkins
    void getIconFileName() {
        AvatarProperty avatarProperty = new AvatarProperty();
        assertNull(avatarProperty.getIconFileName());
    }

    @Test
    @WithoutJenkins
    void getUrlName() {
        AvatarProperty avatarProperty = new AvatarProperty();
        assertEquals("avatar", avatarProperty.getUrlName());
    }
}
