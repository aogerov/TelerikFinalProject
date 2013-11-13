using System;
using System.Collections.Generic;
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
        // api/friends/all?sessionKey={sessionKey}
        [HttpGet]
        [ActionName("all")]
        public HttpResponseMessage GetAllFriends([FromUri]string sessionKey)
        {
            var responseMsg = this.PerformOperationAndHandleExeptions(() =>
            {
                using (var context = new FindMyBuddiesContext())
                {
                    var user = Validator.ValidateSessionKey(context, sessionKey);

                    var friends = user.Friends.OrderBy(u => u.Coordinates.Timestamp);
                    var onlineFriends = new List<FriendModel>();
                    var offlineFriends = new List<FriendModel>();
                    foreach (var friend in friends)
                    {
                        if (friend.Coordinates.Timestamp.AddHours(-1) > DateTime.Now)
                        {
                            friend.IsOnline = false;
                        }

                        var friendModel = Parser.FriendToFriendModel(friend);
                        if (friend.IsOnline)
                        {
                            onlineFriends.Add(friendModel);
                        }
                        else
                        {
                            offlineFriends.Add(friendModel);
                        }
                    }

                    context.SaveChanges();
                    var friendModels = new List<List<FriendModel>>();
                    friendModels.Add(onlineFriends);
                    friendModels.Add(offlineFriends);
					
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
