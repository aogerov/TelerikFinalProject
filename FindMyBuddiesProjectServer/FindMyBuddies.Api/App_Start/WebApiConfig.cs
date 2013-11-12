using System;
using System.Web.Http;

namespace FindMyBuddies.Api
{
    public static class WebApiConfig
    {
        public static void Register(HttpConfiguration config)
        {
            config.Routes.MapHttpRoute(
                name: "UsersApi",
                routeTemplate: "api/users/{action}",
                defaults: new
                {
                    controller = "users"
                }
            );

            config.Routes.MapHttpRoute(
                name: "CoordinatesApi",
                routeTemplate: "api/coordinates/{action}",
                defaults: new
                {
                    controller = "coordinates"
                }
            );

            config.Routes.MapHttpRoute(
                name: "FriendsApi",
                routeTemplate: "api/friends/{action}",
                defaults: new
                {
                    controller = "friends"
                }
            );

            config.Routes.MapHttpRoute(
                name: "RequestsApi",
                routeTemplate: "api/requests/{action}",
                defaults: new
                {
                    controller = "requests"
                }
            );

            config.Routes.MapHttpRoute(
                name: "ImagesApi",
                routeTemplate: "api/images/{action}",
                defaults: new
                {
                    controller = "images"
                }
            );

            // Uncomment the following line of code to enable query support for actions with an IQueryable or IQueryable<T> return type.
            // To avoid processing unexpected or malicious queries, use the validation settings on QueryableAttribute to validate incoming queries.
            // For more information, visit http://go.microsoft.com/fwlink/?LinkId=279712.
            //config.EnableQuerySupport();

            // To disable tracing in your application, please comment out or remove the following line of code
            // For more information, refer to: http://www.asp.net/web-api
            config.EnableSystemDiagnosticsTracing();
        }
    }
}
