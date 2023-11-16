package ServerStudy5Cloud.ServerStudy5Cloud.Controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Controller
@RequiredArgsConstructor
public class S3Controller {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @GetMapping("/")
    public String listFiles(Model model) {
        //getUrl로 객체 URL 가져온 후, List<String>에 넣어 index.html에 반환하기
        List<String> imageUrls = new ArrayList<>();

        //S3 버킷 내 모든 객체 리스트 가져오기
        List<S3ObjectSummary> objectSummaries = amazonS3.listObjects(bucketName).getObjectSummaries();

        //각 객체의 URL을 가져와서 리스트에 추가하기
        for(S3ObjectSummary ob : objectSummaries){ //반복문을 돌면서 리스트의 모든 객체에 접근
            String objectKey = ob.getKey();  //객체의 key값 가져오기
            String objectUrl = amazonS3.getUrl(bucketName, objectKey).toString();  //key값으로 url가져오기
            imageUrls.add(objectUrl); //list에 저장
        }

        //모델에 객체 URL 리스트 추가
            //index.html은 ${fileUrls}를 이용하여 객체에 접근
        model.addAttribute("fileUrls", imageUrls);

        return "index";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file) {

        //putObject와 setObjectAcl로 이미지 업로드하고 ACL 퍼블릭으로 만들기

        try{
            //s3에 파일 업로드
            amazonS3.putObject(bucketName, file.getOriginalFilename(), file.getInputStream(), null);

            //업로드한 객체에 대해 ACL 설정
            amazonS3.setObjectAcl(bucketName, file.getOriginalFilename(), CannedAccessControlList.PublicRead);

        } catch (IOException e) {
           e.printStackTrace();
        }

        return "redirect:/";

    }
}