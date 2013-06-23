package net.hurstfrost.jenkins.avatar.user;

import hudson.Extension;
import hudson.model.*;
import hudson.model.Descriptor.FormException;
import net.sf.json.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.IOUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.ServletException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AvatarProperty extends UserProperty implements Action {
    public static final int MAX_AVATAR_IMAGE_SIZE = 60 * 1000 * 1000;

    private static final Logger log = Logger.getLogger(MyViewsProperty.class.getName());
    
    private byte[]	imageBytes;

    private transient AvatarImage	replacementImage;
    private transient String	lastError;
    private transient String	lastWarning;

    private AvatarImage avatarImage;

    private Object readResolve() {
        if (imageBytes != null) {
            // Upgrade from old version
            try {
                avatarImage = AvatarImage.fromBytes(imageBytes);
            } catch (IOException e) {
                log.warning("Couldn't interpret avatar image : " + e.getMessage());
            }

            imageBytes = null;
        }

        return this;
    }

	@Exported
    public String getAvatarUrl() {
    	if (isHasAvatar()) {
    		return getAvatarImageUrl();
    	}
    	
    	return null;
	}
	
    public String getUnsavedAvatarUrl() {
        return getAvatarImageUrl();
    }

    private String getAvatarImageUrl() {
        return Hudson.getInstance().getRootUrl() + user.getUrl() + "/avatar/image";
    }

    public boolean isHasAvatar() {
		return avatarImage != null;
	}

	public boolean isHasAvatarBeforeSave() {
        if (replacementImage == null) {
            return isHasAvatar();
        }

		return replacementImage.isValid();
	}
	
	public boolean getResetTrigger() {
		replacementImage = null;
		return true;
	}

    public boolean isHasError() {
        return lastError != null;
    }

    public String getLastError() {
        String poppedError = lastError;

        lastError = null;

        return poppedError;
    }

    public boolean isHasWarning() {
        return lastWarning != null;
    }

    public String getLastWarning() {
        String poppedWarning = lastWarning;

        lastWarning = null;

        return poppedWarning;
    }

    public void doImage(StaplerRequest req, StaplerResponse rsp) throws IOException {
    	AvatarImage	imageToReturn = avatarImage;
    	
    	if (req.getParameter("preview") != null) {
    		if (replacementImage != null) {
    			imageToReturn = replacementImage;
    		}
    	}
    	
    	if (imageToReturn == null) {
    		log.log(Level.WARNING, "No image set for user '" + user.getId() + "'");
    		return;
    	}

    	rsp.setContentType(imageToReturn.mimeType);
    	try {
            IOUtils.write(imageToReturn.imageBytes, rsp.getOutputStream());
		} catch (Exception e) {
    		log.log(Level.SEVERE, "Unable to write image for user '" + user.getId() + "'", e);
		}
	}

    /**
     * Receive file upload from startUpload.jelly.
     * File is placed in $JENKINS_HOME/userContent directory.
     * @throws ServletException 
     */
    public void doUpload(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, FormException {
        FileItem file = req.getFileItem("avatarimage.file");

        if (file != null && !file.getName().isEmpty()) {
            if (file.getSize() > MAX_AVATAR_IMAGE_SIZE) {
                lastError = "Uploaded image is too large.";
            } else {
                // Ensure we can interpret this file as an Image
                try {
                    replacementImage = AvatarImage.fromBytes(file.get());
                } catch (Exception e) {
                    // Uploaded file is invalid, ignore
                    lastError = e.getMessage();
                }
            }
        } else {
        	// Indicate that image should be removed
            lastWarning = "Empty image uploaded. Avatar will be removed on save.";
        	replacementImage = new AvatarImage();
        }

        req.getView(this, "configIframe").forward(req, rsp);
    }
    
    @Override
    public UserProperty reconfigure(StaplerRequest req, JSONObject form) throws FormException {
    	req.bindJSON(this, form);
    	
    	if (replacementImage != null) {
    		if (replacementImage.isValid()) {
                avatarImage = replacementImage;
    		} else {
                avatarImage = null;
    		}

            replacementImage = null;
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
        public String getDisplayName() {
            return "Avatar";
        }

        @Override
        public UserProperty newInstance(User user) {
            return new AvatarProperty();
        }
    }

    public static class AvatarImage {
        private byte[]  imageBytes;
        private String  mimeType;
        private String  filenameSuffix;

        static AvatarImage fromBytes(byte[] bytes) throws IOException {
            ImageInputStream imageInputStream = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes));
            Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(imageInputStream);
            if (imageReaders.hasNext()) {
                ImageReader imageReader = imageReaders.next();
                String[] mimeTypes = imageReader.getOriginatingProvider().getMIMETypes();
                String[] fileSuffixes = imageReader.getOriginatingProvider().getFileSuffixes();

                if (mimeTypes.length > 0 && fileSuffixes.length > 0) {
                    AvatarImage avatarImage = new AvatarImage();
                    avatarImage.imageBytes = bytes;
                    avatarImage.mimeType = mimeTypes[0];
                    avatarImage.filenameSuffix = fileSuffixes[0];

                    log.log(Level.FINE, "Avatar image interpreted as " + avatarImage.mimeType + " ." + avatarImage.filenameSuffix);

                    return avatarImage;
                }
            }

            return null;
        }

        public boolean isValid() {
            return imageBytes != null;
        }
    }
}
