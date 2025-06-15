package ai.momoyeyu.figspace.manager.upload;

import ai.momoyeyu.figspace.exception.BusinessException;
import ai.momoyeyu.figspace.exception.ErrorCode;
import ai.momoyeyu.figspace.exception.ThrowUtils;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.*;
import org.springframework.stereotype.Service;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 通过URL上传
 * @author Momoyeyu
 */
@Service
public class UrlFigureUpload extends FigureUploadTemplate{

    @Override
    protected void checkFigure(Object inputSource) {
        String url = (String) inputSource;
        ThrowUtils.throwIf(StrUtil.isEmpty(url), ErrorCode.PARAMS_ERROR);
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件地址格式错误");
        }
        // 校验协议
        ThrowUtils.throwIf(!url.startsWith("http://") && !url.startsWith("https://"), ErrorCode.PARAMS_ERROR,
                "仅支持HTTP和HTTPS协议的地址");
        // HEAD方法校验文件
        try (HttpResponse response = HttpUtil.createRequest(Method.HEAD, url).execute()) {
            if (response.getStatus() != HttpStatus.HTTP_OK) {
                return;
            }
            // 检查文件格式
            String contentType = response.header("Content-Type");
            if (StrUtil.isNotBlank(contentType)) {
                ThrowUtils.throwIf(!ALLOW_CONTENT_LIST.contains(contentType),
                        ErrorCode.PARAMS_ERROR, "不支持该图片格式");
            }
            // 检查文件大小
            String contentLengthStr = response.header("Content-Length");
            try {
                long contetnLength = Long.parseLong(contentLengthStr);
                ThrowUtils.throwIf(contetnLength > MAX_MB_SIZE * ONE_MB, ErrorCode.PARAMS_ERROR);
            } catch (NumberFormatException e) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, String.format("文件大小不能超过 %sMB", MAX_MB_SIZE));
            }
        }
    }

    @Override
    protected String getOriginFilename(Object inputSource) {
        String url = (String) inputSource;
        return FileUtil.mainName(url);
    }

    @Override
    protected void processFile(Object inputSource, File file) {
        String url = (String) inputSource;
        HttpUtil.downloadFile(url, file);
    }
}
