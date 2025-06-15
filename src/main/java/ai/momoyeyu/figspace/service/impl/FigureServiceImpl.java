package ai.momoyeyu.figspace.service.impl;

import ai.momoyeyu.figspace.exception.BusinessException;
import ai.momoyeyu.figspace.exception.ErrorCode;
import ai.momoyeyu.figspace.exception.ThrowUtils;
import ai.momoyeyu.figspace.manager.upload.FigureUploadTemplate;
import ai.momoyeyu.figspace.manager.upload.FileFigureUpload;
import ai.momoyeyu.figspace.manager.upload.UrlFigureUpload;
import ai.momoyeyu.figspace.model.dto.figure.FigureQueryRequest;
import ai.momoyeyu.figspace.model.dto.figure.FigureReviewRequest;
import ai.momoyeyu.figspace.model.dto.figure.FigureUploadByBatchRequest;
import ai.momoyeyu.figspace.model.dto.figure.FigureUploadRequest;
import ai.momoyeyu.figspace.model.dto.file.UploadFigureResult;
import ai.momoyeyu.figspace.model.entity.User;
import ai.momoyeyu.figspace.model.enums.FigureReviewStatus;
import ai.momoyeyu.figspace.model.vo.FigureVO;
import ai.momoyeyu.figspace.model.vo.UserVO;
import ai.momoyeyu.figspace.service.UserService;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ai.momoyeyu.figspace.model.entity.Figure;
import ai.momoyeyu.figspace.service.FigureService;
import ai.momoyeyu.figspace.mapper.FigureMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.exception.ExtCertPathBuilderException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
* @author momoyeyu
* @description 针对表【figure(图片)】的数据库操作Service实现
* @createDate 2025-06-08 21:51:29
*/
@Service
@Slf4j
public class FigureServiceImpl extends ServiceImpl<FigureMapper, Figure>
    implements FigureService{

    @Resource
    private FileFigureUpload fileFigureUpload;

    @Resource
    private UrlFigureUpload urlFigureUpload;

    @Resource
    private UserService userService;

    @Override
    public FigureVO uploadFigure(Object inputSource, FigureUploadRequest figureUploadRequest, User user) {
        // check params
        ThrowUtils.throwIf(ObjectUtil.isNull(user), ErrorCode.NO_AUTH_ERROR);
        ThrowUtils.throwIf(ObjectUtil.isNull(inputSource), ErrorCode.PARAMS_ERROR);
        // update or upload
        Long figureId = null;
        if (figureUploadRequest != null) {
            figureId = figureUploadRequest.getId();
        }
        if (figureId != null) { // check update
            Figure figure = this.getById(figureId);
            ThrowUtils.throwIf(figure == null, ErrorCode.NOT_FOUND_ERROR);
            ThrowUtils.throwIf(!userService.isAdmin(user) && !figure.getUserId().equals(user.getId()), ErrorCode.NO_AUTH_ERROR);
        }
        // 按照用户 ID 划分目录
        String uploadPathPrefix = String.format("public/%s", user.getId());
        // 根据 inputSource 的类型区分上传方式
        FigureUploadTemplate figureUploadTemplate;
        if (inputSource instanceof String) {
            figureUploadTemplate = urlFigureUpload;
        } else if (inputSource instanceof MultipartFile) {
            figureUploadTemplate = fileFigureUpload;
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持该上传方式");
        }
        // 上传图片
        UploadFigureResult uploadFigureResult = figureUploadTemplate.uploadFigure(inputSource, uploadPathPrefix);
        Figure figure = new Figure();
        BeanUtils.copyProperties(uploadFigureResult, figure);
        figure.setUserId(user.getId());
        // write id if update
        if (figureId != null) {
            figure.setId(figureId);
            figure.setEditTime(new Date());
        }
        // 设置默认审核信息
        this.fillDefaultReview(figure, user);
        // write database
        boolean res = this.saveOrUpdate(figure);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "图片上传失败：数据库错误");
        return FigureVO.objToVo(figure);
    }

    @Override
    public QueryWrapper<Figure> getFigureQueryWrapper(FigureQueryRequest figureQueryRequest) {
        QueryWrapper<Figure> queryWrapper = new QueryWrapper<>();
        // check params
        if (figureQueryRequest == null) {
            return queryWrapper;
        }
        Long id = figureQueryRequest.getId();
        String name = figureQueryRequest.getName();
        String intro = figureQueryRequest.getIntro();
        String category = figureQueryRequest.getCategory();
        List<String> tags = figureQueryRequest.getTags();
        Long figSize = figureQueryRequest.getFigSize();
        Integer figWidth = figureQueryRequest.getFigWidth();
        Integer figHeight = figureQueryRequest.getFigHeight();
        Double figScale = figureQueryRequest.getFigScale();
        String figFormat = figureQueryRequest.getFigFormat();
        String searchText = figureQueryRequest.getSearchText();
        Long userId = figureQueryRequest.getUserId();
        Integer reviewStatus = figureQueryRequest.getReviewStatus();
        String reviewMessage = figureQueryRequest.getReviewMessage();
        Long reviewerId = figureQueryRequest.getReviewerId();
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper
                    .and(qw -> qw.like("name", searchText)
                    .or().like("intro", searchText));
        }
        queryWrapper.eq(ObjectUtil.isNotEmpty(id), "id", id);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.like(StringUtils.isNotBlank(intro), "intro", intro);
        queryWrapper.eq(StringUtils.isNotBlank(category), "category", category);
        if (CollectionUtils.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.eq(ObjectUtil.isNotNull(figWidth), "figWidth", figWidth);
        queryWrapper.eq(ObjectUtil.isNotNull(figHeight), "figHeight", figHeight);
        queryWrapper.eq(ObjectUtil.isNotNull(figSize), "figSize", figSize);
        queryWrapper.eq(ObjectUtil.isNotNull(figScale), "figScale", figScale);
        queryWrapper.eq(StringUtils.isNotBlank(figFormat), "figFormat", figFormat);
        queryWrapper.eq(ObjectUtil.isNotNull(userId), "userId", userId);
        // 匹配审核信息
        queryWrapper.eq(ObjectUtil.isNotNull(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.eq(StringUtils.isNotBlank(reviewMessage), "reviewMessage", reviewMessage);
        queryWrapper.eq(ObjectUtil.isNotNull(reviewerId), "reviewerId", reviewerId);
        return queryWrapper;
    }

    @Override
    public FigureVO getFigureVO(Figure figure, HttpServletRequest request) {
        FigureVO figureVO = FigureVO.objToVo(figure);
        Long userId = figure.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            figureVO.setUserVO(userVO);
        }
        return figureVO;
    }

    @Override
    public Page<FigureVO> getFigureVOPage(Page<Figure> figurePage) {
        List<Figure> figureList = figurePage.getRecords();
        Page<FigureVO> figureVOPage = new Page<>(figurePage.getCurrent(), figurePage.getSize(), figurePage.getTotal());
        if (CollectionUtils.isEmpty(figureList)) {
            return figureVOPage;
        }
        List<FigureVO> figureVOList = figureList.stream().map(FigureVO::objToVo).toList();
        Set<Long> userIds = figureList.stream().map(Figure::getUserId).collect(Collectors.toSet());
        Map<Long, User> map = userService.listByIds(userIds).stream().collect(Collectors.toMap(User::getId, user -> user));
        figureVOList.forEach(figureVO -> {
            User user = map.get(figureVO.getUserId());
            UserVO userVO = userService.getUserVO(user);
            figureVO.setUserVO(userVO);
        });
        figureVOPage.setRecords(figureVOList);
        return figureVOPage;
    }

    @Override
    public void validFigure(Figure figure) {
        ThrowUtils.throwIf(ObjectUtil.isNull(figure), ErrorCode.PARAMS_ERROR);
        Long id = figure.getId();
        String url = figure.getUrl();
        String intro = figure.getIntro();
        ThrowUtils.throwIf(ObjectUtil.isNull(id), ErrorCode.PARAMS_ERROR, "图片 id 不能为空");
        if (StringUtils.isNotBlank(url)) {
            ThrowUtils.throwIf(url.length() > 1024, ErrorCode.PARAMS_ERROR, "图片 url 过长");
        }
        if (StringUtils.isNotBlank(intro)) {
            ThrowUtils.throwIf(intro.length() > 800, ErrorCode.PARAMS_ERROR, "图片简介过长");
        }
    }

    @Override
    public void reviewFigure(FigureReviewRequest figureReviewRequest, User reviewer) {
        // check params
        ThrowUtils.throwIf(figureReviewRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = figureReviewRequest.getId();
        FigureReviewStatus reviewStatus = FigureReviewStatus.fromValue(figureReviewRequest.getReviewStatus());
        String reviewMessage = figureReviewRequest.getReviewMessage();
        ThrowUtils.throwIf(!ObjectUtil.isAllNotEmpty(id, reviewStatus), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(reviewStatus.equals(FigureReviewStatus.REVIEW), ErrorCode.PARAMS_ERROR, "非法审核状态");
        // check figure
        Figure figure = this.getById(id);
        ThrowUtils.throwIf(ObjectUtil.isNull(figure), ErrorCode.PARAMS_ERROR);
        // 检查审核
        FigureReviewStatus currentStatus = FigureReviewStatus.fromValue(figure.getReviewStatus());
        ThrowUtils.throwIf(currentStatus.equals(reviewStatus), ErrorCode.PARAMS_ERROR, "请勿重复审核");
        // write data
        figure.setReviewStatus(reviewStatus);
        figure.setReviewMessage(reviewMessage);
        figure.setReviewTime(new Date());
        figure.setReviewerId(reviewer.getId());
        boolean res = this.updateById(figure);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public void fillDefaultReview(Figure figure, User user) {
        if (userService.isAdmin(user)) {
            figure.setReviewStatus(FigureReviewStatus.ACCEPT);
            figure.setReviewerId(user.getId());
            figure.setReviewMessage("管理员自动过审");
            figure.setReviewTime(new Date());
        } else {
            figure.setReviewStatus(FigureReviewStatus.REVIEW);
        }
    }

    @Override
    public Integer uploadFigureByBatch(FigureUploadByBatchRequest figureUploadByBatchRequest, User user) {
        // check params
        ThrowUtils.throwIf(ObjectUtil.isNull(figureUploadByBatchRequest), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(!userService.isAdmin(user), ErrorCode.NO_AUTH_ERROR);
        String searchText = figureUploadByBatchRequest.getSearchText();
        Integer count = figureUploadByBatchRequest.getCount();
        // 图片搜去（通过bing）
        String fetchUrl = String.format("https://cn.bing.com/images/async?q=%s&mmasync=1", searchText);
        Document document;
        try {
            document = Jsoup.connect(fetchUrl).get();
        } catch (IOException e) {
            log.error("获取页面失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取页面失败");
        }
        Element div = document.getElementsByClass("dgControl").first();
        if (ObjectUtil.isNull(div)) {
            log.error("获取元素失败");
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取元素失败");
        }
        Elements imgElementList = div.select("img.mimg");
        int uploadCount = 0;
        for (Element imgElement : imgElementList) {
            String url = imgElement.attr("src");
            // 判空
            if (StringUtils.isNotBlank(url)) {
                log.info("当前链接为空，已跳过：{}", url);
                continue;
            }
            // 处理地址：去除条件，防止转义
            int questionMarkIndex = url.lastIndexOf("?");
            if (questionMarkIndex != -1) {
                url = url.substring(0, questionMarkIndex);
            }
            // 调用图片上传服务
            FigureUploadRequest figureUploadRequest = new FigureUploadRequest();
            try {
                FigureVO figureVO = this.uploadFigure(url, figureUploadRequest, user);
                log.info("图片已上传，ID：{}", figureVO.getId());
                uploadCount++;
            } catch (Exception e) {
                log.error("图片上传失败", e);
            }
            // 判断数量是否达标
            if (uploadCount >= count) {
                break;
            }
        }
        return uploadCount;
    }
}




