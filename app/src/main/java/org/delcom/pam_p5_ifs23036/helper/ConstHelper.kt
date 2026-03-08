package org.delcom.pam_p5_ifs23036.helper

class ConstHelper {
    companion object {
        const val BASE_URL = "http://10.0.2.2:8080/" // Ganti sesuai URL API Anda
    }

    // Route Names
    enum class RouteNames(val path: String) {
        AuthLogin(path = "auth/login"),
        AuthRegister(path = "auth/register"),

        Home(path = "home"),

        Profile(path = "profile"),
        Todos(path = "todos"),
        TodosAdd(path = "todos/add"),
        TodosDetail(path = "todos/{todoId}"),
        TodosEdit(path = "todos/{todoId}/edit"),
    }
}
