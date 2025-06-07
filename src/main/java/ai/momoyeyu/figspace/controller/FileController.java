package ai.momoyeyu.figspace.controller;

import ai.momoyeyu.figspace.annotation.AuthCheck;
import ai.momoyeyu.figspace.common.BaseResponse;
import ai.momoyeyu.figspace.common.ResultUtils;
import ai.momoyeyu.figspace.constant.UserConstant;
import ai.momoyeyu.figspace.exception.BusinessException;
import ai.momoyeyu.figspace.exception.ErrorCode;
import ai.momoyeyu.figspace.manager.CosManager;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;


@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private CosManager cosManager;

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/test/upload")
    public BaseResponse<String> testUploadFile(@RequestPart("file") MultipartFile multipartFile) {
        // 1. 文件名处理
        String filename = multipartFile.getOriginalFilename();
        String filepath = String.format("/test/%s", filename);
        // 2. 上传文件
        File file = null;
        try {
            file = File.createTempFile(filepath, null);
            multipartFile.transferTo(file);
            cosManager.putObject(filepath, file);
            return ResultUtils.success(filepath);
        } catch (Exception e) {
            // 3. 错误处理
            log.error("file upload error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            // 4. 删除临时文件
            if (file != null) {
                // delete temp file
                boolean delete = file.delete();
                if (!delete) {
                    log.error("file delete error, filepath = {}", filepath);
                }
            }
        }
    }

    /**
     * 测试下载
     * @param filepath 文件路径
     * @param response 响应对象
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/test/download")
    public void testDownloadFile(String filepath, HttpServletResponse response) throws Exception {
        COSObjectInputStream cosObjectInputStream = null;
        try {
            COSObject cosObject = cosManager.getObject(filepath);
            cosObjectInputStream = cosObject.getObjectContent();
            byte[] buffer = IOUtils.toByteArray(cosObjectInputStream);
            // set response header
            response.setContentType("application/octet-stream;charset=utf-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + filepath);
            // write data
            response.getOutputStream().write(buffer);
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("download file error, filepath = " + filepath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            if (cosObjectInputStream != null) {
                cosObjectInputStream.close();
            }
        }

    }
}
