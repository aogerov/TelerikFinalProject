using System;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using FindMyBuddies.Api.Assists;
using FindMyBuddies.Api.Models;
using FindMyBuddies.Data;

namespace FindMyBuddies.Api.Controllers
{
    public class UsersController : BaseApiController
    {
        // api/users/login
        [HttpPost]
        [ActionName("login")]
        public HttpResponseMessage PostLoginUser([FromBody]UserModel model)
        {
            var responseMsg = this.PerformOperationAndHandleExeptions(() =>
            {
                using (var context = new FindMyBuddiesContext())
                {
                    var user = Validator.ValidateUserLogin(context, model);

                    if (user.SessionKey == null)
                    {
                        user.SessionKey = Generator.GenerateSessionKey(user.Id);
                    }

                    user.IsOnline = true;
                    context.SaveChanges();

                    var userModel = Parser.UserToUserLoggedModel(user);
                    var response = this.Request.CreateResponse(HttpStatusCode.Created, userModel);
                    return response;
                }
            });

            return responseMsg;
        }

        // api/users/register
        [HttpPost]
        [ActionName("register")]
        public HttpResponseMessage PostRegisterUser([FromBody]UserModel model)
        {
            var responseMsg = this.PerformOperationAndHandleExeptions(() =>
            {
                using (var context = new FindMyBuddiesContext())
                {
                    Validator.ValidateUserRegistration(context, model);

                    var user = Parser.UserModelToUser(model);
                    context.Users.Add(user);
                    
                    var defaultCoordinates = Parser.CreateDefaultCoordinates(model);
                    context.Coordinates.Add(defaultCoordinates);
                    user.Coordinates = defaultCoordinates;
                    context.SaveChanges();
                    
                    user.SessionKey = Generator.GenerateSessionKey(user.Id);
                    user.IsOnline = true;
                    context.SaveChanges();

                    var userModel = Parser.UserToUserLoggedModel(user);
                    var response = this.Request.CreateResponse(HttpStatusCode.Created, userModel);
                    return response;
                }
            });

            return responseMsg;
        }

        // api/users/logout?sessionKey={sessionKey}
        [HttpGet]
        [ActionName("logout")]
        public HttpResponseMessage GetLogoutUser([FromUri]string sessionKey)
        {
            var responseMsg = this.PerformOperationAndHandleExeptions(() =>
            {
                using (var context = new FindMyBuddiesContext())
                {
                    var user = Validator.ValidateSessionKey(context, sessionKey);

                    user.SessionKey = null;
                    user.IsOnline = false;
                    context.SaveChanges();

                    var response = this.Request.CreateResponse(HttpStatusCode.OK);
                    return response;
                }
            });

            return responseMsg;
        }

        // api/users/validate?sessionKey={sessionKey}
        [HttpGet]
        [ActionName("validate")]
        public HttpResponseMessage GetValidateUser([FromUri]string sessionKey)
        {
            var responseMsg = this.PerformOperationAndHandleExeptions(() =>
            {
                using (var context = new FindMyBuddiesContext())
                {
                    var user = Validator.ValidateSessionKey(context, sessionKey);
                    var userModel = Parser.UserToUserLoggedModel(user);

                    var response = this.Request.CreateResponse(HttpStatusCode.OK, userModel);
                    return response;
                }
            });

            return responseMsg;
        }
    }
}