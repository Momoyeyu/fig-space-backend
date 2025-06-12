package ai.momoyeyu.figspace.service.impl;

import ai.momoyeyu.figspace.exception.ErrorCode;
import ai.momoyeyu.figspace.exception.ThrowUtils;
import ai.momoyeyu.figspace.manager.FileManager;
import ai.momoyeyu.figspace.model.dto.figure.FigureUploadRequest;
import ai.momoyeyu.figspace.model.dto.file.UploadFigureResult;
import ai.momoyeyu.figspace.model.entity.User;
import ai.momoyeyu.figspace.model.vo.FigureVO;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ai.momoyeyu.figspace.model.entity.Figure;
import ai.momoyeyu.figspace.service.FigureService;
import ai.momoyeyu.figspace.mapper.FigureMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

/**
* @author momoyeyu
* @description 针对表【figure(图片)】的数据库操作Service实现
* @createDate 2025-06-08 21:51:29
*/
@Service
public class FigureServiceImpl extends ServiceImpl<FigureMapper, Figure>
    implements FigureService{

    @Resource
    private FileManager fileManager;

    @Override
    public FigureVO uploadFigure(MultipartFile file, FigureUploadRequest figureUploadRequest, User user) {
        // check params
        ThrowUtils.throwIf(ObjectUtil.isNull(user), ErrorCode.NO_AUTH_ERROR);
        ThrowUtils.throwIf(ObjectUtil.isNull(file), ErrorCode.PARAMS_ERROR);
        // update or upload
        Long figureId = null;
        if (figureUploadRequest != null) {
            figureId = figureUploadRequest.getId();
        }
        if (figureId != null) { // check update
            boolean exists = this.lambdaQuery()
                    .eq(Figure::getId, figureId)
                    .exists();
            ThrowUtils.throwIf(!exists, ErrorCode.NOT_FOUND_ERROR);
        }
        String uploadPathPrefix = String.format("public/%s", user.getId());
        UploadFigureResult uploadFigureResult = fileManager.uploadFigure(file, uploadPathPrefix);
        Figure figure = new Figure();
        BeanUtils.copyProperties(uploadFigureResult, figure);
        figure.setUserId(user.getId());
        // write id if update
        if (figureId != null) {
            figure.setId(figureId);
            figure.setEditTime(new Date());
        }
        // write database
        boolean res = this.saveOrUpdate(figure);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "图片上传失败：数据库错误");
        return FigureVO.objToVo(figure);
    }
}




