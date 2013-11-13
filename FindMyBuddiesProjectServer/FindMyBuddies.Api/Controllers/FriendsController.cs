using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;
using FindMyBuddies.Api.Assists;
using FindMyBuddies.Api.Models;
using FindMyBuddies.Data;
using FindMyBuddies.Model;

namespace FindMyBuddies.Api.Controllers
{
    public class FriendsController : BaseApiController
    {
        private static const string nickname = "NICKNAME";
        private static const string coordinatesTimestamp = "COORDINATES_TIMESTAMP";

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

                    var onlineFriends = new List<FriendModel>();
                    var offlineFriends = new List<FriendModel>();
                    this.SplitOnlineAndOfflineFriends(user, onlineFriends, offlineFriends);

                    context.SaveChanges();
                    var friendModels = SortFriendLists(orderBy, onlineFriends, offlineFriends);

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

        private void SplitOnlineAndOfflineFriends(User user, List<FriendModel> onlineFriends, List<FriendModel> offlineFriends)
        {
            foreach (var friend in user.Friends)
            {
                if (friend.Coordinates.Timestamp.AddHours(-1) > DateTime.Now)
                {
                    friend.IsOnline = false;
                }

                var friendModel = Parser.FriendToFriendModel(friend);
                if (friendModel.IsOnline)
                {
                    onlineFriends.Add(friendModel);
                }
                else
                {
                    offlineFriends.Add(friendModel);
                }
            }
        }

        private List<FriendModel> SortFriendLists(string orderBy, List<FriendModel> onlineFriends, List<FriendModel> offlineFriends)
        {
            var friendModels = new List<FriendModel>();

            if (orderBy.ToLower() == nickname.ToLower())
            {
                var orderedOnlineFriends = onlineFriends.OrderBy(f => f.Nickname);
                friendModels.AddRange(orderedOnlineFriends);
                var orderedOfflineFriends = offlineFriends.OrderBy(f => f.Nickname);
                friendModels.AddRange(orderedOfflineFriends);
            }
            else if (orderBy.ToLower() == coordinatesTimestamp.ToLower())
            {
                var orderedOnlineFriends = onlineFriends.OrderBy(f => f.CoordinatesTimestamp);
                friendModels.AddRange(orderedOnlineFriends);
                var orderedOfflineFriends = offlineFriends.OrderBy(f => f.CoordinatesTimestamp);
                friendModels.AddRange(orderedOfflineFriends);
            }
            else
            {

            }

            return friendModels;
        }

        public double CalculateDistance(double latitudeA, double longitudeA, double latitudeB, double longitudeB, MeasureUnitsEnum units)
        {
            if (latitudeA <= -90 || latitudeA >= 90 || longitudeA <= -180 || longitudeA >= 180
                || latitudeB <= -90 && latitudeB >= 90 || longitudeB <= -180 || longitudeB >= 180)
            {
                throw new ArgumentException(String.Format("Invalid value point coordinates. Points A({0},{1}) B({2},{3}) ",
                                                          latitudeA, longitudeA, latitudeB, longitudeB));
            }


            double radians = (units == MeasureUnitsEnum.Kilometers) ? 6371 : 3960;
            double radianLatitude = this.ToRadian(latitudeB - latitudeA);
            double radianLongitude = this.ToRadian(longitudeB - longitudeA);

            double pointA = Math.Sin(radianLatitude / 2) * Math.Sin(radianLatitude / 2) +
                Math.Cos(this.ToRadian(latitudeA)) * Math.Cos(this.ToRadian(latitudeB)) *
                Math.Sin(radianLongitude / 2) * Math.Sin(radianLongitude / 2);

            double pointC = 2 * Math.Asin(Math.Min(1, Math.Sqrt(pointA)));
            double pointD = radians * pointC;
            return pointD;
        }

        private double ToRadian(double val)
        {
            return (Math.PI / 180) * val;
        }
    }
}
