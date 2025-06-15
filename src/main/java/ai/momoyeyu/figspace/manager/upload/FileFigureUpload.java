package ai.momoyeyu.figspace.manager.upload;

import ai.momoyeyu.figspace.exception.ErrorCode;
import ai.momoyeyu.figspace.exception.ThrowUtils;
import cn.hutool.core.io.FileUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;

/**
 * 通过MultipartFile上传
 * @author Momoyeyu
 */
@Service
public class FileFigureUpload extends FigureUploadTemplate{

    @Override
    protected void checkFigure(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR);
        // 检查文件大小
        long fileSize = multipartFile.getSize();
        ThrowUtils.throwIf(fileSize > 0, ErrorCode.PARAMS_ERROR);
        // 检查文件类型
        String suffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(suffix), ErrorCode.PARAMS_ERROR);
    }

    @Override
    protected String getOriginFilename(Object inputSource) {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        return multipartFile.getOriginalFilename();
    }

    @Override
    protected void processFile(Object inputSource, File file) throws Exception {
        MultipartFile multipartFile = (MultipartFile) inputSource;
        multipartFile.transferTo(file);
    }
}
