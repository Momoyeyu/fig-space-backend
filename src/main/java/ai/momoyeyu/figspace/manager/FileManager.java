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
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class FileManager {

    private static final long ONE_MB = 1024 * 1024;

    private static final long MAX_MB_SIZE = 2;

    private static final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpeg", "png", "webp ", "jpg");

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 图片上传
     * @param multipartFile 图片文件
     * @param uploadPathPrefix 图片存储前缀路径
     * @return 上传结果
     */
    public UploadFigureResult uploadFigure(MultipartFile multipartFile, String uploadPathPrefix) {
        // 1. 校验图片
        checkFigure(multipartFile);
        // 2. 图片上传地址
        String uuid = UUID.randomUUID().toString();
        String originalFilename = multipartFile.getOriginalFilename();
        String uploadFilename = String.format("%s_%s_%s", DateUtil.formatDate(new Date()), uuid, originalFilename);
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
        // 3. 解析结果并返回
        File file = null;
        try {
            file = File.createTempFile(uploadPath, null);
            multipartFile.transferTo(file);
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
        } catch (Exception e) {
            // 3. 错误处理
            log.error("fail to upload figure to bucket", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 4. 删除临时文件
            deleteTemporalFile(file);
        }
    }

    /**
     * 校验图片是否合法
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
