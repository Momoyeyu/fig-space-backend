package ai.momoyeyu.figspace.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ai.momoyeyu.figspace.model.entity.Figure;
import ai.momoyeyu.figspace.service.FigureService;
import ai.momoyeyu.figspace.mapper.FigureMapper;
import org.springframework.stereotype.Service;

/**
* @author momoyeyu
* @description 针对表【figure(图片)】的数据库操作Service实现
* @createDate 2025-06-08 21:51:29
*/
@Service
public class FigureServiceImpl extends ServiceImpl<FigureMapper, Figure>
    implements FigureService{

}




