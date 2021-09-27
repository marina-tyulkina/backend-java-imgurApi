package ImgurApi;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.io.File;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

public class ImageTests {

    private static int commentId;
    public static String albumHash;
    public static String login = "8a1b8ee93eb11ec680e6336c69a0472c2bffdca5";
    static final String IMAGE_FILE = "src/main/resources/222.jpg";
    String imageDeleteHash;
    String currentImageHash;


    @Test
    @DisplayName("Тест загрузки картинки")
    void uploadImageFileTest() {
        imageDeleteHash = given()
                .auth()
                .oauth2(login)
                .multiPart("image", new File(IMAGE_FILE))
                .formParam("title", "My test")
                .formParam("privacy", "public")
                .expect()
                .statusCode(200)
                .body("success", is(true))
                .body("data.id", Matchers.notNullValue())
                .log()
                .body()
                .when()
                .post("https://api.imgur.com/3/upload")
                .jsonPath()
                .getString("data.deletehash");
    }

    @Test
    @DisplayName("Проверка, что картинка загрузилась")
    void getImageFileTest() {
        currentImageHash = given()
                .auth()
                .oauth2(login)
                .multiPart("image", new File(IMAGE_FILE))
                .formParam("title", "My test")
                .formParam("privacy", "public")
                .expect()
                .statusCode(200)
                .when()
                .get("https://api.imgur.com/3/image/rMpm4Mp")
                .prettyPeek()
                .body()
                .jsonPath()
                .getString("data.id");
    }

    @Test
    @DisplayName("Удаление картинки")
    void deleteImageTest () {
        given()
                .auth()
                .oauth2(login)
                .expect()
                .statusCode(200)
                .when()
                .delete("https://api.imgur.com/3/image/rMpm4Mp")
                .prettyPeek()
                .jsonPath();
    }

}
