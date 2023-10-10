from typing import Callable
from datetime import datetime
import time
import multiprocessing

from rest_framework.throttling import ScopedRateThrottle
import openai
from openai.error import OpenAIError, RateLimitError

from .constants import OPENAI_API_KEY

openai.api_key = OPENAI_API_KEY
multiprocessing.set_start_method("fork")


def timeout(func: Callable):
    """
    Decorator to add timeout to a function.
    To apply timeout, function calls should include a keyword argument `timeout`.
    If the function does not return before the timeout, a TimeoutError is raised.
    Note that the return value of the function will be ignored, and it has to be stored properly in a shared object.
    """

    def timeout_wrapper(*args, **kwargs):
        if "timeout" not in kwargs:
            raise ValueError("timeout argument required")
        seconds = kwargs.pop("timeout")
        assert seconds is None or isinstance(
            seconds, (int, float)
        ), f"wrong type for timeout: {type(seconds)}"

        p = multiprocessing.Process(target=func, args=args, kwargs=kwargs)
        p.start()
        p.join(timeout=seconds)

        if p.is_alive():
            p.terminate()
            raise TimeoutError

    timeout_wrapper: func
    return timeout_wrapper


class GPTError(Exception):
    ...


@timeout
def _gpt_response(prompt: str, container: dict) -> None:
    completion = openai.ChatCompletion.create(
        model="gpt-3.5-turbo",
        messages=[{"role": "user", "content": prompt}],
    )

    container["answer"] = completion.choices[0].message


def call_gpt(
    prompt: str,
    timeout: float,
    wait: int = 10,
    max_trial: int = 3,
) -> str:
    container = multiprocessing.Manager().dict()

    for trial in range(max_trial):
        try:
            _gpt_response(prompt, container, timeout=timeout)

        except TimeoutError:
            print("Time out")
        except RateLimitError:
            print("Rate limit error")
            time.sleep(wait)
        except OpenAIError as e:
            print(f"OpenAI error: {e}")

        if "answer" in container:
            return container["answer"]

    raise GPTError(f"GPT call failed after {max_trial} trials")


class MomentReplyThrottle(ScopedRateThrottle):
    scope = "moment-reply"

    # Override
    def allow_request(self, request, view):
        # We can only determine the scope once we're called by the view.
        self.scope = getattr(view, self.scope_attr, None)

        # If a view does not have a `throttle_scope` always allow the request
        if not self.scope:
            return True

        # Determine the allowed request rate as we normally would during
        # the `__init__` call.
        self.rate = self.get_rate()
        self.num_requests, self.duration = self.parse_rate(self.rate)

        ### from SimpleRateThrottle ###

        if self.rate is None:
            return True

        self.key = self.get_cache_key(request, view)
        if self.key is None:
            return True

        self.history = self.cache.get(self.key, [])
        self.now = self.timer()
        # Drop any requests from the history which have now passed the
        # throttle duration
        while self.history and self._is_outdated(self.history[-1]):
            self.history.pop()
        if len(self.history) >= self.num_requests:
            return self.throttle_failure()
        return self.throttle_success()

    # Override
    def wait(self) -> float:
        now = time.time()
        latest_hour = self._get_latest_hour(now)
        delta = latest_hour + 3600 - now
        return delta

    def _is_outdated(self, timestamp: float) -> bool:
        """
        현재 시각에서 분 이하 단위를 잘라서
        그보다 이전에 들어온 요청인지 검사
        """
        latest_hour = self._get_latest_hour(timestamp)
        return timestamp < latest_hour

    def _get_latest_hour(self, timestamp: float) -> float:
        return (
            datetime.fromtimestamp(timestamp)
            .replace(minute=0, second=0, microsecond=0)
            .timestamp()
        )