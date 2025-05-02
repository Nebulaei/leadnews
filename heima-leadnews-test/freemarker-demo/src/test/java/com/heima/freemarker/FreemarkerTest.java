package com.heima.freemarker;

import com.heima.freemarker.pojos.Student;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

@SpringBootTest
public class FreemarkerTest {

    @Autowired
    private Configuration configuration;

    @Test
    public void test() throws IOException, TemplateException {

        Template template = configuration.getTemplate("freemarker.ftl");

        HashMap<String, Object> dataModel = getDataModel();

        template.process(dataModel, new FileWriter("/Users/dongenzhe/Documents/Program/toutiao/test.html"));
    }

    private HashMap<String, Object> getDataModel() {

        HashMap<String, Object> dataModel = new HashMap<>();
        dataModel.put("name", "kanade");

        Student stu = new Student();
        stu.setName("lmm");
        stu.setAge(18);
        dataModel.put("stu", stu);

        return dataModel;
    }
}
