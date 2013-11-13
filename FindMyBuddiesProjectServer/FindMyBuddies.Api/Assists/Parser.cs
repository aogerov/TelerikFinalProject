using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using FindMyBuddies.Api.Models;
using FindMyBuddies.Model;

namespace FindMyBuddies.Api.Assists
{
    public class Parser
    {
        private const string Nickname = "NICKNAME";
        private const string Distance = "DISTANCE";
        private const string CoordinatesTimestamp = "COORDINATES_TIMESTAMP";
        private const string MeasureUnitsKilometers = "KILOMETERS";
        private const string MeasureUnitsMiles = "MILES";

        public static User UserModelToUser(UserModel userModel)
        {
            return new User
            {
                Username = userModel.Username.Trim().ToLower(),
                Nickname = userModel.Nickname.Trim(),
                AuthCode = userModel.AuthCode.Trim()
            };
        }

        public static UserLoggedModel UserToUserLoggedModel(User user)
        {
            return new UserLoggedModel
            {
                Nickname = user.Nickname,
                SessionKey = user.SessionKey
            };
        }

        public static Coordinates CreateDefaultCoordinates(UserModel model)
        {
            return new Coordinates
            {
                Latitude = 0,
                Longitude = 0
            };
        }

        public static Coordinates CoordinatesModelToCoordinates(CoordinatesModel coordinatesModel)
        {
            return new Coordinates
            {
                Latitude = coordinatesModel.Latitude,
                Longitude = coordinatesModel.Longitude
            };
        }

        public static List<FriendModel> FriendsToFriendModels(User user, ICollection<User> friends, string orderBy, string measureUnits)
        {
            var onlineFriends = new List<FriendModel>();
            var offlineFriends = new List<FriendModel>();
            foreach (var friend in friends)
            {
                if (friend.Coordinates == null)
                {
                    continue;
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

            MeasureUnitsEnum units = MeasureUnitsEnum.Kilometers;
            if (measureUnits.ToLower() == MeasureUnitsMiles.ToLower())
            {
                units = MeasureUnitsEnum.Miles;
            }

            CalculateDistance(user, onlineFriends, units);
            CalculateDistance(user, offlineFriends, units);

            CalculateTimestampDifferences(onlineFriends);
            CalculateTimestampDifferences(offlineFriends);

            SortFriendLists(onlineFriends, orderBy);
            SortFriendLists(offlineFriends, orderBy);

            var friendModels = new List<FriendModel>();
            friendModels.AddRange(onlineFriends);
            friendModels.AddRange(offlineFriends);
            return friendModels;
        }

        public static FriendModel FriendToFriendModel(User friend)
        {
            var friendModel = new FriendModel
            {
                Id = friend.Id,
                Nickname = friend.Nickname,
                IsOnline = friend.IsOnline,
                Latitude = friend.Coordinates.Latitude,
                Longitude = friend.Coordinates.Longitude,
                CoordinatesTimestamp = friend.Coordinates.Timestamp
            };

            return friendModel;
        }

        public static FriendModel UserToFriendFoundModel(User friendFound)
        {
            if (friendFound == null)
            {
                return null;
            }

            return new FriendModel
            {
                Id = friendFound.Id,
                Nickname = friendFound.Nickname,
                IsOnline = friendFound.IsOnline
            };
        }

        public static FriendRequest CreateFriendRequest(User userThatMakesRequest)
        {
            return new FriendRequest
            {
                FromUserId = userThatMakesRequest.Id,
                FromUserNickname = userThatMakesRequest.Nickname
            };
        }

        public static IEnumerable<FriendRequestModel> FriendRequestsToFriendRequestModels(
            IOrderedEnumerable<FriendRequest> friendRequestsEntities)
        {
            var friendRequestModels =
                (from friendRequestEntity in friendRequestsEntities
                 select new FriendRequestModel
                 {
                     FromUserId = friendRequestEntity.FromUserId,
                     FromUserNickname = friendRequestEntity.FromUserNickname,
                     IsShowed = friendRequestEntity.IsShowed
                 });

            return friendRequestModels;
        }

        public static ImageModel ImageToImageModel(Image image)
        {
            return new ImageModel
            {
                Url = image.Url,
                Timestamp = image.Timestamp,
                LatitudeAtCapturing = image.Coordinates.Latitude,
                LongitudeAtCapturing = image.Coordinates.Longitude
            };
        }

        public static Image ImageModelToImage(ImageModel imageModel)
        {
            return new Image
            {
                Url = imageModel.Url,
                Timestamp = imageModel.Timestamp,
                Coordinates = new Coordinates
                {
                    Latitude = imageModel.LatitudeAtCapturing,
                    Longitude = imageModel.LongitudeAtCapturing
                }
            };
        }

        private static void CalculateTimestampDifferences(List<FriendModel> onlineFriends)
        {
            var timeNow = DateTime.Now;
            foreach (var friend in onlineFriends)
            {
                var timeDifference = timeNow.Subtract(friend.CoordinatesTimestamp);
                StringBuilder difference = new StringBuilder();
                difference.Append(timeDifference.Hours + ":");
                if (timeDifference.Minutes < 10)
                {
                    difference.Append("0");
                }

                difference.Append(timeDifference.Minutes + ":");
                if (timeDifference.Seconds < 10)
                {
                    difference.Append("0");
                }

                difference.Append(timeDifference.Seconds);
                friend.CoordinatesTimestampDifference = difference.ToString();
            }
        }

        private static void CalculateDistance(User user, List<FriendModel> onlineFriends, MeasureUnitsEnum units)
        {
            foreach (var friend in onlineFriends)
            {
                double latitudeA = user.Coordinates.Latitude;
                double longitudeA = user.Coordinates.Latitude;
                double latitudeB = friend.Latitude;
                double longitudeB = friend.Longitude;
                if (latitudeA <= -90 || latitudeA >= 90 || longitudeA <= -180 || longitudeA >= 180
                    || latitudeB <= -90 && latitudeB >= 90 || longitudeB <= -180 || longitudeB >= 180)
                {
                    throw new ArgumentException(String.Format("Invalid value point coordinates. Points A({0},{1}) B({2},{3}) ",
                                                              latitudeA, longitudeA, latitudeB, longitudeB));
                }


                double radians = (units == MeasureUnitsEnum.Kilometers) ? 6371 : 3960;
                double radianLatitude = ToRadian(latitudeB - latitudeA);
                double radianLongitude = ToRadian(longitudeB - longitudeA);

                double pointA = Math.Sin(radianLatitude / 2) * Math.Sin(radianLatitude / 2) +
                    Math.Cos(ToRadian(latitudeA)) * Math.Cos(ToRadian(latitudeB)) *
                    Math.Sin(radianLongitude / 2) * Math.Sin(radianLongitude / 2);

                double pointC = 2 * Math.Asin(Math.Min(1, Math.Sqrt(pointA)));
                double pointD = radians * pointC;
                friend.Distance = pointD;
            }
        }

        private static double ToRadian(double val)
        {
            return (Math.PI / 180) * val;
        }

        private static void SortFriendLists(List<FriendModel> friends, string orderBy)
        {
            if (orderBy.ToLower() == Nickname.ToLower())
            {
                var orderedFriends = friends.OrderBy(f => f.Nickname);
                friends = orderedFriends.ToList();
            }
            else if (orderBy.ToLower() == CoordinatesTimestamp.ToLower())
            {
                var orderedFriends = friends.OrderByDescending(f => f.CoordinatesTimestamp);
                friends = orderedFriends.ToList();
            }
            else if (orderBy.ToLower() == Distance.ToLower())
            {
                var orderedFriends = friends.OrderBy(f => f.Distance);
                friends = orderedFriends.ToList();
            }
        }
    }
}