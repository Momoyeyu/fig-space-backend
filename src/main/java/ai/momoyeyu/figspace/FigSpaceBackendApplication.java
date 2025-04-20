package ai.momoyeyu.figspace;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("ai.momoyeyu.figspace.mapper")
public class FigSpaceBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FigSpaceBackendApplication.class, args);
    }

}
