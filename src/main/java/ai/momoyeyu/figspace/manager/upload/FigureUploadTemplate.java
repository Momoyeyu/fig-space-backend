package ai.momoyeyu.figspace.manager.upload;

import ai.momoyeyu.figspace.config.CosClientConfig;
import ai.momoyeyu.figspace.exception.BusinessException;
import ai.momoyeyu.figspace.exception.ErrorCode;
import ai.momoyeyu.figspace.manager.CosManager;
import ai.momoyeyu.figspace.model.dto.file.UploadFigureResult;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import lombok.extern.slf4j.Slf4j;
import jakarta.annotation.Resource;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 使用模版方法设计模式，创建文件上传模版类
 */
@Slf4j  
public abstract class FigureUploadTemplate {

    protected static final long ONE_MB = 1024 * 1024;

    protected static final long MAX_MB_SIZE = 2;

    protected static final List<String> ALLOW_FORMAT_LIST = Arrays.asList("jpeg", "png", "webp ", "jpg");

    protected static final List<String> ALLOW_CONTENT_LIST = Arrays.asList("image/jpeg", "image/png", "image/webp ", "image/jpg");

    @Resource  
    protected CosManager cosManager;  
  
    @Resource  
    protected CosClientConfig cosClientConfig;  
  
    /**  
     * 模板方法，定义上传流程  
     */  
    public final UploadFigureResult uploadFigure(Object inputSource, String uploadPathPrefix) {  
        // 1. 校验图片  
        checkFigure(inputSource);
  
        // 2. 图片上传地址  
        String uuid = RandomUtil.randomString(16);
        String originFilename = getOriginFilename(inputSource);  
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                FileUtil.getSuffix(originFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);  
  
        File file = null;  
        try {  
            // 3. 创建临时文件  
            file = File.createTempFile(uploadPath, null);  
            // 处理文件来源（本地或 URL）  
            processFile(inputSource, file);  
  
            // 4. 上传图片到对象存储  
            PutObjectResult putObjectResult = cosManager.putFigureObject(uploadPath, file);  
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();  
  
            // 5. 封装返回结果  
            return buildResult(originFilename, file, uploadPath, imageInfo);  
        } catch (Exception e) {  
            log.error("图片上传到对象存储失败", e);  
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");  
        } finally {  
            // 6. 清理临时文件  
            deleteTempFile(file);  
        }  
    }  
  
    /**  
     * 校验输入源（本地文件或 URL）  
     */  
    protected abstract void checkFigure(Object inputSource);
  
    /**  
     * 获取输入源的原始文件名  
     */  
    protected abstract String getOriginFilename(Object inputSource);  
  
    /**  
     * 处理输入源并生成本地临时文件  
     */  
    protected abstract void processFile(Object inputSource, File file) throws Exception;
  
    /**  
     * 封装返回结果  
     */  
    private UploadFigureResult buildResult(String originFilename, File file, String uploadPath, ImageInfo imageInfo) {
        UploadFigureResult uploadFigureResult = new UploadFigureResult();  
        int picWidth = imageInfo.getWidth();  
        int picHeight = imageInfo.getHeight();  
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();  
        uploadFigureResult.setName(FileUtil.mainName(originFilename));
        uploadFigureResult.setFigWidth(picWidth);  
        uploadFigureResult.setFigHeight(picHeight);  
        uploadFigureResult.setFigScale(picScale);  
        uploadFigureResult.setFigFormat(imageInfo.getFormat());  
        uploadFigureResult.setFigSize(FileUtil.size(file));  
        uploadFigureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);  
        return uploadFigureResult;  
    }  
  
    /**  
     * 删除临时文件  
     */  
    public static void deleteTempFile(File file) {
        if (file == null) {  
            return;  
        }  
        boolean deleteResult = file.delete();  
        if (!deleteResult) {  
            log.error("file delete error, filepath = {}", file.getAbsolutePath());  
        }  
    }  
}
