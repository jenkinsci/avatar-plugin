package net.hurstfrost.jenkins.avatar.user;

import hudson.Extension;
import hudson.model.User;
import hudson.tasks.UserAvatarResolver;

@Extension
public class AvatarResolver extends UserAvatarResolver {
    @Override
    public String findAvatarFor(User user, int i, int i1) {
        AvatarProperty avatarProperty = user.getProperty(AvatarProperty.class);

        if (avatarProperty != null) {
            return avatarProperty.getAvatarUrl();
        }

        return null;
    }
}
