package ai.momoyeyu.figspace.service;

import ai.momoyeyu.figspace.model.dto.figure.FigureQueryRequest;
import ai.momoyeyu.figspace.model.dto.figure.FigureReviewRequest;
import ai.momoyeyu.figspace.model.dto.figure.FigureUploadRequest;
import ai.momoyeyu.figspace.model.entity.Figure;
import ai.momoyeyu.figspace.model.entity.User;
import ai.momoyeyu.figspace.model.vo.FigureVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

/**
* @author momoyeyu
* @description 针对表【figure(图片)】的数据库操作Service
* @createDate 2025-06-08 21:51:29
*/
public interface FigureService extends IService<Figure> {

    /**
     * 上传图片
     * @param file 图片文件
     * @param figureUploadRequest 上传请求
     * @param user 用户信息
     * @return 脱敏后的图片数据
     */
    FigureVO uploadFigure(MultipartFile file, FigureUploadRequest figureUploadRequest, User user);

    /**
     * 获取查询的 QueryWrapper
     * @param figureQueryRequest 查询条件
     * @return QueryWrapper
     */
    QueryWrapper<Figure> getFigureQueryWrapper(FigureQueryRequest figureQueryRequest);

    /**
     * 图片脱敏时关联用户
     * @param figure 图片
     * @param request 请求
     * @return 图片脱敏
     */
    FigureVO getFigureVO(Figure figure, HttpServletRequest request);

    /**
     * 获取脱敏后的分页图片数据
     * @param page 图片分页原始数据
     * @return 脱敏后的图片分页数据
     */
    Page<FigureVO> getFigureVOPage(Page<Figure> page);

    /**
     * 校验图片信息合法性
     * @param figure 图片信息
     */
    void validFigure(Figure figure);

    /**
     * 【管理员】审核图片
     * @param figureReviewRequest 审核请求
     * @param reviewer 审核者
     */
    void reviewFigure(FigureReviewRequest figureReviewRequest, User reviewer);
}
