package ru.frigesty.tests;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import ru.frigesty.models.*;
import java.util.List;

import static io.qameta.allure.Allure.step;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.frigesty.specs.ApiSpecs.*;

@Tag("api")
public class ApiTests {

    @BeforeEach
    public void beforeEach() {
        RestAssured.baseURI = "https://reqres.in";
        RestAssured.basePath = "/api";
    }

    @DisplayName("Получение пользователя по ID")
    @Test
    public void getUserTest() {
        SingleUserModel response = step("Делаем запрос на получение пользователя", () ->
                given()
                        .spec(requestSpecBase)
                        .when()
                        .get("/users/2")
                        .then()
                        .spec(responseSpecBase)
                        .body(matchesJsonSchemaInClasspath("schemes/singleUserScheme.json"))
                        .extract().as(SingleUserModel.class));

        step("Проверяем данные пользователя", () -> {
            assertEquals(2, response.getData().getId());
            assertEquals("janet.weaver@reqres.in", response.getData().getEmail());
            assertEquals("https://reqres.in/img/faces/2-image.jpg", response.getData().getAvatar());
            assertEquals("Janet", response.getData().getFirstName());
            assertEquals("Weaver", response.getData().getLastName());
        });
    }

    @DisplayName("Проверка корректности данных в List Resource")
    @Test
    void correctDataInPageListResourceTest() {
        List<ListResourceModel.ColorData> expectedColors = List.of(
         new ListResourceModel.ColorData(1, 2000, "cerulean", "#98B2D1", "15-4020"),
         new ListResourceModel.ColorData(2, 2001, "fuchsia rose", "#C74375", "17-2031"),
         new ListResourceModel.ColorData(3, 2002, "true red", "#BF1932", "19-1664"),
         new ListResourceModel.ColorData(4, 2003, "aqua sky", "#7BC4C4", "14-4811"),
         new ListResourceModel.ColorData(5, 2004, "tigerlily", "#E2583E", "17-1456"),
         new ListResourceModel.ColorData(6, 2005, "blue turquoise", "#53B0AE", "15-5217")
        );

        ListResourceModel response = step("Делаем запрос", () ->
                given()
                        .spec(requestSpecBase)
                .when()
                        .get("/unknown")
                .then()
                        .body(matchesJsonSchemaInClasspath("schemes/listResourceScheme.json"))
                        .spec(responseSpecBase)
                        .extract().as(ListResourceModel.class));

        List<ListResourceModel.ColorData> colorsFromResponse = response.getData();

        step("Проверяем все цвета в массиве data", () -> {
            assertEquals(expectedColors.size(), colorsFromResponse.size(),
                    "Некорректное количество элементов в data");
            for (int i = 0; i < expectedColors.size(); i++) {
                assertEquals(expectedColors.get(i).getId(), colorsFromResponse.get(i).getId());
                assertEquals(expectedColors.get(i).getYear(), colorsFromResponse.get(i).getYear());
                assertEquals(expectedColors.get(i).getName(), colorsFromResponse.get(i).getName());
                assertEquals(expectedColors.get(i).getColor(), colorsFromResponse.get(i).getColor());
                assertEquals(expectedColors.get(i).getPantoneValue(), colorsFromResponse.get(i).getPantoneValue());
            }
        });
    }

    @DisplayName("Получение цвета по ID")
    @Test
    public void getResourceByIdTest() {
        SingleResourceModel response = step("Делаем запрос", () ->
                given()
                        .spec(requestSpecBase)
                .when()
                        .get("/unknown/2")
                .then()
                        .body(matchesJsonSchemaInClasspath("schemes/singleResourceScheme.json"))
                        .spec(responseSpecBase)
                        .extract().as(SingleResourceModel.class));

        step("Проверяем данные ресурса", () -> {
                assertEquals(2, response.getData().getId());
                assertEquals("fuchsia rose", response.getData().getName());
                assertEquals(2001, response.getData().getYear());
                assertEquals("#C74375", response.getData().getColor());
                assertEquals("17-2031", response.getData().getPantoneValue());
        });
    }

    @DisplayName("Пользователь не найден (404)")
    @Test
    public void userNotFoundTest() {
        step("Делаем запрос на несуществующего пользователя", () ->
                given()
                        .spec(requestSpecBase)
                        .when()
                        .get("/users/900")
                        .then()
                        .spec(notFoundSpec));
    }

    @DisplayName("Создание нового пользователя")
    @Test
    public void createUserTest() {
        CreateUserModel requestBody = new CreateUserModel();
        requestBody.setName("morpheus");
        requestBody.setJob("leader");

        step("Отправляем запрос на создание пользователя", () ->
                given()
                        .spec(requestSpecBase)
                        .body(requestBody)
                .when()
                        .post("/users")
                .then()
                        .spec(createResponseSpec)
                        .body("name", equalTo(requestBody.getName()))
                        .body("job", equalTo(requestBody.getJob()))
                        .body("id", notNullValue())
                        .body("createdAt", notNullValue()));
    }

    @DisplayName("Обновление данных пользователя")
    @Test
    public void updateUserTest() {
        UpdateUserModel requestBody = new UpdateUserModel();
        requestBody.setName("morpheus");
        requestBody.setJob("zion resident");

        step("Отправляем запрос на обновление пользователя", () ->
                given()
                        .spec(requestSpecBase)
                        .body(requestBody)
                .when()
                        .put("/users/2")
                .then()
                        .spec(responseSpecBase)
                        .body("name", equalTo(requestBody.getName()))
                        .body("job", equalTo(requestBody.getJob()))
                        .body("updatedAt", notNullValue()));
    }

    @DisplayName("Удаление пользователя")
    @Test
    public void deleteUserTest() {
        step("Отправляем запрос на удаление пользователя", () ->
                given()
                        .spec(requestSpecBase)
                .when()
                        .delete("/users/2")
                .then()
                        .spec(deleteResponseSpec));
    }
}