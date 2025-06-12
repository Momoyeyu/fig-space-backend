package ai.momoyeyu.figspace.service;

import ai.momoyeyu.figspace.model.dto.figure.FigureUploadRequest;
import ai.momoyeyu.figspace.model.entity.Figure;
import ai.momoyeyu.figspace.model.entity.User;
import ai.momoyeyu.figspace.model.vo.FigureVO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
* @author momoyeyu
* @description 针对表【figure(图片)】的数据库操作Service
* @createDate 2025-06-08 21:51:29
*/
public interface FigureService extends IService<Figure> {

    /**
     * 上传文件
     * @param file 文件
     * @param figureUploadRequest 上传请求
     * @param user 用户信息
     * @return 上传结果
     */
    FigureVO uploadFigure(MultipartFile file, FigureUploadRequest figureUploadRequest, User user);

}
