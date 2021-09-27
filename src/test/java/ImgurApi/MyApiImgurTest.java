package ImgurApi;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)

public class MyApiImgurTest {
    private static int commentId;
    public static String albumHash;
    public static String login = "8a1b8ee93eb11ec680e6336c69a0472c2bffdca5";

    @Test
    @DisplayName("Получение информации об аккаунте")
    @Order(1)
    void testGetAccountBase() {
        given()
                .auth()
                .oauth2(login)
                .expect()
                .statusCode(200)
                .body("data.id", is(154617445))
                .body("data.url", is("marinatyulkina"))
                .body("data.bio", is("Hi! My name is Marina:)"))
                .body("success", is(true))
                .log()
                .body()
                .when()
                .get("https://api.imgur.com/3/account/marinatyulkina");
    }

    @Test
    @DisplayName("Проверка, что аккуант не заблокирован")
    @Order(2)
    void testGetAccountBlockStatus() {
        given()
                .auth()
                .oauth2(login)
                .expect()
                .body("data.blocked", is(false))
                .log()
                .body()
                .when()
                .get("https://api.imgur.com/account/v1/marinatyulkina/block");

        System.out.println("Аккуант не заблокирован");
    }

    @Test
    @DisplayName("Проверка настроек аккаунта")
    @Order(3)
    void testGetAccountSettings() {
        given()
                .auth()
                .oauth2(login)
                .expect()
                .statusCode(200)
                .body("data.account_url", is("marinatyulkina"))
                .body("data.email", is("marina-tyulkina@rambler.ru"))
                .body("data.album_privacy", is("public"))
                .body("success", is(true))
                .log()
                .body()
                .when()
                .get("https://api.imgur.com/3/account/me/settings");
    }

    @Test
    @DisplayName("Тест на получение id картинки, размещенной на аккуанте")
    @Order(4)
    void testGetAccountImagesId() {
/*      не придумала, как здесь сделать тест лучше, потому что картинок в профиле может быть
        загружено несколько и надо бы все проверить*/
        String actually = given()
                .auth()
                .oauth2(login)
                .expect()
                .statusCode(200)
                .contentType("application/json")
                .log()
                .body()
                .when()
                .get("https://api.imgur.com/3/account/me/images")
                .body()
                .prettyPrint();
        Assertions.assertTrue(actually.contains("PEHjrOG"));
        System.out.println("image_id - PEHjrOG");
    }

    @Test
    @DisplayName("Тест на корректное создание альбома")
    @Order(5)
    void testPostAlbumCreation() {

        albumHash = given()
                .auth()
                .oauth2(login)
                .formParam("title", "My test album")
                .formParam("privacy", "public")
                .expect()
                .statusCode(200)
                .body("success", is(true))
                .body("data.id", Matchers.notNullValue())
                .log()
                .body()
                .when()
                .post("https://api.imgur.com/3/album")
                .jsonPath()
                .getString("data.id");
    }

    @Test
    @DisplayName("Тест на корректное удаление альбома")
    @Order(6)
    void testDeleteAlbum() {
        given()
                .auth()
                .oauth2("8a1b8ee93eb11ec680e6336c69a0472c2bffdca5")
                .expect()
                .statusCode(200)
                .log()
                .all()
                .when()
                .delete("https://api.imgur.com/3/album/T1UUmM3");
        /*здесь получается проверсти тест только вручную прописав Hash альбома,
                через переменную + albumHash возникает ошибка
        "data": {
        "error": "An ID is required.",
        "request": "/3/album/null",
        "method": "DELETE"
        }, - не разобралась, как её устранить, подскажите, пожалуйста, по возможности, где накосячила с
        переменной)*/
    }

    @Test
    @DisplayName("Проверка, что альбом не существует(удален)")
    @Order(7)
    void testAlbumNotExist() {
        String actually = given()
                .auth()
                .oauth2("8a1b8ee93eb11ec680e6336c69a0472c2bffdca5")
                .expect()
                .log()
                .all()
                .when()
                .get("https://api.imgur.com/3/album/T1UUmM3" ) //+ albumHash (такая же ошибка как в тесте выше)
                .body()
                .prettyPrint();
        Assertions.assertTrue(actually.contains("<title>imgur: the simple 404 page</title>"));
    }


    @Test
    @DisplayName("Тест на корректное создание комментария")
    @Order(8)
    void testPostCommentCreation() {

        commentId = given().auth()
                .oauth2(login)
                .formParam("image_id", "PEHjrOG")
                .formParam("comment", "My comment for API test")
                .expect()
                .statusCode(200)
                .body("success", is(true))
                .body("data.id", Matchers.notNullValue())
                .log()
                .body()
                .when()
                .post("https://api.imgur.com/3/comment")
                .jsonPath()
                .getInt("data.id");

        System.out.println("Комментарий № " + commentId);

    }

    @Test
    @DisplayName("Получение информации о созданном комментарии")
    @Order(9)
    void testGetComment() {
        given()
                .auth()
                .oauth2(login)
                .log()
                .all()
                .expect()
                .body("data.id", is(commentId))
                .body("data.image_id", is("PEHjrOG"))
                .body("data.comment", is("My comment for API test"))
                .statusCode(200)
                .log()
                .all()
                .when()
                .get("https://api.imgur.com/3/comment/2138044821");
    }
        /*здесь получается проверсти тест только вручную прописав idComment в url
                через переменную + commentId возникает ошибка, пробовала как в методичке {commentId},
                тоже ошибка
        "data": {
        "error": "An ID is required.",
        "request": "/3/comment/0",
        "method": "GET"
        } )*/


    @Test
    @DisplayName("Тест на корректное удаление комментария")
    @Order(10)
    void testDeleteComment() {
        given()
                .auth()
                .oauth2(login)
                .expect()
                .statusCode(200)
                .log()
                .all()
                .when()
                .delete("https://api.imgur.com/3/comment/2138046233");
    }

}

