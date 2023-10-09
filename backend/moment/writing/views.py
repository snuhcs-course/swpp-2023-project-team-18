from datetime import datetime

from rest_framework.generics import GenericAPIView
from rest_framework.response import Response

from .models import MomentPair
from .serializers import MomentPairQuerySerializer, MomentPairSerializer


# Create your views here.
class MomentView(GenericAPIView):
    serializer_class = MomentPairSerializer

    def get(self, request):
        params = MomentPairQuerySerializer(data=request.query_params)
        params.is_valid(raise_exception=True)
        
        start_date = datetime.fromtimestamp(params.validated_data["start"])
        end_date = datetime.fromtimestamp(params.validated_data["end"])

        moments = MomentPair.objects.filter(
            moment_created_at__range=(start_date, end_date)
        )
        serializer = self.get_serializer(data=moments, many=True)
        serializer.is_valid()
        return Response(serializer.data)
