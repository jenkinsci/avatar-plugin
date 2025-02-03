package net.hurstfrost.jenkins.avatar.user;

import static net.hurstfrost.jenkins.avatar.user.AvatarPropertyTest.AVATAR_CONTENT_TYPE;
import static net.hurstfrost.jenkins.avatar.user.AvatarPropertyTest.TEST_AVATAR_PATH;
import static net.hurstfrost.jenkins.avatar.user.AvatarPropertyTest.TEST_USER_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import hudson.model.User;
import hudson.tasks.UserAvatarResolver;
import java.nio.file.Files;
import java.util.Collections;
import net.hurstfrost.jenkins.avatar.user.util.MockRequest;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Test for {@link AvatarResolver}.
 */
@WithJenkins
class AvatarResolverTest {

    @Test
    void findAvatarFor(JenkinsRule r) throws Exception {
        AvatarResolver resolver = new AvatarResolver();

        // null user
        assertNull(resolver.findAvatarFor(null, 0, 0));

        // user without avatar property
        User user = User.get(TEST_USER_NAME, true, Collections.emptyMap());
        assertNull(resolver.findAvatarFor(user, 0, 0));

        // user with avatar property and no avatar
        AvatarProperty avatarProperty = new AvatarProperty();
        user.addProperty(avatarProperty);
        assertNull(resolver.findAvatarFor(user, 0, 0));

        // user with avatar property and avatar
        byte[] buffer = Files.readAllBytes(TEST_AVATAR_PATH);
        MockRequest request = new MockRequest(buffer, AVATAR_CONTENT_TYPE);
        avatarProperty.reconfigure(request, null);
        assertEquals(r.getURL() + "user/" + TEST_USER_NAME + "/avatar/image", resolver.findAvatarFor(user, 0, 0));
    }

    @Test
    void extensionOverride(JenkinsRule r) throws Exception {
        User user = User.get(TEST_USER_NAME, true, Collections.emptyMap());

        // detects TestAvatarResolver
        assertEquals("derp", UserAvatarResolver.resolve(user, "48x48"));

        // override
        AvatarProperty avatarProperty = new AvatarProperty();
        user.addProperty(avatarProperty);
        byte[] buffer = Files.readAllBytes(TEST_AVATAR_PATH);
        MockRequest request = new MockRequest(buffer, AVATAR_CONTENT_TYPE);
        avatarProperty.reconfigure(request, null);

        assertEquals(
                r.getURL() + "user/" + TEST_USER_NAME + "/avatar/image", UserAvatarResolver.resolve(user, "48x48"));
    }

    @TestExtension("extensionOverride")
    @SuppressWarnings("unused")
    public static class TestAvatarResolver extends UserAvatarResolver {

        @Override
        public String findAvatarFor(User u, int width, int height) {
            return "derp";
        }
    }
}
