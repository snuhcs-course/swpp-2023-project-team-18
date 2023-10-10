from typing import Callable
import time
import multiprocessing

import openai
from openai.error import OpenAIError, RateLimitError

from .constants import OPENAI_API_KEY


openai.api_key = OPENAI_API_KEY


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
