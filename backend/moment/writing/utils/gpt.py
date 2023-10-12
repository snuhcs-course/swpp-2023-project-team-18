import multiprocessing
import time
from typing import Callable, Optional

import openai
from openai.error import OpenAIError, RateLimitError

from writing.constants import OPENAI_API_KEY
from writing.utils.log import log

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


class GPTAgent:
    def __init__(self, model: str = "gpt-3.5-turbo"):
        self.model = model
        self._messages = []

    def add_message(self, content: str, role: str = "user"):
        self._messages.append({"role": role, "content": content})

    def reset_messages(self):
        self._messages = []

    def get_answer(
        self,
        timeout: Optional[float],
        temperature: float = 1.0,
        rate_limit_wait: int = 10,
        max_trial: int = 3,
    ) -> str:
        """
        Wrapper for GPT API call.
        Set `timeout=None` to disable timeout.
        """
        container = multiprocessing.Manager().dict()

        for trial in range(max_trial):
            try:
                self._call(container, timeout=timeout, temperature=temperature)

            except TimeoutError:
                log("GPT API timeout", tag="fail", place="GPTAgent.get_answer")
            except RateLimitError:
                log(
                    f"GPT API rate limit exceeded; Waiting {rate_limit_wait} seconds",
                    tag="fail",
                    place="GPTAgent.get_answer",
                )
                time.sleep(rate_limit_wait)
            except OpenAIError as e:
                log(f"OpenAIError: {e}", tag="fail", place="GPTAgent.get_answer")
            except Exception as e:
                log(f"Unexpected Error: {e}", tag="fail", place="GPTAgent.get_answer")

            if "answer" in container:
                return container["answer"]

        log(f"GPT API max trial reached", tag="error", place="GPTAgent.get_answer")
        raise GPTAgent.GPTError(f"GPT call failed after {max_trial} trials")

    @timeout
    def _call(self, container: dict, **kwargs):
        messages = self._messages
        completion = openai.ChatCompletion.create(
            model=self.model,
            messages=messages,
            **kwargs,
        )

        container["answer"] = completion.choices[0].message.content

    class GPTError(Exception):
        ...
