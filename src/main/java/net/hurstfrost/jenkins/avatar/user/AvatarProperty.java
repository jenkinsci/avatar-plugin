package net.hurstfrost.jenkins.avatar.user;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.Hudson;
import hudson.model.MyViewsProperty;
import hudson.model.User;
import hudson.model.UserProperty;
import hudson.model.UserPropertyDescriptor;
import hudson.model.Descriptor.FormException;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;

import net.sf.json.JSONObject;

import org.apache.commons.fileupload.FileItem;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;

public class AvatarProperty extends UserProperty implements Action {

    private static final Logger log = Logger.getLogger(MyViewsProperty.class.getName());
    
    private byte[]	imageBytes;

    private transient byte[]	replacementImageBytes;

    public byte[] getImageBytes() {
		return imageBytes;
	}

	public void setImageBytes(byte[] imageBytes) {
		this.imageBytes = imageBytes;
	}

	@Exported
    public String getAvatarUrl() {
    	if (imageBytes != null) {
    		return getUnsavedAvatarUrl();
    	}
    	
    	return null;
	}
	
    public String getUnsavedAvatarUrl() {
   		return Hudson.getInstance().getRootUrl() + user.getUrl() + "/avatar/image";
	}
	
	public boolean getHasAvatar() {
		return imageBytes != null && imageBytes.length > 0;
	}

	public boolean getHasAvatarBeforeSave() {
		if (replacementImageBytes == null) {
			return getHasAvatar();
		}
		
		return replacementImageBytes.length > 0;
	}
	
	public boolean getResetTrigger() {
		replacementImageBytes = null;
		return true;
	}

    public void doImage(StaplerRequest req, StaplerResponse rsp) throws IOException {
    	byte[]	imageToReturn = imageBytes;
    	
    	if (req.getParameter("preview") != null) {
    		if (replacementImageBytes != null) {
    			imageToReturn = replacementImageBytes;
    		}
    	}
    	
    	if (imageToReturn == null || imageToReturn.length == 0) {
    		log.log(Level.SEVERE, "No image set for user '" + user.getId() + "'");
    		return;
    	}

    	rsp.setContentType("image/png");
    	ServletOutputStream outputStream = rsp.getOutputStream();
    	ImageIO.getWriterFormatNames();
    	try {
    		BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageToReturn));
			ImageIO.write(image, "png", outputStream);
		} catch (Exception e) {
    		log.log(Level.SEVERE, "Unable to write image for user '" + user.getId() + "'", e);
		}
	}

    /**
     * Receive file upload from startUpload.jelly.
     * File is placed in $JENKINS_HOME/userContent directory.
     * @throws ServletException 
     */
    public void doUpload(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        FileItem file = req.getFileItem("avatarimage.file");

        if (file != null && !file.getName().isEmpty()) {
        	// Ensure we can interpret this file as an Image
        	try {
				BufferedImage	image = null;
				if ((image = ImageIO.read(file.getInputStream())) != null) {
					ByteArrayOutputStream bAOS = new ByteArrayOutputStream();
					ImageIO.write(image, "png", bAOS);
					replacementImageBytes = bAOS.toByteArray();
				}
			} catch (Exception e) {
				// Uploaded file is invalid, ignore
			}
        } else {
        	// Indicate that image should be removed
        	replacementImageBytes = new byte[0];
        }

        req.getView(this, "configIframe").forward(req, rsp);
    }
    
    @Override
    public UserProperty reconfigure(StaplerRequest req, JSONObject form) throws FormException {
    	req.bindJSON(this, form);
    	
    	if (replacementImageBytes != null) {
    		if (replacementImageBytes.length == 0) {
    			imageBytes = null;
    		} else {
    			imageBytes = replacementImageBytes;
    		}
    		
    		replacementImageBytes = null;
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
}
