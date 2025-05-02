import com.heima.file.constant.FileTypeEnum;
import com.heima.file.service.FileStorageService;
import com.heima.minio.MinioApp;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@SpringBootTest(classes = MinioApp.class)
public class MinioTest {

    @Autowired
    private FileStorageService fileStorageService;

    @Test
    public void test() throws ServerException, InvalidBucketNameException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        FileInputStream inputStream = new FileInputStream("/Users/dongenzhe/Documents/Program/toutiao/test.html");

        MinioClient minioClient = MinioClient.builder()
                .credentials("admin", "admin123")
                .endpoint("http://localhost:9000")
                .build();

        PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                .bucket("leadnews")
                .object("test.html")
                .contentType("text/html")
                .stream(inputStream, inputStream.available(), -1)
                .build() ;

        minioClient.putObject(putObjectArgs);
    }

    @Test
    public void testFileStarter() throws FileNotFoundException {
        String path = fileStorageService.uploadFile("", "test.html", new FileInputStream("/Users/dongenzhe/Documents/Program/toutiao/test.html"), FileTypeEnum.HTML);
        System.out.println(path);
    }
}
