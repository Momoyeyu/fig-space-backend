package ai.momoyeyu.figspace.manager;

import ai.momoyeyu.figspace.config.CosClientConfig;
import ai.momoyeyu.figspace.exception.BusinessException;
import ai.momoyeyu.figspace.exception.ErrorCode;
import ai.momoyeyu.figspace.exception.ThrowUtils;
import ai.momoyeyu.figspace.model.dto.file.UploadFigureResult;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class FileManager {

    private static final long ONE_MB = 1024 * 1024;

    private static final long MAX_MB_SIZE = 2;

    private static final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpeg", "png", "webp ", "jpg");
    
    private static final List<String> ALLOW_CONTENT_LIST = Arrays.asList("image/jpeg", "image/png", "image/webp ", "image/jpg");

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 文件上传
     * @param multipartFile 文件文件
     * @param uploadPathPrefix 文件存储前缀路径
     * @return 上传结果
     */
    public UploadFigureResult uploadFigure(MultipartFile multipartFile, String uploadPathPrefix) {
        // 1. 校验文件
        checkFigure(multipartFile);
        // 2. 文件上传地址
        String uuid = UUID.randomUUID().toString();
        String originalFilename = multipartFile.getOriginalFilename();
        String uploadFilename = String.format("%s_%s_%s", DateUtil.formatDate(new Date()), uuid, originalFilename);
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
        // 3. 解析结果并返回
        File file = null;
        try {
            file = File.createTempFile(uploadPath, null);
            multipartFile.transferTo(file);
            return getUploadFigureResult(originalFilename, uploadPath, file);
        } catch (Exception e) {
            // 3. 错误处理
            log.error("fail to upload figure to bucket", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 4. 删除临时文件
            deleteTemporalFile(file);
        }
    }

    public UploadFigureResult uploadFigureByUrl(String url, String uploadPathPrefix) {
        // 校验文件
        checkFigure(url);
        // 文件上传地址
        String uuid = UUID.randomUUID().toString();
        String originalFilename = FileUtil.mainName(url);
        String uploadFilename = String.format("%s_%s_%s", DateUtil.formatDate(new Date()), uuid, originalFilename);
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
        File file = null;
        try {
            file = File.createTempFile(uploadPath, null);
            // 下载图片
            HttpUtil.downloadFile(url, file);
            // same to previous
            return getUploadFigureResult(originalFilename, uploadPath, file);
        } catch (Exception e) {
            // 错误处理
            log.error("fail to upload figure to bucket", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 删除临时文件
            deleteTemporalFile(file);
        }
    }

    /**
     * 上传文件通用部分
     * @param originalFilename 待上传文件的文件名
     * @param uploadPath 上传指定路径
     * @param file 待上传的文件对象
     * @return 上传结果
     */
    private UploadFigureResult getUploadFigureResult(String originalFilename, String uploadPath, File file) {
        PutObjectResult putObjectResult = cosManager.putFigureObject(uploadPath, file);
        ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
        int figWidth = imageInfo.getWidth();
        int figHeight = imageInfo.getHeight();
        double figScale = NumberUtil.round((double) figWidth / figHeight, 2).doubleValue();
        UploadFigureResult uploadFigureResult = new UploadFigureResult();
        uploadFigureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
        uploadFigureResult.setName(FileUtil.mainName(originalFilename));
        uploadFigureResult.setFigSize(FileUtil.size(file));
        uploadFigureResult.setFigWidth(figWidth);
        uploadFigureResult.setFigHeight(figHeight);
        uploadFigureResult.setFigScale(figScale);
        uploadFigureResult.setFigFormat(imageInfo.getFormat());
        return uploadFigureResult;
    }

    /**
     * 校验文件是否合法
     * @param multipartFile 文件
     */
    private static void checkFigure(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "文件不能为空");
        // 校验文件大小
        long fileSize = multipartFile.getSize();
        ThrowUtils.throwIf(fileSize > MAX_MB_SIZE * ONE_MB, ErrorCode.PARAMS_ERROR,
                String.format("文件大小不能超过 %dMB", MAX_MB_SIZE));
        // 校验文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(fileSuffix), ErrorCode.PARAMS_ERROR, String.format("不支持 %s 文件", fileSuffix));
    }

    /**
     * 通过URL校验文件是否合法
     * @param url 文件URL
     */
    private static void checkFigure(String url) {
        // check params
        ThrowUtils.throwIf(StrUtil.isEmpty(url), ErrorCode.PARAMS_ERROR);
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式错误");
        }
        // 校验协议
        ThrowUtils.throwIf(!url.startsWith("http://") && !url.startsWith("https://"), ErrorCode.PARAMS_ERROR,
                "仅支持HTTP和HTTPS协议的地址");
        // HEAD方法验证文件元信息
        try (HttpResponse response = HttpUtil.createRequest(Method.HEAD, url).execute()) {
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }
            // 校验文件类型
            String contentType = response.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)) {
                ThrowUtils.throwIf(!ALLOW_CONTENT_LIST.contains(contentType), ErrorCode.PARAMS_ERROR, "文件类型不支持");
            }
            // 校验文件大小
            String contentLengthStr = response.header("Content-Length");
            if (StrUtil.isNotBlank(contentLengthStr)) {
                try {
                    long contentLength = Long.parseLong(contentLengthStr);
                    ThrowUtils.throwIf(contentLength > MAX_MB_SIZE * ONE_MB, ErrorCode.PARAMS_ERROR,
                            String.format("文件大小不能超过 %dMB", MAX_MB_SIZE));
                } catch (NumberFormatException e) { // parseLong
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小格式错误");
                }
            }
        }
    }

    /**
     * 删除临时文件
     * @param file 文件对象
     */
    public static void deleteTemporalFile(File file) {
        if (file != null) {
            // delete temp file
            boolean deleteResult = file.delete();
            if (!deleteResult) {
                log.error("file delete error, filepath = {}", file.getAbsolutePath());
            }
        }
    }
}
