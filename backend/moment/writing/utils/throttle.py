import time
from datetime import datetime

from rest_framework.throttling import ScopedRateThrottle


class MomentReplyThrottle(ScopedRateThrottle):
    scope = "moment-reply"

    # Override
    def allow_request(self, request, view) -> bool:
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
        latest_hour = self._get_latest_hour(time.time())
        return timestamp < latest_hour

    def _get_latest_hour(self, timestamp: float) -> float:
        return (
            datetime.fromtimestamp(timestamp)
            .replace(minute=0, second=0, microsecond=0)
            .timestamp()
        )
