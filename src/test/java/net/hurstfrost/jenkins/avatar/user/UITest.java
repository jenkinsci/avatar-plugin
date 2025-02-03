package net.hurstfrost.jenkins.avatar.user;

import static net.hurstfrost.jenkins.avatar.user.AvatarPropertyTest.AVATAR_FILE_NAME;
import static net.hurstfrost.jenkins.avatar.user.AvatarPropertyTest.TEST_AVATAR_PATH;
import static net.hurstfrost.jenkins.avatar.user.AvatarPropertyTest.TEST_USER_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hudson.model.User;
import java.io.File;
import java.util.Collections;
import jenkins.model.Jenkins;
import org.apache.commons.io.FileUtils;
import org.htmlunit.html.HtmlButton;
import org.htmlunit.html.HtmlFileInput;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlImage;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.html.HtmlSvg;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockAuthorizationStrategy;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

/**
 * Various UI Tests.
 */
@WithJenkins
class UITest {

    @Test
    void uploadAvatar(JenkinsRule r) throws Throwable {
        User user = User.get(TEST_USER_NAME, true, Collections.emptyMap());

        r.jenkins.setSecurityRealm(r.createDummySecurityRealm());

        MockAuthorizationStrategy strategy = new MockAuthorizationStrategy();
        strategy.grant(Jenkins.ADMINISTER).everywhere().to(user);
        r.jenkins.setAuthorizationStrategy(strategy);

        try (JenkinsRule.WebClient webClient = r.createWebClient().login(TEST_USER_NAME)) {
            // validate default avatar
            HtmlPage userPage = webClient.goTo("user/" + TEST_USER_NAME);
            HtmlSvg defaultUserAvatar = (HtmlSvg) userPage.getElementsByTagName("svg").stream()
                    .filter(img -> "jenkins-avatar".equals(img.getAttribute("class")))
                    .findFirst()
                    .orElseThrow();
            assertNotNull(defaultUserAvatar);

            // validate default preview
            HtmlPage configure = webClient.goTo("user/" + TEST_USER_NAME + "/account/");
            HtmlForm form = configure.getFormByName("config");
            HtmlSvg defaultPreview = (HtmlSvg) configure.getElementById("avatar-preview");
            assertNotNull(defaultPreview);
            assertFalse(new File(user.getUserFolder(), AVATAR_FILE_NAME).exists());

            // upload avatar
            HtmlFileInput uploadElement = (HtmlFileInput) configure.getElementById("avatar-file");
            uploadElement.setFiles(TEST_AVATAR_PATH.toFile());

            // validate updated preview
            HtmlImage preview = (HtmlImage) configure.getElementById("avatar-preview");
            assertNotNull(preview);
            assertNotNull(preview.getSrc());

            r.submit(form);

            // validate updated avatar after saving
            userPage = webClient.goTo("user/" + TEST_USER_NAME);
            HtmlImage userAvatar = (HtmlImage) userPage.getElementsByTagName("img").stream()
                    .filter(img -> "jenkins-avatar".equals(img.getAttribute("class")))
                    .findFirst()
                    .orElseThrow();
            assertNotNull(userAvatar);
            assertEquals(r.getURL() + "user/" + TEST_USER_NAME + "/avatar/image", userAvatar.getSrc());

            // validate updated preview after saving
            configure = webClient.goTo("user/" + TEST_USER_NAME + "/account/");
            preview = (HtmlImage) configure.getElementById("avatar-preview");
            form = configure.getFormByName("config");

            assertNotNull(preview);
            assertNotNull(preview.getSrc());
            assertTrue(new File(user.getUserFolder(), AVATAR_FILE_NAME).exists());
            assertTrue(FileUtils.contentEquals(
                    TEST_AVATAR_PATH.toFile(), new File(user.getUserFolder(), AVATAR_FILE_NAME)));

            // remove avatar
            HtmlButton removeButton = (HtmlButton) configure.getElementById("avatar-remove-button");
            assertNotNull(removeButton);
            removeButton.click();

            r.submit(form);

            // validate updated avatar after saving
            userPage = webClient.goTo("user/" + TEST_USER_NAME);
            defaultUserAvatar = (HtmlSvg) userPage.getElementsByTagName("svg").stream()
                    .filter(img -> "jenkins-avatar".equals(img.getAttribute("class")))
                    .findFirst()
                    .orElseThrow();
            assertNotNull(defaultUserAvatar);

            // validate updated preview after saving
            configure = webClient.goTo("user/" + TEST_USER_NAME + "/account/");
            defaultPreview = (HtmlSvg) configure.getElementById("avatar-preview");
            assertNotNull(defaultPreview);
            assertFalse(new File(user.getUserFolder(), AVATAR_FILE_NAME).exists());
        }
    }
}
