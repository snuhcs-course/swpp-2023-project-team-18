import datetime

from django.test import TestCase
from rest_framework.test import APIRequestFactory, force_authenticate
from freezegun import freeze_time

from user.views import UserRetrieveUpdateView, RegisterView, LoginView
from user.models import User

intended_day = datetime.datetime(
    year=2023, month=10, day=1, hour=3, minute=0, second=0, microsecond=0
)
intended_day_string = "2023-10-01T03:00:00Z"
intended_day_string_with_microsecond = "2023-10-01T03:00:00.000001Z"


# Create your tests here.
class GetUserInfoTest(TestCase):
    username = "username"
    password = "password"
    nickname = "nickname"

    @freeze_time(lambda: intended_day)
    def setUp(self):
        test_user = User.objects.create(username=self.username, nickname=self.nickname)
        test_user.set_password(self.password)
        test_user.save()

    def test_get_user_info(self):
        factory = APIRequestFactory()
        view = UserRetrieveUpdateView.as_view()
        user = User.objects.get(username=self.username)
        request = factory.get("user/info")
        force_authenticate(request, user=user)
        response = view(request)
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.data["nickname"], self.nickname)
        self.assertEqual(response.data["username"], self.username)
        self.assertEqual(response.data["createdAt"], intended_day_string)


class UpdateNicknameTest(TestCase):
    username = "username"
    password = "password"
    nickname = "nickname"
    nickname2 = "nickname2"

    def setUp(self):
        test_user = User.objects.create(username=self.username, nickname=self.nickname)
        test_user.set_password(self.password)
        test_user.save()

    def test_update_nickname(self):
        factory = APIRequestFactory()
        view = UserRetrieveUpdateView.as_view()
        user = User.objects.get(username=self.username)
        data = {"nickname": self.nickname2}
        request = factory.put("user/info", data=data)
        force_authenticate(request, user=user)
        response = view(request)

        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.data["nickname"], self.nickname2)
        self.assertEqual(
            User.objects.get(username=self.username).nickname, self.nickname2
        )


class LoginTest(TestCase):
    username = "username"
    password = "password"
    nickname = "nickname"

    @freeze_time(lambda: intended_day + datetime.timedelta(microseconds=1))
    def setUp(self):
        test_user = User.objects.create(username=self.username, nickname=self.nickname)
        test_user.set_password(self.password)
        test_user.save()

    def test_login_success(self):
        factory = APIRequestFactory()
        view = LoginView.as_view()
        data = {"username": self.username, "password": self.password}
        request = factory.post("user/login", data=data)
        response = view(request)

        self.assertEqual(response.status_code, 200)
        self.assertTrue(response.data["token"].get("access_token", False))
        self.assertTrue(response.data["token"].get("refresh_token", False))
        self.assertEqual(response.data["user"]["username"], self.username)
        self.assertEqual(response.data["user"]["nickname"], self.nickname)
        self.assertEqual(
            response.data["user"]["createdAt"], intended_day_string_with_microsecond
        )

    def test_login_no_passwrod(self):
        factory = APIRequestFactory()
        view = LoginView.as_view()
        data = {"username": self.username}
        request = factory.post(view, data=data)
        response = view(request)

        self.assertEqual(response.status_code, 400)
        self.assertEqual(response.data["password"][0], "This field is required.")

    def test_login_wrong_password(self):
        factory = APIRequestFactory()
        view = LoginView.as_view()
        data = {"username": self.username, "password": "wrong password"}
        request = factory.post("user/login", data=data)
        response = view(request)

        self.assertEqual(response.status_code, 400)
        self.assertEqual(
            response.data["error"][0], "Unable to log in with provided credentials."
        )


class RegisterTest(TestCase):
    username = "username"
    password = "password"
    nickname = "nickname"

    def test_no_nickname_fail(self):
        factory = APIRequestFactory()
        view = RegisterView.as_view()

        data = {"username": self.username, "password": self.password}
        request = factory.post("user/register", data=data)
        response = view(request)
        self.assertEqual(response.data["nickname"][0], "This field is required.")
        self.assertEqual(response.status_code, 400)

    def test_register_success(self):
        factory = APIRequestFactory()
        view = RegisterView.as_view()

        data = {
            "username": self.username,
            "password": self.password,
            "nickname": self.nickname,
        }
        request = factory.post("user/register/", data=data)
        response = view(request)

        self.assertEqual(response.status_code, 201)
        self.assertEqual(
            response.data["user"],
            {"username": self.username, "nickname": self.nickname},
        )
        self.assertTrue(response.data["token"].get("access_token", False))
        self.assertTrue(response.data["token"].get("refresh_token", False))

        new_user = User.objects.get(username=self.username)
        self.assertEqual(new_user.nickname, self.nickname)
        self.assertEqual(new_user.check_password(self.password), True)

    def test_register_username_already_exists_fail(self):
        factory = APIRequestFactory()
        view = RegisterView.as_view()

        data = {
            "username": self.username,
            "password": self.password,
            "nickname": self.nickname,
        }
        request = factory.post("user/register", data=data)
        view(request)
        response = view(request)
        self.assertEqual(response.status_code, 400)
        self.assertEqual(
            response.data["username"][0], "A user with that username already exists."
        )
