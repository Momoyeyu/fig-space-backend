package ai.momoyeyu.figspace.controller;

import ai.momoyeyu.figspace.annotation.AuthCheck;
import ai.momoyeyu.figspace.common.BaseResponse;
import ai.momoyeyu.figspace.common.ResultUtils;
import ai.momoyeyu.figspace.constant.UserConstant;
import ai.momoyeyu.figspace.exception.BusinessException;
import ai.momoyeyu.figspace.exception.ErrorCode;
import ai.momoyeyu.figspace.manager.CosManager;
import ai.momoyeyu.figspace.manager.FileManager;
import ai.momoyeyu.figspace.model.dto.figure.FigureUploadRequest;
import ai.momoyeyu.figspace.model.entity.User;
import ai.momoyeyu.figspace.model.vo.FigureVO;
import ai.momoyeyu.figspace.service.FigureService;
import ai.momoyeyu.figspace.service.UserService;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.utils.IOUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;


@RestController
@RequestMapping("/figure")
@Slf4j
public class FigureController {

    @Resource
    private UserService userService;

    @Resource
    private FigureService figureService;

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/upload")
    public BaseResponse<FigureVO> uploadFigure(
            @RequestPart("file") MultipartFile multipartFile,
            FigureUploadRequest figureUploadRequest,
            HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        FigureVO figureVO = figureService.uploadFigure(multipartFile, figureUploadRequest, user);
        return ResultUtils.success(figureVO);
    }

}