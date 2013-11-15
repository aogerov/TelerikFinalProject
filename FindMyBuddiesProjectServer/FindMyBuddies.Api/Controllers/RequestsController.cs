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
    public class RequestsController : BaseApiController
    {
        // api/requests/all?sessionKey={sessionKey}
        [HttpGet]
        [ActionName("all")]
        public HttpResponseMessage GetAllFriends([FromUri]string sessionKey)
        {
            var responseMsg = this.PerformOperationAndHandleExeptions(() =>
            {
                using (var context = new FindMyBuddiesContext())
                {
                    var user = Validator.ValidateSessionKey(context, sessionKey);

                    var friendRequests = user.FriendRequests.OrderBy(r => r.IsShowed).ThenBy(r => r.FromUserNickname);
                    var friendRequestModels = Parser.FriendRequestsToFriendRequestModels(friendRequests);

                    var response = this.Request.CreateResponse(HttpStatusCode.OK, friendRequestModels);
                    return response;
                }
            });

            return responseMsg;
        }

        // api/requests/newRequestsCount?sessionKey={sessionKey}
        [HttpGet]
        [ActionName("newRequestsCount")]
        public HttpResponseMessage GetAllNotShowedFriends([FromUri]string sessionKey)
        {
            var responseMsg = this.PerformOperationAndHandleExeptions(() =>
            {
                using (var context = new FindMyBuddiesContext())
                {
                    var user = Validator.ValidateSessionKey(context, sessionKey);

                    var friendRequests = user.FriendRequests.Where(r => !r.IsShowed).OrderBy(r => r.FromUserNickname);
                    int newFriendRequestsCount = friendRequests.Count();

                    var response = this.Request.CreateResponse(HttpStatusCode.OK, newFriendRequestsCount);
                    return response;
                }
            });

            return responseMsg;
        }

        // api/requests/add?sessionKey={sessionKey}
        [HttpPost]
        [ActionName("add")]
        public HttpResponseMessage PostAddFriendRequest([FromBody]FriendModel model, [FromUri]string sessionKey)
        {
            var responseMsg = this.PerformOperationAndHandleExeptions(() =>
            {
                using (var context = new FindMyBuddiesContext())
                {
                    var userWhoMakesRequest = Validator.ValidateSessionKey(context, sessionKey);
                    var friendFound = Validator.ValidateFriendInDb(context, model.Id, model.Nickname);
                    Validator.ValidateIdConflicts(userWhoMakesRequest, friendFound);
                    var friendRequest = Parser.CreateFriendRequest(userWhoMakesRequest);

                    if (friendFound.FriendRequests.FirstOrDefault(
                        r => r.FromUserId == friendRequest.FromUserId) == null)
                    {
                        friendFound.FriendRequests.Add(friendRequest);
                        context.SaveChanges();
                    }

                    var response = this.Request.CreateResponse(HttpStatusCode.OK);
                    return response;
                }
            });

            return responseMsg;
        }

        // api/requests/response?sessionKey={sessionKey}
        [HttpPost]
        [ActionName("response")]
        public HttpResponseMessage PostResponseToFriendRequest(
            [FromBody]FriendRequestResponseModel requestResponse, [FromUri]string sessionKey)
        {
            var responseMsg = this.PerformOperationAndHandleExeptions(() =>
            {
                using (var context = new FindMyBuddiesContext())
                {
                    var userWhoRespondsToRequest = Validator.ValidateSessionKey(context, sessionKey);                    
                    var friendWhoMadeRequest = Validator.ValidateFriendInDb(
                        context, requestResponse.FromUserId, requestResponse.FromUserNickname);

                    Validator.ValidateIdConflicts(userWhoRespondsToRequest, friendWhoMadeRequest);
                    var request = Validator.ValidateRequestExistence(
                        context, userWhoRespondsToRequest, friendWhoMadeRequest);

                    if (requestResponse.IsAccepted)
                    {
                        userWhoRespondsToRequest.Friends.Add(friendWhoMadeRequest);
                        friendWhoMadeRequest.Friends.Add(userWhoRespondsToRequest);
                    }
                    
                    if (requestResponse.IsLeftForLater)
                    {
                        request.IsShowed = true;
                    }
                    
                    if (requestResponse.IsAccepted || !requestResponse.IsLeftForLater)
                    {
                        userWhoRespondsToRequest.FriendRequests.Remove(request);
                        context.FriendRequests.Remove(request);
                    }

                    context.SaveChanges();
                    var response = this.Request.CreateResponse(HttpStatusCode.OK);
                    return response;
                }
            });

            return responseMsg;
        }
    }
}
