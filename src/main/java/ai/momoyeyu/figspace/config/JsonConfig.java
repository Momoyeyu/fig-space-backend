package ai.momoyeyu.figspace.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
 
/**
 * Spring MVC Json 配置
 * @author Momoyeyu
 */
@JsonComponent
public class JsonConfig {
 
    /**
     * Long 类型转为 JSON 时可能出现精度丢失，因为 JSON 的通用数字类型不指定位数大小以及 JavaScript 存储大数字时的精度限制。‌
     * JSON 标准：JSON 本身没有专门的长整数类型，它的 number 类型没有明确的位数大小限制，通常由解析它的编程语言来决定如何处理。
     * JavaScript 限制：JavaScript 中的 Number 类型采用 IEEE 754 双精度 64 位浮点数表示，能精确表示的最大整数是 2^53 - 1。
     * 当 Java 的 Long 类型值超过这个范围时，JavaScript 在解析时就会出现精度丢失。
     *
     * @param builder Spring 提供的一个构建器，用于创建 ObjectMapper 实例
     */
    @Bean
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        SimpleModule module = new SimpleModule();
        // ToStringSerializer 会把 Long 类型的值转换为字符串，这样在转换为 JSON 时就不会出现精度丢失的问题。
        module.addSerializer(Long.class, ToStringSerializer.instance);
        module.addSerializer(Long.TYPE, ToStringSerializer.instance);
        // 将自定义模块注册到 ObjectMapper 中，使配置生效。
        objectMapper.registerModule(module);
        return objectMapper;
    }
}