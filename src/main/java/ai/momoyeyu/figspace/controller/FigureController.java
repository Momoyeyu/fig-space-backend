package ai.momoyeyu.figspace.controller;

import ai.momoyeyu.figspace.annotation.AuthCheck;
import ai.momoyeyu.figspace.common.BaseResponse;
import ai.momoyeyu.figspace.common.DeleteRequest;
import ai.momoyeyu.figspace.common.ResultUtils;
import ai.momoyeyu.figspace.constant.UserConstant;
import ai.momoyeyu.figspace.exception.ErrorCode;
import ai.momoyeyu.figspace.exception.ThrowUtils;
import ai.momoyeyu.figspace.model.dto.figure.FigureEditRequest;
import ai.momoyeyu.figspace.model.dto.figure.FigureQueryRequest;
import ai.momoyeyu.figspace.model.dto.figure.FigureUpdateRequest;
import ai.momoyeyu.figspace.model.dto.figure.FigureUploadRequest;
import ai.momoyeyu.figspace.model.entity.Figure;
import ai.momoyeyu.figspace.model.entity.User;
import ai.momoyeyu.figspace.model.vo.FigureTagCategory;
import ai.momoyeyu.figspace.model.vo.FigureVO;
import ai.momoyeyu.figspace.service.FigureService;
import ai.momoyeyu.figspace.service.UserService;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;
import java.util.Date;

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

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteFigure(@RequestPart DeleteRequest deleteRequest, HttpServletRequest request) {
        // check params
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        long figureId = deleteRequest.getId();
        Figure figure = figureService.getById(figureId);
        ThrowUtils.throwIf(figure == null, ErrorCode.NOT_FOUND_ERROR);
        // check role
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(!figure.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR);
        // database
        boolean res = figureService.removeById(figureId);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "删除图片失败");
        return ResultUtils.success(true);
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/update")
    public BaseResponse<Boolean> updateFigure(@RequestBody FigureUpdateRequest figureUpdateRequest, HttpServletRequest request) {
        // check params
        ThrowUtils.throwIf(figureUpdateRequest == null || figureUpdateRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        Figure figure = figureService.getById(figureUpdateRequest.getId());
        ThrowUtils.throwIf(figure == null, ErrorCode.NOT_FOUND_ERROR);
        // apply changes
        BeanUtils.copyProperties(figureUpdateRequest, figure);
        figure.setTags(JSONUtil.toJsonStr(figureUpdateRequest.getTags()));
        // database
        figureService.validFigure(figure);
        boolean res = figureService.updateById(figure);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @GetMapping("/get")
    public BaseResponse<Figure> getFigureById(long figureId) {
        ThrowUtils.throwIf(figureId <= 0, ErrorCode.PARAMS_ERROR);
        Figure figure = figureService.getById(figureId);
        ThrowUtils.throwIf(figure == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(figure);
    }

    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/list/page")
    public BaseResponse<Page<Figure>> listFigureByPage(@RequestBody FigureQueryRequest figureQueryRequest) {
        long current = figureQueryRequest.getCurrent();
        long size = figureQueryRequest.getPageSize();
        Page<Figure> figurePage = figureService.page(new Page<>(current, size), figureService.getFigureQueryWrapper(figureQueryRequest));
        return ResultUtils.success(figurePage);
    }

    @GetMapping("/get/vo")
    public BaseResponse<FigureVO> getFigureVOById(long figureId, HttpServletRequest request) {
        ThrowUtils.throwIf(figureId <= 0, ErrorCode.PARAMS_ERROR);
        Figure figure = figureService.getById(figureId);
        ThrowUtils.throwIf(figure == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(figureService.getFigureVO(figure, request));
    }

    @PostMapping("/list/page/vo")
    public BaseResponse<Page<FigureVO>> listFigureVOByPage(@RequestBody FigureQueryRequest figureQueryRequest) {
        long current = figureQueryRequest.getCurrent();
        long size = figureQueryRequest.getPageSize();
        Page<Figure> figurePage = figureService.page(new Page<>(current, size), figureService.getFigureQueryWrapper(figureQueryRequest));
        return ResultUtils.success(figureService.getFigureVOPage(figurePage));
    }

    @PostMapping("/edit")
    public BaseResponse<Boolean> updateFigure(@RequestBody FigureEditRequest figureEditRequest, HttpServletRequest request) {
        // check params
        ThrowUtils.throwIf(figureEditRequest == null || figureEditRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        Figure figure = figureService.getById(figureEditRequest.getId());
        ThrowUtils.throwIf(figure == null, ErrorCode.NOT_FOUND_ERROR);
        // check role
        User loginUser = userService.getLoginUser(request);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        ThrowUtils.throwIf(!figure.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR);
        // apply changes
        BeanUtils.copyProperties(figureEditRequest, figure);
        figure.setTags(JSONUtil.toJsonStr(figureEditRequest.getTags()));
        figure.setEditTime(new Date());
        // database
        figureService.validFigure(figure);
        boolean res = figureService.updateById(figure);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    @GetMapping("/tag_category")
    public BaseResponse<FigureTagCategory> getFigureTagCategory() {
        FigureTagCategory figureTagCategory = new FigureTagCategory();
        figureTagCategory.setTagList(Arrays.asList("背景", "人像", "绘画"));
        figureTagCategory.setCategoryList(Arrays.asList("照片", "表情", "素材"));
        return ResultUtils.success(figureTagCategory);
    }

}