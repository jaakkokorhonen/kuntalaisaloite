package fi.om.municipalityinitiative.service;

import com.google.common.collect.Lists;
import fi.om.municipalityinitiative.dao.AttachmentDao;
import fi.om.municipalityinitiative.dao.InitiativeDao;
import fi.om.municipalityinitiative.dto.service.AttachmentFile;
import fi.om.municipalityinitiative.dto.service.AttachmentFileInfo;
import fi.om.municipalityinitiative.dto.service.ManagementSettings;
import fi.om.municipalityinitiative.dto.ui.AttachmentCreateDto;
import fi.om.municipalityinitiative.dto.user.LoginUserHolder;
import fi.om.municipalityinitiative.dto.user.User;
import fi.om.municipalityinitiative.exceptions.AccessDeniedException;
import fi.om.municipalityinitiative.exceptions.FileUploadException;
import fi.om.municipalityinitiative.exceptions.InvalidAttachmentException;
import fi.om.municipalityinitiative.exceptions.OperationNotAllowedException;
import fi.om.municipalityinitiative.util.ImageModifier;
import org.aspectj.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.util.Arrays;
import java.util.List;

public class AttachmentService {

    private String attachmentDir;

    private static final Logger log = LoggerFactory.getLogger(AttachmentService.class);

    @Resource
    private AttachmentDao attachmentDao;

    @Resource
    private InitiativeDao initiativeDao;

    @Resource
    private ImageModifier imageModifier;

    @Resource
    private ValidationService validationService;

    public AttachmentService(String attachmentDir) {
        this.attachmentDir = attachmentDir;
    }

    public AttachmentService() { // For spring AOP
    }

    @Transactional(readOnly = false, rollbackFor = Throwable.class)
    public void addAttachment(Long initiativeId, LoginUserHolder<User> loginUserHolder, MultipartFile file, String description) throws FileUploadException, InvalidAttachmentException {
        loginUserHolder.assertManagementRightsForInitiative(initiativeId);

        if (!ManagementSettings.of(initiativeDao.get(initiativeId)).isAllowAddAttachments()) {
            throw new OperationNotAllowedException("Add attachments");
        }

        file.getSize(); // TODO: Don't allow too large files

        String fileType = parseFileType(file.getOriginalFilename());
        assertFileType(fileType);
        assertContentType(file.getContentType());

        Long attachmentId = attachmentDao.addAttachment(initiativeId, description, file.getContentType(), fileType);

        try {
            if (AttachmentFileInfo.isPdfContentType(file.getContentType())) {
                try (FileOutputStream fileOutputStream = new FileOutputStream(getFilePath(attachmentId, fileType))) {
                    fileOutputStream.write(file.getBytes());
                }
            }
            else {
                imageModifier.modify(file.getInputStream(), getFilePath(attachmentId, fileType), fileType, ImageProperties.MAX_WIDTH, ImageProperties.MAX_HEIGHT);
                imageModifier.modify(file.getInputStream(), getThumbnailPath(attachmentId, fileType), fileType, ImageProperties.THUMBNAIL_MAX_WIDTH, ImageProperties.THUMBNAIL_MAX_HEIGHT);
            }
        } catch (Throwable t) {
            log.error("Error while uploading file: " + file.getOriginalFilename(), t);
            throw new FileUploadException(t);
        }

    }

    private String getFilePath(Long attachmentId, String fileType) {
        return attachmentDir + "/" + attachmentId + "." + fileType;
    }

    private String getThumbnailPath(Long attachmentId, String fileType) {
        return attachmentDir + "/" + attachmentId + "_thumbnail" + "." + fileType;
    }

    @Transactional(readOnly = true)
    // TODO: Cache
    // TODO: Handle if errors?
    public AttachmentFile getAttachment(Long attachmentId, String fileName, LoginUserHolder loginUserHolder) throws IOException {
        AttachmentFileInfo attachmentInfo = attachmentDao.getAttachment(attachmentId);

        if (!attachmentInfo.getFileName().equals(fileName)) {
            throw new AccessDeniedException("Invalid filename for attachment " + attachmentId + " - " + fileName);
        }

        assertViewAllowance(loginUserHolder, attachmentInfo);
        byte[] attachmentBytes = getFileBytes(getFilePath(attachmentId, attachmentInfo.getFileType()));
        return new AttachmentFile(attachmentInfo, attachmentBytes);
    }

    @Transactional(readOnly = true)
    // TODO: Cache
    // TODO: Return empty if errors
    public AttachmentFile getThumbnail(Long attachmentId, LoginUserHolder loginUserHolder) throws IOException {
        AttachmentFileInfo attachmentInfo = attachmentDao.getAttachment(attachmentId);
        assertViewAllowance(loginUserHolder, attachmentInfo);
        if (attachmentInfo.isPdf()) {
            throw new AccessDeniedException("no thumbnail for pdf");
        }

        byte[] attachmentBytes = getFileBytes(getThumbnailPath(attachmentId, attachmentInfo.getFileType()));
        return new AttachmentFile(attachmentInfo, attachmentBytes);
    }

    private void assertViewAllowance(LoginUserHolder loginUserHolder, AttachmentFileInfo attachmentInfo) {
        if (!attachmentInfo.isAccepted()) {
            loginUserHolder.assertViewRightsForInitiative(attachmentInfo.getInitiativeId());
        }
    }

    private byte[] getFileBytes(String filePath) throws IOException {
        File file = new File(filePath);
        byte[] bytes = FileUtil.readAsByteArray(file);
        return Arrays.copyOf(bytes, bytes.length);
    }



    private static void assertFileType(String givenFileType) throws InvalidAttachmentException {
        for (String fileType : ImageProperties.FILE_TYPES) {
            if (fileType.equals(givenFileType))
                return;
        }
        throw new InvalidAttachmentException("Invalid fileName");
    }

    private static String parseFileType(String fileName) throws InvalidAttachmentException {
        String[] split = fileName.split("\\.");
        if (split.length == 1) {
            throw new InvalidAttachmentException("Invalid filename");
        }

        return split[split.length-1];
    }

    private static void assertContentType(String contentType) throws InvalidAttachmentException {
        for (String type : ImageProperties.CONTENT_TYPES) {
            if (type.equals(contentType))
                return;
        }
        throw new InvalidAttachmentException("Invalid content-type:" + contentType);
    }

    @Transactional(readOnly = true)
    public Attachments findAcceptedAttachments(Long initiativeId) {
        return new Attachments(attachmentDao.findAcceptedAttachments(initiativeId));
    }

    @Transactional(readOnly = true)
    public Attachments findAttachments(Long initiativeId, LoginUserHolder loginUserHolder) {
        if (loginUserHolder.getUser().isOmUser() || loginUserHolder.hasManagementRightsForInitiative(initiativeId)) {
            return new Attachments(attachmentDao.findAllAttachments(initiativeId));
        }
        else {
            return new Attachments(attachmentDao.findAcceptedAttachments(initiativeId));
        }
    }

    @Transactional(readOnly = true)
    public Attachments findAllAttachments(Long initiativeId, LoginUserHolder loginUserHolder) {
        loginUserHolder.assertViewRightsForInitiative(initiativeId);
        return new Attachments(attachmentDao.findAllAttachments(initiativeId));
    }

    String getAttachmentDir() { // For tests
        return attachmentDir;
    }

    @Transactional(readOnly = false)
    public Long deleteAttachment(Long attachmentId, LoginUserHolder loginUserHolder) {

        AttachmentFileInfo attachmentFileInfo = attachmentDao.getAttachment(attachmentId);
        loginUserHolder.assertManagementRightsForInitiative(attachmentFileInfo.getInitiativeId());
        attachmentDao.deleteAttachment(attachmentId);
        return attachmentFileInfo.getInitiativeId();
    }

    @Transactional(readOnly = true)
    public boolean validationSuccessful(Long initiativeId, AttachmentCreateDto attachmentCreateDto, BindingResult bindingResult, Model model) {

        if (attachmentDao.findAllAttachments(initiativeId).size() >= ImageProperties.MAX_ATTACHMENTS) {
            addAttachmentValidationError(bindingResult, "attachment.error.too.many.attachments", String.valueOf(ImageProperties.MAX_ATTACHMENTS));
        }
        else {
            validationService.validationSuccessful(attachmentCreateDto, bindingResult, model);
            if (attachmentCreateDto.getImage().getSize() == 0) {
                addAttachmentValidationError(bindingResult, "attachment.error.NotEmpty", "");
            }
            else {
                try {
                    assertFileType(parseFileType(attachmentCreateDto.getImage().getOriginalFilename()));
                } catch (InvalidAttachmentException e) {
                    addAttachmentValidationError(bindingResult, "attachment.error.invalid.file.type", Arrays.toString(ImageProperties.FILE_TYPES));
                }

                if (attachmentCreateDto.getImage().getSize() > ImageProperties.MAX_FILESIZE_IN_BYTES) {
                    addAttachmentValidationError(bindingResult, "attachment.error.too.large.file", ImageProperties.MAX_FILESIZE_IN_KILOBYTES);
                }
            }
        }


        return bindingResult.getErrorCount() == 0;
    }

    private void addAttachmentValidationError(BindingResult bindingResult, String message, String argument) {
        bindingResult.addError(new FieldError("attachment", "image", "", false, new String[]{message}, new String[]{argument}, message));
    }

    public static class Attachments {
        private final List<AttachmentFileInfo> images = Lists.newArrayList();
        private final List<AttachmentFileInfo> pdfs = Lists.newArrayList();

        public Attachments(List<AttachmentFileInfo> attachments) {
            for (AttachmentFileInfo attachment : attachments) {
                if (attachment.isPdf()) {
                    pdfs.add(attachment);
                }
                else {
                    images.add(attachment);
                }
            }
        }

        public List<AttachmentFileInfo> getImages() {
            return images;
        }

        public List<AttachmentFileInfo> getPdfs() {
            return pdfs;
        }

        public List<AttachmentFileInfo> getAll() {
            List<AttachmentFileInfo> all = Lists.newArrayList(images);
            all.addAll(pdfs);
            return all;
        }
    }

    public static class ImageProperties {

        public static final int MAX_WIDTH = 1000;

        public static final int MAX_HEIGHT = 1000;
        public static final int THUMBNAIL_MAX_WIDTH = 100;
        public static final int THUMBNAIL_MAX_HEIGHT = 100;
        public static final String[] FILE_TYPES = { "png", "jpg", "jpeg", "pdf" };
        public static final int MAX_FILESIZE_IN_BYTES = 1024 * 2 * 8;
        public static final String MAX_FILESIZE_IN_KILOBYTES = String.valueOf(ImageProperties.MAX_FILESIZE_IN_BYTES / 8) + "KB";
        public static final int MAX_ATTACHMENTS = 10;

        public static final String[] CONTENT_TYPES = { "image/png", "image/jpg", "image/jpeg", "application/pdf" };
        private static final ImageProperties imageProperties = new ImageProperties();

        private ImageProperties() { }

        public static ImageProperties get() {
            return imageProperties;
        }

        public Integer getMaxWidth() {
            return MAX_WIDTH;
        }

        public Integer getMaxHeight() {
            return MAX_HEIGHT;
        }

        public Integer getThumbnailMaxWidth() {
            return THUMBNAIL_MAX_WIDTH;
        }

        public Integer getThumbnailMaxHeight() {
            return THUMBNAIL_MAX_HEIGHT;
        }

        public String[] getFileTypes() {
            return FILE_TYPES;
        }

        public int getMaxFilesizeInBytes() {
            return MAX_FILESIZE_IN_BYTES;
        }

        public String getMaxFilesizeInKilobytes() {
            return MAX_FILESIZE_IN_KILOBYTES;
        }

        public int getMaxAttachments() {
            return MAX_ATTACHMENTS;
        }

        public String[] getContentTypes() {
            return CONTENT_TYPES;
        }
    }
}
