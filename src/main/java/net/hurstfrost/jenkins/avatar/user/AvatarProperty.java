package net.hurstfrost.jenkins.avatar.user;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Failure;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.fileupload2.core.FileItem;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;
import org.kohsuke.stapler.export.Exported;

public class AvatarProperty extends UserProperty implements Action {
    public static final int MAX_AVATAR_IMAGE_SIZE = 10 * 1024 * 1024;

    private static final Logger LOGGER = Logger.getLogger(AvatarProperty.class.getName());

    private AvatarImage avatarImage;

    @Exported
    public String getAvatarUrl() {
        if (isHasAvatar()) {
            return getAvatarImageUrl();
        }

        return null;
    }

    private String getAvatarImageUrl() {
        return Jenkins.get().getRootUrl() + user.getUrl() + "/avatar/image";
    }

    public boolean isHasAvatar() {
        return avatarImage != null && avatarImage.isValid();
    }

    /**
     * Used to serve images as part of {@link AvatarResolver}.
     */
    public void doImage(StaplerRequest2 req, StaplerResponse2 rsp) {
        if (avatarImage == null) {
            LOGGER.log(Level.WARNING, "No image set for user '" + user.getId() + "'");
            return;
        }

        File file = new File(user.getUserFolder(), "avatar." + avatarImage.filenameSuffix);
        if (!file.exists()) {
            LOGGER.log(Level.WARNING, "Avatar image for user '" + user.getId() + "' does not exist");
            return;
        }

        try (FileInputStream fileInputStream = new FileInputStream(file); ) {
            rsp.setContentType(avatarImage.mimeType);
            rsp.serveFile(
                    req, fileInputStream, file.lastModified(), file.length(), "avatar." + avatarImage.filenameSuffix);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to write image for user '" + user.getId() + "'", e);
        }
    }

    @Override
    public UserProperty reconfigure(StaplerRequest2 req, JSONObject form) {
        req.bindJSON(this, form);

        AvatarImage replacementImage;
        try {
            FileItem<?> file = req.getFileItem2("avatar");

            if (file != null && !file.getName().isEmpty()) {
                if (file.getSize() > MAX_AVATAR_IMAGE_SIZE) {
                    throw new Failure("Uploaded image is too large.");
                } else {
                    byte[] bytes = file.get();
                    replacementImage = AvatarImage.fromBytes(bytes);

                    if (replacementImage == null) {
                        throw new Failure("Unsupported image format " + file.getName());
                    }

                    if (replacementImage.isValid()) {
                        File fileToSave = new File(user.getUserFolder(), "avatar." + replacementImage.filenameSuffix);
                        try (FileOutputStream fos = new FileOutputStream(fileToSave)) {
                            fos.write(bytes);
                        }
                        avatarImage = replacementImage;
                    } else {
                        avatarImage = null;
                    }
                }
            } else {
                if (!req.getParameter("existingAvatar").equals("present")) {
                    if (avatarImage != null && avatarImage.filenameSuffix != null) {
                        File fileToDelete = new File(user.getUserFolder(), "avatar." + avatarImage.filenameSuffix);
                        Files.deleteIfExists(fileToDelete.toPath());
                    }
                    avatarImage = null;
                }
            }
        } catch (jakarta.servlet.ServletException | IOException e) {
            throw new RuntimeException(e);
        }

        return this;
    }

    public String getDisplayName() {
        return "Avatar";
    }

    public String getIconFileName() {
        return null;
    }

    public String getUrlName() {
        return "avatar";
    }

    @Extension
    public static class DescriptorImpl extends UserPropertyDescriptor {

        @Override
        @NonNull
        public String getDisplayName() {
            return "Avatar";
        }

        @Override
        public UserProperty newInstance(User user) {
            return new AvatarProperty();
        }
    }

    public static class AvatarImage {
        private String mimeType;
        private String filenameSuffix;

        static AvatarImage fromBytes(byte[] bytes) throws IOException {
            ImageInputStream imageInputStream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes));
            Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(imageInputStream);
            if (imageReaders.hasNext()) {
                ImageReader imageReader = imageReaders.next();
                String[] mimeTypes = imageReader.getOriginatingProvider().getMIMETypes();
                String[] fileSuffixes = imageReader.getOriginatingProvider().getFileSuffixes();

                if (mimeTypes.length > 0 && fileSuffixes.length > 0) {
                    AvatarImage avatarImage = new AvatarImage();
                    avatarImage.mimeType = mimeTypes[0];
                    avatarImage.filenameSuffix = fileSuffixes[0];

                    LOGGER.log(
                            Level.FINE,
                            "Avatar image interpreted as " + avatarImage.mimeType + " ." + avatarImage.filenameSuffix);

                    return avatarImage;
                }
            }

            return null;
        }

        public boolean isValid() {
            return filenameSuffix != null;
        }
    }
}
