from django.contrib.auth.models import User
from rest_framework import serializers
from .models import Post

class PostSerializer(serializers.ModelSerializer):
    author = serializers.PrimaryKeyRelatedField(queryset=User.objects.all())
    image = serializers.ImageField(use_url=True, allow_null=True, required=False)

    class Meta:
        model = Post
        fields = ('author', 'title', 'text', 'created_date', 'published_date', 'image')

    
    