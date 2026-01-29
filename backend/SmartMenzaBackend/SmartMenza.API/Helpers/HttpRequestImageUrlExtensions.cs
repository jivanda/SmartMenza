using Microsoft.AspNetCore.Http;
using System;

namespace SmartMenza.API.Helpers
{
    public static class HttpRequestImageUrlExtensions
    {
        public static string? ToAbsoluteImageUrl(this HttpRequest request, string? imageUrl)
        {
            if (string.IsNullOrWhiteSpace(imageUrl)) return null;

            if (Uri.TryCreate(imageUrl, UriKind.Absolute, out _))
                return imageUrl;

            imageUrl = imageUrl.Trim();

            if (!imageUrl.Contains('/'))
                imageUrl = $"/images/meals/{imageUrl.TrimStart('/')}";

            if (!imageUrl.StartsWith("/"))
                imageUrl = "/" + imageUrl;

            return $"{request.Scheme}://{request.Host}{imageUrl}";
        }
    }
}