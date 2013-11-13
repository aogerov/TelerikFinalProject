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
    public class FriendsController : BaseApiController
    {
        // api/friends/all?orderBy={orderBy}?measureUnits={measureUnits}?sessionKey={sessionKey}
        [HttpGet]
        [ActionName("all")]
        public HttpResponseMessage GetAllFriends([FromUri]string orderBy, [FromUri]string measureUnits, [FromUri]string sessionKey)
        {
            var responseMsg = this.PerformOperationAndHandleExeptions(() =>
            {
                using (var context = new FindMyBuddiesContext())
                {
                    var user = Validator.ValidateSessionKey(context, sessionKey);

                    foreach (var friend in user.Friends)
                    {
                        if (friend.Coordinates == null)
                        {
                            continue;
                        }
                        
                        if (friend.Coordinates.Timestamp.AddHours(-2) > DateTime.Now)
                        {
                            friend.IsOnline = false;
                        }
                    }

                    context.SaveChanges();
                    var friendModels = Parser.FriendsToFriendModels(user, user.Friends, orderBy, measureUnits);

                    var response = this.Request.CreateResponse(HttpStatusCode.OK, friendModels);
                    return response;
                }
            });

            return responseMsg;
        }

        // api/friends/find?friendNickname={friendNickname}&sessionKey={sessionKey}
        [HttpGet]
        [ActionName("find")]
        public HttpResponseMessage GetFindFriend([FromUri]string friendNickname, [FromUri]string sessionKey)
        {
            var responseMsg = this.PerformOperationAndHandleExeptions(() =>
            {
                using (var context = new FindMyBuddiesContext())
                {
                    string friendNicknameToLower = friendNickname.Trim().ToLower();
                    Validator.ValidateNickname(friendNicknameToLower);
                    var user = Validator.ValidateSessionKey(context, sessionKey);

                    if (user.Nickname.ToLower() == friendNicknameToLower ||
                        user.Friends.FirstOrDefault(f => f.Nickname.ToLower() == friendNicknameToLower) != null)
                    {
                        return this.Request.CreateResponse(HttpStatusCode.OK);
                    }

                    var friendFound = context.Users.FirstOrDefault(u => u.Nickname == friendNicknameToLower);
                    var friendFoundModel = Parser.UserToFriendFoundModel(friendFound);
                    
                    var response = this.Request.CreateResponse(HttpStatusCode.OK, friendFoundModel);
                    return response;
                }
            });

            return responseMsg;
        }

        // api/friends/remove?sessionKey={sessionKey}
        [HttpPost]
        [ActionName("remove")]
        public HttpResponseMessage PostAddFriendRequest([FromBody]FriendModel model, [FromUri]string sessionKey)
        {
            var responseMsg = this.PerformOperationAndHandleExeptions(() =>
            {
                using (var context = new FindMyBuddiesContext())
                {
                    var user = Validator.ValidateSessionKey(context, sessionKey);
                    var friend = Validator.ValidateFriendInDb(context, model.Id, model.Nickname);

                    if (user.Friends.Contains(friend))
                    {
                        user.Friends.Remove(friend);
                    }

                    if (friend.Friends.Contains(user))
                    {
                        friend.Friends.Remove(user);
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
