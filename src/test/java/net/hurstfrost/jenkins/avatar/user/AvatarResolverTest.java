package net.hurstfrost.jenkins.avatar.user;

import static net.hurstfrost.jenkins.avatar.user.AvatarPropertyTest.AVATAR_CONTENT_TYPE;
import static net.hurstfrost.jenkins.avatar.user.AvatarPropertyTest.TEST_AVATAR_PATH;
import static net.hurstfrost.jenkins.avatar.user.AvatarPropertyTest.TEST_USER_NAME;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import hudson.model.User;
import java.nio.file.Files;
import java.util.Collections;
import net.hurstfrost.jenkins.avatar.user.util.MockRequest;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Test for {@link AvatarResolver}.
 */
@WithJenkins
class AvatarResolverTest {

    @Test
    void findAvatarFor(@SuppressWarnings("unused") JenkinsRule r) throws Exception {
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
        assertNotNull(resolver.findAvatarFor(user, 0, 0));
    }
}
