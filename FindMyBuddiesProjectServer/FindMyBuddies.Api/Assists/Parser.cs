﻿using System;
using System.Collections.Generic;
using System.Device.Location;
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
        private const string TimeDifferenceMoreThanDay = "more than 24 hours";
        private const int MetersInKilometer = 1000;
        private const int YardsInMile = 1760;
        private const double MetersToKilometer = 0.001;
        private const double MetersToYards = 1.0936133;
        private const double MetersToMiles = 0.000621371192;

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

        public static List<FriendModel> FriendsToFriendModels(User user, ICollection<User> friends, string orderBy)
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

            CalculateDistance(user, onlineFriends);
            CalculateDistance(user, offlineFriends);

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

        private static void CalculateTimestampDifferences(List<FriendModel> friends)
        {
            var timeNow = DateTime.Now;
            foreach (var friend in friends)
            {
                var timeDifference = timeNow.Subtract(friend.CoordinatesTimestamp);
                if (timeDifference.Days > 0)
                {
                    friend.CoordinatesTimestampDifference = TimeDifferenceMoreThanDay;
                    continue;
                }

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

        private static void CalculateDistance(User user, List<FriendModel> friends)
        {
            if (user.Coordinates == null)
            {
                return;
            }

            var userGeoCoordinate = new GeoCoordinate(user.Coordinates.Latitude, user.Coordinates.Longitude);
            foreach (var friend in friends)
            {
                var friendGeoCoordinate = new GeoCoordinate(friend.Latitude, friend.Longitude);
                int meters = Convert.ToInt32(userGeoCoordinate.GetDistanceTo(friendGeoCoordinate));
                friend.DistanceInMeters = meters;

                double kilometers = meters * MetersToKilometer;
                int yards = Convert.ToInt32(meters * MetersToYards);
                double miles = meters * MetersToMiles;

                ParseDistancesToString(friend, meters, kilometers, yards, miles);
            }
        }

        private static void ParseDistancesToString(FriendModel friend, int meters, double kilometers, int yards, double miles)
        {
            if (meters < MetersInKilometer)
            {
                friend.DistanceInKilometersAsString = String.Format("{0} meters", meters);
            }
            else
            {
                friend.DistanceInKilometersAsString = String.Format("{0:F2} kilometers", kilometers);
            }

            if (yards < YardsInMile)
            {
                friend.DistanceInMilesAsString = String.Format("{0} yards", yards);
            }
            else
            {
                friend.DistanceInMilesAsString = String.Format("{0:F2} miles", miles);
            }
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
                var orderedFriends = friends.OrderBy(f => f.DistanceInMeters);
                friends = orderedFriends.ToList();
            }
        }
    }
}